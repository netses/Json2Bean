package com.s1243808733.library.json2bean;

import com.s1243808733.library.json2bean.util.FileUtils;
import com.s1243808733.library.json2bean.util.Utils;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Json2Bean {

    public static final String VERSION = "1.0";

    private final Options mOptions;

    public Json2Bean() {
        this.mOptions = new Builder();
    }

    public Json2Bean(Options options)  {
        this.mOptions = options;
    }

    public JavaBean toBean(final File file) throws IOException, JSONException {
        return toBean(FileUtils.readFile2String(file));
    }

    public JavaBean toBean(final Reader reader) throws IOException, JSONException  {
        return toBean(FileUtils.read2String(reader));
    }

    public JavaBean toBean(final String json) throws JSONException {
        Object object = null;
        try {
            object = new JSONObject(json);
        } catch (JSONException e) {
            try {
                object = new JSONArray(json);
            } catch (JSONException e2) {
                throw new JSONException("JSON parse failed");
            }
        }
        if (object instanceof JSONObject) {
            return toBean((JSONObject)object);
        } else {
            return toBean((JSONArray)object);
        }
    }

    public JavaBean toBean(final JSONArray ja) throws JSONException  {
        if (ja.length() > 0) {
            final Object object = ja.get(0);
            if (object instanceof JSONObject) {
                return toBean((JSONObject)object);
            } else {
                throw new JSONException("Keyless array conversion is not supported");
            }
        } else {
            return toBean(new JSONObject("{}"));
        }
    }

    public JavaBean toBean(final JSONObject jo) throws JSONException {
        JavaClass root = createClass(null, mOptions.getClassName(), mOptions.getRootClassModifiers());
        return new JavaBean(mOptions, root, parseObject(root, jo));
    }

    private JavaClass parseObject(final JavaClass parent, final JSONObject jo) throws JSONException {
        Iterator<String> it = jo.keys();
        while (it.hasNext()) {
            final String key = it.next();
            if (Utils.isTrimEmpty(key)) continue;
            final Object value = jo.get(key);
            if (JSONObject.NULL == value && !mOptions.isAcceptNullValue()) {
                continue;
            }
            parseObject(parent, key, value, jo);
        }
        return parent;
    }

    private void parseObject(final JavaClass parent, final String key, final Object value, final Object parentValue) throws JSONException {
        if (value instanceof JSONObject) {
            final JSONObject jo = (JSONObject)value;
            handleObject(parent, key, jo, parentValue instanceof JSONArray);
        } else if (value instanceof JSONArray) {
            final JSONArray ja = (JSONArray)value;
            if (ja.length() > 0) {
                parseObject(parent, key, ja.get(0), value);
            } else {
                //empty array []
            }
        } else {
            handleValue(parent, key, value, parentValue instanceof JSONArray, parent.getImportTypes());
        }
    }

    private JavaField createField(final JavaClass parent, final String jsonKey, final Object value, final boolean isJsonArray) {

        final JavaField field = new JavaField()
            .setModifiers(mOptions.getFieldModifiers())
            .setKey(jsonKey)
            .setIsArray(isJsonArray);

        field.setType(value);
        if (value instanceof JavaClass) {
            field.setTypeName(((JavaClass)value).getClassName());
        } else {
            field.setTypeName(value.getClass().getSimpleName());
        }

        if (mOptions.isUseLongIntegers() && Integer.class == value.getClass()) {
            field.setValue(new Long((Integer)value));
        } else {
            field.setValue(value);
        }

        String fieldName;
        if (Utils.isJavaKeyword(jsonKey)) {
            fieldName = getFieldNeme(parent.getFields(), jsonKey, 1);
        } else {
            String prettyType;
            prettyType: {
                boolean isKeepUnderline = true;
                if (mOptions.hasAnnotationStyle()) {
                    isKeepUnderline = false;
                }
                prettyType = Utils.getPrettyType(jsonKey, isKeepUnderline, !mOptions.hasAnnotationStyle());
            }
            fieldName = getFieldNeme(parent.getFields(), prettyType, 0);
        }

        field.setName(fieldName);
        return field;
    }

    private static String getFieldNeme(final List<JavaField> fields, final String name, final int index) {
        final String fieldName = format("%1$s%2$s", name, (index > 0 ?String.valueOf(index): ""));
        for (final JavaField field: fields) {
            if (fieldName.equals(field.getName())) 
                return getFieldNeme(fields, name, index + 1);
        }
        return fieldName;
    }

    private static String getClassNeme(final JavaClass parent, final String name, final int index) {
        final String clsName = format("%1$s%2$s", name, (index > 0 ?String.valueOf(index): ""));
        if (clsName.equals(parent.getClassName())) 
            return getClassNeme(parent, name, index + 1);

        List<JavaClass> kinds = parent.getChildClass();
        for (final JavaClass clazz: kinds) {
            if (clsName.equals(clazz.getClassName())) 
                return getClassNeme(parent, name, index + 1);
        }
        return clsName;
    }

    private JavaClass createClass(final JavaClass parent, final String className, final int modifiers) {
        final JavaClass clazz = new JavaClass(parent, className, modifiers);
        addBaseImportedTypes(clazz.getImportTypes());

        if (mOptions.isMakeClassesParcelable()) 
            clazz.addInterfaces("Parcelable");

        if (mOptions.isMakeClassesSerializable()) 
            clazz.addInterfaces("Serializable");

        setPackageName: {
            String packageName = Utils.getOrDefault(mOptions.getPackageName(), "");
            final List<JavaClass> parents = clazz.getParents();
            final int parentClassCount = parents.size();
            if (parentClassCount > 1) {
                StringBuilder sb = new StringBuilder();
                boolean b = false;
                int start = parentClassCount - 2;
                for (int i = start; i >= 0; i--) {
                    if (i != start) {
                        b = !b;
                        if (b) continue;
                    }
                    if (sb.length() != 0) sb.append(".");   
                    final JavaClass kind = parents.get(i);
                    sb.append(kind.getClassName());
                }
                if (packageName.length() != 0) packageName += ".";   
                packageName += sb.toString().toLowerCase(Locale.ENGLISH);
            }
            clazz.setPackageName(packageName);
        }
        return clazz;
    }

    private void addBaseImportedTypes(final ImportTypes importTypes) {

        if (mOptions.hasAnnotationStyle()) {
            if (mOptions.hasAnnotationStyle(AnnotationStyle.GSON)) {
                importTypes.add("com.google.gson.annotations.SerializedName");
            }
            if (mOptions.hasAnnotationStyle(AnnotationStyle.FASTJSON)) {
                importTypes.add("com.alibaba.fastjson.annotation.JSONField");
            } 
        }

        if (mOptions.isMakeClassesParcelable()) {
            importTypes.add("android.os.Parcel", "android.os.Parcelable");
        }

        if (mOptions.isMakeClassesSerializable()) {
            importTypes.add("java.io.Serializable");
        }

    }

    private void handleObject(final JavaClass parent, final String jsonKey, final JSONObject jsonObject, final boolean isArray) throws JSONException {

        final JavaClass javaClass = createClass(parent, null, mOptions.getClassModifiers());

        setClassName: {
            String className = Utils.getPrettyType(Utils.toUpperCaseFirst(jsonKey), false, true);
            className = getClassNeme(parent, className, 0);
            javaClass.setClassName(className);
        }

        parseObject(javaClass, jsonObject);

        final JavaField field = createField(parent, jsonKey, javaClass, isArray);

        handleField(parent, field, parent.getImportTypes());

        addFieldAndMethod(parent, field);
        parent.addChildClass(javaClass);
    }

    private void handleValue(final JavaClass parent, final String jsonKey, final Object value, final boolean isArray, final ImportTypes importedTypes) {
        final JavaField field = createField(parent, jsonKey, value, isArray);

        if (JSONObject.NULL == value) {
            field.setTypeName("Object");
        } else {
            setTypeNeme: {
                String type;
                final Class<?> valueClass =field.getValue().getClass();

                if (isArray) {
                    if (Utils.isBaseDataTypeWarpper(valueClass)) {
                        type = valueClass.getSimpleName();
                    } else {
                        if (mOptions.isUsePrimitiveTypes()) {
                            type = valueClass.getSimpleName();
                        } else {
                            type = Utils.unwarpper(valueClass).getSimpleName();
                        }
                    }
                } else if (Utils.isBaseDataTypeWarpper(valueClass)) {
                    if (mOptions.isUsePrimitiveTypes()) {
                        type = valueClass.getSimpleName();
                    } else {
                        type = Utils.unwarpper(valueClass).getSimpleName();
                    }
                } else {
                    type = valueClass.getSimpleName();
                }

                if (!Utils.isBaseDataTypeWarpper(value.getClass())) {
                    field.setTypeName(Utils.getPrettyType(type, false, !mOptions.hasAnnotationStyle()));
                } else {
                    field.setTypeName(type);
                }

            }

            handleField(parent, field, importedTypes);
        }
        addFieldAndMethod(parent, field);
    }

    private void handleField(JavaClass parent, final JavaField field, final ImportTypes importedTypes) {
        if (field.isArray()) {

            importedTypes.add("java.util.List");
            {
                String name = field.getTypeName();
                field.setTypeName(format("List<%s>", name));
            }

            if (mOptions.isInitializeCollections()) {
                importedTypes.add("java.util.ArrayList");
                field.setValueName(format("new ArrayList<%s>()", ""));
            }

            if (mOptions.isMakeClassesParcelable()) {
                final Object value = field.getValue();
                final Class<?> valueType = value.getClass();
                if (JavaClass.class == valueType
                    || String.class == valueType
                    || Utils.isBaseDataTypeWarpper(valueType)) {
                    importedTypes.add("java.util.ArrayList");
                }
            }

        }
    }

    private void addFieldAndMethod(final JavaClass clazz, final JavaField field) {

        final String fieldType = field.getTypeName();
        final String fieldName = field.getName();

        clazz.addField(field);

        if (mOptions.isUseSetter()) {
            
            final JavaMethod method=new SetterMethod(field)
                .setModifiers(mOptions.getMethodModifiers())
                .addStatement(format("this.%1$s = %2$s", fieldName, fieldName));

            method.setParameters(new MethodParameter(fieldType, fieldName));

            setMethodName: {
                String methodName = Utils.getPrettyType(Utils.toUpperCaseFirst(fieldName), !false, true);
                method.setName(format("set%s", methodName));
            }

            if (mOptions.isUseReturnThis()) {
                method.setType(clazz.getClassName());
                method.addStatement("return this");
            } else {
                method.setType("void");
            }

            clazz.addMethod(method);
        }

        if (mOptions.isUseGetter()) {

            final JavaMethod method = new GetterMethod(field)
                .setModifiers(mOptions.getMethodModifiers())
                .setType(field.getTypeName())
                .addStatement(format("return %s", fieldName));

            setMethodName: {
                String methodName = Utils.getPrettyType(Utils.toUpperCaseFirst(fieldName), !false, true);
                method.setName(format("%1$s%2$s", field.getValue() instanceof Boolean ?"is": "get", methodName));
            }

            clazz.addMethod(method);
        }

    }

    private static String format(String format, Object...args) {
        return String.format(format, args);
    }

    private static abstract class FieldMethod extends JavaMethod {

        private JavaField mField;

        public FieldMethod(JavaField field) {
            this.mField = field;
        }

        public boolean hasField() {
            return mField != null;
        }

        public void setField(JavaField field) {
            this.mField = field;
        }

        public JavaField getField() {
            return mField;
        }
    }

    private static class SetterMethod extends FieldMethod {

        public SetterMethod(JavaField field) {
            super(field);
        }

    }

    private static class GetterMethod extends FieldMethod {

        public GetterMethod(JavaField field) {
            super(field);
        }

    }

    public static class Builder extends Options {

        public Builder() {
            super();
        }

        public Builder(String className) {
            this.mClassName = className;
        }

        public Builder(String packageName, String className) {
            this.mPackageName = packageName;
            this.mClassName = className;
        }

        public Builder setRootClassModifiers(int rootClassModifiers) {
            this.mRootClassModifiers = rootClassModifiers;
            return this;
        }

        public Builder setClassModifiers(int classModifiers) {
            this.mClassModifiers = classModifiers;
            return this;
        }

        public Builder setFieldModifiers(int fieldModifiers) {
            this.mFieldModifiers = fieldModifiers;
            return this;
        }

        public Builder setMethodModifiers(int methodModifiers) {
            this.mNethodModifiers = methodModifiers;
            return this;
        }

        public Builder setPackageName(String packageName) {
            this.mPackageName = packageName;
            return this;
        }

        public Builder setClassName(String className) {
            this.mClassName = className;
            return this;
        }

        public Builder setUseGetter(boolean useGetter) {
            this.nUseGetter = useGetter;
            return this;
        }

        public Builder setUseSetter(boolean useSetter) {
            this.mUseSetter = useSetter;
            return this;
        }

        public Builder setUseInteger(boolean useInteger) {
            this.mUseInteger = useInteger;
            return this;
        }

        public Builder setOverrideToString(boolean overrideToString) {
            this.mOverrideToString = overrideToString;
            return this;
        }

        public Builder setUseReturnThis(boolean useReturnThis) {
            this.mUseReturnThis = useReturnThis;
            return this;
        }

        public Builder setAcceptNullValue(boolean acceptNullValue) {
            this.mAcceptNullValue = acceptNullValue;
            return this;
        }

        public Builder setInitializeCollections(boolean initializeCollections) {
            this.mInitializeCollections = initializeCollections;
            return this;
        }

        public Builder setMakeClassesParcelable(boolean makeClassesParcelable) {
            this.mMakeClassesParcelable = makeClassesParcelable;
            return this;
        }

        public Builder setMakeClassesSerializable(boolean makeClassesSerializable) {
            this.mMakeClassesSerializable = makeClassesSerializable;
            return this;
        }

        public Builder setUsePrimitiveTypes(boolean usePrimitiveTypes) {
            this.mUsePrimitiveTypes = usePrimitiveTypes;
            return this;
        }

        public Builder setUseLongIntegers(boolean useLongIntegers) {
            this.mUseLongIntegers = useLongIntegers;
            return this;
        }

        public Builder setOverrideEquals(boolean overrideEquals) {
            this.mOverrideEquals = overrideEquals;
            return this;
        }

        public Builder setOverrideHashCode(boolean overrideHashCode) {
            this.mOverrideHashCode = overrideHashCode;
            return this;
        }

        public Builder addAnnotationStyle(AnnotationStyle... annotationStyles) {
            if (annotationStyles != null && annotationStyles.length > 0) {
                for (AnnotationStyle annotationStyle : annotationStyles) {
                    this.mAnnotationStyle.put(annotationStyle, annotationStyle);
                }
            }
            return this;
        }

        public Builder removeAnnotationStyle(AnnotationStyle annotationStyle) {
            if (hasAnnotationStyle(annotationStyle)) 
                this.mAnnotationStyle.remove(annotationStyle);
            return this;
        }

        public Builder clearAnnotationStyle() {
            mAnnotationStyle.clear();
            return this;
        }

        public Json2Bean create() {
            return new Json2Bean(this);
        }

    }

}
