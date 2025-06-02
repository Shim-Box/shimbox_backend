package sansam.shimbox.driver.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import sansam.shimbox.auth.domain.User;
import sansam.shimbox.driver.enums.Attendance;
import sansam.shimbox.global.common.BaseTimeEntity;
import sansam.shimbox.product.domain.Shipp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "TB_DRIVER")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE TB_DRIVER SET is_deleted = true, deleted_date = NOW() WHERE driver_id = ?")
@SQLRestriction("is_deleted = false")
public class Driver extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "driver_id")
    private Long driverId;

    @Enumerated(EnumType.STRING)
    @Column(name = "attendance", nullable = false)
    private Attendance attendance = Attendance.BEFORE_WORK;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "delete_date")
    private LocalDateTime deleteDate;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @OneToMany(mappedBy = "driver", cascade = CascadeType.ALL)
    private List<Shipp> shipps;

    @OneToMany(mappedBy = "driver", cascade = CascadeType.ALL)
    private List<Health> healthRecords;
}
