package com.kindergarten.management_school.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, Object> error = baseError(HttpStatus.BAD_REQUEST, "Validation Error");
        ex.getBindingResult().getFieldErrors().forEach(fieldError -> {
            error.put(fieldError.getField(), fieldError.getDefaultMessage());
        });
        error.put("message", "Dữ liệu đầu vào không hợp lệ. Vui lòng kiểm tra lại.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }


    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        Map<String, Object> error = baseError(HttpStatus.BAD_REQUEST, "Data Integrity Violation");
        String msg = extractDuplicateMessage(ex.getMessage());
        error.put("message", msg);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }


    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        Map<String, Object> error = baseError(HttpStatus.FORBIDDEN, "Access Denied");
        error.put("message", "Bạn không có quyền truy cập tài nguyên này.");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }


    @ExceptionHandler({ExpiredJwtException.class, SignatureException.class})
    public ResponseEntity<Map<String, Object>> handleJwtException(Exception ex) {
        Map<String, Object> error = baseError(HttpStatus.UNAUTHORIZED, "JWT Error");
        if (ex instanceof ExpiredJwtException) {
            error.put("message", "Token đã hết hạn. Vui lòng đăng nhập lại.");
        } else {
            error.put("message", "Token không hợp lệ hoặc bị giả mạo.");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }


    @ExceptionHandler(SQLException.class)
    public ResponseEntity<Map<String, Object>> handleSQLException(SQLException ex) {
        Map<String, Object> error = baseError(HttpStatus.INTERNAL_SERVER_ERROR, "SQL Error");
        error.put("message", "Lỗi cơ sở dữ liệu: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }


//    @ExceptionHandler(CustomAppException.class)
//    public ResponseEntity<Map<String, Object>> handleCustomAppException(CustomAppException ex) {
//        Map<String, Object> error = baseError(ex.getStatus(), ex.getError());
//        error.put("message", ex.getMessage());
//        return ResponseEntity.status(ex.getStatus()).body(error);
//    }

    /**
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        Map<String, Object> error = baseError(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }


    private Map<String, Object> baseError(HttpStatus status, String errorType) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("status", status.value());
        error.put("error", errorType);
        return error;
    }

    private String extractDuplicateMessage(String fullMsg) {
        if (fullMsg != null && fullMsg.contains("Duplicate entry")) {
            try {
                int start = fullMsg.indexOf("Duplicate entry");
                int end = fullMsg.indexOf("for key");
                if (start != -1 && end != -1) {
                    String duplicateValue = fullMsg.substring(start, end).trim();
                    return "Dữ liệu bị trùng lặp: " + duplicateValue;
                }
            } catch (Exception ignored) {}
        }
        return "Dữ liệu bị trùng lặp hoặc vi phạm ràng buộc.";
    }
}
