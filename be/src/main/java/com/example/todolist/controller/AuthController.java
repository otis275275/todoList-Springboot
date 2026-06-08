package com.example.todolist.controller;

import com.example.todolist.dto.JwtResponse;
import com.example.todolist.dto.LoginRequest;
import com.example.todolist.dto.MessageResponse;
import com.example.todolist.dto.SignupRequest;
import com.example.todolist.event.OnRegistrationCompleteEvent;
import com.example.todolist.exception.EmailAlreadyExistsException;
import com.example.todolist.exception.UsernameAlreadyExistsException;
import com.example.todolist.model.User;
import com.example.todolist.model.VerificationToken;
import com.example.todolist.repository.UserRepository;
import com.example.todolist.repository.VerificationTokenRepository;
import com.example.todolist.security.JwtUtils;
import com.example.todolist.security.UserDetailsImpl;
import com.example.todolist.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Calendar;
import java.util.Locale;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    IUserService userService;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    ApplicationEventPublisher eventPublisher;

    @Autowired
    private VerificationTokenRepository tokenRepository;

    // ===== API ĐĂNG NHẬP =====
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        // Bước 1: Xác thực username + password — ném BadCredentialsException nếu sai
        // Ở đây isAuthenticated = false
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Bước 2: Kiểm tra xem user này có bật MFA không
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isMfaEnabled()) {
            // MFA đang bật → CHƯA trả JWT thật
            // Sinh temp token ngắn hạn (5 phút) để FE dùng trong bước tiếp theo
            String tempToken = jwtUtils.generateTempMfaToken(userDetails.getUsername());

            // Trả về requiresMfa=true để FE biết cần chuyển sang màn nhập OTP
            return ResponseEntity.ok(new JwtResponse(
                    userDetails.getId(),
                    userDetails.getUsername(),
                    userDetails.getRole(),
                    tempToken  // Constructor thứ 2: requiresMfa=true, token=null
            ));
        }

        // MFA chưa bật → Trả JWT thật ngay (flow bình thường như cũ)
        String jwt = jwtUtils.generateJwtToken(authentication);
        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getRole()));
    }

    // ===== API ĐĂNG KÝ (USER thường) =====
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest, HttpServletRequest request) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            throw new UsernameAlreadyExistsException("Username is already taken!");
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new EmailAlreadyExistsException("Email is already in use!");
        }

        // Tạo user mới với role mặc định = "USER"
        User user = new User(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));

        userService.save(user);

        String appUrl = request.getContextPath(); //Lấy link gốc của ứng dụng, ví dụ: http://localhost:8086
        eventPublisher.publishEvent(new OnRegistrationCompleteEvent(user, appUrl));


        return ResponseEntity.ok(new MessageResponse("User registered successfully! Please check your email for verification."));
    }

    //[GET]: http://localhost:8086/api/auth/registrationConfirm?token=abc123
    @GetMapping("/registrationConfirm")
    public ResponseEntity<?> confirmRegistration(WebRequest request, @RequestParam("token") String token) {

        String result = userService.validateVerificationToken(token);
        //Thành công
        if (result == null) {
            return ResponseEntity.ok(new MessageResponse("Account verified successfully!"));
        }
        return ResponseEntity.badRequest().body(new MessageResponse(result));
    }


    // ===== API ĐĂNG KÝ ADMIN (chỉ dùng cho development/test) =====
    @PostMapping("/admin-signup")
    public ResponseEntity<?> registerAdmin(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            throw new UsernameAlreadyExistsException("Username is already taken!");
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new EmailAlreadyExistsException("Email is already in use!");
        }

        // ★ Tạo user với role = "ADMIN"
        User user = new User(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()),
                "ADMIN");
        user.setEnabled(true);

        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("Admin registered successfully!"));
    }
}
