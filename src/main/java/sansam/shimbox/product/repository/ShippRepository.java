package sansam.shimbox.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sansam.shimbox.product.domain.Shipp;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShippRepository extends JpaRepository<Shipp, Long> {
    List<Shipp> findAllByIsDeletedFalseAndDriver_DriverId(Long driverId);
    Optional<Shipp> findByProduct_ProductId(Long productId);
}
