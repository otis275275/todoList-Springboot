package com.example.todolist.dto;

import lombok.Data;

/**
 * Response trả về sau khi đăng nhập thành công.
 *
 * Có 2 trường hợp:
 * [1] MFA chưa bật (mfaEnabled=false):
 *     → token = JWT thật, requiresMfa = false, tempToken = null
 *     → FE lưu token vào localStorage và vào thẳng dashboard
 *
 * [2] MFA đã bật (mfaEnabled=true):
 *     → token = null, requiresMfa = true, tempToken = JWT tạm thời (5 phút)
 *     → FE chuyển sang màn nhập OTP, gửi tempToken + code lên /api/auth/mfa/verify
 */
@Data
public class JwtResponse {
    // JWT thật — dùng để gọi các API cần xác thực (null nếu cần MFA)
    private String token;

    // Luôn là "Bearer"
    private String type = "Bearer";

    private Long id;
    private String username;
    private String role;

    // true khi user đã bật MFA → FE cần hiện màn nhập OTP
    private boolean requiresMfa = false;

    // JWT tạm thời ngắn hạn, chỉ dùng 1 lần cho bước verify OTP (null nếu không cần MFA)
    private String tempToken;

    // Constructor dùng khi đăng nhập thường (không MFA)
    public JwtResponse(String accessToken, Long id, String username, String role) {
        this.token = accessToken;
        this.id = id;
        this.username = username;
        this.role = role;
    }

    // Constructor dùng khi MFA bật → chưa trả JWT thật, chỉ trả tempToken
    public JwtResponse(Long id, String username, String role, String tempToken) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.requiresMfa = true;
        this.tempToken = tempToken;
    }
}
