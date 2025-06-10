package sansam.shimbox.driver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sansam.shimbox.driver.domain.Driver;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {
}
