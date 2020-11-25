package com.s1243808733.java2beans.ui.widget;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.RadioGroup;
import com.s1243808733.java2beans.R;

public class SPRadioGroup extends RadioGroup implements RadioGroup.OnCheckedChangeListener {
   
    private static final String key = "checkPos";
    
    private SharedPreferences sp;

    private int defCheck;

    private RadioGroup.OnCheckedChangeListener mOnCheckedChangeListener;

    public SPRadioGroup(Context context) {
        this(context, null);
    }

    public SPRadioGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        super.setOnCheckedChangeListener(this);
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SPRadioGroup);
            String spName = a.getString(R.styleable.SPRadioGroup_sp);
            if (TextUtils.isEmpty(spName)) {
                throw new IllegalArgumentException();
            }
            sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
            defCheck = a.getResourceId(R.styleable.SPRadioGroup_defCheck, 0);
            a.recycle();
        }
    }

    @Override
    public void setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener listene) {
        this.mOnCheckedChangeListener = listene;
    }

    @Override
    public void onCheckedChanged(RadioGroup view, int checkedId) {
        if (sp != null) {
            label:
            for (int i = 0; i < getChildCount(); i++) {
                if (checkedId == getChildAt(i).getId()) {
                    sp.edit().putInt(key, i).commit();
                    break label;
                }
            }
        }
        if (mOnCheckedChangeListener != null) {
            mOnCheckedChangeListener.onCheckedChanged(view, checkedId);
        }
    }

    public void up() {
        if (sp != null) {
            int pos = sp.getInt(key, -1);
            if (pos == -1) {
                check(defCheck);
                return;
            }
            label:
            for (int i = 0; i < getChildCount(); i++) {
                if (pos == i) {
                    check(getChildAt(i).getId());
                    break label;
                }
            }
        }
    }

    public void setSp(SharedPreferences sp) {
        this.sp = sp;
    }

    public SharedPreferences getSp() {
        return sp;
    }

    public void setDefCheck(int defCheck) {
        this.defCheck = defCheck;
    }

    public int getDefCheck() {
        return defCheck;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        up();
    }

}
