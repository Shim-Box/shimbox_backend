package sansam.shimbox.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sansam.shimbox.product.domain.Product;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("SELECT p FROM Product p JOIN p.shipp s WHERE s.driver.driverId = :driverId AND DATE(p.estimatedArrivalTime) = CURRENT_DATE AND p.isDeleted = false")
    List<Product> findAllByDriverAndToday(@Param("driverId") Long driverId);
}
