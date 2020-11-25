package com.s1243808733.java2beans.common.activity;

import android.app.ActionBar;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.fragment.app.FragmentActivity;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.s1243808733.java2beans.R;
import org.xutils.x;

public abstract class BaseActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initStatusBar();
        ToastUtils.getDefaultMaker().setMode(getResources().getBoolean(R.bool.night_mode)
                                             ?ToastUtils.MODE.DARK: ToastUtils.MODE.LIGHT);
        initActionBar();
        x.view().inject(this);
        
    }

    private void initStatusBar() {
        if (Build.VERSION.SDK_INT >= 23) {
            BarUtils.setStatusBarLightMode(this, !getResources().getBoolean(R.bool.night_mode));
        }
    }

    private void initActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar == null) return;
        if (canGoBack()) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public boolean canGoBack() {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (canGoBack())finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    
}
