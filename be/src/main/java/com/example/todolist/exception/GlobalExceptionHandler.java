package com.example.todolist.exception;
import com.example.todolist.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;

// Dòng này báo cho Spring biết: Đây là trạm hứng lỗi toàn cục!
@RestControllerAdvice 
public class GlobalExceptionHandler {

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUsernameAlreadyExists(UsernameAlreadyExistsException ex, WebRequest request) {
        ErrorResponse message = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(), // 400
                new Date(),
                ex.getMessage(),
                request.getDescription(false));
        
        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }

    // Bắt một lỗi cụ thể (Ví dụ: Lỗi NullPointerException)
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ErrorResponse> handleNullPointer(NullPointerException ex, WebRequest request) {
        ErrorResponse message = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), // 500
                new Date(),
                "Dữ liệu bị rỗng rùi bồ ơi!",
                request.getDescription(false));
        
        return new ResponseEntity<>(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, WebRequest request) {
        ErrorResponse message = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(), // 401
                new Date(),
                "Sai tên đăng nhập hoặc mật khẩu!",
                request.getDescription(false)); // Lấy câu thông báo lỗi gốc
        
        return new ResponseEntity<>(message, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({DisabledException.class, LockedException.class})
    public ResponseEntity<ErrorResponse> handleAccountLockedException(Exception ex, WebRequest request) {
        ErrorResponse message = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(), // Mã 403
                new Date(),
                "Tài khoản của bạn đã bị khóa hoặc chưa được kích hoạt!",
                request.getDescription(false));
                
        return new ResponseEntity<>(message, HttpStatus.FORBIDDEN);
    }


    // Bắt TẤT CẢ các lỗi còn lại chưa được liệt kê
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {
        ErrorResponse message = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), // 500
                new Date(),
                ex.getMessage(), // Lấy câu thông báo lỗi gốc
                request.getDescription(false));
        
        return new ResponseEntity<>(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}