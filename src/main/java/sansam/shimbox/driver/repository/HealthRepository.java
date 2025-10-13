package sansam.shimbox.driver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sansam.shimbox.driver.domain.Driver;
import sansam.shimbox.driver.domain.Health;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HealthRepository extends JpaRepository<Health, Integer> {
    Optional<Health> findTopByDriverOrderByCreatedDateDesc(Driver driver);
    Optional<Health> findTopByDriverAndLeaveWorkTimeIsNotNullOrderByLeaveWorkTimeDesc(Driver driver);
    List<Health> findByDriverAndCreatedDateBetweenAndIsDeletedFalse(Driver driver, LocalDateTime startDate, LocalDateTime endDate);
}
