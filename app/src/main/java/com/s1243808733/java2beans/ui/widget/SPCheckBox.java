package com.s1243808733.java2beans.ui.widget;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.CheckBox;
import com.s1243808733.java2beans.R;

public class SPCheckBox extends CheckBox {

    private SharedPreferences sp;

    private String key;

    private boolean defValue;

    public SPCheckBox(Context context) {
        this(context, null);
    }

    public SPCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SPCheckBox);
            String spName = a.getString(R.styleable.SPCheckBox_sp);
            key = a.getString(R.styleable.SPCheckBox_key);
            defValue = a.getBoolean(R.styleable.SPCheckBox_value, false);
            if (TextUtils.isEmpty(spName)) {
                throw new IllegalArgumentException();
            }
            sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
            a.recycle();
            up();
        }
    }

    public void up() {
        if (sp != null && !TextUtils.isEmpty(key)) {
            boolean isChecked = sp.getBoolean(key, defValue);
            if (isChecked != this.isChecked()) {
                setChecked(isChecked);
            }
        }
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setDefValue(boolean defValue) {
        this.defValue = defValue;
    }

    public boolean getDefValue() {
        return defValue;
    }

    public void setSp(SharedPreferences sp) {
        this.sp = sp;
    }

    public SharedPreferences getSp() {
        return sp;
    }

    @Override
    public void setChecked(boolean checked) {
        super.setChecked(checked);
        if (sp != null && !TextUtils.isEmpty(key)) {
            sp.edit().putBoolean(key, checked).commit();
        }
    }

}
