package com.urigym.domain.gym.kakaoimport;

import java.util.ArrayList;
import java.util.List;

/**
 * Square grid of search-center points covering South Korea, for scanning the whole
 * country with Kakao's radius-limited (max 20km) keyword search. Cells are spaced at
 * 1.5x the search radius so adjacent circles overlap slightly instead of leaving gaps
 * at the corners — duplicate hits across overlapping cells are deduped by kakaoPlaceId.
 */
public final class KoreaGrid {

    private KoreaGrid() {
    }

    private static final double MIN_LAT = 33.0;   // south of Jeju
    private static final double MAX_LAT = 38.75;  // near the DMZ
    private static final double MIN_LNG = 124.5;  // west coast islands
    private static final double MAX_LNG = 131.0;  // Ulleungdo/Dokdo
    private static final double KM_PER_DEG_LAT = 111.0;

    public record Cell(double lat, double lng) {
    }

    public static List<Cell> cells(double radiusKm) {
        double step = radiusKm * 1.5;
        double latStep = step / KM_PER_DEG_LAT;

        List<Cell> cells = new ArrayList<>();
        for (double lat = MIN_LAT; lat <= MAX_LAT; lat += latStep) {
            double lngStep = step / (KM_PER_DEG_LAT * Math.cos(Math.toRadians(lat)));
            for (double lng = MIN_LNG; lng <= MAX_LNG; lng += lngStep) {
                cells.add(new Cell(lat, lng));
            }
        }
        return cells;
    }
}
