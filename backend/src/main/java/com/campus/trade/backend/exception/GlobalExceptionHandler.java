package com.campus.trade.backend.exception;

import com.campus.trade.backend.domain.dto.MessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 捕获通用的运行时异常，例如我们手动抛出的“用户名已存在”
     * @param ex 运行时异常
     * @return 返回一个包含错误信息的ResponseEntity，HTTP状态码为400 (Bad Request)
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<MessageResponse> handleRuntimeException(RuntimeException ex) {
        // 在真实项目中，这里应该记录详细日志
        // log.error("RuntimeException: ", ex);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new MessageResponse(ex.getMessage()));
    }
}

