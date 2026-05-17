package com.seckill.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * ClassName: Result
 * Package: com.seckill.dto
 * Description:
 *
 * @Author AnXin
 * @Create 2026/3/27 16:26
 * @Version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result {
    private Boolean success;
    private String errorMsg;
    private Object data;
    private Long total;
    /** 游标分页：下一页的起始时间 */
    private String cursor;
    /** 游标分页：下一页的起始 id */
    private Long lastId;

    public static Result ok() {
        return new Result(true, null, null, null, null, null);
    }
    public static Result ok(Object data) {
        return new Result(true, null, data, null, null, null);
    }
    public static Result ok(List<?> data, Long total) {
        return new Result(true, null, data, total, null, null);
    }
    /** 游标分页结果 */
    public static Result ok(List<?> data, String cursor, Long lastId) {
        return new Result(true, null, data, null, cursor, lastId);
    }
    public static Result fail(String errorMsg) {
        return new Result(false, errorMsg, null, null, null, null);
    }
}
