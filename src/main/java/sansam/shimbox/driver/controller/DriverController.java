package sansam.shimbox.driver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sansam.shimbox.driver.dto.request.*;
import sansam.shimbox.driver.dto.request.RequestLeaveWorkDto;
import sansam.shimbox.driver.dto.response.*;
import sansam.shimbox.driver.dto.response.record.DeliveryGroupDto;
import sansam.shimbox.driver.dto.response.record.DeliveryLocationSummaryDto;
import sansam.shimbox.driver.service.DriverService;
import sansam.shimbox.driver.service.HeartRateTimelineService;
import sansam.shimbox.location.service.LocationRoomService;
import sansam.shimbox.location.dto.response.MessageDriverHealth;
import sansam.shimbox.global.common.BaseResponse;
import sansam.shimbox.global.exception.ErrorCode;
import sansam.shimbox.global.security.CurrentUser;
import sansam.shimbox.global.swagger.ApiErrorCodeExamples;

import java.util.List;

@Tag(name = "DriverController", description = "기사")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/driver")
public class DriverController {

    private final DriverService driverService;
    private final LocationRoomService locationRoomService;
    private final HeartRateTimelineService heartRateTimelineService;

    @Operation(summary = "근태 상태 변경 API")
    @ApiErrorCodeExamples({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.FORBIDDEN,
            ErrorCode.INVALID_ATTENDANCE_TRANSITION,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @PatchMapping("/attendance")
    public ResponseEntity<BaseResponse<ResponseAttendanceDto>> updateAttendance(
            @Parameter(hidden = true) @CurrentUser Long userId,
            @RequestBody RequestUpdateAttendanceDto dto) {
        ResponseAttendanceDto update = driverService.updateAttendanceStatus(userId, dto.getStatus());
        return ResponseEntity.ok(BaseResponse.success(update, "상태 변경 완료", HttpStatus.OK));
    }

    @Operation(summary = "퇴근 시 기사 건강 설문 저장")
    @ApiErrorCodeExamples({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.DRIVER_NOT_FOUND,
            ErrorCode.HEALTH_RECORD_NOT_FOUND,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @PostMapping("/health/survey")
    public ResponseEntity<BaseResponse<ResponseSurveySaveDto>> saveHealthSurvey(
            @Parameter(hidden = true) @CurrentUser Long userId,
            @RequestBody RequestLeaveWorkDto dto) {
        ResponseSurveySaveDto result = driverService.leaveWorkHealthSave(userId, dto);
        return ResponseEntity.ok(BaseResponse.success(result, "건강 설문 저장 완료", HttpStatus.OK));
    }

    @Operation(summary = "기사 퇴근 후 건강 데이터 조회")
    @ApiErrorCodeExamples({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.DRIVER_NOT_FOUND,
            ErrorCode.HEALTH_RECORD_NOT_FOUND,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @GetMapping("/health/today")
    public ResponseEntity<BaseResponse<ResponseLeaveWorkDto>> getTodayHealthSummary(
            @Parameter(hidden = true) @CurrentUser Long userId) {
        ResponseLeaveWorkDto dto = driverService.getTodayHealthSummary(userId);
        return ResponseEntity.ok(BaseResponse.success(dto, "퇴근 후 건강 데이터 조회 성공", HttpStatus.OK));
    }

    @Operation(summary = "배정 지역 조회 API")
    @ApiErrorCodeExamples({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @GetMapping("/location")
    public ResponseEntity<BaseResponse<List<DeliveryLocationSummaryDto>>> getDeliveryLocations(
            @Parameter(hidden = true) @CurrentUser Long userId) {
        List<DeliveryLocationSummaryDto> locations = driverService.getAssignedLocationSummary(userId);
        return ResponseEntity.ok(BaseResponse.success(locations, "배정 지역 및 배송 건수 조회 성공", HttpStatus.OK));
    }

    @Operation(summary = "배정 상품 조회 API")
    @ApiErrorCodeExamples({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.DELIVERY_NOT_FOUND,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @GetMapping("/summary")
    public ResponseEntity<BaseResponse<List<DeliveryGroupDto>>> getDeliverySummary(
            @Parameter(hidden = true) @CurrentUser Long userId) {
        List<DeliveryGroupDto> result = driverService.getGroupedDeliverySummaryByDriver(userId);
        return ResponseEntity.ok(BaseResponse.success(result, "배송 조회 성공", HttpStatus.OK));
    }

    @Operation(summary = "배송 상태 변경 API")
    @ApiErrorCodeExamples({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.FORBIDDEN,
            ErrorCode.INVALID_SHIPPING_TRANSITION,
            ErrorCode.SHIPP_NOT_FOUND,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @PatchMapping("/product/status")
    public ResponseEntity<BaseResponse<ResponseShippingStatusDto>> updateShippingStatus(
            @Parameter(hidden = true) @CurrentUser Long userId,
            @RequestBody RequestUpdateShippingStatusDto dto) {
        ResponseShippingStatusDto result = driverService.updateShippingStatus(userId, dto);
        return ResponseEntity.ok(BaseResponse.success(result, "배송 상태 변경 완료", HttpStatus.OK));
    }

    @Operation(summary = "배송도착 이미지 저장 API")
    @ApiErrorCodeExamples({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.FORBIDDEN,
            ErrorCode.SHIPP_NOT_FOUND,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @PostMapping("/delivery/image")
    public ResponseEntity<BaseResponse<String>> uploadDeliveryImage(
            @Parameter(hidden = true) @CurrentUser Long userId,
            @RequestBody RequestSaveImageUrlDto dto) {
        String imageUrl = driverService.saveDeliveryImageUrl(userId, dto);
        return ResponseEntity.ok(BaseResponse.success(imageUrl, "이미지 업로드 성공", HttpStatus.OK));
    }


    @Operation(summary = "기사 본인의 배정 지역 조회")
    @ApiErrorCodeExamples({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.DRIVER_NOT_FOUND,
            ErrorCode.INTERNAL_SERVER_ERROR,
            ErrorCode.DRIVER_REGION_NOT_FOUND,
    })
    @GetMapping("/my/region")
    public ResponseEntity<BaseResponse<List<ResponseDriverRegionInfoDto>>> getMyRegion(
            @Parameter(hidden = true) @CurrentUser Long userId) {
        List<ResponseDriverRegionInfoDto> regions = driverService.getDriverAssignedRegion(userId);
        return ResponseEntity.ok(BaseResponse.success(regions, "내 배정 지역 조회 성공", HttpStatus.OK));
    }

    @Operation(summary = "근무 통계 조회 API", description = "기사의 최근 5일 동안 근무 시간과 배달 건수 통계 조회")
    @ApiErrorCodeExamples({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.DRIVER_NOT_FOUND,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @GetMapping("/my/weekly-stats")
    public ResponseEntity<BaseResponse<ResponseWeeklyWorkStatsDto>> getWeeklyWorkStats(
            @Parameter(hidden = true) @CurrentUser Long userId) {
        ResponseWeeklyWorkStatsDto stats = driverService.getWeeklyWorkStats(userId);
        return ResponseEntity.ok(BaseResponse.success(stats, "근무 통계 조회 성공", HttpStatus.OK));
    }
}
