package com.s1243808733.java2beans.ui.view;

import android.app.Activity;
import android.view.View;
import org.xutils.x;

public abstract class BaseActivityViewHolder {

    public final Activity activity;

    private final View view;

    public BaseActivityViewHolder(Activity activity) {
        this(activity, android.R.id.content);
    }

    public BaseActivityViewHolder(Activity activity, int contentId) {
        this(activity, activity.findViewById(contentId));
    }

    public BaseActivityViewHolder(Activity activity, View view) {
        this.activity = activity;
        this.view = view;
        x.view().inject(this, view);
    }

    public View getContentView() {
        return view;
    }

}

