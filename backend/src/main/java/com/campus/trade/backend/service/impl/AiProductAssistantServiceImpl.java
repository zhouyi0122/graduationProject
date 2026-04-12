package com.campus.trade.backend.service.impl;

import com.campus.trade.backend.service.AiProductAssistantService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI商品发布辅助服务实现。
 *
 * 核心流程：
 * 1) 构造约束明确的Prompt（要求严格JSON输出）
 * 2) 调用大模型生成建议
 * 3) 解析并兜底返回结构化结果
 */
@Service
public class AiProductAssistantServiceImpl implements AiProductAssistantService {

    /**
     * Spring AI 的聊天客户端。
     */
    private final ChatClient chatClient;

    /**
     * Jackson对象映射器，用于将模型返回JSON解析为Map。
     */
    private final ObjectMapper objectMapper;

    public AiProductAssistantServiceImpl(ChatClient.Builder chatClientBuilder, ObjectMapper objectMapper) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    /**
     * 生成商品标题/成色/描述/价格建议。
     *
     * @param title     用户已有标题（可空）
     * @param condition 用户已有成色（可空）
     * @param imageUrls 商品图片URL列表
     * @return 结构化建议结果
     */
    @Override
    public Map<String, Object> generateDescriptionAndPricing(String title, String condition, List<String> imageUrls) {
        // 1. 构造Prompt
        String promptText = buildPrompt(title, condition, imageUrls);
        if (promptText == null) {
            promptText = "";
        }

        // 2. 调用模型。若返回null，统一兜底为"{}"避免空指针
        String content = java.util.Objects.toString(
                chatClient.prompt(new Prompt(List.of(new UserMessage(promptText))))
                        .call()
                        .content(),
                "{}"
        );

        try {
            // 3. 规范化模型返回文本并解析JSON
            String normalized = normalizeJson(content);
            Map<String, Object> result = objectMapper.readValue(normalized, new TypeReference<>() {});

            // 4. 补齐缺失字段，保证前端读取稳定
            result.putIfAbsent("title", "");
            result.putIfAbsent("condition", "");
            result.putIfAbsent("description", "");
            result.putIfAbsent("priceMin", 0);
            result.putIfAbsent("priceMax", 0);
            result.putIfAbsent("suggestedPrice", 0);
            result.putIfAbsent("reason", "");
            return result;
        } catch (Exception e) {
            // 5. 解析失败兜底，避免接口报错影响发布流程
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("title", title == null ? "闲置好物转让" : title);
            fallback.put("condition", (condition == null || condition.isBlank()) ? "九成新" : condition);
            fallback.put("description", "这是一件闲置好物，成色良好，欢迎联系咨询。可线下当面验货交易。");
            fallback.put("priceMin", 0);
            fallback.put("priceMax", 0);
            fallback.put("suggestedPrice", 0);
            fallback.put("reason", "AI返回格式异常，已返回默认文案，请手动调整价格。");
            return fallback;
        }
    }

    @Override
    public Map<String, Object> reviewProductCompliance(String title, String description, String condition, BigDecimal price, List<String> imageUrls) {
        String promptText = buildCompliancePrompt(title, description, condition, price, imageUrls);
        if (promptText == null) {
            promptText = "";
        }
        String content = java.util.Objects.toString(
                chatClient.prompt(new Prompt(List.of(new UserMessage(promptText))))
                        .call()
                        .content(),
                "{}"
        );

        try {
            String normalized = normalizeJson(content);
            Map<String, Object> result = objectMapper.readValue(normalized, new TypeReference<>() {});

            int riskScore = Math.max(0, Math.min(10, Integer.parseInt(String.valueOf(result.getOrDefault("riskScore", 0)))));
            Object tagsObj = result.get("riskTags");
            String riskTags;
            if (tagsObj instanceof List<?> tagsList) {
                riskTags = tagsList.stream().map(String::valueOf).collect(Collectors.joining(","));
            } else {
                riskTags = String.valueOf(tagsObj == null ? "" : tagsObj);
            }
            String aiReason = String.valueOf(result.getOrDefault("aiReason", "未发现明显违规风险"));

            Map<String, Object> out = new HashMap<>();
            out.put("riskScore", riskScore);
            out.put("riskTags", riskTags);
            out.put("aiReason", aiReason);
            return out;
        } catch (Exception e) {
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("riskScore", 3);
            fallback.put("riskTags", "待人工复核");
            fallback.put("aiReason", "AI审核解析失败，建议人工复核");
            return fallback;
        }
    }

    /**
     * 构造Prompt文本。
     * 通过强约束让模型返回可解析JSON，减少前端/后端适配成本。
     */
    private String buildPrompt(String title, String condition, List<String> imageUrls) {
        String images = (imageUrls == null || imageUrls.isEmpty()) ? "[]" : String.join(", ", imageUrls);
        return "你是校园二手交易平台的发布助手。请结合以下信息生成商品描述与定价建议。" +
                "\n要求：\n" +
                "1) 输出必须是严格JSON，不要markdown，不要```。\n" +
                "2) 仅输出以下字段：title, condition, description, priceMin, priceMax, suggestedPrice, reason。\n" +
                "3) 若输入标题为空，请根据图片自动生成简洁标题(10~20字)。若输入标题不为空，原样返回该标题。\n" +
                "4) 若输入成色为空，请在[全新, 九成新, 八成新, 七成新及以下]中选择最合适值。若输入成色不为空，原样返回该成色。\n" +
                "5) priceMin、priceMax、suggestedPrice为数字，单位人民币元。\n" +
                "6) 价格区间需符合校园二手交易场景，保守合理。\n" +
                "7) description 80~160字，突出成色、使用场景和交易建议。\n" +
                "\n输入信息：\n" +
                "标题: " + (title == null ? "" : title) + "\n" +
                "成色: " + (condition == null ? "" : condition) + "\n" +
                "图片URL列表: " + images;
    }

    /**
     * 构造商品违规审核Prompt。
     */
    private String buildCompliancePrompt(String title, String description, String condition, BigDecimal price, List<String> imageUrls) {
        String images = (imageUrls == null || imageUrls.isEmpty()) ? "[]" : String.join(", ", imageUrls);
        return "你是校园二手交易平台的内容安全审核助手，请对商品信息进行违规风险识别。" +
                "\n要求：\n" +
                "1) 输出必须是严格JSON，不要markdown，不要```。\n" +
                "2) 仅输出字段：riskScore, riskTags, aiReason。\n" +
                "3) riskScore 为0-10整数，分值越高风险越高。\n" +
                "4) riskTags 可为字符串数组或逗号分隔字符串，例如[广告引流,疑似违禁,交易风险,色情低俗,仿冒侵权,诈骗风险,其他]。\n" +
                "5) aiReason 用1-2句话说明判定依据。\n" +
                "\n输入信息：\n" +
                "标题: " + (title == null ? "" : title) + "\n" +
                "描述: " + (description == null ? "" : description) + "\n" +
                "成色: " + (condition == null ? "" : condition) + "\n" +
                "价格: " + (price == null ? "" : price.toPlainString()) + "\n" +
                "图片URL列表: " + images;
    }

    /**
     * 规范化模型返回文本为纯JSON字符串。
     * 兼容模型偶发返回```json ... ```包裹格式。
     */
    private String normalizeJson(String text) {
        if (text == null) return "{}";
        String t = text.trim();
        if (t.startsWith("```")) {
            t = t.replaceAll("^```json", "").replaceAll("^```", "").replaceAll("```$", "").trim();
        }
        int start = t.indexOf('{');
        int end = t.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return t.substring(start, end + 1);
        }
        return t;
    }
}
