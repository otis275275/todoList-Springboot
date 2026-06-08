package com.example.todolist.security;

import com.example.todolist.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

// UserDetailsImpl là lớp cài đặt UserDetails của Spring Security,
// đại diện cho thông tin người dùng đã đăng nhập
public class UserDetailsImpl implements UserDetails {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;

    @JsonIgnore
    private String password;

    private String role;  // Thêm role
    private boolean enabled; // Trạng thái kích hoạt (email verified)

    public UserDetailsImpl(Long id, String username, String password, String role, boolean enabled) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.enabled = enabled;
    }

    // Factory method: chuyển từ User Entity → UserDetailsImpl
    public static UserDetailsImpl build(User user) {
        return new UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getRole(),
                user.isEnabled());   // Lấy trạng thái enabled từ User entity
    }

    // ★ QUAN TRỌNG: Trả về authorities (quyền) dựa trên role
    // Spring Security dùng authorities này để phân quyền khi gọi @PreAuthorize
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Prefix "ROLE_" bắt buộc theo convention của Spring Security
        // VD: role = "ADMIN" → authority = "ROLE_ADMIN"
        //     role = "USER"  → authority = "ROLE_USER"
        //Thay vì tạo 1 list rồi add từng authority, ở đây chỉ có 1 role nên dùng 
        // Collections.singletonList để trả về 1 list chứa duy nhất 1 authority
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
    }

    public Long getId() {
        return id;
    }

    public String getRole() {
        return role;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled; // Trả về trạng thái thực từ DB — user chưa verify email sẽ bị chặn login
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDetailsImpl user = (UserDetailsImpl) o;
        return Objects.equals(id, user.id);
    }
}
