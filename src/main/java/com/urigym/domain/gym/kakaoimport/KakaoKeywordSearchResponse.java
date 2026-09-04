package com.urigym.domain.gym.kakaoimport;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KakaoKeywordSearchResponse(List<Document> documents, Meta meta) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Document(
            String id,
            @JsonProperty("place_name") String placeName,
            @JsonProperty("address_name") String addressName,
            @JsonProperty("road_address_name") String roadAddressName,
            String phone,
            /** Longitude, as a string per Kakao's API. */
            String x,
            /** Latitude, as a string per Kakao's API. */
            String y
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Meta(@JsonProperty("is_end") boolean isEnd) {
    }
}
