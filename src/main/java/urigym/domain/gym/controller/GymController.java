package urigym.domain.gym.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import urigym.common.error.ResponseCode;
import urigym.common.rest.BaseController;
import urigym.domain.gym.service.GymService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gym")
@RequiredArgsConstructor
@Tag(name = "Gym", description = "체육관 API")
public class GymController extends BaseController {

    private final GymService gymService;

    @GetMapping
    @Operation(summary = "모든 체육관 조회", description = "전체 체육관 목록을 페이징하여 조회합니다.")
    @PreAuthorize("hasAnyRole()")
    public ResponseEntity getAllGyms(){

        var result = gymService.getAllGyms();

        return response(ResponseCode.OK, result);
    }

//    @GetMapping("/{id}")
//    @Operation(summary = "체육관 상세 조회", description = "특정 체육관의 상세 정보를 조회합니다.")
//
//    @GetMapping("/category/{category}")
//    @Operation(summary = "카테고리별 체육관 조회", description = "특정 카테고리의 체육관 목록을 조회합니다.")
//
//    @GetMapping("/search")
//    @Operation(summary = "체육관 검색", description = "키워드로 체육관을 검색합니다.")
//
//    @GetMapping("/location")
//    @Operation(summary = "위치 기반 체육관 조회", description = "특정 위치 범위 내의 체육관을 조회합니다.")
}
