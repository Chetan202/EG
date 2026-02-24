package com.pm.userservice.repository;

import com.pm.userservice.entity.Enterprise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Enterprise entity
 */
@Repository
public interface EnterpriseRepository extends JpaRepository<Enterprise, String> {

    Optional<Enterprise> findByCode(String code);

    Optional<Enterprise> findByEmail(String email);

    boolean existsByCode(String code);
}

