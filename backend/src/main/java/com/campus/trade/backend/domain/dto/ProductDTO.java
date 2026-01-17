package com.campus.trade.backend.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 用于创建和更新商品的DTO
 */
@Data
public class ProductDTO {

    @NotBlank(message = "商品标题不能为空")
    private String title;

    private String description;

    @NotNull(message = "商品分类不能为空")
    private Integer categoryId;

    private BigDecimal originalPrice;

    @NotNull(message = "商品售价不能为空")
    @Positive(message = "价格必须为正数")
    private BigDecimal price;

    // 图片URL列表等其他信息可以在后续迭代中添加
}

