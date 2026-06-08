package com.example.todolist.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private int jwtExpirationMs;

    // Thời hạn của temp token dùng trong bước MFA = 5 phút
    private static final int MFA_TEMP_TOKEN_EXPIRATION_MS = 5 * 60 * 1000;

    // Chuyển chuỗi secret thành SecretKey object theo yêu cầu của JJWT 0.11.x
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // ========================================================
    // JWT thật — dùng sau khi login thành công hoàn toàn
    // ========================================================

    /**
     * Sinh JWT chính thức cho user đã xác thực (kể cả vượt qua MFA nếu có).
     * Token này dùng để gọi mọi API cần xác thực trong ứng dụng.
     */
    public String generateJwtToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Sinh JWT thật từ username (String) thay vì Authentication object.
     * Dùng trong MfaController sau khi verify OTP thành công —
     * lúc đó không có Authentication trong SecurityContext nên không dùng được generateJwtToken().
     */
    public String generateJwtTokenFromUsername(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    // ========================================================
    // Temp JWT — chỉ dùng trong bước MFA verify
    // ========================================================

    /**
     * Sinh JWT tạm thời chỉ tồn tại 5 phút.
     * Mục đích: sau khi xác thực username/password thành công nhưng user còn phải vượt qua MFA,
     * backend trả tempToken thay vì JWT thật.
     * FE dùng tempToken này để gọi POST /api/auth/mfa/verify.
     *
     * Token có thêm claim "type"="MFA_TEMP" để phân biệt với JWT thật.
     * AuthTokenFilter sẽ từ chối token loại này khi gọi các API thông thường.
     */
    public String generateTempMfaToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .claim("type", "MFA_TEMP")          // Đánh dấu đây là temp token, không phải JWT thật
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + MFA_TEMP_TOKEN_EXPIRATION_MS))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Lấy username từ temp token (dùng trong MfaController để biết ai đang verify).
     */
    public String getUsernameFromTempToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * Kiểm tra temp token có hợp lệ và đúng loại MFA_TEMP không.
     * Tránh trường hợp ai đó dùng JWT thật để giả mạo bước verify.
     */
    public boolean validateTempMfaToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token) //Check token có hợp lệ (chữ ký + expiration) trước
                    .getBody();
            // Bắt buộc phải có claim type = "MFA_TEMP"
            return "MFA_TEMP".equals(claims.get("type", String.class));
        } catch (Exception e) {
            logger.error("Invalid MFA temp token: {}", e.getMessage());
            return false;
        }
    }

    // ========================================================
    // Các method dùng chung cho JWT thật
    // ========================================================

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(authToken)
                    .getBody();

            // Từ chối temp token nếu ai cố tình dùng nó để vào API thật
            if ("MFA_TEMP".equals(claims.get("type", String.class))) {
                logger.warn("Attempt to use MFA temp token as real JWT — rejected");
                return false;
            }
            return true;
        } catch (Exception e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        }
        return false;
    }
}

