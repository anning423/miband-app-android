package com.anning423.mibandapp.device;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.anning423.mibandapp.R;
import com.anning423.mibandapp.antilost.AntiLostService;
import com.zhaoxiaodan.miband.ActionCallback;
import com.zhaoxiaodan.miband.model.VibrationMode;

public class DeviceFragment extends Fragment {

    private static final int REQUEST_SCAN = 100;

    private BleService.BleBinder mBinder;

    private View vScanDevice;
    private View vDisconnect;
    private View vVibrateOnce;

    private TextView vReadRssi;
    private TextView vReadBattery;

    private View vStartAntiLost;
    private View vStopAntiLost;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_device, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        vScanDevice = view.findViewById(R.id.vScanDevice);
        vScanDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ScanActivity.class);
                startActivityForResult(intent, REQUEST_SCAN);
            }
        });

        vDisconnect = view.findViewById(R.id.vDisconnect);
        vDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBinder.disconnect();
            }
        });

        vReadRssi = (TextView) view.findViewById(R.id.vReadRssi);
        vReadRssi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBinder.getMiband().readRssi(new ActionCallback() {
                    @Override
                    public void onSuccess(Object data) {
                        vReadRssi.setText("rssi: " + data);
                    }

                    @Override
                    public void onFail(int errorCode, String msg) {
                        vReadRssi.setText("rssi: " + msg);
                    }
                });
            }
        });

        vReadBattery = (TextView) view.findViewById(R.id.vReadBattery);
        vReadBattery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBinder.getMiband().getBatteryInfo(new ActionCallback() {
                    @Override
                    public void onSuccess(Object data) {
                        vReadBattery.setText("battery: " + data);
                    }

                    @Override
                    public void onFail(int errorCode, String msg) {
                        vReadBattery.setText("battery: " + msg);
                    }
                });
            }
        });

        vVibrateOnce = view.findViewById(R.id.vVibrateOnce);
        vVibrateOnce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBinder.getMiband().startVibration(VibrationMode.VIBRATION_WITH_LED);
            }
        });

        vStartAntiLost = view.findViewById(R.id.vStartAntiLost);
        vStartAntiLost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getActivity();
                Intent intent = new Intent(context, AntiLostService.class);
                context.startService(intent);
                updateViews();
            }
        });

        vStopAntiLost = view.findViewById(R.id.vStopAntiLost);
        vStopAntiLost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getActivity();
                Intent intent = new Intent(context, AntiLostService.class);
                context.stopService(intent);
                updateViews();
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Context context = getActivity();
        Intent intent = new Intent(context, BleService.class);
        context.bindService(intent, mConn, Context.BIND_AUTO_CREATE);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BleService.ACTION_STATE_CHANGED);
        context.registerReceiver(mReceiver, filter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_SCAN && resultCode == Activity.RESULT_OK) {
            BluetoothDevice device = data.getParcelableExtra(ScanActivity.EXTRA_DEVICE);
            if (mBinder != null) {
                mBinder.connect(device);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        Context context = getActivity();
        context.unbindService(mConn);
        context.unregisterReceiver(mReceiver);
    }

    private void updateViews() {
        int state = mBinder == null ? -1 : mBinder.getState();
        boolean antiLostStarted = AntiLostService.isStarted();
        vScanDevice.setEnabled(state == BleService.STATE_IDLE);
        vDisconnect.setEnabled(state != BleService.STATE_IDLE);
        vReadRssi.setEnabled(state == BleService.STATE_DISCOVERED);
        vReadBattery.setEnabled(state == BleService.STATE_DISCOVERED);
        vVibrateOnce.setEnabled(state == BleService.STATE_DISCOVERED);
        vStartAntiLost.setEnabled(state != BleService.STATE_IDLE && !antiLostStarted);
        vStopAntiLost.setEnabled(antiLostStarted);
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
