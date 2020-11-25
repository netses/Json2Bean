package com.s1243808733.java2beans.ui.widget;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import com.blankj.utilcode.util.ReflectUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.s1243808733.java2beans.R;

public class CustomButton extends Button {

	private String mOnClickMethodName;

    public CustomButton(Context context) {
		this(context, null);
	}

    public CustomButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		if (attrs != null) {
			TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomButton);
			mOnClickMethodName = a.getString(R.styleable.CustomButton_click);
			a.recycle();
		}
	}

	public void setClickMethodName(String methodName) {
		this.mOnClickMethodName = methodName;
	}

	public String getClickMethodName() {
		return mOnClickMethodName;
	}

	public void bindClick(final Object target) {
		bindClick(target, getClickMethodName());
	}

	public void bindClick(final Object target, final String methodName) {
		if (methodName != null) setOnClickListener(new OnClickListener(){

                    @Override
                    public void onClick(View view) {
                        try {
                            ReflectUtils.reflect(target).method(methodName, view);
                        } catch (Throwable e) {
                            ToastUtils.showShort(e.getMessage());
                        }
                    }
                });
	}

}
