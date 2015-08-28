package com.anning423.mibandapp.device;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.anning423.mibandapp.MyApplication;
import com.zhaoxiaodan.miband.DeviceStateListener;
import com.zhaoxiaodan.miband.LogUtil;
import com.zhaoxiaodan.miband.MiBand;
import com.zhaoxiaodan.miband.model.UserInfo;

public class BleService extends Service {

    private static final String LOG_TAG = "BleService";

    public static final String ACTION_STATE_CHANGED = MyApplication.getInstance().getPackageName() + ".ACTION_STATE_CHANGED";

    public static final String EXTRA_STATE = "EXTRA_STATE";

    public static final int STATE_IDLE = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;
    public static final int STATE_DISCOVERED = 3;
    public static final int STATE_DISCONNECTED = 4;

    private final Object LOCKER = new Object();

    private int mState = STATE_IDLE;

    private MiBand mMiband;

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.i(LOG_TAG, "onCreate");

        mMiband = new MiBand(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.i(LOG_TAG, "onDestroy");

        mMiband.disconnect();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        LogUtil.i(LOG_TAG, "onBind: intent=%s", intent);
        return new BleBinder();
    }

    public static String getStateString(int state) {
        switch (state) {
            case STATE_IDLE:
                return "idle";
            case STATE_CONNECTING:
                return "connecting";
            case STATE_CONNECTED:
                return "connected";
            case STATE_DISCOVERED:
                return "discovered";
            case STATE_DISCONNECTED:
                return "disconnected";
            default:
                return null;
        }
    }

    private void setState(int newState) {
        synchronized (LOCKER) {
            LogUtil.i(LOG_TAG, "setState: from %d to %d", mState, newState);

            if (mState != newState) {
                mState = newState;

                Intent intent = new Intent(ACTION_STATE_CHANGED);
                intent.putExtra(EXTRA_STATE, newState);
                sendBroadcast(intent);
            }
        }
    }

    public class BleBinder extends Binder {

        public int getState() {
            return mState;
        }

        public String getStateString() {
            return BleService.getStateString(mState);
        }

        public MiBand getMiband() {
            return mMiband;
        }

        public void connect(BluetoothDevice device) {
            synchronized (LOCKER) {
                disconnect();

                LogUtil.i(LOG_TAG, "connect: device=%s", device);

                if (device != null) {
                    setState(STATE_CONNECTING);
                    mMiband.connect(device, new MyDeviceListener());
                }
            }
        }

        public void disconnect() {
            synchronized (LOCKER) {
                if (mState != STATE_IDLE) {
                    LogUtil.i(LOG_TAG, "disconnect");
                    mMiband.disconnect();
                    setState(STATE_IDLE);
                }
            }
        }
    }

    private class MyDeviceListener implements DeviceStateListener {

        @Override
        public void onConnectionStateChange(int status, int newState) {
            synchronized (LOCKER) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        setState(STATE_CONNECTED);
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        setState(STATE_DISCONNECTED);
                        setState(STATE_CONNECTING); // autoConnect == true
                    }
                } else {
                    setState(STATE_DISCONNECTED);
                }
            }
        }

        @Override
        public void onServicesDiscovered(int status) {
            synchronized (LOCKER) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    if (mState == STATE_CONNECTED) {
                        setState(STATE_DISCOVERED);

                        // init user info
                        UserInfo userInfo = new UserInfo(20111111, 1, 32, 180, 55, "胖梁", 0);
                        mMiband.setUserInfo(userInfo);
                    }
                }
            }
        }
    }
}
