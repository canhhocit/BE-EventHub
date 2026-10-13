package com.sa.event_mng.modules.identity.presentation.controller;

import com.sa.event_mng.modules.identity.application.dto.request.UserUpdateRequest;
import com.sa.event_mng.modules.identity.application.dto.response.OrganizerResponse;
import com.sa.event_mng.modules.identity.application.dto.response.UserResponse;
import com.sa.event_mng.modules.identity.application.service.UserService;
import com.sa.event_mng.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Tag(name = "Người dùng", description = "Hồ sơ và quản lý người dùng")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    // ===================== CHUNG =====================

    @Operation(summary = "Lấy thông tin của chính mình")
    @GetMapping("/my-info")
    public ApiResponse<UserResponse> getMyInfo() {
        return ApiResponse.<UserResponse>builder().result(userService.getMyInfo()).build();
    }

    @Operation(summary = "Cập nhật hồ sơ cá nhân")
    @PutMapping("/{username}")
    public ApiResponse<UserResponse> updateUser(@PathVariable String username,
            @RequestBody @Valid UserUpdateRequest request) {
        return ApiResponse.<UserResponse>builder().result(userService.updateUser(username, request)).build();
    }

    // ===================== ADMIN: quản lý CUSTOMER & ORGANIZER =====================

    @Operation(summary = "[ADMIN] Lấy danh sách Customer, Organizer, Staff với bộ lọc & tìm kiếm",
               description = "Tham số: role (CUSTOMER/ORGANIZER/STAFF), status (ACTIVE/LOCKED), search (username/fullname/email), sort (asc/desc)")
    @GetMapping("/admin")
    public ApiResponse<Page<UserResponse>> getUsersByRole(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "desc") String sort) {
        Sort.Direction direction = "asc".equalsIgnoreCase(sort) || "createdAt,asc".equalsIgnoreCase(sort)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        PageRequest pageRequest = PageRequest.of(Math.max(0, page - 1), size, Sort.by(direction, "createdAt"));
        return ApiResponse.<Page<UserResponse>>builder()
                .result(userService.getUsersByRole(role, status, search, pageRequest))
                .build();
    }

    @Operation(summary = "[ADMIN] Thống kê số lượng Customer và Organizer")
    @GetMapping("/admin/stats")
    public ApiResponse<Map<String, Long>> getUserStats() {
        return ApiResponse.<Map<String, Long>>builder().result(userService.getUserStats()).build();
    }

    @Operation(summary = "[ADMIN] Vô hiệu hóa tài khoản Customer/Organizer")
    @DeleteMapping("/admin/{username}")
    public ApiResponse<String> deleteUser(@PathVariable String username) {
        return ApiResponse.<String>builder().result(userService.deleteUser(username)).build();
    }

    @Operation(summary = "[ADMIN] Mở khóa tài khoản Customer/Organizer")
    @PatchMapping("/admin/{username}/unlock")
    public ApiResponse<String> unlockUser(@PathVariable String username) {
        return ApiResponse.<String>builder().result(userService.unlockUser(username)).build();
    }

    // ===================== ORGANIZER: quản lý STAFF của mình =====================

    @Operation(summary = "[ORGANIZER] Xem danh sách Staff của mình")
    @GetMapping("/organizer/my-staff")
    public ApiResponse<Page<UserResponse>> getMyStaff(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        return ApiResponse.<Page<UserResponse>>builder()
                .result(userService.getMyStaff(search, pageRequest))
                .build();
    }

    @Operation(summary = "[STAFF] Xem thông tin organizer của mình")
    @GetMapping("/staff/organizer/{organizerId}")
    public ApiResponse<OrganizerResponse> getMyOrganizer(@PathVariable Long organizerId) {
        return ApiResponse.<OrganizerResponse>builder()
                .result(userService.getMyOrganizer(organizerId))
                .build();
    }

    @Operation(summary = "[ORGANIZER] Thêm tài khoản Staff mới")
    @PostMapping({"/organizer/staff", "/organizer/create-staff"})
    public ApiResponse<String> createMyStaff(@RequestBody @Valid com.sa.event_mng.modules.identity.application.dto.request.UserCreateRequest request) {
        return ApiResponse.<String>builder().result(userService.createStaff(request)).build();
    }

    @Operation(summary = "[ORGANIZER] Xóa nhân viên của mình")
    @DeleteMapping({"/organizer/staff/{username}", "/organizer/staff/{username}/delete"})
    public ApiResponse<String> deleteMyStaff(@PathVariable String username) {
        return ApiResponse.<String>builder().result(userService.deleteMyStaff(username)).build();
    }

    @Operation(summary = "[ORGANIZER] Vô hiệu hóa (Khóa) Staff của mình")
    @PatchMapping("/organizer/staff/{username}/disable")
    public ApiResponse<String> disableMyStaff(@PathVariable String username) {
        return ApiResponse.<String>builder().result(userService.disableMyStaff(username)).build();
    }

    @Operation(summary = "[ORGANIZER] Kích hoạt lại (Mở khóa) Staff của mình")
    @PatchMapping("/organizer/staff/{username}/enable")
    public ApiResponse<String> enableMyStaff(@PathVariable String username) {
        return ApiResponse.<String>builder().result(userService.enableMyStaff(username)).build();
    }
}
