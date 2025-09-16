package sansam.shimbox.driver.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import sansam.shimbox.driver.dto.response.ResponseDriverLocationDto;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class SimulatedLocationService {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final ConcurrentHashMap<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final Random random = new Random();
    private final ConcurrentHashMap<Long, ScheduledFuture<?>> schedules = new ConcurrentHashMap<>();

    public List<ResponseDriverLocationDto> generateSnapshot(
            int count,
            double minLat,
            double minLng,
            double maxLat,
            double maxLng
    ) {
        List<ResponseDriverLocationDto> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            list.add(randomLocation("sim-" + (i + 1), minLat, minLng, maxLat, maxLng));
        }
        return list;
    }

    public SseEmitter streamLocations(
            long clientId,
            int count,
            double minLat,
            double minLng,
            double maxLat,
            double maxLng,
            long intervalMillis
    ) {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.put(clientId, emitter);

        Runnable cleanup = () -> {
            emitters.remove(clientId);
            ScheduledFuture<?> future = schedules.remove(clientId);
            if (future != null) {
                future.cancel(true);
            }
        };

        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> cleanup.run());

        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            SseEmitter current = emitters.get(clientId);
            if (current == null) {
                return;
            }
            try {
                List<ResponseDriverLocationDto> list = generateSnapshot(count, minLat, minLng, maxLat, maxLng);
                current.send(SseEmitter.event()
                        .name("locations")
                        .data(list));
            } catch (IOException e) {
                current.completeWithError(e);
                cleanup.run();
            }
        }, 0, Math.max(200, intervalMillis), TimeUnit.MILLISECONDS);
        schedules.put(clientId, future);

        return emitter;
    }

    private ResponseDriverLocationDto randomLocation(String id,
                                                     double minLat,
                                                     double minLng,
                                                     double maxLat,
                                                     double maxLng) {
        double lat = minLat + random.nextDouble() * (maxLat - minLat);
        double lng = minLng + random.nextDouble() * (maxLng - minLng);
        double heading = random.nextDouble() * 360.0;
        double speed = 1.0 + random.nextDouble() * 4.0; // 1~5 m/s

        return ResponseDriverLocationDto.builder()
                .driverId(id)
                .latitude(lat)
                .longitude(lng)
                .heading(heading)
                .speed(speed)
                .updatedAt(LocalDateTime.now())
                .build();
    }
}


