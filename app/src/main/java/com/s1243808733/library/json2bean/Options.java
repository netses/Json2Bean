package com.s1243808733.library.json2bean;

import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class Options {

    protected int mRootClassModifiers;

    protected int mClassModifiers;

    protected int mFieldModifiers;

    protected int mNethodModifiers;

    protected String mPackageName;

    protected String mClassName;

    protected boolean mUseSetter;

    protected boolean nUseGetter;

    protected boolean mUseInteger;

    protected boolean mUseReturnThis;

    protected boolean mOverrideToString;

    protected boolean mAcceptNullValue;

    protected boolean mInitializeCollections;

    protected boolean mMakeClassesParcelable;

    protected boolean mMakeClassesSerializable;

    protected boolean mUsePrimitiveTypes;

    protected boolean mUseLongIntegers;

    protected boolean mOverrideEquals;

    protected boolean mOverrideHashCode;

    protected Map<AnnotationStyle,AnnotationStyle> mAnnotationStyle = new LinkedHashMap<>();

    public Options() {
        setDefValue();   
    }

    private void setDefValue() {
        
        mRootClassModifiers = Modifier.PUBLIC;
        mClassModifiers = Modifier.PUBLIC | Modifier.STATIC;
        mFieldModifiers = Modifier.PRIVATE;
        mNethodModifiers = Modifier.PUBLIC;

        mUseSetter = true;
        nUseGetter = true;
        mUseReturnThis = false;
        
    }

    public boolean isOverrideEquals() {
        return mOverrideEquals;
    }

    public boolean isOverrideHashCode() {
        return mOverrideHashCode;
    }

    public boolean isUseLongIntegers() {
        return mUseLongIntegers;
    }

    public boolean isUsePrimitiveTypes() {
        return mUsePrimitiveTypes;
    }

    public boolean isMakeClassesSerializable() {
        return mMakeClassesSerializable;
    }

    public boolean isMakeClassesParcelable() {
        return mMakeClassesParcelable;
    }

    public boolean isInitializeCollections() {
        return mInitializeCollections;
    }

    public boolean isAcceptNullValue() {
        return mAcceptNullValue;
    }

    public int getClassModifiers() {
        return mClassModifiers;
    }

    public int getRootClassModifiers() {
        return mRootClassModifiers;
    }

    public int getFieldModifiers() {
        return mFieldModifiers;
    }

    public int getMethodModifiers() {
        return mNethodModifiers;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public String getClassName() {
        return mClassName;
    }

    public boolean isUseSetter() {
        return mUseSetter;
    }

    public boolean isUseGetter() {
        return nUseGetter;
    }

    public boolean isUseInteger() {
        return mUseInteger;
    }

    public boolean isUseReturnThis() {
        return mUseReturnThis;
    }

    public boolean isOverrideToString() {
        return mOverrideToString;
    }
    
    public boolean hasAnnotationStyle(AnnotationStyle annotationStyle) {
        return hasAnnotationStyle() && mAnnotationStyle.containsKey(annotationStyle);
    }

    public boolean hasAnnotationStyle() {
        return mAnnotationStyle != null && !mAnnotationStyle.isEmpty();
    }
    
}
