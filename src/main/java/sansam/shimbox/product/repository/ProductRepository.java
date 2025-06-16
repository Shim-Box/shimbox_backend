package sansam.shimbox.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sansam.shimbox.product.domain.Product;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {


    @Query("SELECT p FROM Product p WHERE p.isDeleted = false AND p.driver.user.id = :userId")
    List<Product> findActiveProductsByDriverId(@Param("userId") Long userId);

    @Query("SELECT p FROM Product p WHERE p.isDeleted = false AND p.driver.user.id = :userId")
    List<Product> findActiveProductsByDriverUserId(@Param("userId") Long userId);
}
