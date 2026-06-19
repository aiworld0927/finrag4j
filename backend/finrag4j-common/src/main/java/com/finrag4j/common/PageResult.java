package com.finrag4j.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 分页响应结果
 */
@Data
@Schema(description = "分页响应结果")
public class PageResult<T> implements Serializable {

    @Schema(description = "总记录数")
    private long total;

    @Schema(description = "当前页数据")
    private List<T> records;

    @Schema(description = "当前页码")
    private int pageNum;

    @Schema(description = "每页数量")
    private int pageSize;

    @Schema(description = "总页数")
    private int totalPages;

    public static <T> PageResult<T> of(long total, List<T> records, int pageNum, int pageSize) {
        PageResult<T> result = new PageResult<>();
        result.setTotal(total);
        result.setRecords(records);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        result.setTotalPages((int) Math.ceil((double) total / pageSize));
        return result;
    }
}
