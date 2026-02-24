package com.pm.userservice.controller;

import com.pm.userservice.entity.Enterprise;
import com.pm.userservice.repository.EnterpriseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * Enterprise Management Controller
 * Manages multiple companies/tenants in the system
 */
@RestController
@RequestMapping("/api/enterprises")
@RequiredArgsConstructor
@Slf4j
public class EnterpriseController {

    private final EnterpriseRepository enterpriseRepository;

    /**
     * Create new enterprise
     * POST /api/enterprises
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Enterprise> createEnterprise(@Valid @RequestBody Enterprise enterprise) {
        log.info("Creating new enterprise: {}", enterprise.getCode());
        Enterprise saved = enterpriseRepository.save(enterprise);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * Get enterprise by ID
     * GET /api/enterprises/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Enterprise> getEnterprise(@PathVariable String id) {
        log.info("Fetching enterprise: {}", id);
        Enterprise enterprise = enterpriseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Enterprise not found: " + id));
        return ResponseEntity.ok(enterprise);
    }

    /**
     * Get enterprise by code
     * GET /api/enterprises/code/{code}
     */
    @GetMapping("/code/{code}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Enterprise> getEnterpriseByCode(@PathVariable String code) {
        log.info("Fetching enterprise by code: {}", code);
        Enterprise enterprise = enterpriseRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Enterprise not found with code: " + code));
        return ResponseEntity.ok(enterprise);
    }

    /**
     * Get all enterprises
     * GET /api/enterprises
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Enterprise>> getAllEnterprises() {
        log.info("Fetching all enterprises");
        List<Enterprise> enterprises = enterpriseRepository.findAll();
        return ResponseEntity.ok(enterprises);
    }

    /**
     * Update enterprise
     * PUT /api/enterprises/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Enterprise> updateEnterprise(
            @PathVariable String id,
            @Valid @RequestBody Enterprise enterpriseDetails) {
        log.info("Updating enterprise: {}", id);

        Enterprise enterprise = enterpriseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Enterprise not found: " + id));

        enterprise.setName(enterpriseDetails.getName());
        enterprise.setDescription(enterpriseDetails.getDescription());
        enterprise.setEmail(enterpriseDetails.getEmail());
        enterprise.setPhoneNumber(enterpriseDetails.getPhoneNumber());
        enterprise.setAddress(enterpriseDetails.getAddress());
        enterprise.setCity(enterpriseDetails.getCity());
        enterprise.setCountry(enterpriseDetails.getCountry());
        enterprise.setZipCode(enterpriseDetails.getZipCode());
        enterprise.setActive(enterpriseDetails.getActive());

        Enterprise updated = enterpriseRepository.save(enterprise);
        return ResponseEntity.ok(updated);
    }

    /**
     * Deactivate enterprise
     * DELETE /api/enterprises/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateEnterprise(@PathVariable String id) {
        log.info("Deactivating enterprise: {}", id);

        Enterprise enterprise = enterpriseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Enterprise not found: " + id));

        enterprise.setActive(false);
        enterpriseRepository.save(enterprise);

        return ResponseEntity.noContent().build();
    }

    /**
     * Health check
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Enterprise Service is UP");
    }
}

