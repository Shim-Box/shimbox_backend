package sansam.shimbox.driver.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import sansam.shimbox.driver.enums.Region;
import sansam.shimbox.global.common.BaseTimeEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "TB_DRIVER_REGION")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE TB_DRIVER_REGION SET is_deleted = true, delete_date = NOW() WHERE driver_region_id = ?")
@SQLRestriction("is_deleted = false")
public class DriverRegion extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "driver_region_id")
    private Long driverRegionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @Enumerated(EnumType.STRING)
    @Column(name = "region", nullable = false)
    private Region region;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @Column(name = "delete_date")
    private LocalDateTime deleteDate;
}
