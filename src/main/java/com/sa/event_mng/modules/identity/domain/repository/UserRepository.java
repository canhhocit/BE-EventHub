package com.sa.event_mng.modules.identity.domain.repository;

import com.sa.event_mng.modules.identity.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByUsernameAndEnabledTrue(String username);
    Optional<User> findByEmailAndEnabledTrue(String email);
    Optional<User> findByIdAndEnabledTrue(Long id);
    Page<User> findAllByEnabledTrue(Pageable pageable);
    Optional<User> findByVerificationToken(String verificationToken);
    boolean existsByUsername(String username);
    boolean existsByEmailAndEnabledTrue(String email);
    Optional<User> findByOtp(String otp);

    // Admin: tìm user theo role (CUSTOMER hoặc ORGANIZER)
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName AND (u.username LIKE %:search% OR u.fullName LIKE %:search%)")
    Page<User> findByRoleNameAndSearch(@Param("roleName") String roleName, @Param("search") String search, Pageable pageable);

    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE " +
           "r.name IN :roleNames AND " +
           "(:status IS NULL OR u.enabled = :status) AND (" +
           ":search IS NULL OR :search = '' OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(COALESCE(u.fullName, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(COALESCE(u.email, '')) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> findByRoleNamesAndSearch(
            @Param("roleNames") List<String> roleNames,
            @Param("status") Boolean status,
            @Param("search") String search,
            Pageable pageable);

    // Đếm user theo role
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.name = :roleName AND u.enabled = true")
    long countByRoleName(@Param("roleName") String roleName);

    // Organizer: lấy danh sách Staff thuộc mình
    Page<User> findByOrganizer_IdAndRoles_Name(Long organizerId, String roleName, Pageable pageable);

    List<User> findByRoles_Name(String roleName);
}

