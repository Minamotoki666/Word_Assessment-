package com.example.demo.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 捕捉所有业务层抛出的 RuntimeException
     * 并将其转换为 JSON 格式返回给前端 Axios 拦截器
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException e) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 400);
        result.put("message", e.getMessage()); // 这里就是我们在 Service 里写的中文提示！

        // 打印到后端控制台方便调试
        System.err.println("拦截到业务异常： " + e.getMessage());

        // 返回 400 状态码和 JSON
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }
}