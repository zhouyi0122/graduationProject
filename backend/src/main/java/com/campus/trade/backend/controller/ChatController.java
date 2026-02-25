package com.campus.trade.backend.controller;

import com.campus.trade.backend.domain.entity.Conversation;
import com.campus.trade.backend.domain.entity.ConversationMessage;
import com.campus.trade.backend.domain.entity.User;
import com.campus.trade.backend.mapper.ConversationMapper;
import com.campus.trade.backend.mapper.ConversationMessageMapper;
import com.campus.trade.backend.service.UserService;
import com.campus.trade.backend.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin
public class ChatController {

    @Autowired
    private ConversationMapper conversationMapper;

    @Autowired
    private ConversationMessageMapper messageMapper;

    @Autowired
    private UserService userService;

    @GetMapping("/conversations")
    public ResponseEntity<?> getConversations() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetailsImpl userDetails)) {
            return ResponseEntity.status(401).body("未登录");
        }
        Long currentUserId = userDetails.getId();

        List<Conversation> conversations = conversationMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Conversation>()
                .nested(i -> i.eq("user1_id", currentUserId).or().eq("user2_id", currentUserId))
                .orderByDesc("last_time")
        );

        for (Conversation conv : conversations) {
            Long otherUserId = conv.getUser1Id().equals(currentUserId) ? conv.getUser2Id() : conv.getUser1Id();
            User otherUser = userService.getById(otherUserId);
            if (otherUser != null) {
                otherUser.setPassword(null);
                conv.setOtherUser(otherUser);
            }

            // 统计当前用户的未读消息数
            Long unreadCount = messageMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ConversationMessage>()
                    .eq("conversation_id", conv.getId())
                    .ne("sender_id", currentUserId)
                    .eq("is_read", 0)
            );
            conv.setUnreadCount(unreadCount.intValue());
        }

        return ResponseEntity.ok(conversations);
    }

    @PutMapping("/conversations/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetailsImpl userDetails)) {
            return ResponseEntity.status(401).body("未登录");
        }
        Long currentUserId = userDetails.getId();

        // 将该会话中，发送者不是当前用户的所有未读消息标记为已读
        int rows = messageMapper.update(null,
            new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<ConversationMessage>()
                .set("is_read", 1)
                .eq("conversation_id", id)
                .ne("sender_id", currentUserId)
                .eq("is_read", 0)
        );

        return ResponseEntity.ok(Map.of("message", "已标记为已读", "rows", rows));
    }

    /**
     * 获取或创建两个用户之间的会话
     */
    @PostMapping("/conversations")
    public ResponseEntity<?> getOrCreateConversation(@RequestBody Map<String, Long> request) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetailsImpl userDetails)) {
            return ResponseEntity.status(401).body("未登录");
        }
        Long currentUserId = userDetails.getId();
        Long otherUserId = request.get("otherUserId");

        if (otherUserId == null) {
            return ResponseEntity.badRequest().body("缺少对方用户ID");
        }

        if (currentUserId.equals(otherUserId)) {
            return ResponseEntity.badRequest().body("不能和自己聊天");
        }

        // 查找现有会话：(user1=A and user2=B) OR (user1=B and user2=A)
        Conversation conv = conversationMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Conversation>()
                .nested(i -> i.eq("user1_id", currentUserId).eq("user2_id", otherUserId))
                .or(i -> i.eq("user1_id", otherUserId).eq("user2_id", currentUserId))
        );

        if (conv == null) {
            conv = new Conversation();
            conv.setUser1Id(currentUserId);
            conv.setUser2Id(otherUserId);
            conv.setIsSupport(0);
            conv.setLastTime(new Date());
            conversationMapper.insert(conv);
        }

        return ResponseEntity.ok(conv);
    }

    /**
     * 获取指定会话的所有消息
     */
    @GetMapping("/conversations/{id}/messages")
    public ResponseEntity<?> getMessages(@PathVariable Long id) {
        List<ConversationMessage> messages = messageMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ConversationMessage>()
                .eq("conversation_id", id)
                .orderByAsc("create_time")
        );
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/messages")
    public ResponseEntity<?> sendMessage(@RequestBody Map<String, Object> request) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetailsImpl userDetails)) {
            return ResponseEntity.status(401).body("未登录");
        }
        Long currentUserId = userDetails.getId();

        Long conversationId = Long.valueOf(request.get("conversationId").toString());
        String content = request.get("content").toString();

        ConversationMessage message = new ConversationMessage();
        message.setConversationId(conversationId);
        message.setSenderId(currentUserId);
        message.setContent(content);
        message.setIsRead(0);
        messageMapper.insert(message);

        // 更新会话最后一条消息和时间
        Conversation conv = conversationMapper.selectById(conversationId);
        if (conv != null) {
            conv.setLastMessage(content);
            conv.setLastTime(new Date());
            conversationMapper.updateById(conv);
        }

        return ResponseEntity.ok(message);
    }
}
