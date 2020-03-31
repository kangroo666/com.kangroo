package com.lagou.utils;

public class ParameterMapping {
    //content是#{}里的参数
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ParameterMapping(String content) {
        this.content = content;
    }
}
