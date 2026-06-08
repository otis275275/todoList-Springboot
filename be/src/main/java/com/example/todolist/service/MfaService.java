package com.example.todolist.service;

import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.util.Utils;
import org.springframework.stereotype.Service;

/**
 * Service xử lý toàn bộ nghiệp vụ MFA (Multi-Factor Authentication).
 *
 * Luồng tổng quan:
 * ┌─────────────┐    ┌──────────────────┐    ┌──────────────────┐
 * │  1. Setup   │ →  │  2. Activate     │ →  │  3. Login + OTP  │
 * │  Sinh QR    │    │  Verify lần đầu  │    │  Verify mỗi lần  │
 * └─────────────┘    └──────────────────┘    └──────────────────┘
 */
@Service
public class MfaService {

    private final CodeVerifier codeVerifier;

    // QrGenerator chịu trách nhiệm vẽ ảnh QR PNG (dùng thư viện ZXing)
    private final QrGenerator qrGenerator = new ZxingPngQrGenerator();

    // Spring inject CodeVerifier bean từ MfaConfig.java
    public MfaService(CodeVerifier codeVerifier) {
        this.codeVerifier = codeVerifier;
    }

    // ========================================================
    // 1. Sinh Secret Key
    // ========================================================

    /**
     * Tạo chuỗi secret ngẫu nhiên dài 32 ký tự Base32.
     * Đây là "hạt giống" dùng để tính toán mã OTP — mỗi user có 1 secret riêng.
     * Secret này được lưu vào cột mfa_secret của bảng users.
     *
     * Ví dụ output: "JBSWY3DPEHPK3PXP" (Base32 — chỉ dùng A-Z và 2-7)
     */
    public String generateNewSecret() {
        return new DefaultSecretGenerator().generate();
    }

    // ========================================================
    // 2. Tạo QR Code
    // ========================================================

    /**
     * Tạo ảnh QR Code dựa trên secret và email của user.
     * Trả về chuỗi Data URI (base64) có thể gán trực tiếp vào thẻ <img src="...">
     *
     * Google Authenticator sẽ quét QR này và tự động lưu secret.
     * Sau đó, mỗi 30 giây nó sẽ tính ra 1 mã OTP mới từ secret đó.
     *
     * @param secret  Chuỗi Base32 secret đã sinh ở bước trên
     * @param email   Email hiển thị trong app Google Authenticator (dùng để nhận diện tài khoản)
     * @return        Data URI dạng "data:image/png;base64,..." → gán vào <img src={qrCode} />
     */
    public String generateQrCodeUri(String secret, String email) throws QrGenerationException {
        // QrData chứa các tham số để Google Authenticator hiểu cách tính OTP
        QrData data = new QrData.Builder()
                .label(email)                         // Nhãn hiển thị trong app (vd: user@gmail.com)
                .secret(secret)                       // Secret key — quan trọng nhất
                .issuer("TodoList App")               // Tên ứng dụng hiển thị trong Authenticator
                .algorithm(HashingAlgorithm.SHA1)     // Thuật toán hash (chuẩn Google Authenticator)
                .digits(6)                            // Mã 6 số
                .period(30)                           // Thay đổi mỗi 30 giây
                .build();

        // Sinh ảnh PNG từ QrData
        byte[] imageData = qrGenerator.generate(data);

        // Chuyển PNG bytes → Base64 Data URI để FE nhúng vào <img> không cần upload ảnh
        return Utils.getDataUriForImage(imageData, qrGenerator.getImageMimeType());
    }

    // ========================================================
    // 3. Xác thực mã OTP
    // ========================================================

    /**
     * Kiểm tra mã 6 số người dùng nhập có khớp với secret không.
     * CodeVerifier sẽ tính lại mã OTP dựa trên secret + thời gian hiện tại,
     * rồi so sánh với mã người dùng gõ vào.
     *
     * @param code    Mã 6 số người dùng nhập (String, vd: "123456")
     * @param secret  Secret key của user lấy từ DB
     * @return        true nếu hợp lệ, false nếu sai/hết hạn
     */
    public boolean verifyCode(String code, String secret) {
        return codeVerifier.isValidCode(secret, code);
    }
}
