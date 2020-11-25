package com.s1243808733.library.json2bean;

class MethodParameter {

    private String type;

    private String name;

    public MethodParameter(String type, String name) {
        this.type = type;
        this.name = name;
    }

    public MethodParameter setType(String type) {
        this.type = type;
        return this;
    }

    public String getType() {
        return type;
    }

    public MethodParameter setName(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

}
