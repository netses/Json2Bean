package com.s1243808733.library.json2bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JavaMethod {
    
    private int modifiers;

    private String type;

    private String name;

    private List<MethodParameter> parameters = new ArrayList<>();

    private List<String> statements = new ArrayList<>();

    public JavaMethod() {
    }

    public JavaMethod setModifiers(int modifiers) {
        this.modifiers = modifiers;
        return this;
    }

    public int getModifiers() {
        return modifiers;
    }

    public JavaMethod setType(String type) {
        this.type = type;
        return this;
    }

    public String getType() {
        return type;
    }

    public JavaMethod setName(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    public JavaMethod setParameters(List<MethodParameter> parameters) {
        this.parameters = parameters;
        return this;
    }

    public JavaMethod setParameters(MethodParameter... parameters) {
        return setParameters(Arrays.asList(parameters));
    }

    public List<MethodParameter> getParameters() {
        return parameters;
    }

    public JavaMethod setStatements(List<String> statements) {
        this.statements = statements;
        return this;
    }

    public List<String> getStatements() {
        return statements;
    }

    public JavaMethod addStatement(String statement) {
        statements.add(statement);
        return this;
    }

    public JavaMethod addParameter(MethodParameter parameter) {
        parameters.add(parameter);
        return this;
    }

}
