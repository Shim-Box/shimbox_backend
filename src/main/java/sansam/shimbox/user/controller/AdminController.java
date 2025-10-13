package sansam.shimbox.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sansam.shimbox.driver.enums.Attendance;
import sansam.shimbox.driver.enums.ConditionStatus;
import sansam.shimbox.global.exception.ErrorCode;
import sansam.shimbox.global.swagger.ApiErrorCodeExamples;
import sansam.shimbox.location.domain.LocationData;
import sansam.shimbox.location.dto.request.RequestLocationQueryDto;
import sansam.shimbox.location.dto.response.MessageDriverHealth;
import sansam.shimbox.location.service.LocationRoomService;
import sansam.shimbox.driver.service.HeartRateTimelineService;
import sansam.shimbox.driver.dto.response.ResponseHeartRateTimelineDto;
import sansam.shimbox.user.dto.request.RequestDriverRegionDto;
import sansam.shimbox.user.dto.request.RequestUserStatusDto;
import sansam.shimbox.user.dto.response.ResponseDriverRegionDto;
import sansam.shimbox.user.dto.response.ResponseUserApprovedDto;
import sansam.shimbox.user.dto.response.ResponseUserPendingDto;
import sansam.shimbox.user.service.AdminService;
import sansam.shimbox.product.service.ProductService;
import sansam.shimbox.product.dto.response.ResponseProductTimelineDto;
import sansam.shimbox.product.dto.response.ResponseProductDto;
import sansam.shimbox.product.enums.ShippingStatus;
import sansam.shimbox.user.dto.response.ResponseDriverProfileDto;
import sansam.shimbox.global.common.BaseResponse;
import sansam.shimbox.global.common.PagedResponse;
import sansam.shimbox.global.common.RequestPagingDto;

import java.util.ArrayList;
import java.util.List;

@Tag(name = "AdminController", description = "관리자")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/admin")
@Slf4j
public class AdminController {

        private final AdminService adminService;
        private final LocationRoomService locationRoomService;
        private final ProductService productService;
        private final HeartRateTimelineService heartRateTimelineService;

    @Operation(summary = "가입 대기자 조회 API")
    @ApiErrorCodeExamples({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.FORBIDDEN,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/pending")
    public ResponseEntity<BaseResponse<PagedResponse<ResponseUserPendingDto>>> userFindAll(@ParameterObject @ModelAttribute RequestPagingDto dto) {
        PagedResponse<ResponseUserPendingDto> users = adminService.userFindAll(dto);
        return ResponseEntity.ok(BaseResponse.success(users, "승인 대기 목록 조회 성공", HttpStatus.OK));
    }

    @Operation(summary = "회원 승인 API")
    @ApiErrorCodeExamples({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.FORBIDDEN,
            ErrorCode.INVALID_REQUEST,
            ErrorCode.USERS_NOT_FOUND,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @PatchMapping("/status")
    public BaseResponse<List<Long>> approveUser(@Valid @RequestBody RequestUserStatusDto dto) {
        List<Long> approvedIds = adminService.approveUser(dto);
        return BaseResponse.success(approvedIds, "승인 성공", HttpStatus.OK);
    }

    @Operation(summary = "승인된 회원 목록 조회 API")
    @ApiErrorCodeExamples({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.FORBIDDEN,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/approved")
    public ResponseEntity<BaseResponse<PagedResponse<ResponseUserApprovedDto>>> approvedUserFindAll(
            @RequestParam(required = false) String residence,
            @RequestParam(required = false) Attendance attendance,
            @RequestParam(required = false) ConditionStatus conditionStatus,
            @ParameterObject @ModelAttribute RequestPagingDto pagingDto) {
        PagedResponse<ResponseUserApprovedDto> users =
                adminService.approvedUserFindAll(residence, attendance, conditionStatus, pagingDto.toPageable());
        return ResponseEntity.ok(BaseResponse.success(users, "승인된 회원 목록 조회 성공", HttpStatus.OK));
    }

    @Operation(summary = "기사 실시간 위치 조회 API - 초기 로딩용", description = "지역별 필터링")
    @ApiErrorCodeExamples({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.FORBIDDEN,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/location/realtime")
    public ResponseEntity<BaseResponse<List<LocationData>>> getRealtime(@ModelAttribute RequestLocationQueryDto queryDto) {
        List<LocationData> locations = locationRoomService.getRealtimeLocations(queryDto);
        return ResponseEntity.ok(BaseResponse.success(locations, "실시간 위치 목록", HttpStatus.OK));
    }

    @Operation(summary = "기사 지역 배정 API", description = "관리자가 기사를 2개 지역에 배정")
    @ApiErrorCodeExamples({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.FORBIDDEN,
            ErrorCode.DRIVER_NOT_FOUND,
            ErrorCode.DRIVER_ALREADY_ASSIGNED,
            ErrorCode.INVALID_REQUEST,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/driver/assign-region")
    public ResponseEntity<BaseResponse<ResponseDriverRegionDto>> assignDriverRegion(
            @Valid @RequestBody RequestDriverRegionDto dto) {
        ResponseDriverRegionDto result = adminService.assignDriverRegion(dto);
        return ResponseEntity.ok(BaseResponse.success(result, "기사 지역 배정 완료", HttpStatus.OK));
    }

    @Operation(summary = "상품 타임라인 조회 API", description = "상품의 배송 상태 변경 이력 조회")
    @ApiErrorCodeExamples({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.FORBIDDEN,
            ErrorCode.SHIPP_NOT_FOUND,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/product/{productId}/timeline")
    public ResponseEntity<BaseResponse<List<ResponseProductTimelineDto>>> getProductTimeline(
            @PathVariable Long productId) {
        List<ResponseProductTimelineDto> timeline = productService.getProductTimeline(productId);
        return ResponseEntity.ok(BaseResponse.success(timeline, "상품 타임라인 조회 완료", HttpStatus.OK));
    }

    @Operation(summary = "기사 배정 상품 조회 API", description = "특정 기사가 배정받은 상품 목록을 조회합니다. (배송 상태별 필터링 가능)")
    @ApiErrorCodeExamples({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.FORBIDDEN,
            ErrorCode.DRIVER_NOT_FOUND,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/driver/{driverId}/products")
    public ResponseEntity<BaseResponse<List<ResponseProductDto>>> getDriverProducts(
            @PathVariable Long driverId,
            @RequestParam(required = false) ShippingStatus shippingStatus) {
        List<ResponseProductDto> products = adminService.getDriverProducts(driverId, shippingStatus);
        return ResponseEntity.ok(BaseResponse.success(products, "기사 배정 상품 조회 완료", HttpStatus.OK));
    }

    @Operation(summary = "기사 프로필 조회 API", description = "특정 기사의 상세 프로필 정보 조회")
    @ApiErrorCodeExamples({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.FORBIDDEN,
            ErrorCode.DRIVER_NOT_FOUND,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/driver/{driverId}/profile")
    public ResponseEntity<BaseResponse<ResponseDriverProfileDto>> getDriverProfile(
            @PathVariable Long driverId) {
        ResponseDriverProfileDto profile = adminService.getDriverProfile(driverId);
        return ResponseEntity.ok(BaseResponse.success(profile, "기사 프로필 조회 완료", HttpStatus.OK));
    }

    @Operation(summary = "실시간 기사 건강 데이터 조회 API", description = "기사들의 실시간 심박수, 걸음수, 건강상태 조회")
    @ApiErrorCodeExamples({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.FORBIDDEN,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/realtime/health")
    public ResponseEntity<BaseResponse<List<MessageDriverHealth.Payload>>> getRealtimeHealth(
            @RequestParam(required = false) String region) {
        List<MessageDriverHealth.Payload> healthData = locationRoomService.getRealtimeHealth(region);
        return ResponseEntity.ok(BaseResponse.success(healthData, "실시간 건강 데이터 조회 완료", HttpStatus.OK));
    }

    @Operation(summary = "특정 기사 심박수 타임라인 조회 API", description = "특정 기사의 심박수 타임라인 조회 (30분 단위)")
    @ApiErrorCodeExamples({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.FORBIDDEN,
            ErrorCode.DRIVER_NOT_FOUND,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/driver/{driverId}/heartrate-timeline")
    public ResponseEntity<BaseResponse<List<ResponseHeartRateTimelineDto>>> getDriverHeartRateTimeline(
            @PathVariable Long driverId,
            @RequestParam(required = false) String date,
            @RequestParam(defaultValue = "1") int days) {
        
        List<ResponseHeartRateTimelineDto> timeline;
        
        if (date != null && !date.isEmpty()) {
            // 특정 날짜의 타임라인 조회
            timeline = heartRateTimelineService.getHeartRateTimeline(driverId, date);
        } else {
            // date가 없으면 빈 리스트 반환
            timeline = new ArrayList<>();
        }
        
        return ResponseEntity.ok(BaseResponse.success(timeline, "기사 심박수 타임라인 조회 완료", HttpStatus.OK));
    }
}
