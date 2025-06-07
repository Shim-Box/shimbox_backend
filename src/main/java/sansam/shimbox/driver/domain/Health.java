package sansam.shimbox.driver.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import sansam.shimbox.driver.enums.ConditionStatus;
import sansam.shimbox.global.common.BaseTimeEntity;
import sansam.shimbox.driver.enums.Finish1;
import sansam.shimbox.driver.enums.Finish2;
import sansam.shimbox.driver.enums.Finish3;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "TB_HEALTH")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE TB_HEALTH SET is_deleted = true, deleted_date = NOW() WHERE health_id = ?")
@SQLRestriction("is_deleted = false")
public class Health extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "health_id")
    private Long healthId;

    @Enumerated(EnumType.STRING)
    @Column(name = "finish1")
    private Finish1 finish1;

    @Enumerated(EnumType.STRING)
    @Column(name = "finish2")
    private Finish2 finish2;

    @Enumerated(EnumType.STRING)
    @Column(name = "finish3")
    private Finish3 finish3;

    @Column(name = "work_time")
    private LocalDateTime workTime;

    @Column(name = "leave_work_time")
    private LocalDateTime leaveWorkTime;

    @Column(name = "step")
    private Integer step;

    @Column(name = "heart_rate")
    private Integer heartRate;

    @Column(name = "fatigue", precision = 3, scale = 2)
    private BigDecimal fatigue;

    @Enumerated(EnumType.STRING)
    @Column(name = "contition_status")
    private ConditionStatus conditionStatus;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "delete_date")
    private LocalDateTime deleteDate;

    @ManyToOne
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    public void markLeaveTime(LocalDateTime leaveTime) {
        this.leaveWorkTime = leaveTime;
    }
}