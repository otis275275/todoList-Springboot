package com.example.todolist.controller;

import com.example.todolist.dto.JwtResponse;
import com.example.todolist.dto.MfaCodeRequest;
import com.example.todolist.dto.MfaSetupResponse;
import com.example.todolist.dto.MfaVerifyRequest;
import com.example.todolist.model.User;
import com.example.todolist.repository.UserRepository;
import com.example.todolist.security.JwtUtils;
import com.example.todolist.security.UserDetailsImpl;
import com.example.todolist.service.MfaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

/**
 * Controller xử lý toàn bộ luồng MFA (Multi-Factor Authentication).
 *
 * Các endpoint:
 * ┌────────┬────────────────────────────┬──────────────────────────────────────────┐
 * │ Method │ Path                       │ Mô tả                                    │
 * ├────────┼────────────────────────────┼──────────────────────────────────────────┤
 * │ GET    │ /api/auth/mfa/setup        │ Sinh QR + secret để quét vào Authenticator│
 * │ POST   │ /api/auth/mfa/activate     │ Xác nhận lần đầu → bật MFA chính thức    │
 * │ POST   │ /api/auth/mfa/verify       │ Xác thực OTP lúc login                   │
 * │ POST   │ /api/auth/mfa/disable      │ Tắt MFA, xóa secret                      │
 * └────────┴────────────────────────────┴──────────────────────────────────────────┘
 *
 * Bảo mật:
 * - /setup, /activate, /disable → yêu cầu JWT thật (user đã đăng nhập)
 * - /verify → public, chỉ cần tempToken (nhận được từ /signin khi MFA bật)
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth/mfa")
public class MfaController {

    private final MfaService mfaService;
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;

    public MfaController(MfaService mfaService, UserRepository userRepository, JwtUtils jwtUtils) {
        this.mfaService = mfaService;
        this.userRepository = userRepository;
        this.jwtUtils = jwtUtils;
    }

    // ========================================================
    // SETUP: Sinh QR Code để user quét vào Google Authenticator
    // ========================================================

    /**
     * [GET] /api/auth/mfa/setup
     * Yêu cầu: JWT thật (user đã đăng nhập) → @AuthenticationPrincipal tự inject thông tin user
     *
     * Luồng:
     * 1. Sinh secret key ngẫu nhiên mới
     * 2. Lưu secret vào DB (nhưng mfaEnabled vẫn = false, chưa chính thức bật)
     * 3. Tạo ảnh QR từ secret
     * 4. Trả QR + secret về cho FE
     *
     * Tại sao chưa bật ngay? Vì cần đảm bảo user quét QR thành công trước.
     * Nếu bật ngay mà user chưa quét được QR → bị khóa tài khoản!
     */
    @GetMapping("/setup")
    public ResponseEntity<?> setupMfa(@AuthenticationPrincipal UserDetailsImpl currentUser) {
        try {
            User user = userRepository.findByUsername(currentUser.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Sinh secret key mới mỗi lần gọi setup (reset nếu đã có)
            String secret = mfaService.generateNewSecret();
            user.setMfaSecret(secret);
            userRepository.save(user); // Lưu secret vào DB, mfaEnabled vẫn false

            // Tạo QR code dạng Data URI để FE dùng: <img src={qrCode} />
            String qrCodeDataUri = mfaService.generateQrCodeUri(secret, user.getEmail());

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new MfaSetupResponse(qrCodeDataUri, secret));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi khi tạo QR code: " + e.getMessage()));
        }
    }

    // ========================================================
    // ACTIVATE: Xác nhận quét QR thành công → bật MFA chính thức
    // ========================================================

    /**
     * [POST] /api/auth/mfa/activate
     * Body: { "code": "123456" }
     * Yêu cầu: JWT thật
     *
     * User quét QR xong, nhập thử mã 6 số vào đây để xác nhận.
     * Nếu đúng → mfaEnabled = true → từ lần login tiếp theo sẽ phải nhập OTP.
     * Nếu sai → không bật, báo lỗi.
     */
    @PostMapping("/activate")
    public ResponseEntity<?> activateMfa(@AuthenticationPrincipal UserDetailsImpl currentUser,
                                         @Valid @RequestBody MfaCodeRequest request) {


        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        System.out.println(">>> [MFA ACTIVATE] User found: " + user.getUsername() + ", mfaSecret = " + (user.getMfaSecret() != null ? "EXISTS" : "NULL"));

        // Kiểm tra user đã có secret chưa (phải gọi /setup trước)
        if (user.getMfaSecret() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Chưa khởi tạo MFA. Vui lòng gọi /setup trước."));
        }

        // Xác thực mã OTP với secret đã lưu
        boolean isValid = mfaService.verifyCode(request.getCode(), user.getMfaSecret());

        if (isValid) {
            // Mã đúng → Chính thức bật MFA cho tài khoản này
            user.setMfaEnabled(true);
            userRepository.save(user);
            System.out.println(">>> [MFA ACTIVATE] MFA enabled successfully!");
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "MFA đã được kích hoạt thành công! Từ lần đăng nhập tiếp theo sẽ yêu cầu mã OTP."
            ));
        }

        System.out.println(">>> [MFA ACTIVATE] Invalid code!");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("success", false, "message", "Mã OTP không hợp lệ hoặc đã hết hạn 30 giây."));
    }

    // ========================================================
    // VERIFY: Xác thực OTP trong luồng đăng nhập
    // ========================================================

    /**
     * [POST] /api/auth/mfa/verify
     * Body: { "tempToken": "eyJ...", "code": "123456" }
     * Yêu cầu: PUBLIC (không cần JWT thật, Bearer — chỉ cần tempToken)
     *
     * Đây là bước 2 của luồng đăng nhập khi MFA bật:
     * 1. User đã đăng nhập đúng username/password → nhận tempToken từ /signin
     * 2. User nhập mã OTP + gửi tempToken vào đây
     * 3. Nếu đúng → trả JWT thật
     * 4. Nếu sai → 401
     */
    @PostMapping("/verify")
    public ResponseEntity<?> verifyMfa(@Valid @RequestBody MfaVerifyRequest request) {
        // Kiểm tra tempToken có hợp lệ không (đúng chữ ký + đúng type MFA_TEMP + chưa hết hạn)
        if (!jwtUtils.validateTempMfaToken(request.getTempToken())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Temp token không hợp lệ hoặc đã hết hạn 5 phút."));
        }

        // Lấy username từ tempToken để tìm user trong DB
        String username = jwtUtils.getUsernameFromTempToken(request.getTempToken());
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Xác thực mã OTP
        boolean isValid = mfaService.verifyCode(request.getCode(), user.getMfaSecret());

        if (isValid) {
            // Mã đúng → Sinh JWT thật và trả về như login thường
            // Cần build Authentication object thủ công vì không qua AuthenticationManager
            String jwt = jwtUtils.generateJwtTokenFromUsername(username);

            return ResponseEntity.ok(new JwtResponse(jwt, user.getId(), user.getUsername(), user.getRole()));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("success", false, "message", "Mã OTP không hợp lệ hoặc đã hết hạn 30 giây."));
    }

    // ========================================================
    // DISABLE: Tắt MFA
    // ========================================================

    /**
     * [POST] /api/auth/mfa/disable
     * Body: { "code": "123456" }
     * Yêu cầu: JWT thật
     *
     * User phải xác minh bằng OTP hiện tại trước khi tắt MFA.
     * Điều này ngăn hacker tắt MFA nếu chiếm được session.
     */
    @PostMapping("/disable")
    public ResponseEntity<?> disableMfa(@AuthenticationPrincipal UserDetailsImpl currentUser,
                                        @Valid @RequestBody MfaCodeRequest request) {
        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isMfaEnabled()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "MFA chưa được bật."));
        }

        // Phải xác nhận bằng OTP hiện tại trước khi tắt
        boolean isValid = mfaService.verifyCode(request.getCode(), user.getMfaSecret());

        if (isValid) {
            // Tắt MFA và xóa secret
            user.setMfaEnabled(false);
            user.setMfaSecret(null);
            userRepository.save(user);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "MFA đã được tắt thành công."
            ));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("success", false, "message", "Mã OTP không đúng, không thể tắt MFA."));
    }
}
