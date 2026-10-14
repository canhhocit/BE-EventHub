package com.sa.event_mng.modules.identity.domain.model;

import com.sa.event_mng.shared.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
// Entity đại diện cho người dùng và ban tổ chức trong hệ thống
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String fullName;

    private String phone;

    private String address;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;

    private boolean enabled;

    private String verificationToken;
    
    private String otp;
    private java.time.LocalDateTime otpExpiry;

    // Đánh dấu tài khoản bị admin reset mật khẩu → phải đổi mật khẩu lần đầu khi đăng nhập
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean mustChangePassword;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id")
    private User organizer;

}
