package sansam.shimbox.product.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import sansam.shimbox.driver.domain.Driver;
import sansam.shimbox.global.common.BaseTimeEntity;
import sansam.shimbox.product.enums.ShippingStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "TB_SHIPP")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE TB_SHIPP SET is_deleted = true, deleted_date = NOW() WHERE shipp_id = ?")
@SQLRestriction("is_deleted = false")
public class Shipp extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shipp_id")
    private Long shippId;

    @Enumerated(EnumType.STRING)
    @Column(name = "shipping_status", nullable = false)
    private ShippingStatus shippingStatus = ShippingStatus.WAITING;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "delete_date")
    private LocalDateTime deleteDate;

    @OneToOne
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;
}