package sansam.shimbox.driver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sansam.shimbox.driver.domain.Health;

@Repository
public interface HealthRepository extends JpaRepository<Health, Integer> {
}
