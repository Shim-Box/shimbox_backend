package sansam.shimbox.driver.domain;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sansam.shimbox.driver.enums.ConditionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "TB_DRIVER_REALTIME")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverRealtime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "driver_realtime_id")
    private Long driverRealtimeId;

    @OneToOne
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @Enumerated(EnumType.STRING)
    @Column(name = "real_time_condition_status", nullable = false)
    private ConditionStatus realTimeConditionStatus;

    @Column(name = "real_time_work_minutes")
    private Integer realTimeWorkMinutes;

    @Column(name = "real_time_delivery_count")
    private Integer realTimeDeliveryCount;

    @Column(name = "real_time_heart_rate")
    private Integer realTimeHeartRate;

    @Column(name = "real_time_fatigue")
    private BigDecimal realTimeFatigue;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public static DriverRealtime createNew(Driver driver) {
        return DriverRealtime.builder()
                .driver(driver)
                .realTimeConditionStatus(ConditionStatus.GOOD)
                .realTimeWorkMinutes(0)
                .realTimeDeliveryCount(0)
                .realTimeHeartRate(0)
                .realTimeFatigue(BigDecimal.ZERO)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public void updateWorkMinutes(int minutes) {
        this.realTimeWorkMinutes = minutes;
    }
}