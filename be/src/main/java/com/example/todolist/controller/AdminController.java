package com.example.todolist.controller;

import com.example.todolist.dto.UserResponse;
import com.example.todolist.model.User;
import com.example.todolist.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    // ===== GET: Lấy danh sách tất cả user =====
    // ★ @PreAuthorize: Kiểm tra quyền TRƯỚC khi chạy method
    //   hasRole('ADMIN') → Spring tự thêm prefix "ROLE_" → match "ROLE_ADMIN" trong authorities
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<User> users = userRepository.findAll();

        // Map User Entity → UserResponse DTO (loại bỏ password)
        List<UserResponse> userResponses = users.stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getRole()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(userResponses);
    }

    // ===== GET: Đếm tổng số user =====
    @GetMapping("/users/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> getUserCount() {
        return ResponseEntity.ok(userRepository.count());
    }
}
