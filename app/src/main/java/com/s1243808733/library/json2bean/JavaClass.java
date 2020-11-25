package com.s1243808733.library.json2bean;

import java.util.ArrayList;
import java.util.List;

public class JavaClass {

    private JavaClass parent;

    private int modifiers;

    private ImportTypes importTypes = new ImportTypes();

    private String packageName;

    private String className;

    private List<String> interfaces = new ArrayList<>();

    private List<JavaField> fields = new ArrayList<>();

    private List<JavaMethod> methods = new ArrayList<>();

    private List<JavaClass> childClass = new ArrayList<>();

    public JavaClass(String className) {
        this.className = className;
    }

    public JavaClass(JavaClass parent, String className) {
        this.parent = parent;
        this.className = className;
    }

    public JavaClass(JavaClass parent, String className, int modifiers) {
        this.parent = parent;
        this.className = className;
        this.modifiers = modifiers;
    }

    public JavaClass setPackageName(String packageName) {
        this.packageName = packageName;
        return this;
    }

    public String getPackageName() {
        return packageName;
    }

    public JavaClass setImportTypes(ImportTypes importTypes) {
        this.importTypes = importTypes;
        return this;
    }

    public ImportTypes getImportTypes() {
        return importTypes;
    }

    public void addInterfaces(String type) {
        interfaces.add(type);
    }

    public void setInterfaces(List<String> interfaces) {
        this.interfaces = interfaces;
    }

    public List<String> getInterfaces() {
        return interfaces;
    }

    public void setParent(JavaClass parent) {
        this.parent = parent;
    }

    public JavaClass getParent() {
        return parent;
    }

    public JavaClass setModifiers(int modifiers) {
        this.modifiers = modifiers;
        return this;
    }

    public int getModifiers() {
        return modifiers;
    }

    public JavaClass setClassName(String className) {
        this.className = className;
        return this;
    }

    public String getClassName() {
        return className;
    }

    public void setFields(List<JavaField> fields) {
        this.fields = fields;
    }

    public List<JavaField> getFields() {
        return fields;
    }

    public void setMethods(List<JavaMethod> methods) {
        this.methods = methods;
    }

    public List<JavaMethod> getMethods() {
        return methods;
    }

    public void setChildClass(List<JavaClass> childClass) {
        this.childClass = childClass;
    }

    public List<JavaClass> getChildClass() {
        return childClass;
    }

    public List<JavaClass> getAllChildClass() {
        return getAllChildClass(this);
    }

    private List<JavaClass> getAllChildClass(JavaClass kind) {
        List<JavaClass> kinds = new ArrayList<>();
        kinds.add(kind);
        for (JavaClass child : kind.getChildClass()) {
            kinds.addAll(getAllChildClass(child));
        }
        return kinds;
    }

    public JavaClass addField(JavaField field) {
        fields.add(field);
        return this;
    }

    public JavaClass addMethod(JavaMethod method) {
        methods.add(method);
        return this;
    }

    public JavaClass addChildClass(JavaClass kind) {
        childClass.add(kind);
        return this;
    }

    public List<JavaClass> getParents() {
        List<JavaClass> lists = new ArrayList<>();
        int parentCount = getParentCount();
        for (int i = 0;i < parentCount;i++) {
            lists.add(getParent(i));
        }
        return lists;
    }

    public JavaClass getParent(int index) {
        int parentCount = getParentCount();
        if (index >= parentCount) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + parentCount);
        }
        int i = 0;
        JavaClass _parent = this;
        while ((_parent = _parent.getParent()) != null) {
            if (i == index) {
                return _parent;
            }
            i++;
        }
        return null;
    }

    public int getParentCount() {
        int parentCount = 0;
        JavaClass parent = this;
        while ((parent = parent.getParent()) != null) {
            parentCount++;
        }
        return parentCount;
    }

}
