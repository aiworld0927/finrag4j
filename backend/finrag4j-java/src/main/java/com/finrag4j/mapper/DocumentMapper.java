package com.finrag4j.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.finrag4j.entity.Document;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 文档Mapper接口
 * 
 * 功能说明：
 * - 文档CRUD操作
 * - 文件去重查询
 * - 租户隔离查询
 * 
 * @author FinRag4j Team
 * @version 1.0.0
 */
@Mapper
public interface DocumentMapper extends BaseMapper<Document> {

    /**
     * 根据MD5查询文档（用于去重）
     * 
     * @param fileMd5 文件MD5
     * @param tenantId 租户ID
     * @return 文档实体
     */
    @Select("SELECT * FROM document WHERE file_md5 = #{fileMd5} AND tenant_id = #{tenantId} AND deleted = 0 LIMIT 1")
    Document selectByMd5(@Param("fileMd5") String fileMd5, @Param("tenantId") Long tenantId);
}