package com.miaoyi.response;

public class HttpResponse {
    private int code = 200;
    private String state = "success";
    private String errmsg;
    private Object data;

    public static HttpResponse success(Object data) {
        HttpResponse response = new HttpResponse();
        response.setData(data);

        return response;
    }

    public static HttpResponse error(String description) {
        HttpResponse response = new HttpResponse();
        response.setErrmsg(description);
        response.state = "error";

        return response;
    }
    public void setCode(int code) {
        this.code = code;
    }

    public void setErrmsg(String msg) {
        this.errmsg = msg;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public void setState(String state) { this.state = state; }

    public int getCode() {
        return code;
    }

    public Object getData() {
        return data;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public String getState() { return state; }

}
