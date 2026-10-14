package com.sa.event_mng.modules.identity.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {
    private String token;
    // Yêu cầu người dùng đổi mật khẩu ngay sau khi login (admin reset)
    private boolean mustChangePassword;
}
