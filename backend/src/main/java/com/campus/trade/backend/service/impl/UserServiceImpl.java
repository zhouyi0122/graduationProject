package com.campus.trade.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.trade.backend.domain.dto.JwtResponse;
import com.campus.trade.backend.domain.dto.LoginRequest;
import com.campus.trade.backend.domain.entity.CampusCertification;
import com.campus.trade.backend.domain.entity.User;
import com.campus.trade.backend.mapper.CampusCertificationMapper;
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

    @Autowired
    private CampusCertificationMapper campusCertificationMapper;

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
        user.setCertificationStatus(0); // 默认为未认证

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

    @Autowired
    private com.campus.trade.backend.mapper.WalletTransactionMapper walletTransactionMapper;

    @Override
    public void updateBalance(Long userId, java.math.BigDecimal amount, Integer type, String description) {
        User user = this.getById(userId);
        if (user != null) {
            java.math.BigDecimal currentBalance = user.getBalance() != null ? user.getBalance() : java.math.BigDecimal.ZERO;
            user.setBalance(currentBalance.add(amount));
            this.updateById(user);

            // 记录流水
            com.campus.trade.backend.domain.entity.WalletTransaction tx = new com.campus.trade.backend.domain.entity.WalletTransaction();
            tx.setUserId(userId);
            tx.setAmount(amount);
            tx.setType(type);
            tx.setDescription(description);
            walletTransactionMapper.insert(tx);
        }
    }

    @Override
    public void updateProfile(User user) {
        // 仅更新允许修改的字段：昵称、性别、个人简介、手机号、头像
        User existingUser = this.getById(user.getId());
        if (existingUser != null) {
            existingUser.setNickname(user.getNickname());
            existingUser.setGender(user.getGender());
            existingUser.setBio(user.getBio());
            existingUser.setPhone(user.getPhone());
            existingUser.setAvatar(user.getAvatar());
            this.updateById(existingUser);
        }
    }

    @Override
    public void submitCampusCertification(Long userId, String school, String studentId, String realName) {
        User user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (user.getCertificationStatus() != null && user.getCertificationStatus() == 1) {
            throw new RuntimeException("您已有认证申请在审核中");
        }

        CampusCertification certification = new CampusCertification();
        certification.setUserId(userId);
        certification.setSchool(school);
        certification.setStudentId(studentId);
        certification.setRealName(realName);
        certification.setStatus(0);
        campusCertificationMapper.insert(certification);

        user.setCertificationStatus(1); // 审核中
        this.updateById(user);
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("旧密码错误");
        }

        // 设置新密码（加密）
        user.setPassword(passwordEncoder.encode(newPassword));
        this.updateById(user);
    }
}
