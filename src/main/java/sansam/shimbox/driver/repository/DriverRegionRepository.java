package sansam.shimbox.driver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sansam.shimbox.driver.domain.Driver;
import sansam.shimbox.driver.domain.DriverRegion;
import sansam.shimbox.driver.enums.Region;

import java.util.List;

@Repository
public interface DriverRegionRepository extends JpaRepository<DriverRegion, Long> {

    List<DriverRegion> findByDriverOrderByCreatedDateAsc(Driver driver);
    
    List<DriverRegion> findByDriverDriverIdOrderByCreatedDateAsc(Long driverId);
    
    List<DriverRegion> findByRegion(Region region);
    
    int countByDriver(Driver driver);
}
