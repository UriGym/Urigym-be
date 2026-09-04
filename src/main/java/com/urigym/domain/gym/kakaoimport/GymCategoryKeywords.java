package com.urigym.domain.gym.kakaoimport;

import java.util.List;
import java.util.Map;

/**
 * Search terms per gym category (id matches {@code GYM_CATEGORIES} in the frontend's
 * CategoryFilter). Kakao's place search matches real place names/types, so abstract
 * category labels like "구기종목" won't find anything — each maps to the concrete
 * facility words people actually search.
 */
public final class GymCategoryKeywords {

    private GymCategoryKeywords() {
    }

    public static final Map<String, List<String>> KEYWORDS_BY_CATEGORY = Map.ofEntries(
            Map.entry("헬스장", List.of("헬스장")),
            Map.entry("크로스핏", List.of("크로스핏")),
            Map.entry("요가", List.of("요가")),
            Map.entry("필라테스", List.of("필라테스")),
            Map.entry("구기종목", List.of("풋살장", "축구장", "농구장", "배드민턴장", "탁구장", "야구연습장")),
            Map.entry("투기종목", List.of("복싱", "태권도", "유도", "무에타이", "주짓수", "킥복싱")),
            Map.entry("수영장", List.of("수영장")),
            Map.entry("클라이밍", List.of("클라이밍")),
            Map.entry("골프", List.of("골프연습장", "스크린골프")),
            Map.entry("댄스", List.of("댄스학원", "무용학원"))
    );
}
