package com.urigym.domain.gym.kakaoimport;

import com.urigym.domain.gym.GymService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Best-effort nationwide gym import from Kakao Local, admin-triggered. Runs on a
 * background thread since scanning the whole country (a grid of search points x every
 * category keyword, each possibly multi-page) takes long enough that an HTTP request
 * shouldn't block on it — progress is polled via {@link #status()} instead.
 * ponytail: single in-memory run, no persistence/resume across restarts — good enough
 * for an admin-operated one-off backfill; add a job table if this needs to survive a
 * restart or run unattended on a schedule.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GymImportService {

    private static final double RADIUS_KM = 15.0;
    private static final int RADIUS_METERS = 15000;
    private static final int MAX_PAGES = 3;
    private static final int THREADS = 6;

    private final KakaoLocalClient kakaoLocalClient;
    private final GymService gymService;

    private final AtomicInteger totalUnits = new AtomicInteger();
    private final AtomicInteger completedUnits = new AtomicInteger();
    private final AtomicInteger createdCount = new AtomicInteger();
    private final AtomicInteger skippedCount = new AtomicInteger();
    private final AtomicInteger errorCount = new AtomicInteger();
    private volatile boolean running = false;
    private volatile String lastError;

    public record Status(boolean running, int totalUnits, int completedUnits,
                          int created, int skipped, int errors, String lastError) {
    }

    /** @return false if an import is already running (no-op instead of starting a second one) */
    public synchronized boolean start() {
        if (running) {
            return false;
        }
        if (!kakaoLocalClient.isConfigured()) {
            throw new IllegalStateException("카카오 REST API 키(kakao.local.rest-api-key)가 설정되지 않았습니다.");
        }
        running = true;
        totalUnits.set(0);
        completedUnits.set(0);
        createdCount.set(0);
        skippedCount.set(0);
        errorCount.set(0);
        lastError = null;

        Thread thread = new Thread(this::runImport, "gym-kakao-import");
        thread.setDaemon(true);
        thread.start();
        return true;
    }

    public Status status() {
        return new Status(running, totalUnits.get(), completedUnits.get(),
                createdCount.get(), skippedCount.get(), errorCount.get(), lastError);
    }

    private void runImport() {
        List<KoreaGrid.Cell> cells = KoreaGrid.cells(RADIUS_KM);
        List<Map.Entry<String, List<String>>> categoryEntries =
                new ArrayList<>(GymCategoryKeywords.KEYWORDS_BY_CATEGORY.entrySet());
        int keywordsPerCell = categoryEntries.stream().mapToInt(e -> e.getValue().size()).sum();
        totalUnits.set(cells.size() * keywordsPerCell);

        log.info("Kakao gym import started: {} cells x {} keywords = {} units",
                cells.size(), keywordsPerCell, totalUnits.get());

        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        try {
            List<Future<?>> futures = new ArrayList<>();
            for (KoreaGrid.Cell cell : cells) {
                futures.add(executor.submit(() -> importCell(cell, categoryEntries)));
            }
            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (Exception e) {
                    log.warn("Cell import task failed: {}", e.getMessage());
                }
            }
        } finally {
            executor.shutdown();
            running = false;
            log.info("Kakao gym import finished: created={} skipped={} errors={}",
                    createdCount.get(), skippedCount.get(), errorCount.get());
        }
    }

    private void importCell(KoreaGrid.Cell cell, List<Map.Entry<String, List<String>>> categoryEntries) {
        for (Map.Entry<String, List<String>> entry : categoryEntries) {
            String category = entry.getKey();
            for (String keyword : entry.getValue()) {
                try {
                    importKeywordAt(cell, category, keyword);
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    lastError = "%s @ (%.3f,%.3f): %s".formatted(keyword, cell.lat(), cell.lng(), e.getMessage());
                    log.warn("Kakao search failed for {} at ({}, {}): {}", keyword, cell.lat(), cell.lng(), e.getMessage());
                } finally {
                    completedUnits.incrementAndGet();
                }
            }
        }
    }

    private void importKeywordAt(KoreaGrid.Cell cell, String category, String keyword) throws InterruptedException {
        for (int page = 1; page <= MAX_PAGES; page++) {
            KakaoKeywordSearchResponse response =
                    kakaoLocalClient.searchKeyword(keyword, cell.lat(), cell.lng(), RADIUS_METERS, page);
            // Be a polite API citizen — Kakao doesn't publish a per-second cap, but this
            // keeps 6 threads well under any plausible burst limit.
            Thread.sleep(150);

            if (response == null || response.documents() == null || response.documents().isEmpty()) {
                return;
            }

            for (KakaoKeywordSearchResponse.Document doc : response.documents()) {
                String address = doc.roadAddressName() != null && !doc.roadAddressName().isBlank()
                        ? doc.roadAddressName()
                        : doc.addressName();
                boolean created = gymService.upsertFromKakao(
                        doc.id(), doc.placeName(), category, address, doc.phone(),
                        parseDouble(doc.y()), parseDouble(doc.x()));
                if (created) {
                    createdCount.incrementAndGet();
                } else {
                    skippedCount.incrementAndGet();
                }
            }

            if (response.meta() == null || response.meta().isEnd()) {
                return;
            }
        }
    }

    private Double parseDouble(String value) {
        try {
            return value == null ? null : Double.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
