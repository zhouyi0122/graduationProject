package com.campus.trade.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.trade.backend.domain.dto.JwtResponse;
import com.campus.trade.backend.domain.dto.LoginRequest;
import com.campus.trade.backend.domain.entity.User;
import com.campus.trade.backend.mapper.UserMapper;
import com.campus.trade.backend.security.services.UserDetailsImpl;
import com.campus.trade.backend.service.UserService;
import com.campus.trade.backend.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 用户注册
     * @param user 包含用户名和原始密码的用户对象
     * @return 注册成功并持久化后的用户对象
     * @throws RuntimeException 如果用户名已存在
     */
    @Override
    public User register(User user) {
        // 检查用户名是否已存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", user.getUsername());
        if (userMapper.exists(queryWrapper)) {
            throw new RuntimeException("用户名已存在!");
        }

        // 对密码进行加密
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        // 设置默认值
        user.setRole(0); // 默认为普通用户
        user.setStatus(0); // 默认为正常状态
        user.setIsCertified(0); // 默认为未认证

        // 插入用户数据
        userMapper.insert(user);
        return user;
    }

    /**
     * 用户登录认证
     * @param loginRequest 包含用户名和密码的登录请求对象
     * @return 包含JWT和用户基本信息的响应对象
     */
    @Override
    public JwtResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles);
    }
}
