package sansam.shimbox.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sansam.shimbox.product.domain.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}
