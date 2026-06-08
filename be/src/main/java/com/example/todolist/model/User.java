package com.example.todolist.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role = "USER";

    // Tài khoản chỉ được đăng nhập khi đã xác minh email (enabled = true)
    private boolean enabled = false;

    // ===== MFA Fields =====

    // Chuỗi Base32 ngẫu nhiên dùng làm "hạt giống" tính toán mã OTP 6 số
    // Được lưu vào DB khi user bắt đầu setup MFA
    // Production nên mã hóa AES trước khi lưu
    @Column(name = "mfa_secret")
    private String mfaSecret;

    // Cờ đánh dấu user đã kích hoạt MFA hay chưa
    // - false (mặc định): Login sẽ trả JWT ngay sau khi đúng username/password
    // - true: Login sẽ yêu cầu thêm bước nhập mã OTP 6 số từ Google Authenticator
    @Column(name = "mfa_enabled")
    private boolean mfaEnabled = false;

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = "USER";
    }

    public User(String username, String email, String password, String role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public void setMfaSecret(String mfaSecret) {
        this.mfaSecret = mfaSecret;
    }
}
