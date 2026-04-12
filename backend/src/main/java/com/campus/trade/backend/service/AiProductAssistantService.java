package com.campus.trade.backend.service;

import java.util.List;
import java.util.Map;

/**
 * AI商品发布辅助服务。
 * 负责根据标题/成色/图片生成：标题、成色、描述与价格建议。
 */
public interface AiProductAssistantService {

    /**
     * 生成商品发布建议。
     *
     * @param title     用户已填写标题（可为空）
     * @param condition 用户已填写成色（可为空）
     * @param imageUrls 商品图片URL列表（至少建议1张）
     * @return 结构化建议结果：title/condition/description/priceMin/priceMax/suggestedPrice/reason
     */
    Map<String, Object> generateDescriptionAndPricing(String title, String condition, List<String> imageUrls);

    /**
     * 审核商品是否存在违规风险。
     *
     * @param title 商品标题
     * @param description 商品描述
     * @param condition 商品成色
     * @param price 商品价格
     * @param imageUrls 商品图片URL列表
     * @return 结构化审核结果：riskScore(0-10)/riskTags/aiReason
     */
    Map<String, Object> reviewProductCompliance(String title, String description, String condition, java.math.BigDecimal price, List<String> imageUrls);
}
