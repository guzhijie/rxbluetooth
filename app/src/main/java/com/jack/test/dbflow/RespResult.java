package com.jack.test.dbflow;

/**
 * 描述:
 *
 * @author :jack.gu
 * @since : 2019/12/19
 */
public class RespResult<T> {
    private int code;
    private String message;
    private T data;

    public int getCode() {
        return code;
    }

    public RespResult<T> setCode(int code) {
        this.code = code;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public RespResult<T> setMessage(String message) {
        this.message = message;
        return this;
    }

    public T getData() {
        return data;
    }

    public RespResult<T> setData(T data) {
        this.data = data;
        return this;
    }
}
