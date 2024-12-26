package com.github.xdli.model;

public class Configuration {
    private String dataId;
    private String group;
    private String content;
    private long version;

    public Configuration(String dataId, String group, String content) {
        this.dataId = dataId;
        this.group = group;
        this.content = content;
        this.version = System.currentTimeMillis();
    }

    // Getters and setters
    public String getDataId() {
        return dataId;
    }

    public String getGroup() {
        return group;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
        this.version = System.currentTimeMillis();
    }

    public long getVersion() {
        return version;
    }
} 