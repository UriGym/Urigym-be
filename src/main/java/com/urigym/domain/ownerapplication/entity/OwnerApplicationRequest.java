package com.urigym.domain.ownerapplication.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OwnerApplicationRequest {

    @NotBlank(message = "사업자등록증 이미지가 필요합니다.")
    private String businessRegImageUrl;

    @NotBlank(message = "관장 자격증 이미지가 필요합니다.")
    private String licenseImageUrl;

    private String businessNumber;
}
