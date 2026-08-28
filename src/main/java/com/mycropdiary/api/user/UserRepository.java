package com.mycropdiary.api.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // --- Auth queries ---
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);

    // --- Admin list queries (soft-delete aware) ---
    Page<User> findByDeletedAtIsNull(Pageable pageable);

    Page<User> findByRoleAndDeletedAtIsNull(User.Role role, Pageable pageable);

    Page<User> findByStatusAndDeletedAtIsNull(User.Status status, Pageable pageable);

    Page<User> findByRoleAndStatusAndDeletedAtIsNull(
            User.Role role, User.Status status, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL AND " +
           "(LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> searchByEmailOrName(@Param("search") String search, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL AND " +
           "(LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')))")  
    Page<User> searchByEmailOrName(@Param("search") String search, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL AND u.role = :role AND " +
           "(LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')))")  
    Page<User> searchByEmailOrNameAndRole(
            @Param("search") String search,
            @Param("role") User.Role role,
            Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL AND u.status = :status AND " +
           "(LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')))")  
    Page<User> searchByEmailOrNameAndStatus(
            @Param("search") String search,
            @Param("status") User.Status status,
            Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL AND u.role = :role AND u.status = :status AND " +
           "(LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')))")  
    Page<User> searchByEmailOrNameAndRoleAndStatus(
            @Param("search") String search,
            @Param("role") User.Role role,
            @Param("status") User.Status status,
            Pageable pageable);

    // --- Get by id excluding soft-deleted ---
    Optional<User> findByIdAndDeletedAtIsNull(Long id);
}
