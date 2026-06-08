package com.example.todolist.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Request body cho POST /api/auth/mfa/activate và POST /api/auth/mfa/disable.
 * Chỉ cần gửi mã 6 số OTP — user đã có JWT thật nên backend biết danh tính qua token.
 */
@Data
@NoArgsConstructor
public class MfaCodeRequest {

    // Mã 6 số từ Google Authenticator
    @NotBlank
    @Size(min = 6, max = 6, message = "OTP code must be exactly 6 digits")
    private String code;
}
