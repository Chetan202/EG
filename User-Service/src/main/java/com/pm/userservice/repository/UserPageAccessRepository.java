package com.pm.userservice.repository;

import com.pm.userservice.entity.UserPageAccess;
import com.pm.userservice.enums.PageAccessLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for UserPageAccess entity
 */
@Repository
public interface UserPageAccessRepository extends JpaRepository<UserPageAccess, String> {

    /**
     * Find access record for user and page
     */
    Optional<UserPageAccess> findByUserIdAndPage(String userId, PageAccessLevel page);

    /**
     * Find all page access records for a user
     */
    List<UserPageAccess> findByUserId(String userId);

    /**
     * Find all granted pages for a user
     */
    List<UserPageAccess> findByUserIdAndGrantedTrue(String userId);

    /**
     * Find all revoked pages for a user
     */
    List<UserPageAccess> findByUserIdAndGrantedFalse(String userId);

    /**
     * Find custom access grants for a user
     */
    @Query("SELECT upa FROM UserPageAccess upa WHERE upa.user.id = :userId AND upa.granted = true")
    List<UserPageAccess> findCustomGrantsForUser(@Param("userId") String userId);

    /**
     * Check if access is granted (can be overridden by custom record)
     */
    @Query("SELECT CASE WHEN upa IS NULL THEN true WHEN upa.granted = true THEN true ELSE false END " +
           "FROM UserPageAccess upa WHERE upa.user.id = :userId AND upa.page = :page")
    Boolean isPageAccessGranted(@Param("userId") String userId, @Param("page") PageAccessLevel page);

    /**
     * Delete access record
     */
    void deleteByUserIdAndPage(String userId, PageAccessLevel page);
}

