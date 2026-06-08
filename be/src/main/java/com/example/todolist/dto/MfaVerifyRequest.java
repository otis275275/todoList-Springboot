package com.example.todolist.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Request body cho POST /api/auth/mfa/verify — bước xác thực OTP lúc đăng nhập.
 * User gửi lên mã 6 số từ Google Authenticator + temp token nhận được sau bước login.
 */
@Data
@NoArgsConstructor
public class MfaVerifyRequest {

    // Temp JWT ngắn hạn (5 phút) nhận được từ response login khi mfaEnabled=true
    // Dùng để backend biết đây là ai đang verify — không cần gửi username/password lại
    @NotBlank
    private String tempToken;

    // Mã 6 số từ Google Authenticator
    @NotBlank
    @Size(min = 6, max = 6, message = "OTP code must be exactly 6 digits")
    private String code;
}
