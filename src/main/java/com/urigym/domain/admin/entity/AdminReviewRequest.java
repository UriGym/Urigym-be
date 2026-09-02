package com.urigym.domain.admin.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Admin decision on a pending owner application. */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdminReviewRequest {

    private boolean approve;
    private String adminNote;
}
