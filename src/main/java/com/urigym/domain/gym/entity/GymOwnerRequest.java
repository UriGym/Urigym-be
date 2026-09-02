package com.urigym.domain.gym.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Pricing is mandatory here — gyms must publish their price to stay listed.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GymOwnerRequest {

    @NotBlank(message = "체육관 이름을 입력해주세요.")
    private String name;

    @NotBlank(message = "카테고리를 입력해주세요.")
    private String category;

    @NotBlank(message = "주소를 입력해주세요.")
    private String address;

    private String description;
    private String phone;
    private String imageUrl;
    private Double lat;
    private Double lng;

    @NotNull(message = "가격 등록은 필수입니다.")
    @PositiveOrZero(message = "가격은 0원 이상이어야 합니다.")
    private Integer priceMin;

    @PositiveOrZero(message = "가격은 0원 이상이어야 합니다.")
    private Integer priceMax;

    private Boolean isOpen;
    private List<String> tags;
}
