package sansam.shimbox.product.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import sansam.shimbox.global.common.BaseTimeEntity;
import sansam.shimbox.product.enums.ShippingStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "TB_PRODUCT_TIMELINE")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE TB_PRODUCT_TIMELINE SET is_deleted = true, delete_date = NOW() WHERE timeline_id = ?")
@SQLRestriction("is_deleted = false")
public class ProductTimeLine extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "timeline_id")
    private Long timeLineId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ShippingStatus status;

    @Column(name = "status_changed_at", nullable = false)
    private String statusChangedAt;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "address_short")
    private String addressShort;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @Column(name = "delete_date")
    private LocalDateTime deleteDate;
}