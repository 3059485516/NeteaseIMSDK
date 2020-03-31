package com.netease.nim.yl.upload;

/**
 * Created by hzwangxiaoming on 2016/12/9.
 */

public class HTTPResult {
    private int statusCode;
    private String msg;
    private Exception exception;

    public HTTPResult(int statusCode, String msg, Exception e) {
        this.statusCode = statusCode;
        this.msg = msg;
        this.exception = e;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Exception getException() {
        return exception;
    }

    protected void setException(Exception exception) {
        this.exception = exception;
    }

}
