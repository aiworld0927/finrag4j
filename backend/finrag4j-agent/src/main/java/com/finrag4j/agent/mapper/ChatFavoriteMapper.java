package com.finrag4j.agent.mapper;

import com.finrag4j.agent.entity.ChatFavorite;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 聊天收藏Mapper
 */
@Mapper
public interface ChatFavoriteMapper extends BaseMapper<ChatFavorite> {
}
