package com.anning423.mibandapp;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.widget.TextView;

import com.anning423.mibandapp.device.BleService;
import com.anning423.mibandapp.device.DeviceFragment;
import com.zhaoxiaodan.miband.LogUtil;

public class MainActivity extends FragmentActivity {

    private static final String LOG_TAG = "MainActivity";

    private TextView vState;
    private FragmentTabHost vTabHost;

    private BleService.BleBinder mBinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.i(LOG_TAG, "onCreate");

        setContentView(R.layout.activity_main);

        vState = (TextView) findViewById(R.id.vState);

        vTabHost = (FragmentTabHost) findViewById(R.id.vTabHost);
        vTabHost.setup(this, getSupportFragmentManager(), R.id.vTabContent);
        vTabHost.addTab(vTabHost.newTabSpec("Device").setIndicator("Device"), DeviceFragment.class, null);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BleService.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);

        Intent intent = new Intent(this, BleService.class);
        bindService(intent, mConn, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.i(LOG_TAG, "onDestroy");

        unregisterReceiver(mReceiver);
        unbindService(mConn);
    }

    private void updateViews() {
        String stateStr = mBinder == null ? null : mBinder.getStateString();
        vState.setText("state: " + stateStr);
    }

    private ServiceConnection mConn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinder = (BleService.BleBinder) service;
            updateViews();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBinder = null;
            updateViews();
        }
    };

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BleService.ACTION_STATE_CHANGED.equals(action)) {
                updateViews();
            }
        }
    };
}
