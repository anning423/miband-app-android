package com.anning423.mibandapp.antilost;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.anning423.mibandapp.MyApplication;
import com.anning423.mibandapp.device.BleService;
import com.zhaoxiaodan.miband.ActionCallback;
import com.zhaoxiaodan.miband.LogUtil;

import java.util.List;

public class AntiLostService extends Service {

    private static final String LOG_TAG = "AntiLostService";

    private static final String ACTION_READ_RSSI = MyApplication.getInstance().getPackageName() + ".ACTION_READ_RSSI";

    private BleService.BleBinder mBinder;

    private PendingIntent mAlarmIntent;
    private AlarmManager mAlarmManager;

    private AlarmNotify mAlarmNotify;

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.i(LOG_TAG, "onCreate");

        mAlarmNotify = new AlarmNotify(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BleService.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);

        Intent intent = new Intent(this, BleService.class);
        bindService(intent, mConn, Context.BIND_AUTO_CREATE);

        intent = new Intent(this, AntiLostService.class);
        intent.setAction(ACTION_READ_RSSI);
        mAlarmIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        //mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000, mAlarmIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.i(LOG_TAG, "onDestroy");

        mAlarmNotify.stop();
        mAlarmManager.cancel(mAlarmIntent);
        unregisterReceiver(mReceiver);
        unbindService(mConn);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent == null ? null : intent.getAction();
        if (ACTION_READ_RSSI.equals(action)) {
            if (mBinder != null) {
                mBinder.getMiband().readRssi(new ActionCallback() {
                    @Override
                    public void onSuccess(Object data) {
                        LogUtil.i(LOG_TAG, "readRssi: onSuccess, data=%s", data);
                    }

                    @Override
                    public void onFail(int errorCode, String msg) {
                        LogUtil.e(LOG_TAG, "readRssi: onFail, msg=%s", msg);
                    }
                });
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static boolean isStarted() {
        Context context = MyApplication.getInstance();
        ComponentName component = new ComponentName(context, AntiLostService.class);
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> services = manager.getRunningServices(Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo service : services) {
            if (service.service.equals(component)) {
                return service.started;
            }
        }
        return false;
    }

    private ServiceConnection mConn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinder = (BleService.BleBinder) service;
            mAlarmNotify.setMiband(mBinder.getMiband());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mAlarmNotify.setMiband(null);
            mBinder = null;
        }
    };

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BleService.ACTION_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(BleService.EXTRA_STATE, -1);
                switch (state) {
                    case BleService.STATE_DISCOVERED:
                        mAlarmNotify.stop();
                        break;
                    case BleService.STATE_DISCONNECTED:
                        mAlarmNotify.start();
                        break;
                }
            }
        }
    };
}
