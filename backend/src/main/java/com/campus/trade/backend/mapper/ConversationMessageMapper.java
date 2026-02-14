package com.campus.trade.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.trade.backend.domain.entity.ConversationMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ConversationMessageMapper extends BaseMapper<ConversationMessage> {
}
