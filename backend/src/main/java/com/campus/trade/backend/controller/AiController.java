package com.campus.trade.backend.controller;

import com.campus.trade.backend.security.services.UserDetailsImpl;
import com.campus.trade.backend.service.AiProductAssistantService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI相关接口控制器。
 * 当前提供商品发布辅助接口：根据图片与输入信息生成标题/成色/描述/价格建议。
 */
@RestController
@RequestMapping("/api/ai")
@CrossOrigin
public class AiController {

    /**
     * AI商品发布辅助业务服务。
     */
    private final AiProductAssistantService aiProductAssistantService;

    public AiController(AiProductAssistantService aiProductAssistantService) {
        this.aiProductAssistantService = aiProductAssistantService;
    }

    /**
     * 商品发布AI辅助接口。
     *
     * 请求示例：
     * {
     *   "title": "可选，已填标题",
     *   "condition": "可选，已填成色",
     *   "imageUrls": ["http://.../1.jpg", "http://.../2.jpg"]
     * }
     *
     * 返回字段：
     * title, condition, description, priceMin, priceMax, suggestedPrice, reason
     */
    @PostMapping("/product-assistant")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> generateProductAssistant(@RequestBody Map<String, Object> request) {
        // 仅允许登录用户调用
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetailsImpl)) {
            return ResponseEntity.status(401).body("未登录");
        }

        // 容错读取入参：缺失时给空串/空列表，交给AI做补全
        String title = request.get("title") == null ? "" : String.valueOf(request.get("title"));
        String condition = request.get("condition") == null ? "" : String.valueOf(request.get("condition"));
        List<String> imageUrls = request.get("imageUrls") instanceof List<?> list
                ? list.stream().map(String::valueOf).toList()
                : List.of();

        Map<String, Object> result = aiProductAssistantService.generateDescriptionAndPricing(title, condition, imageUrls);
        return ResponseEntity.ok(result);
    }
}
