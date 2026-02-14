package com.campus.trade.backend.controller;

import com.campus.trade.backend.domain.entity.User;
import com.campus.trade.backend.domain.entity.Notification;
import com.campus.trade.backend.service.UserService;
import com.campus.trade.backend.mapper.WalletTransactionMapper;
import com.campus.trade.backend.mapper.NotificationMapper;
import com.campus.trade.backend.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private WalletTransactionMapper walletTransactionMapper;

    @Autowired
    private NotificationMapper notificationMapper;

    @PostMapping("/recharge")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> recharge(@RequestBody Map<String, Object> request) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetailsImpl userDetails)) {
            return ResponseEntity.status(401).body("未登录");
        }

        Object amountObj = request.get("amount");
        if (amountObj == null) {
            return ResponseEntity.badRequest().body("充值金额不能为空");
        }

        BigDecimal amount = new BigDecimal(amountObj.toString());
        userService.updateBalance(userDetails.getId(), amount, 0, "充值");
        
        return ResponseEntity.ok(Map.of("message", "充值成功", "newBalance", userService.getById(userDetails.getId()).getBalance()));
    }

    @GetMapping("/transactions")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getTransactions() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetailsImpl userDetails)) {
            return ResponseEntity.status(401).body("未登录");
        }
        
        return ResponseEntity.ok(walletTransactionMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.campus.trade.backend.domain.entity.WalletTransaction>()
                .eq("user_id", userDetails.getId())
                .orderByDesc("create_time")
        ));
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getProfile() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetailsImpl userDetails)) {
            return ResponseEntity.status(401).body("未登录");
        }
        return ResponseEntity.ok(userService.getById(userDetails.getId()));
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateProfile(@RequestBody User userRequest) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetailsImpl userDetails)) {
            return ResponseEntity.status(401).body("未登录");
        }
        
        userRequest.setId(userDetails.getId());
        userService.updateProfile(userRequest);
        
        return ResponseEntity.ok(Map.of("message", "个人资料更新成功"));
    }

    /**
     * 校园认证
     */
    @PostMapping("/certify")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> certify() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetailsImpl userDetails)) {
            return ResponseEntity.status(401).body("未登录");
        }
        userService.certifyUser(userDetails.getId());
        return ResponseEntity.ok(Map.of("message", "认证成功"));
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetailsImpl userDetails)) {
            return ResponseEntity.status(401).body("未登录");
        }

        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");

        if (oldPassword == null || newPassword == null) {
            return ResponseEntity.badRequest().body("密码不能为空");
        }

        try {
            userService.changePassword(userDetails.getId(), oldPassword, newPassword);
            return ResponseEntity.ok(Map.of("message", "密码修改成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 随机获取一个管理员（作为客服）
     */
    @GetMapping("/random-admin")
    public ResponseEntity<?> getRandomAdmin() {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        queryWrapper.eq("role", 1).eq("status", 0); // 角色为管理员且状态正常
        List<User> admins = userService.list(queryWrapper);
        
        if (admins.isEmpty()) {
            return ResponseEntity.status(404).body("暂无在线客服");
        }
        
        // 随机选择一个
        java.util.Collections.shuffle(admins);
        User randomAdmin = admins.get(0);
        randomAdmin.setPassword(null); // 脱敏
        
        return ResponseEntity.ok(randomAdmin);
    }

    /**
     * 获取系统通知列表
     */
    @GetMapping("/notifications")
    public ResponseEntity<?> getNotifications() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetailsImpl)) {
            return ResponseEntity.status(401).body("未登录");
        }
        return ResponseEntity.ok(new com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper<>(
                notificationMapper).orderByDesc(Notification::getCreateTime).list());
    }
}
