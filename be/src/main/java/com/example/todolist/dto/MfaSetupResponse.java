package com.example.todolist.dto;

import lombok.Data;

/**
 * Response trả về khi user gọi GET /api/auth/mfa/setup.
 * Chứa thông tin để FE hiển thị QR code và secret key.
 */
@Data
public class MfaSetupResponse {

    // Data URI của ảnh QR (dạng "data:image/png;base64,...")
    // FE dùng: <img src={qrCode} />
    private String qrCode;

    // Secret key dạng Base32 — FE có thể hiển thị để user nhập tay vào app
    // Trường hợp camera không đọc được QR
    private String secret;

    public MfaSetupResponse(String qrCode, String secret) {
        this.qrCode = qrCode;
        this.secret = secret;
    }
}
