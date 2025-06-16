package sansam.shimbox.driver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sansam.shimbox.driver.dto.request.*;
import sansam.shimbox.driver.dto.response.*;
import sansam.shimbox.driver.dto.response.record.DeliveryGroupDto;
import sansam.shimbox.driver.dto.response.record.DeliveryLocationSummaryDto;
import sansam.shimbox.driver.service.DriverService;
import sansam.shimbox.driver.service.RealtimeHealthService;
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
    private final RealtimeHealthService realtimeHealthService;

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

    @Operation(summary = "기사 실시간 건강 데이터 생성 (심박수, 걸음수, 건강상태)")
    @ApiErrorCodeExamples({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.DRIVER_NOT_FOUND,
            ErrorCode.REDIS_SAVE_FAILED,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @PostMapping("/realtime")
    public ResponseEntity<BaseResponse<Void>> saveRealtimeHealth(
            @Parameter(hidden = true) @CurrentUser Long userId,
            @RequestBody RequestRealTimeHealthSaveDto dto) {
        realtimeHealthService.realTimeHealthSave(userId, dto);
        return ResponseEntity.ok(BaseResponse.success(null, "실시간 건강 데이터 저장 성공", HttpStatus.OK));
    }

    @Operation(summary = "기사 실시간 건강 데이터 조회")
    @ApiErrorCodeExamples({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.DRIVER_NOT_FOUND,
            ErrorCode.HEALTH_RECORD_NOT_FOUND,
            ErrorCode.REDIS_PARSE_FAILED,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @GetMapping("/realtime")
    public ResponseEntity<BaseResponse<ResponseRealTimeHealthDto>> getRealtimeHealth(
            @Parameter(hidden = true) @CurrentUser Long userId) {
        ResponseRealTimeHealthDto response = realtimeHealthService.getRealtimeHealth(userId);
        return ResponseEntity.ok(BaseResponse.success(response, "실시간 건강 데이터 조회 성공", HttpStatus.OK));
    }

    @Operation(summary = "모든 기사 실시간 건강 데이터 조회 (지도용)")
    @ApiErrorCodeExamples({
            ErrorCode.REDIS_PARSE_FAILED,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @GetMapping("/realtime/all")
    public ResponseEntity<BaseResponse<List<ResponseRealTimeHealthDto>>> getAllRealtimeHealth() {
        List<ResponseRealTimeHealthDto> list = realtimeHealthService.getAllRealtimeHealth();
        return ResponseEntity.ok(BaseResponse.success(list, "전체 실시간 건강 데이터 조회 성공", HttpStatus.OK));
    }
}
