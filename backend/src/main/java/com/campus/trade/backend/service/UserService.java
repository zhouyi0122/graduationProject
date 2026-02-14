package com.campus.trade.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.trade.backend.domain.entity.User;

import com.campus.trade.backend.domain.dto.JwtResponse;
import com.campus.trade.backend.domain.dto.LoginRequest;

public interface UserService extends IService<User> {
    User register(User user);

    JwtResponse login(LoginRequest loginRequest);

    /**
     * 更新用户余额并记录流水
     * @param userId 用户ID
     * @param amount 变更金额
     * @param type 交易类型 (0:充值, 1:消费, 2:提现, 3:卖出收入)
     * @param description 描述文案
     */
    void updateBalance(Long userId, java.math.BigDecimal amount, Integer type, String description);

    /**
     * 更新用户信息
     * @param user 包含更新信息的对象
     */
    void updateProfile(User user);

    /**
     * 校园认证
     * @param userId 用户ID
     */
    void certifyUser(Long userId);

    /**
     * 修改密码
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    void changePassword(Long userId, String oldPassword, String newPassword);
}

