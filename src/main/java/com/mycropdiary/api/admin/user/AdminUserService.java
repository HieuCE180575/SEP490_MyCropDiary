package com.mycropdiary.api.admin.user;

import com.mycropdiary.api.common.api.PageResponse;
import com.mycropdiary.api.common.exception.BusinessException;
import com.mycropdiary.api.common.exception.ResourceNotFoundException;
import com.mycropdiary.api.user.User;
import com.mycropdiary.api.user.UserRepository;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class AdminUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // -------------------------------------------------------------------------
    // List with optional filter + search
    // -------------------------------------------------------------------------

    public PageResponse<AdminUserResponse> listUsers(
            String search, User.Role role, User.Status status, int page, int size) {

        PageRequest pageable = PageRequest.of(page, Math.min(size, 100),
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<User> result;
        if (search != null && !search.isBlank()) {
            String q = search.trim();
            if (role != null && status != null) {
                result = userRepository.searchByEmailOrNameAndRoleAndStatus(q, role, status, pageable);
            } else if (role != null) {
                result = userRepository.searchByEmailOrNameAndRole(q, role, pageable);
            } else if (status != null) {
                result = userRepository.searchByEmailOrNameAndStatus(q, status, pageable);
            } else {
                result = userRepository.searchByEmailOrName(q, pageable);
            }
        } else if (role != null && status != null) {
            result = userRepository.findByRoleAndStatusAndDeletedAtIsNull(role, status, pageable);
        } else if (role != null) {
            result = userRepository.findByRoleAndDeletedAtIsNull(role, pageable);
        } else if (status != null) {
            result = userRepository.findByStatusAndDeletedAtIsNull(status, pageable);
        } else {
            result = userRepository.findByDeletedAtIsNull(pageable);
        }

        return PageResponse.from(result.map(AdminUserResponse::from));
    }

    // -------------------------------------------------------------------------
    // Get single user
    // -------------------------------------------------------------------------

    public AdminUserResponse getUser(Long id) {
        return AdminUserResponse.from(findActiveUser(id));
    }

    // -------------------------------------------------------------------------
    // Create user
    // -------------------------------------------------------------------------

    @Transactional
    public AdminUserResponse createUser(CreateUserRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new BusinessException("EMAIL_TAKEN",
                    "Email address is already registered: " + normalizedEmail,
                    HttpStatus.CONFLICT);
        }

        User user = new User();
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName().trim());
        user.setPhoneNumber(request.phoneNumber());
        user.setRole(request.role() != null ? request.role() : User.Role.USER);
        // Admin-created accounts are immediately active (no email verification required)
        user.setStatus(User.Status.ACTIVE);
        user.setEmailVerifiedAt(Instant.now());

        return AdminUserResponse.from(userRepository.save(user));
    }

    // -------------------------------------------------------------------------
    // Update user
    // -------------------------------------------------------------------------

    @Transactional
    public AdminUserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = findActiveUser(id);

        if (request.fullName() != null && !request.fullName().isBlank()) {
            user.setFullName(request.fullName().trim());
        }
        if (request.phoneNumber() != null) {
            user.setPhoneNumber(request.phoneNumber());
        }
        if (request.address() != null) {
            user.setAddress(request.address());
        }
        if (request.avatarUrl() != null) {
            user.setAvatarUrl(request.avatarUrl());
        }
        if (request.role() != null) {
            user.setRole(request.role());
        }

        return AdminUserResponse.from(userRepository.save(user));
    }

    // -------------------------------------------------------------------------
    // Lock / Unlock
    // -------------------------------------------------------------------------

    @Transactional
    public void lockUser(Long id, Long callerAdminId) {
        if (id.equals(callerAdminId)) {
            throw new BusinessException("SELF_LOCK_FORBIDDEN",
                    "An admin cannot lock their own account", HttpStatus.FORBIDDEN);
        }
        User user = findActiveUser(id);
        if (user.getStatus() == User.Status.LOCKED) {
            throw new BusinessException("ALREADY_LOCKED",
                    "User is already locked", HttpStatus.CONFLICT);
        }
        user.setStatus(User.Status.LOCKED);
        userRepository.save(user);
    }

    @Transactional
    public void unlockUser(Long id) {
        User user = findActiveUser(id);
        if (user.getStatus() != User.Status.LOCKED) {
            throw new BusinessException("NOT_LOCKED",
                    "User is not locked", HttpStatus.CONFLICT);
        }
        user.setStatus(User.Status.ACTIVE);
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);
    }

    // -------------------------------------------------------------------------
    // Soft delete
    // -------------------------------------------------------------------------

    @Transactional
    public void deleteUser(Long id, Long callerAdminId) {
        if (id.equals(callerAdminId)) {
            throw new BusinessException("SELF_DELETE_FORBIDDEN",
                    "An admin cannot delete their own account", HttpStatus.FORBIDDEN);
        }
        User user = findActiveUser(id);
        user.setDeletedAt(Instant.now());
        userRepository.save(user);
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private User findActiveUser(Long id) {
        return userRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    // =========================================================================
    // DTOs
    // =========================================================================

    public record AdminUserResponse(
            Long id,
            String email,
            String fullName,
            String phoneNumber,
            String avatarUrl,
            String address,
            User.Role role,
            User.Status status,
            Instant emailVerifiedAt,
            Instant lastLoginAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        static AdminUserResponse from(User u) {
            return new AdminUserResponse(
                    u.getId(), u.getEmail(), u.getFullName(), u.getPhoneNumber(),
                    u.getAvatarUrl(), u.getAddress(), u.getRole(), u.getStatus(),
                    u.getEmailVerifiedAt(), u.getLastLoginAt(),
                    u.getCreatedAt(), u.getUpdatedAt());
        }
    }

    public record CreateUserRequest(
            @Email @NotBlank String email,
            @NotBlank @Size(min = 8, max = 72) String password,
            @NotBlank @Size(max = 150) String fullName,
            @Size(max = 20) String phoneNumber,
            @Schema(description = "Default USER if not provided") User.Role role
    ) {}

    public record UpdateUserRequest(
            @Size(max = 150) String fullName,
            @Size(max = 20) String phoneNumber,
            @Size(max = 500) String address,
            @Size(max = 500) String avatarUrl,
            User.Role role
    ) {}
}
