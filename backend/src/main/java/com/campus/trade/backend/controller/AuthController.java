package com.campus.trade.backend.controller;

import com.campus.trade.backend.domain.dto.JwtResponse;
import com.campus.trade.backend.domain.dto.LoginRequest;
import com.campus.trade.backend.domain.dto.MessageResponse;
import com.campus.trade.backend.domain.dto.RegisterRequest;
import com.campus.trade.backend.domain.entity.User;
import com.campus.trade.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 负责处理用户注册和登录相关的HTTP请求
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin // 允许跨域请求，方便前后端分离开发
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * 处理用户注册请求
     * @param registerRequest 包含用户名和密码的请求体
     * @return 包含成功消息的响应
     */
    @PostMapping("/register")
    public MessageResponse register(@Valid @RequestBody RegisterRequest registerRequest) {
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(registerRequest.getPassword());
        userService.register(user);
        return new MessageResponse("注册成功！");
    }

    /**
     * 处理用户登录请求
     * @param loginRequest 包含用户名和密码的请求体
     * @return 包含JWT Token和用户信息的响应
     */
    @PostMapping("/login")
    public JwtResponse login(@Valid @RequestBody LoginRequest loginRequest) {
        return userService.login(loginRequest);
    }
}
