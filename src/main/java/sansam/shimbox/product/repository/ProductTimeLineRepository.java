package sansam.shimbox.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sansam.shimbox.product.domain.Product;
import sansam.shimbox.product.domain.ProductTimeLine;

import java.util.List;

@Repository
public interface ProductTimelineRepository extends JpaRepository<ProductTimeLine, Long> {

    @Query("SELECT pt FROM ProductTimeLine pt WHERE pt.product = :product AND pt.isDeleted = false ORDER BY pt.statusChangedAt ASC")
    List<ProductTimeLine> findByProductOrderByStatusChangedAtAsc(@Param("product") Product product);

    @Query("SELECT pt FROM ProductTimeLine pt WHERE pt.product.productId = :productId AND pt.isDeleted = false ORDER BY pt.statusChangedAt ASC")
    List<ProductTimeLine> findByProductIdOrderByStatusChangedAtAsc(@Param("productId") Long productId);
}