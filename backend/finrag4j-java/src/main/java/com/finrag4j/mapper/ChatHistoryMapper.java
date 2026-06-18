package com.finrag4j.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.finrag4j.entity.ChatHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 对话历史Mapper接口
 */
@Mapper
public interface ChatHistoryMapper extends BaseMapper<ChatHistory> {

    @Select("SELECT ch.* FROM chat_history ch WHERE ch.session_id = #{sessionId} AND ch.tenant_id = #{tenantId} AND ch.deleted = 0 ORDER BY ch.created_at ASC")
    List<ChatHistory> selectBySessionId(@Param("sessionId") String sessionId, @Param("tenantId") Long tenantId);

    @Select("SELECT ch.* FROM chat_history ch WHERE ch.tenant_id = #{tenantId} AND ch.user_id = #{userId} AND ch.deleted = 0 ORDER BY ch.created_at DESC LIMIT #{limit}")
    List<ChatHistory> selectRecentByUserId(@Param("tenantId") Long tenantId, @Param("userId") Long userId, @Param("limit") Integer limit);

    @Select("SELECT COUNT(*) FROM chat_history ch WHERE ch.tenant_id = #{tenantId} AND ch.created_at >= #{startTime} AND ch.created_at <= #{endTime}")
    Integer countTodayRequests(@Param("tenantId") Long tenantId, @Param("startTime") String startTime, @Param("endTime") String endTime);
}