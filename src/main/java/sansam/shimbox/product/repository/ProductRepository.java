package sansam.shimbox.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sansam.shimbox.driver.domain.Driver;
import sansam.shimbox.product.domain.Product;
import sansam.shimbox.product.enums.ShippingStatus;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {


    @Query("SELECT p FROM Product p WHERE p.isDeleted = false AND p.driver.user.id = :userId")
    List<Product> findActiveProductsByDriverId(@Param("userId") Long userId);

    @Query("SELECT p FROM Product p WHERE p.isDeleted = false AND p.driver.user.id = :userId")
    List<Product> findActiveProductsByDriverUserId(@Param("userId") Long userId);

    List<Product> findByDriverAndIsDeletedFalse(Driver driver);
    
    List<Product> findByDriverAndShippingStatusInAndIsDeletedFalse(Driver driver, List<ShippingStatus> shippingStatuses);
    
    // 특정 배송 상태의 상품 조회
    List<Product> findByDriverAndShippingStatusAndIsDeletedFalse(Driver driver, ShippingStatus shippingStatus);
    
    // 최신 라운드 상품 조회 (ProductService에서 사용)
    List<Product> findByDriverAndShippingStatusInAndIsDeletedFalseAndAssignedRound(
            Driver driver, List<ShippingStatus> shippingStatuses, Integer assignedRound);
    
    // 최신 라운드 상품 조회 (한 번의 쿼리로 최신 라운드만 조회)
    @Query("SELECT p FROM Product p WHERE p.driver = :driver " +
           "AND p.shippingStatus IN :shippingStatuses " +
           "AND p.isDeleted = false " +
           "AND p.assignedRound = (SELECT MAX(p2.assignedRound) FROM Product p2 WHERE p2.driver = :driver AND p2.isDeleted = false AND p2.assignedRound IS NOT NULL)")
    List<Product> findLatestRoundProductsByDriver(
            @Param("driver") Driver driver,
            @Param("shippingStatuses") List<ShippingStatus> shippingStatuses);
    
    @Query("SELECT p FROM Product p WHERE p.driver = :driver " +
           "AND p.shippingStatus = :shippingStatus " +
           "AND p.isDeleted = false " +
           "AND p.assignedRound = (SELECT MAX(p2.assignedRound) FROM Product p2 WHERE p2.driver = :driver AND p2.isDeleted = false AND p2.assignedRound IS NOT NULL)")
    List<Product> findLatestRoundProductsByDriverAndStatus(
            @Param("driver") Driver driver,
            @Param("shippingStatus") ShippingStatus shippingStatus);
    
    // 할당되지 않은 상품 조회 (기사에게 할당하기 전) - shippingStatus가 null
    List<Product> findByDriverIsNullAndShippingStatusIsNullAndIsDeletedFalse();

}
