package com.s1243808733.library.json2bean;

import com.s1243808733.library.json2bean.JavaClass;
import com.s1243808733.library.json2bean.util.FileUtils;
import com.s1243808733.library.json2bean.util.JavaWriter;
import com.s1243808733.library.json2bean.util.Utils;
import com.s1243808733.library.json2bean.util.ZipUtils;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.json.JSONObject;

public class JavaBean {

    private final Options mOptions;

    private final JavaClass     mRoot;

    private final JavaClass     mClass;

    private Collection<String>  mMergedImportTypes;

    public JavaBean(Options options, JavaClass root, JavaClass kind) {
        this.mOptions = options;
        this.mRoot = root;
        this.mClass = kind;
        mMergedImportTypes = mergeImportTypes(mRoot.getAllChildClass()).values();
    }

    public void output(final File out, final OutputType outputType) throws IOException {

        if (OutputType.WHOLE == outputType) {

            FileUtils.writeFileFromString(out, toJava());

        } else if (OutputType.ZIP == outputType) {

            final File tempDir=new File(out.getParentFile(), UUID.randomUUID().toString());
            final File outDir = new File(tempDir, Utils.getOrDefault(mOptions.getPackageName(), "")
                                         .replace(".", File.separator));
            output(outDir, OutputType.SPLIST);
            File[] listFiles = tempDir.listFiles();

            try {
                if (listFiles == null || listFiles.length == 0) {
                    throw new IOException("not files");
                } else {
                    ZipUtils.zipFiles(Arrays.asList(listFiles), out);
                }
            } finally {
                FileUtils.deleteAllFileInDir(tempDir);
            }

        } else if (OutputType.SPLIST == outputType) {

            final List<JavaClass> allClass = mRoot.getAllChildClass();

            for (final JavaClass clazz:allClass) {
                final StringWriter sw = new StringWriter();
                final JavaWriter jw = new JavaWriter(sw);

                final String classPkg = Utils.getOrDefault(clazz.getPackageName(), "");
                jw.emitPackage(classPkg);

                emitImports: {

                    ImportTypes importTypes = new ImportTypes();
                    importTypes.add(clazz.getImportTypes().values());

                    for (JavaClass child : clazz.getChildClass()) {
                        String childPkg = child.getPackageName();

                        if (!classPkg.equals(childPkg)) {
                            StringBuilder importType = new StringBuilder();
                            if (!Utils.isEmpty(childPkg)) {
                                importType.append(childPkg);
                                importType.append(".");
                            }
                            importType.append(child.getClassName());
                            importTypes.add(importType.toString());
                        }

                    }

                    if (!importTypes.values().isEmpty()) {
                        jw.emitImports(importTypes.values());
                        jw.emitEmptyLine();
                    }
                }

                writeClassAnnotation(jw, clazz);

                beginType: {
                    int modifiers = clazz.getModifiers();
                    if ((modifiers & Modifier.STATIC) != 0) {
                        modifiers &= ~Modifier.STATIC;
                    }
                    jw.beginType(clazz.getClassName(), "class", modifiers , null, clazz.getInterfaces().toArray(new String[]{}));
                }

                writeJava(jw, clazz, true);
                jw.endType();

                final String code = sw.toString();

                File outDir; {
                    String packageName = classPkg;
                    if (Utils.isEmpty(packageName)) {
                        outDir = new File(out.getAbsolutePath());
                    } else {
                        if (mOptions.getPackageName() != null && packageName.startsWith(mOptions.getPackageName())) {
                            packageName = packageName.substring(mOptions.getPackageName().length());
                        }
                        if (packageName.startsWith(".")) {
                            packageName = packageName.substring(1);
                        }
                        outDir = new File(out, packageName.replace(".", File.separator));
                    }
                }

                String fileName = String.format("%1$s%2$s", clazz.getClassName(), ".java");
                File outFile = new File(outDir, fileName);

                FileUtils.writeFileFromString(outFile, code);
            }
        }

    }
    
    public String toJava() throws IOException {
        StringWriter out = new StringWriter();
        addHeaderInfos(out);
        toJava(out);
        return out.toString();
    }
   
    public void addHeaderInfos(Writer out){
        
    }

    public Writer toJava(Writer out) throws IOException {
        JavaWriter jw = new JavaWriter(out);

        jw.emitPackage(Utils.getOrDefault(mOptions.getPackageName(), ""));

        Collection<String> importTypes = mMergedImportTypes;
        if (!importTypes.isEmpty()) {
            jw.emitImports(importTypes);
            jw.emitEmptyLine();
        }

        writeClassAnnotation(jw, mClass);

        jw.beginType(Utils.getOrDefault(mOptions.getClassName(), ""), "class", mRoot.getModifiers(), null, mRoot.getInterfaces().toArray(new String[]{}));
        writeJava(jw, mClass, false);
        jw.endType();
        
        return out;
    }

    private ImportTypes mergeImportTypes(final Collection<JavaClass> kinds) {
        return mergeImportTypes(kinds.toArray(new JavaClass[]{}));
    }

    private ImportTypes mergeImportTypes(final JavaClass... kinds) {
        ImportTypes importTypes = new ImportTypes();
        for (JavaClass clazz : kinds) {
            importTypes.add(clazz.getImportTypes().values());
        }
        return importTypes;
    }

    private String getClassName(JavaClass kind, boolean splist) {
        String className = kind.getClassName();
        if (splist) {
            for (final JavaClass clazz : kind.getChildClass()) {
                if (className.equals(clazz.getClassName())) {
                    final StringBuilder sb = new StringBuilder();
                    if (!Utils.isEmpty(kind.getPackageName())) {
                        sb.append(kind.getPackageName()).append(".");
                    }
                    sb.append(kind.getClassName());
                    className = sb.toString();
                    return className;
                }
            }
        }
        return className;
    }

    private static String getFieldNeme(final List<JavaField> fields, final String name, final int index) {
        final String fieldName = String.format("%1$s%2$s", name, (index > 0 ?String.valueOf(index): ""));
        for (final JavaField field : fields) {
            if (fieldName.equals(field.getName())) {
                return getFieldNeme(fields, name, index + 1);
            }
        }
        return fieldName;
    }

    private void writeJava(final JavaWriter jw, final JavaClass kind, final boolean splist) throws IOException {

        final String simpleClassName = kind.getClassName();
        final String className = getClassName(kind, splist);

        writeSerialVersionUID:
        if (mOptions.isMakeClassesSerializable()) {
            jw.emitEmptyLine()
                .emitField("long", "serialVersionUID", Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL, "1L");
        }

        writeField:
        for (final JavaField field : kind.getFields()) {
            jw.emitEmptyLine();

            emitAnnotation: {
                if (mOptions.hasAnnotationStyle()) {
                    String keyLiteral = JavaWriter.stringLiteral(field.getKey());
                    if (mOptions.hasAnnotationStyle(AnnotationStyle.GSON)) {
                        jw.emitAnnotation("SerializedName", keyLiteral);
                    }
                    if (mOptions.hasAnnotationStyle(AnnotationStyle.FASTJSON)) {
                        jw.emitAnnotation("JSONField", String.format("name = %s", keyLiteral));
                    }
                }
            }

            if (!field.getKey().equals(field.getName()) && !mOptions.hasAnnotationStyle()) {
                jw.emitField(field.getTypeName(), field.getName(), field.getModifiers(), field.getValueName(), false);
                jw.write(" //" + field.getKey() + "\n");
            } else {
                jw.emitField(field.getTypeName(), field.getName(), field.getModifiers(), field.getValueName());
            }

        }

        writeParcelable:
        if (mOptions.isMakeClassesParcelable()) {

            jw.emitEmptyLine()
                .beginMethod(null, simpleClassName, Modifier.PUBLIC)
                .endMethod();

            jw.emitEmptyLine();

            final String parcelVarName = "in";
            jw.beginMethod(null, simpleClassName, Modifier.PROTECTED, new String[]{"final Parcel",parcelVarName});

            final String classLoaderVarName = "classLoader";

            initClassLoaderVar:
            for (final JavaField field : kind.getFields()) {
                final Object value = field.getValue();
                final Class<?> valueType = value.getClass();
                if (field.isArray()
                    || JavaClass.class == valueType
                    || JSONObject.NULL == value) {
                    jw.emitStatement("ClassLoader %s = getClass().getClassLoader()", classLoaderVarName);
                    break initClassLoaderVar;
                }
            }

            for (final JavaField field : kind.getFields()) {
                final String fieldName = field.getName();

                String formatedFieldName = field.getName();
                if (parcelVarName.equals(fieldName)) {
                    formatedFieldName = String.format("this.%s", formatedFieldName);
                }

                final boolean isArray = field.isArray();

                final Class<?> valueType = field.getValue().getClass();

                if (JavaClass.class == valueType) {
                    if (isArray) {
                        jw.emitStatement("%1$s.readList(%2$s, %3$s)", parcelVarName, String.format("%1$s = new ArrayList<>()", formatedFieldName), classLoaderVarName);
                    } else {
                        jw.emitStatement("%1$s = %2$s.readParcelable(%3$s)", formatedFieldName, parcelVarName, classLoaderVarName);
                    }
                } else if (Boolean.class == valueType) {
                    if (isArray) {
                        jw.emitStatement("%1$s.readList(%2$s = new ArrayList<>(), %3$s)", parcelVarName, formatedFieldName, classLoaderVarName);
                    } else {
                        jw.emitStatement("%1$s = %2$s.readInt() == 1", formatedFieldName, parcelVarName);
                    }
                } else if (Integer.class == valueType) {
                    if (isArray) {
                        jw.emitStatement("%1$s.readList(%2$s = new ArrayList<>(), %3$s)", parcelVarName, formatedFieldName, classLoaderVarName);
                    } else {
                        jw.emitStatement("%1$s = %2$s.readInt()", formatedFieldName, parcelVarName);
                    }
                } else if (Long.class == valueType) {
                    if (isArray) {
                        jw.emitStatement("%1$s.readList(%2$s = new ArrayList<>(), %3$s)", parcelVarName, formatedFieldName, classLoaderVarName);
                    } else {
                        jw.emitStatement("%1$s = %2$s.readLong()", formatedFieldName, parcelVarName);
                    }
                } else if (Double.class == valueType) {
                    if (isArray) {
                        jw.emitStatement("%1$s.readList(%2$s = new ArrayList<>(), %3$s)", parcelVarName, formatedFieldName, classLoaderVarName);
                    } else {
                        jw.emitStatement("%1$s = %2$s.readDouble()", formatedFieldName, parcelVarName);
                    }
                } else if (String.class == valueType) {
                    if (isArray) {
                        jw.emitStatement("%1$s.readStringList(%12s = new ArrayList<>())", parcelVarName,  formatedFieldName);
                    } else {
                        jw.emitStatement("%1$s = %2$s.readString()", formatedFieldName, parcelVarName);
                    }
                } else if (JSONObject.NULL == field.getValue()) {
                    jw.emitStatement("%1$s = %2$s.readValue(%3$s)", formatedFieldName, parcelVarName, classLoaderVarName);
                }
            }

            jw.endMethod();
        }

        writeMethod:
        for (final JavaMethod method:kind.getMethods()) {
            final List<String> parameters = new ArrayList<>();

            for (final MethodParameter parameter : method.getParameters()) {
                parameters.add(parameter.getType());
                parameters.add(parameter.getName());
            }

            jw.emitEmptyLine();
            jw.beginMethod(method.getType(), method.getName(), method.getModifiers(), parameters.toArray(new String[]{}));

            for (final String code:method.getStatements()) {
                jw.emitStatement(code);
            }
            jw.endMethod();

        }

        writeOverrideEquals:
        if (mOptions.isOverrideEquals()) {

            final String objVarName = "obj";
            final String rhsVarName = "rhs";

            jw.emitEmptyLine();
            jw.emitAnnotation("Override");
            jw.beginMethod("boolean", "equals", Modifier.PUBLIC, new String[]{"Object",objVarName});
            jw.emitStatement("if (this == %s) return true", objVarName);
            jw.emitStatement("if (%1$s == null || !(%1$s instanceof %2$s)) return false", objVarName, className);
            jw.emitStatement("%1$s %2$s = (%1$s)%3$s", className, rhsVarName, objVarName);

            for (final JavaField field : kind.getFields()) {
                final String fieldName = field.getName();
                String formatedFieldName=field.getName();

                if (objVarName.equals(fieldName)
                    || rhsVarName.equals(fieldName)) {
                    formatedFieldName = String.format("this.%s", fieldName);
                }

                final Object value=field.getValue();
                final Class<?> valueClass = value.getClass();

                if (Utils.isBaseDataType(valueClass) || Utils.isBaseDataTypeWarpper(valueClass)) {
                    if (mOptions.isUsePrimitiveTypes() || field.isArray() || value instanceof String) {
                        jw.emitStatement("if (%1$s != null && !%1$s.equals(%2$s.%3$s)) return false", formatedFieldName, rhsVarName, fieldName);
                    } else {
                        jw.emitStatement("if (%1$s != %2$s.%3$s) return false", formatedFieldName, rhsVarName, fieldName);
                    }
                } else {
                    jw.emitStatement("if (%1$s != null && !%1$s.equals(%2$s.%3$s)) return false", formatedFieldName, rhsVarName, fieldName);
                }
            }

            jw.emitStatement("return true", objVarName);
            jw.endMethod();

        }

        overrideHashCode:
        if (mOptions.isOverrideHashCode()) {
            jw.emitEmptyLine();
            jw.emitAnnotation("Override");
            jw.beginMethod("int", "hashCode", Modifier.PUBLIC);

            if (kind.getFields().isEmpty()) {
                jw.emitStatement("return super.hashCode()");
            } else {
                final String resultVarNamName = "result";
                jw.emitStatement("int %s = 17", resultVarNamName);

                for (final JavaField field:kind.getFields()) {
                    String fieldName = field.getName();
                    if (resultVarNamName.equals(fieldName)) {
                        fieldName = String.format("this.%s", fieldName);
                    }

                    final Object value = field.getValue();
                    final Class<?> valueClass = value.getClass();

                    if (Utils.isBaseDataType(valueClass) || Utils.isBaseDataTypeWarpper(valueClass)) {
                        if (mOptions.isUsePrimitiveTypes() || field.isArray() || value instanceof String) {
                            jw.emitStatement("%1$s = 31 * %1$s + (%2$s == null ? 0 : %2$s.hashCode())", resultVarNamName, fieldName);
                        } else {
                            if (value instanceof Boolean) {
                                jw.emitStatement("%1$s = 31 * %1$s + (%2$s ? 1 : 0)", resultVarNamName, fieldName);
                            }  else if (value instanceof Long) {
                                jw.emitStatement("%1$s = 31 * %1$s + (int)(%2$s ^ (%2$s >>> 32))", resultVarNamName, fieldName);
                            } else if (value instanceof Double) {
                                String longBits = String.format("Double.doubleToLongBits(%s)", fieldName);
                                jw.emitStatement("%1$s = 31 * %1$s + (int)(%2$s ^ (%2$s >>> 32))", resultVarNamName, longBits);
                            } else {
                                jw.emitStatement("%1$s = 31 * %1$s + %2$s", resultVarNamName, fieldName);
                            }
                        }
                    } else {
                        jw.emitStatement("%1$s = 31 * %1$s + (%2$s == null ? 0 : %2$s.hashCode())", resultVarNamName, fieldName);
                    }

                }
                jw.emitStatement("return %s", resultVarNamName);
            }
            jw.endMethod();
        }

        writeOverrideToString:
        if (mOptions.isOverrideToString()) {
            jw.emitEmptyLine();
            jw.emitAnnotation("Override");
            jw.beginMethod("String", "toString", Modifier.PUBLIC);

            if (kind.getFields().isEmpty()) {
                jw.emitStatement("return \"%s{}\"", kind.getClassName());
            } else {
                final StringBuilder sb = new StringBuilder();
                for (final JavaField field:kind.getFields()) {
                    String fieldName=field.getName();
                    if (sb.length() > 0) {
                        sb.append("\n + ");
                        fieldName = ", " + fieldName;
                    }
                    sb.append(String.format("\"%1$s=\" + %2$s", fieldName, field.getName()));
                }
                jw.emitStatement("return %s", String.format("\"%1$s{\"\n + %2$s \n + \"}\"", kind.getClassName(), sb.toString()));
            }
            jw.endMethod();
        }

        writeParcelable:
        if (mOptions.isMakeClassesParcelable()) {
            describeContents: {
                jw.emitEmptyLine();
                jw.emitAnnotation("Override");
                jw.beginMethod("int", "describeContents", Modifier.PUBLIC);
                jw.emitStatement("return 0");
                jw.endMethod();
            }

            writeToParcel: {
                final String parcelVarName = "dest";
                final String flagsVarName = "flags";

                jw.emitEmptyLine();
                jw.emitAnnotation("Override");
                jw.beginMethod("void", "writeToParcel", Modifier.PUBLIC, new String[]{"Parcel",parcelVarName,"int",flagsVarName});

                for (final JavaField field:kind.getFields()) {
                    String fieldName = field.getName();
                    final boolean isArray = field.isArray();
                    final Object value = field.getValue();
                    final Class<?> valueType = value.getClass();

                    String formatedFieldName = field.getName();

                    if (parcelVarName.equals(fieldName)
                        || flagsVarName.equals(fieldName)) {
                        formatedFieldName = String.format("this.%s", fieldName);
                    }

                    if (JavaClass.class == valueType) {
                        if (isArray) {
                            jw.emitStatement("%1$s.writeList(%2$s)", parcelVarName, formatedFieldName);
                        } else {
                            jw.emitStatement("%1$s.writeParcelable(%2$s, %3$s)", parcelVarName, formatedFieldName, flagsVarName);
                        }
                    } else if (Boolean.class == valueType) {
                        if (isArray) {
                            jw.emitStatement("%1$s.writeList(%2$s)", parcelVarName, formatedFieldName);
                        } else {
                            jw.emitStatement("%1$s.writeInt(%2$s ? 1 : 0)", parcelVarName, formatedFieldName);
                        }
                    } else if (Integer.class == valueType) {
                        if (isArray) {
                            jw.emitStatement("%1$s.writeList(%2$s)", parcelVarName, formatedFieldName);
                        } else {
                            jw.emitStatement("%1$s.writeInt(%2$s)", parcelVarName, formatedFieldName);
                        }
                    } else if (Long.class == valueType) {
                        if (isArray) {
                            jw.emitStatement("%1$s.writeList(%2$s)", parcelVarName, formatedFieldName);
                        } else {
                            jw.emitStatement("%1$s.writeLong(%2$s)", parcelVarName, formatedFieldName);
                        }
                    } else if (Double.class == valueType) {
                        if (isArray) {
                            jw.emitStatement("%1$s.writeList(%2$s)", parcelVarName, formatedFieldName);
                        } else {
                            jw.emitStatement("%1$s.writeDouble(%2$s)", parcelVarName, formatedFieldName);
                        }
                    } else if (String.class == valueType) {
                        if (isArray) {
                            jw.emitStatement("%1$s.writeStringList(%2$s)", parcelVarName, formatedFieldName);

                        } else {
                            jw.emitStatement("%1$s.writeString(%2$s)", parcelVarName, formatedFieldName);
                        }
                    } else if (JSONObject.NULL == field.getValue()) {
                        jw.emitStatement("%1$s.writeValue(%2$s)", parcelVarName, formatedFieldName);
                    }
                }

                jw.endMethod();
            }

            creator: {
                String creatorClassName = String.format("%sCreator", simpleClassName);
                creatorClassName = getCreatorClassName(creatorClassName, kind.getChildClass(), 0);

                jw.emitEmptyLine();
                jw.emitField(String.format("Creator<%s>", className), "CREATOR", Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL, String.format("new %s()", creatorClassName));

                jw.emitEmptyLine();
                jw.beginType(creatorClassName, "class", Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL, null, String.format("Creator<%s>", className));

                createFromParcel: {
                    jw.emitEmptyLine();
                    jw.emitAnnotation("SuppressWarnings", JavaWriter.stringLiteral("unchecked"));
                    jw.emitAnnotation("Override");
                    jw.beginMethod(className, "createFromParcel", Modifier.PUBLIC, new String[]{"Parcel","source"});
                    jw.emitStatement("return new %1$s(%2$s)", className, "source");
                    jw.endMethod();
                }

                newArray: {
                    jw.emitEmptyLine();
                    jw.emitAnnotation("Override");
                    jw.beginMethod(String.format("%s[]", className), "newArray", Modifier.PUBLIC, new String[]{"int","size"});
                    jw.emitStatement("return new %1$s[%2$s]", className, "size");
                    jw.endMethod();
                }

                jw.emitEmptyLine();
                jw.endType();
            }
        }

        if (!splist) {
            for (final JavaClass child : kind.getChildClass()) {
                jw.emitEmptyLine();
                writeClassAnnotation(jw, child);
                final int modifiers = child.getModifiers();
                jw.beginType(child.getClassName(), "class", modifiers , null , child.getInterfaces().toArray(new String[]{}));
                writeJava(jw, child, splist);
                jw.endType();
            }
        }

        jw.emitEmptyLine();
    }

    private String getCreatorClassName(String creatorClassName, List<JavaClass> childClass, int index) {
        String name = creatorClassName;
        if (index > 0) {
            name = creatorClassName + index;
        }
        for (JavaClass child : childClass) {
            if (name.equals(child.getClassName())) {
                return getCreatorClassName(creatorClassName, childClass, index + 1);
            }
        }
        return name;
    }

    private void writeClassAnnotation(final JavaWriter jw, final JavaClass kind) throws IOException {

    }

}
