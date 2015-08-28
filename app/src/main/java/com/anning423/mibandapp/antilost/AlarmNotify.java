package com.anning423.mibandapp.antilost;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Vibrator;
import android.provider.Settings;

import com.zhaoxiaodan.miband.MiBand;
import com.zhaoxiaodan.miband.model.VibrationMode;

/**
 * Created by an on 2015/8/27.
 */
public class AlarmNotify {

    private Context mContext;
    private boolean mStarted = false;

    private MiBand mMiband;
    private Vibrator mVibrator;
    private MediaPlayer mPlayer;

    public AlarmNotify(Context context) {
        mContext = context;
        mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public synchronized void setMiband(MiBand miband) {
        mMiband = miband;
    }

    public synchronized void start() {
        if (!mStarted) {
            //startSound();
            startActivity();
            //startVibratePhone();
            //startVibrateMiband();
            mStarted = true;
        }
    }

    public synchronized void stop() {
        stopSound();
        stopActivity();
        stopVibratePhone();
        stopVibrateMiband();
        mStarted = false;
    }

    public synchronized void startSound() {
        Uri ringUri = Uri.parse(Settings.System.DEFAULT_RINGTONE_URI.toString());
        mPlayer = MediaPlayer.create(mContext, ringUri);
        mPlayer.setLooping(true);
        mPlayer.start();
    }

    public synchronized void stopSound() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    public synchronized void startVibratePhone() {
        long[] pattern = {0, 1000, 1000};
        mVibrator.vibrate(pattern, 1);
    }

    public synchronized void stopVibratePhone() {
        mVibrator.cancel();
    }

    public synchronized void startVibrateMiband() {
        startVibrateMiband(VibrationMode.VIBRATION_UNTIL_CALL_STOP);
    }

    public synchronized void startVibrateMiband(VibrationMode mode) {
        if (mMiband != null) {
            mMiband.startVibration(mode);
        }
    }

    public synchronized void stopVibrateMiband() {
        if (mMiband != null) {
            mMiband.stopVibration();
        }
    }

    public synchronized void startActivity() {
        Intent intent = new Intent(mContext, AlarmActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    public synchronized void stopActivity() {

    }
}
