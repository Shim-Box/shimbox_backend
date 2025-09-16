package sansam.shimbox.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sansam.shimbox.product.domain.ProductTimeLine;

import java.util.List;

@Repository
public interface ProductTimeLineRepository extends JpaRepository<ProductTimeLine, Long> {
    List<ProductTimeLine> findByProductProductIdOrderByCreatedDateDesc(Long productId);
}
