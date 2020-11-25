package com.s1243808733.library.json2bean;

public class JavaField {

    private int modifiers;

    private Object type;

    private String typeName;

    private String name;

    private String valueName;

    private Object value;

    private String key;

    private boolean isArray;

    public JavaField setModifiers(int modifiers) {
        this.modifiers = modifiers;
        return this;
    }

    public int getModifiers() {
        return modifiers;
    }

    public JavaField setType(Object type) {
        this.type = type;
        return this;
    }

    public Object getType() {
        return type;
    }

    public JavaField setTypeName(String typeName) {
        this.typeName = typeName;
        return this;
    }

    public String getTypeName() {
        return typeName;
    }

    public JavaField setName(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    public JavaField setValueName(String valueName) {
        this.valueName = valueName;
        return this;
    }

    public String getValueName() {
        return valueName;
    }

    public JavaField setValue(Object value) {
        this.value = value;
        return this;
    }

    public Object getValue() {
        return value;
    }

    public JavaField setKey(String key) {
        this.key = key;
        return this;
    }

    public String getKey() {
        return key;
    }

    public JavaField setIsArray(boolean isArray) {
        this.isArray = isArray;
        return this;
    }

    public boolean isArray() {
        return isArray;
    }

}
