package com.finrag4j.agent.mapper;

import com.finrag4j.agent.entity.ChatHistory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 聊天记录Mapper
 */
@Mapper
public interface ChatHistoryMapper extends BaseMapper<ChatHistory> {
}
