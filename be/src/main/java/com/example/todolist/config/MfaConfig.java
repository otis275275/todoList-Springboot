package com.example.todolist.config;

import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.time.SystemTimeProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình bean cho hệ thống MFA TOTP.
 *
 * TOTP (Time-based One-Time Password) hoạt động theo cơ chế:
 * 1. Server và Google Authenticator đều biết cùng 1 "secret key"
 * 2. Cả hai dùng công thức HMAC-SHA1(secret, thời_gian_hiện_tại / 30s) → ra 6 số giống nhau
 * 3. Server chỉ cần so khớp mã user nhập với kết quả tính toán của mình
 *
 * Lớp này đăng ký CodeVerifier vào Spring Container để inject vào MfaService
 */
@Configuration
public class MfaConfig {

    /**
     * Bean thực hiện việc xác thực mã OTP 6 số.
     * Bao gồm:
     * - DefaultCodeGenerator: tính mã OTP từ secret + thời gian
     * - SystemTimeProvider: lấy thời gian thực của hệ thống
     * - Tự động chấp nhận sai lệch ±1 cửa sổ thời gian (30s) để tránh lỗi do đồng hồ lệch nhau
     */
    @Bean
    public CodeVerifier codeVerifier() {
        DefaultCodeVerifier verifier = new DefaultCodeVerifier(
            new DefaultCodeGenerator(),    // Sinh mã theo chuẩn RFC 6238 (TOTP)
            new SystemTimeProvider()        // Lấy System.currentTimeMillis()
        );
        // Cho phép sai lệch 1 window (= 30 giây) để tránh lỗi khi đồng hồ không đồng bộ hoàn toàn
        verifier.setAllowedTimePeriodDiscrepancy(1);
        return verifier;
    }
}
