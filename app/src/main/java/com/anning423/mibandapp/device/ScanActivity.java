package com.anning423.mibandapp.device;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.anning423.mibandapp.R;
import com.zhaoxiaodan.miband.LogUtil;

import java.util.ArrayList;

public class ScanActivity extends ListActivity {

    private static final String LOG_TAG = "ScanActivity";

    public static final String EXTRA_DEVICE = "EXTRA_DEVICE";

    private BluetoothAdapter mBlueAdapter;
    private ArrayList<BluetoothDevice> mDeviceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        mDeviceList = new ArrayList<>();
        setListAdapter(mListAdapter);

        mBlueAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBlueAdapter.isEnabled()) {
            mBlueAdapter.enable();
        }
        mBlueAdapter.startLeScan(mScanCallback);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        mBlueAdapter.stopLeScan(mScanCallback);

        BluetoothDevice device = mDeviceList.get(position);
        LogUtil.i(LOG_TAG, "onListItemClick: device=%s", device);

        Intent result = new Intent();
        result.putExtra(EXTRA_DEVICE, device);
        setResult(RESULT_OK, result);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mBlueAdapter.stopLeScan(mScanCallback);
    }

    private BaseAdapter mListAdapter = new BaseAdapter() {

        @Override
        public int getCount() {
            return mDeviceList.size();
        }

        @Override
        public Object getItem(int position) {
            return mDeviceList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(ScanActivity.this).inflate(android.R.layout.simple_list_item_1, parent, false);
            }

            BluetoothDevice device = mDeviceList.get(position);
            String text = String.format("%s, %s", device.getAddress(), device.getName());
            ((TextView) convertView).setText(text);

            return convertView;
        }
    };

    private BluetoothAdapter.LeScanCallback mScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if (!mDeviceList.contains(device)) {
                        LogUtil.i(LOG_TAG, "onLeScan: add to list, device=%s, rssi=%d", device, rssi);
                        mDeviceList.add(device);
                        mListAdapter.notifyDataSetChanged();
                    }
                }
            });
        };
    };
}
