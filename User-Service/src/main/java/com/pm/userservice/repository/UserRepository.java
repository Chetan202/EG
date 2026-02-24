package com.pm.userservice.repository;

import com.pm.userservice.entity.User;
import com.pm.userservice.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for User entity with multi-tenant support
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * Find user by email and enterprise (multi-tenant)
     */
    Optional<User> findByEmailAndEnterpriseId(String email, String enterpriseId);

    /**
     * Find user by employee ID within an enterprise
     */
    Optional<User> findByEmployeeIdAndEnterpriseId(String employeeId, String enterpriseId);

    /**
     * Find all users in an enterprise
     */
    List<User> findByEnterpriseId(String enterpriseId);

    /**
     * Find all active users in an enterprise
     */
    List<User> findByEnterpriseIdAndActiveTrue(String enterpriseId);

    /**
     * Find users by role in an enterprise
     */
    List<User> findByEnterpriseIdAndRole(String enterpriseId, UserRole role);

    /**
     * Find reports of a manager in an enterprise
     */
    List<User> findByManagerIdAndEnterpriseId(String managerId, String enterpriseId);

    /**
     * Find all HR users in an enterprise
     */
    @Query("SELECT u FROM User u WHERE u.enterprise.id = :enterpriseId AND u.role IN ('HR', 'ADMIN_HR', 'ADMIN')")
    List<User> findHRUsersInEnterprise(@Param("enterpriseId") String enterpriseId);

    /**
     * Check if email exists in enterprise
     */
    boolean existsByEmailAndEnterpriseId(String email, String enterpriseId);

    /**
     * Check if employee ID exists in enterprise
     */
    boolean existsByEmployeeIdAndEnterpriseId(String employeeId, String enterpriseId);
}

