package com.campus.trade.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.trade.backend.domain.entity.User; // 临时引用实体包路径下的收藏类（如果存在）
import org.apache.ibatis.annotations.Mapper;

/**
 * 收藏表 Mapper 接口
 */
@Mapper
public interface FavoriteMapper extends BaseMapper<com.campus.trade.backend.domain.entity.Favorite> {
}
