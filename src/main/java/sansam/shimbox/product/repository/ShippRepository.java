package sansam.shimbox.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sansam.shimbox.product.domain.Shipp;

@Repository
public interface ShippRepository extends JpaRepository<Shipp, Long> {
}
