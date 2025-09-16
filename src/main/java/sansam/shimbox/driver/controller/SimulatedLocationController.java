package sansam.shimbox.driver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import sansam.shimbox.driver.dto.response.ResponseDriverLocationDto;
import sansam.shimbox.driver.service.SimulatedLocationService;
import sansam.shimbox.global.common.BaseResponse;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Simulated Location", description = "가상 기사 위치 모의 API")
@RequestMapping("/api/v1/mock")
public class SimulatedLocationController {

    private final SimulatedLocationService simulatedLocationService;

    @Operation(summary = "가상 기사 위치 스냅샷 목록")
    @GetMapping("/locations")
    public ResponseEntity<BaseResponse<List<ResponseDriverLocationDto>>> getLocations(
            @RequestParam(defaultValue = "20") int count,
            @RequestParam double minLat,
            @RequestParam double minLng,
            @RequestParam double maxLat,
            @RequestParam double maxLng
    ) {
        List<ResponseDriverLocationDto> list = simulatedLocationService.generateSnapshot(count, minLat, minLng, maxLat, maxLng);
        return ResponseEntity.ok(BaseResponse.success(list, "모의 위치 생성", org.springframework.http.HttpStatus.OK));
    }

    @Operation(summary = "가상 기사 위치 SSE 스트림")
    @GetMapping(path = "/locations/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamLocations(
            @RequestParam(defaultValue = "1") long clientId,
            @RequestParam(defaultValue = "20") int count,
            @RequestParam double minLat,
            @RequestParam double minLng,
            @RequestParam double maxLat,
            @RequestParam double maxLng,
            @RequestParam(defaultValue = "1000") long intervalMillis
    ) {
        return simulatedLocationService.streamLocations(clientId, count, minLat, minLng, maxLat, maxLng, intervalMillis);
    }
}




