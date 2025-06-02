package sansam.shimbox.auth.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import sansam.shimbox.auth.enums.*;
import sansam.shimbox.driver.domain.Driver;
import sansam.shimbox.global.common.BaseTimeEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "TB_USER")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@SQLDelete(sql = "UPDATE TB_USER SET is_deleted = true, deleted_date = NOW() WHERE user_id = ?")
@SQLRestriction("is_deleted = false")
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "email", length = 100, nullable = false, unique = true)
    private String email;

    @Column(name = "password", length = 100, nullable = false)
    private String password;

    @Column(name = "name", length = 10, nullable = false)
    private String name;

    @Column(name = "identification_number",  length = 7, nullable = false)
    private String identificationNumber;

    @Column(name = "residence", length = 100, nullable = false)
    private String residence;

    @Column(name = "height", nullable = false)
    private Long height;

    @Column(name = "weight", nullable = false)
    private Long weight;

    @Column(name = "license_image", nullable = false)
    private String licenseImage;

    @Enumerated(EnumType.STRING)
    @Column(name = "license", nullable = false)
    private License license;

    @Enumerated(EnumType.STRING)
    @Column(name = "career", nullable = false)
    private Career career;

    @Enumerated(EnumType.STRING)
    @Column(name = "average_working")
    private AverageWorking averageWorking;

    @Enumerated(EnumType.STRING)
    @Column(name = "average_delivery")
    private AverageDelivery averageDelivery;

    @Enumerated(EnumType.STRING)
    @Column(name = "blood_pressure", nullable = false)
    private BloodPressure bloodPressure;

    @Column(name = "profile_image")
    private String profileImage;

    @Column(name = "approval_status", nullable = false)
    private Boolean approvalStatus = false;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "deleted_date")
    private LocalDateTime deleteDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Driver driver;
}