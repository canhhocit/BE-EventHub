package com.sa.event_mng.modules.identity.application.service;

import com.sa.event_mng.modules.identity.application.dto.request.UserCreateRequest;
import com.sa.event_mng.modules.identity.application.dto.request.UserUpdateRequest;
import com.sa.event_mng.modules.identity.application.dto.response.OrganizerResponse;
import com.sa.event_mng.modules.identity.application.dto.response.UserResponse;
import com.sa.event_mng.shared.exception.AppException;
import com.sa.event_mng.shared.exception.ErrorCode;
import com.sa.event_mng.modules.identity.application.mapper.UserMapper;
import com.sa.event_mng.modules.identity.domain.model.Role;
import com.sa.event_mng.modules.identity.domain.model.User;
import com.sa.event_mng.modules.identity.domain.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    com.sa.event_mng.modules.identity.domain.repository.RoleRepository roleRepository;
    com.sa.event_mng.shared.infrastructure.email.EmailService emailService;

    @PreAuthorize("isAuthenticated()")
    public UserResponse getMyInfo() {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(name)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return userMapper.toUserResponse(user);
    }

    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsernameAndEnabledTrue(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return userMapper.toUserResponse(user);
    }

    // ===================== ADMIN: quản lý CUSTOMER và ORGANIZER =====================

    @PreAuthorize("hasRole('ADMIN')")
    public Page<UserResponse> getUsersByRole(String role, String search, PageRequest pageRequest) {
        // Admin chỉ được xem CUSTOMER và ORGANIZER, không thấy STAFF
        String normalizedRole = role != null && !role.isBlank() ? role.toUpperCase() : null;
        if (normalizedRole != null && !normalizedRole.equals("CUSTOMER") && !normalizedRole.equals("ORGANIZER")) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        List<String> allowedRoles = normalizedRole != null
                ? List.of(normalizedRole)
                : List.of("CUSTOMER", "ORGANIZER");
        return userRepository.findByRoleNamesAndSearch(allowedRoles, search != null ? search : "", pageRequest)
                .map(userMapper::toUserResponse);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Long> getUserStats() {
        long organizerCount = userRepository.countByRoleName("ORGANIZER");
        long staffCount = userRepository.countByRoleName("STAFF");
        long customerCount = userRepository.countByRoleName("CUSTOMER");
        return Map.of(
                "customers", customerCount,
                "organizers", organizerCount,
                "staffs", staffCount,
                "total", customerCount + organizerCount + staffCount
        );
    }

    @PreAuthorize("hasRole('ADMIN') or #username == authentication.name")
    public UserResponse updateUser(String username, UserUpdateRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (!user.isEnabled()) throw new AppException(ErrorCode.USER_NOT_EXISTED);

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmailAndEnabledTrue(request.getEmail()))
                throw new AppException(ErrorCode.EMAIL_EXISTED);
            user.setEmail(request.getEmail());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank())
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getAddress() != null) user.setAddress(request.getAddress());

        return userMapper.toUserResponse(userRepository.save(user));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public String deleteUser(String username) {
        User user = userRepository.findByUsernameAndEnabledTrue(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        // Admin không được xóa STAFF (do Organizer quản lý)
        boolean isStaff = user.getRoles().stream().anyMatch(r -> r.getName().equals("STAFF"));
        if (isStaff) throw new AppException(ErrorCode.UNAUTHORIZED);
        user.setEnabled(false);
        userRepository.save(user);
        return "User disabled";
    }

    @PreAuthorize("hasRole('ADMIN')")
    public String unlockUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        boolean isStaff = user.getRoles().stream().anyMatch(r -> r.getName().equals("STAFF"));
        if (isStaff) throw new AppException(ErrorCode.UNAUTHORIZED);
        user.setEnabled(true);
        userRepository.save(user);
        return "User unlocked";
    }

    // ===================== ORGANIZER: quản lý STAFF của mình =====================

    @PreAuthorize("hasRole('ORGANIZER')")
    public Page<UserResponse> getMyStaff(String search, PageRequest pageRequest) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User organizer = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return userRepository.findByOrganizer_IdAndRoles_Name(organizer.getId(), "STAFF", pageRequest)
                .map(userMapper::toUserResponse);
    }

    @PreAuthorize("hasRole('STAFF')")
    public OrganizerResponse getMyOrganizer(Long organizerId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User staff = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (staff.getOrganizer() == null || !staff.getOrganizer().getId().equals(organizerId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        User organizer = userRepository.findByIdAndEnabledTrue(organizerId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return OrganizerResponse.builder()
                .id(organizer.getId())
                .fullName(organizer.getFullName())
                .email(organizer.getEmail())
                .build();
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    public String createStaff(UserCreateRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User organizer = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USERNAME_EXISTED);
        }
        if (userRepository.existsByEmailAndEnabledTrue(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }

        String token = UUID.randomUUID().toString();
        
        Role role = roleRepository.findById("STAFF")
                .orElseGet(() -> roleRepository.save(Role.builder().name("STAFF").description("Staff role").build()));

        Set<Role> roles = new HashSet<>();
        roles.add(role);

        User staff = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .address(request.getAddress())
                .roles(roles)
                .enabled(true)
                .verificationToken(token)
                .organizer(organizer)
                .build();

        userRepository.save(staff);
        try {
            emailService.sendVerificationEmail(staff.getEmail(), token);
        } catch (Exception e) {
            log.warn("Không thể gửi email xác thực cho staff: {}", e.getMessage());
        }
        return "Tạo tài khoản nhân viên thành công.";
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    public String disableMyStaff(String staffUsername) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User organizer = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        User staff = userRepository.findByUsernameAndEnabledTrue(staffUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        // Chỉ được vô hiệu hóa Staff of mình
        if (staff.getOrganizer() == null || !staff.getOrganizer().getId().equals(organizer.getId()))
            throw new AppException(ErrorCode.UNAUTHORIZED);
        staff.setEnabled(false);
        userRepository.save(staff);
        return "Staff disabled";
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    public String enableMyStaff(String staffUsername) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User organizer = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        User staff = userRepository.findByUsername(staffUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        if (staff.getOrganizer() == null || !staff.getOrganizer().getId().equals(organizer.getId()))
            throw new AppException(ErrorCode.UNAUTHORIZED);
        staff.setEnabled(true);
        userRepository.save(staff);
        return "Staff enabled";
    }
}
