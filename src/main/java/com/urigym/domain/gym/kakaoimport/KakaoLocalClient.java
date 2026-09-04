package com.urigym.domain.gym.kakaoimport;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Calls Kakao Local's keyword search, used to seed unclaimed gyms nationwide. */
@Component
public class KakaoLocalClient {

    private final RestClient restClient = RestClient.create("https://dapi.kakao.com");
    private final String restApiKey;

    public KakaoLocalClient(@Value("${kakao.local.rest-api-key:}") String restApiKey) {
        this.restApiKey = restApiKey;
    }

    public boolean isConfigured() {
        return restApiKey != null && !restApiKey.isBlank();
    }

    /** One page (max 15 results) of keyword search around (lat, lng) within radiusMeters (max 20000). */
    public KakaoKeywordSearchResponse searchKeyword(String query, double lat, double lng, int radiusMeters, int page) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v2/local/search/keyword.json")
                        .queryParam("query", query)
                        .queryParam("x", lng)
                        .queryParam("y", lat)
                        .queryParam("radius", radiusMeters)
                        .queryParam("size", 15)
                        .queryParam("page", page)
                        .build())
                .header("Authorization", "KakaoAK " + restApiKey)
                .retrieve()
                .body(KakaoKeywordSearchResponse.class);
    }
}
