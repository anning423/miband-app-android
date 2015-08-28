package com.anning423.mibandapp;

import android.app.Application;
import android.util.Log;

import com.zhaoxiaodan.miband.LogUtil;

import java.io.File;
import java.io.FileNotFoundException;

public class MyApplication extends Application {

    private static final String LOG_TAG = "MyApplication";

    private static MyApplication INSTANCE;

    public static MyApplication getInstance() {
        return INSTANCE;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;

        try {
            LogUtil.logToDir(new File("/sdcard/1.miband"), Log.INFO);
        } catch (FileNotFoundException e) {
            LogUtil.e(LOG_TAG, "LogUtil.logToDir failed", e);
        }

        Thread.UncaughtExceptionHandler inner = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(inner));
    }

    private static class CrashHandler implements Thread.UncaughtExceptionHandler {

        private Thread.UncaughtExceptionHandler mInner;

        public CrashHandler(Thread.UncaughtExceptionHandler inner) {
            mInner = inner;
        }

        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            LogUtil.e(LOG_TAG, "uncaughtException", ex);

            if (mInner != null) {
                mInner.uncaughtException(thread, ex);
            }
        }
    }
}
