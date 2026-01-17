package com.campus.trade.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.trade.backend.domain.entity.User;

import com.campus.trade.backend.domain.dto.JwtResponse;
import com.campus.trade.backend.domain.dto.LoginRequest;

public interface UserService extends IService<User> {
    User register(User user);

    JwtResponse login(LoginRequest loginRequest);
}

