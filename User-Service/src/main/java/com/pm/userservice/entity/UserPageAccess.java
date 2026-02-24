package com.pm.userservice.entity;

import com.pm.userservice.enums.PageAccessLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * User Page Access entity
 * Tracks custom page access grants/revokes for users
 */
@Entity
@Table(name = "user_page_access", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "page_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPageAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "page_id", nullable = false)
    @Enumerated(EnumType.STRING)
    private PageAccessLevel page;

    @Column(nullable = false)
    @Builder.Default
    private Boolean granted = true; // true = granted, false = revoked

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "granted_by_id", nullable = false)
    private User grantedBy; // Admin who granted/revoked access

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime modifiedAt;

    @Column(length = 500)
    private String reason; // Reason for grant/revoke

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        modifiedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        modifiedAt = LocalDateTime.now();
    }
}

