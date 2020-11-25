package com.s1243808733.java2beans;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import com.blankj.utilcode.util.CrashUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.s1243808733.java2beans.common.crash.CrashActivity;
import java.io.File;
import me.weishu.reflection.Reflection;
import org.xutils.x;

public final class App extends Application {

	private static App sApp;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
		Reflection.unseal(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
		sApp = this;
        initUtils();
		initX();
    }

    private void initX() {
        x.Ext.init(this);
        x.Ext.setDebug(false);
    }

    private void initUtils() {
        com.blankj.utilcode.util.Utils.init(this);
        initToast();

        File logDir = new File(getExternalCacheDir(), "log");
        LogUtils.Config config = LogUtils.getConfig();
        config.setLogSwitch(true);
        config.setLog2FileSwitch(true);
        config.setSaveDays(2);
        config.setDir(logDir);

        Thread.setDefaultUncaughtExceptionHandler(null);
        CrashUtils.init(new File(logDir, "crash"), new CrashUtils.OnCrashListener(){

                @Override
                public void onCrash(CrashUtils.CrashInfo info) {
                    Intent intent = new Intent(getApp(), CrashActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("crashInfo", info.toString());
                    startActivity(intent);
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(1);
                }

            });

    }

    private void initToast() {
        ToastUtils maker = ToastUtils.getDefaultMaker();
        maker.setLeftIcon(R.mipmap.ic_launcher);
    }

	public static App getApp() {
		return sApp;
	}

}
