package com.urigym.domain.gym.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GymCreateRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Category is required")
    private String category;

    @NotBlank(message = "Address is required")
    private String address;

    private String description;
    private String phone;
    private String imageUrl;
    private Double lat;
    private Double lng;
    private Integer priceMin;
    private Integer priceMax;
    private List<String> tags;
}
