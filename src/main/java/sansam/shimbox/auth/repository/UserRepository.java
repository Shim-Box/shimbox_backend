package sansam.shimbox.auth.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sansam.shimbox.auth.domain.User;
import sansam.shimbox.driver.enums.Attendance;
import sansam.shimbox.driver.enums.ConditionStatus;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    Page<User> findAllByApprovalStatusFalse(Pageable pageable);

    @Query("""
    SELECT u FROM User u
    JOIN u.driver d
    JOIN d.driverRealtime dr
    WHERE u.approvalStatus = true
    AND (:residence IS NULL OR u.residence = :residence)
    AND (:attendance IS NULL OR d.attendance = :attendance)
    AND (:conditionStatus IS NULL OR dr.realTimeConditionStatus = :conditionStatus)
    """)
    Page<User> findApprovedUsersWithFilter(
            @Param("residence") String residence,
            @Param("attendance") Attendance attendance,
            @Param("conditionStatus") ConditionStatus conditionStatus,
            Pageable pageable
    );
}