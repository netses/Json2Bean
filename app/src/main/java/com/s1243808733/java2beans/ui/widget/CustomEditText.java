package com.s1243808733.java2beans.ui.widget;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.EditText;

public class CustomEditText extends EditText {

	private DelayCloseErrorTask mDelayCloseErrorTask;

	private boolean autoCloseError = true;

    private final Handler mHandler;

	public CustomEditText(Context context) {
		this(context, null);
	}

    public CustomEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
        mHandler = new Handler();
	}

	@Override
	public void setError(CharSequence error) {
		super.setError(null);
		super.setError(error);
		if (mDelayCloseErrorTask != null) {
			mDelayCloseErrorTask.cancel();
			mDelayCloseErrorTask = null;
		}
		if (autoCloseError) {
			mDelayCloseErrorTask = new DelayCloseErrorTask();
			mDelayCloseErrorTask.start();
		}
	}

	private class DelayCloseErrorTask {
        
		private boolean mCancel;

		public void cancel() {
			this.mCancel = true;
		}

		public void start() {
			mHandler.postDelayed(new Runnable(){

					@Override
					public void run() {
						if (!mCancel)
							setError(null);
					}
				}, 1500);
		}
	}

}
