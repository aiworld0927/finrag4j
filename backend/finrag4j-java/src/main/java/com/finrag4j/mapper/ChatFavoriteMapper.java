package com.finrag4j.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.finrag4j.entity.ChatFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 问答收藏Mapper接口
 */
@Mapper
public interface ChatFavoriteMapper extends BaseMapper<ChatFavorite> {

    @Select("SELECT cf.* FROM chat_favorite cf WHERE cf.tenant_id = #{tenantId} AND cf.user_id = #{userId} AND cf.deleted = 0 ORDER BY cf.created_at DESC")
    List<ChatFavorite> selectByUserId(@Param("tenantId") Long tenantId, @Param("userId") Long userId);

    @Select("SELECT cf.* FROM chat_favorite cf WHERE cf.chat_id = #{chatId} AND cf.tenant_id = #{tenantId} AND cf.deleted = 0")
    ChatFavorite selectByChatId(@Param("chatId") Long chatId, @Param("tenantId") Long tenantId);
}