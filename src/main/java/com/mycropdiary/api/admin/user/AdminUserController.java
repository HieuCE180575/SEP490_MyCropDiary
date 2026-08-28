package com.mycropdiary.api.admin.user;

import com.mycropdiary.api.common.api.ApiResponse;
import com.mycropdiary.api.common.api.PageResponse;
import com.mycropdiary.api.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin endpoints for user management.
 * All endpoints require ADMIN role.
 *
 * <p>Note: callerAdminId is temporarily read from the {@code X-Demo-User-Id} header.
 * Replace with JWT security context when Week 3 authentication is implemented.
 */
@Tag(name = "Admin – User Management", description = "CRUD and lock/unlock users (ADMIN only)")
@RestController
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService service;

    public AdminUserController(AdminUserService service) {
        this.service = service;
    }

    // -------------------------------------------------------------------------
    // GET /admin/users  – list with optional filter + search
    // -------------------------------------------------------------------------

    @Operation(summary = "List users", description = "Paginated list with optional role/status filter and name/email search")
    @GetMapping
    ApiResponse<PageResponse<AdminUserService.AdminUserResponse>> listUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) User.Role role,
            @RequestParam(required = false) User.Status status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(service.listUsers(search, role, status, page, size));
    }

    // -------------------------------------------------------------------------
    // GET /admin/users/{id}  – get single user
    // -------------------------------------------------------------------------

    @Operation(summary = "Get user detail")
    @GetMapping("/{id}")
    ApiResponse<AdminUserService.AdminUserResponse> getUser(@PathVariable Long id) {
        return ApiResponse.success(service.getUser(id));
    }

    // -------------------------------------------------------------------------
    // POST /admin/users  – create user
    // -------------------------------------------------------------------------

    @Operation(summary = "Create user", description = "Admin creates a new user. Account is immediately ACTIVE.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ApiResponse<AdminUserService.AdminUserResponse> createUser(
            @Valid @RequestBody AdminUserService.CreateUserRequest request) {
        return ApiResponse.success("MSG-ADM-001", "User created successfully",
                service.createUser(request));
    }

    // -------------------------------------------------------------------------
    // PUT /admin/users/{id}  – update user info
    // -------------------------------------------------------------------------

    @Operation(summary = "Update user", description = "Updates fullName, phoneNumber, address, avatarUrl, role. Email cannot be changed.")
    @PutMapping("/{id}")
    ApiResponse<AdminUserService.AdminUserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody AdminUserService.UpdateUserRequest request) {
        return ApiResponse.success("MSG-ADM-002", "User updated successfully",
                service.updateUser(id, request));
    }

    // -------------------------------------------------------------------------
    // PATCH /admin/users/{id}/lock  – lock user
    // -------------------------------------------------------------------------

    @Operation(summary = "Lock user", description = "Sets user status to LOCKED. Admin cannot lock themselves.")
    @PatchMapping("/{id}/lock")
    ApiResponse<Void> lockUser(
            @PathVariable Long id,
            @RequestParam(name = "callerId", defaultValue = "0") Long callerId) {
        service.lockUser(id, callerId);
        return ApiResponse.success("MSG-ADM-003", "User locked successfully", null);
    }

    // -------------------------------------------------------------------------
    // PATCH /admin/users/{id}/unlock  – unlock user
    // -------------------------------------------------------------------------

    @Operation(summary = "Unlock user", description = "Sets user status back to ACTIVE and resets failed login counter.")
    @PatchMapping("/{id}/unlock")
    ApiResponse<Void> unlockUser(@PathVariable Long id) {
        service.unlockUser(id);
        return ApiResponse.success("MSG-ADM-004", "User unlocked successfully", null);
    }

    // -------------------------------------------------------------------------
    // DELETE /admin/users/{id}  – soft delete
    // -------------------------------------------------------------------------

    @Operation(summary = "Delete user (soft)", description = "Sets deleted_at timestamp. Admin cannot delete themselves.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteUser(
            @PathVariable Long id,
            @RequestParam(name = "callerId", defaultValue = "0") Long callerId) {
        service.deleteUser(id, callerId);
    }
}
