package sansam.shimbox.auth.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sansam.shimbox.auth.domain.User;
import sansam.shimbox.auth.user.dto.request.RequestUserApproveDto;
import sansam.shimbox.auth.user.dto.response.ResponseUserFindAllDto;
import sansam.shimbox.auth.user.service.AdminService;
import sansam.shimbox.global.common.BaseResponse;
import sansam.shimbox.global.common.PagedResponse;
import sansam.shimbox.global.common.RequestPagingDto;

import java.util.List;

@Tag(name = "AdminController", description = "관리자")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/pending")
    public ResponseEntity<BaseResponse<PagedResponse<ResponseUserFindAllDto>>> userFindAll(RequestPagingDto pagingDto) {
        PagedResponse<ResponseUserFindAllDto> users = adminService.userFindAll(pagingDto);
        return ResponseEntity.ok(BaseResponse.success(users, "승인 대기 목록 조회 성공", HttpStatus.OK));
    }

    @Operation(summary = "회원 승인")
    @PatchMapping("/approve")
    public BaseResponse<List<Long>> approveUser(@Valid @RequestBody RequestUserApproveDto dto) {
        List<Long> approvedIds = adminService.approveUser(dto);
        return BaseResponse.success(approvedIds, "승인 성공", HttpStatus.OK);
    }
}
