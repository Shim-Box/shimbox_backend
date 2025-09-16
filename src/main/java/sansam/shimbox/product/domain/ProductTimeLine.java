package sansam.shimbox.product.domain;

import jakarta.persistence.*;
import lombok.*;
import sansam.shimbox.auth.domain.User;
import sansam.shimbox.driver.domain.Driver;
import sansam.shimbox.global.common.BaseTimeEntity;
import sansam.shimbox.product.enums.ShippingStatus;

@Entity
@Table(name = "TB_PRODUCT_TIME")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductTimeLine extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_time_line_id")
    private Long productTimeLineId;

    @Enumerated(EnumType.STRING)
    @Column(name = "prev_status", nullable = false, length = 50)
    private ShippingStatus prevStatus;

    @Column(name = "location", nullable = false, length = 50)
    private String location;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Driver driver;
}
