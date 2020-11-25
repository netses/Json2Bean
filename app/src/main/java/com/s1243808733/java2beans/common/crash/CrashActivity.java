package com.s1243808733.java2beans.common.crash;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.ClipboardUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.s1243808733.java2beans.R;
import com.s1243808733.java2beans.common.activity.BaseActivity;

public class CrashActivity extends BaseActivity {

    private TextView mErrorView;

    private String mCrashInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crash);
        mErrorView = findViewById(R.id.message);
        mCrashInfo = getIntent().getStringExtra("crashInfo");
        mErrorView.setText(mCrashInfo);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_crash, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        AppUtils.relaunchApp(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.copy:
                ClipboardUtils.copyText(mCrashInfo);
                ToastUtils.showShort(R.string.message_copied_to_clipboard);
                break;
            case R.id.restart:
                AppUtils.relaunchApp(true);
                break;

        }
        return super.onOptionsItemSelected(item);
    }

}


