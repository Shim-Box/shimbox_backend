package sansam.shimbox.product.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import sansam.shimbox.global.common.BaseTimeEntity;
import sansam.shimbox.product.enums.ProductStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "TB_PRODUCT")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE TB_PRODUCT SET is_deleted = true, deleted_date = NOW() WHERE product_id = ?")
@SQLRestriction("is_deleted = false")
public class Product extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "waybil", nullable = false, columnDefinition = "BINARY(16)")
    private UUID waybill = UUID.randomUUID();

    @Column(name = "product_name", length = 100, nullable = false)
    private String productName;

    @Column(name = "product_weight", precision = 5, scale = 2, nullable = false)
    private BigDecimal productWeight;

    @Column(name = "recipient_name", length = 50, nullable = false)
    private String recipientName;

    @Column(nullable = false, length = 50)
    private String address;

    @Column(name = "detail_address", nullable = false)
    private String detailAddress;

    @Column(name = "recipient_phone_number", length = 20, nullable = false)
    private String recipientPhoneNumber;

    @Column(name = "postal_code", length = 10, nullable = false)
    private String postalCode;

    @Column(name = "estimated_arrival_time", nullable = false)
    private LocalDateTime estimatedArrivalTime;

    @Column(name = "shipping_location", nullable = false)
    private String shippingLocation;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_status", nullable = false)
    private ProductStatus productStatus = ProductStatus.BEFORE_SORTING;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "delete_date")
    private LocalDateTime deleteDate;

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private Shipp shipp;
}