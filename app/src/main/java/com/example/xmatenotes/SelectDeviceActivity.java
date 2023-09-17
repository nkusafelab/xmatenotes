package com.example.xmatenotes;

import java.lang.reflect.Field;
import java.util.ArrayList;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.core.app.ActivityCompat;

import com.example.xmatenotes.ui.BaseActivity;
import com.example.xmatenotes.App.XmateNotesApplication;
import com.example.xmatenotes.logic.manager.PenMacManager;
import com.tqltech.tqlpencomm.BLEException;
import com.tqltech.tqlpencomm.BLEScanner;
import com.tqltech.tqlpencomm.PenCommAgent;

/**
 * 蓝牙设备选择连接活动
 */
public class SelectDeviceActivity extends BaseActivity {
    private final static String TAG = "SelectDeviceActivity";
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private boolean mScanning = true;
    private static final int REQUEST_ENABLE_BT = 1;

    private ListView listView;
    private PenCommAgent bleManager;
    public static PenMacManager penMacManager = null;

    //是否申请过相关权限
    private static boolean isrequestPermissions = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "on Create start");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("可用的设备");
        //actionBar.setTitle(ApplicationResources.getLocalVersionName(this));
        actionBar.setDisplayHomeAsUpEnabled(true);

        penMacManager = PenMacManager.getInstance();

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE is not supported", Toast.LENGTH_SHORT).show();
            finish();
        }

//        StatusBarCompat.setStatusBarColor(this, getResources().getColor(R.color.statusColor), true);

        bleManager = PenCommAgent.GetInstance(getApplication());
        listView = (ListView) findViewById(R.id.lv_device);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.selectdeviceactivitymenu, menu);
        if (!mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.menu_scan:
                mLeDeviceListAdapter.clear();
                scanLeDevice(true);
                break;
            case R.id.menu_stop:
                scanLeDevice(false);
                break;
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (bleManager != null) {
            Log.e(TAG, "onResume()：select devices resume");
            bleManager.init();
        }

        // Initializes list view adapter.
        mLeDeviceListAdapter = new LeDeviceListAdapter(this);
        listView.setAdapter(mLeDeviceListAdapter);
        listView.setOnItemClickListener(itemClickListener);
        //listView.setListAdapter(mLeDeviceListAdapter);

        //判断安卓版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //需要申请的权限
            String[] permission = {
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
            };
            for (int j = 0; j < permission.length; j++) {
                //判断是否有权限
                if (XmateNotesApplication.context.checkSelfPermission(permission[j]) != PackageManager.PERMISSION_GRANTED) {
                    SelectDeviceActivity.this.requestPermissions(permission, j);
                }
            }
            isrequestPermissions = true;
        }

        View rootView = ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
        int actionBarHeight = getSupportActionBar().getHeight();
        int statusHeight = getStatusBarHeight();
        rootView.setY(actionBarHeight + statusHeight + 10);

        try {
            scanLeDevice(true);
        } catch (Exception e) {
            Log.i(TAG, "onResume scan----" + e.toString());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        Log.i(TAG, "onActivityResult:" + resultCode);
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume start");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause start");

    }

    @Override
    protected void onStop() {
        super.onStop();
        scanLeDevice(false);
        mLeDeviceListAdapter.clear();
        isrequestPermissions = false;
    }

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
            Log.i(TAG, "onListItemClick device " + device);
            if (device == null) {
                return;
            }
            try {
                bleManager.stopFindAllDevices();
                Bundle b = new Bundle();
                XmateNotesApplication.mBTMac = device.getAddress();
                penMacManager.putMac(XmateNotesApplication.mBTMac);
//                Toast.makeText(SelectDeviceActivity.this, "选中了device.getAddress(): "+device.getAddress(),Toast.LENGTH_SHORT).show();
                b.putString(BluetoothDevice.EXTRA_DEVICE, mLeDeviceListAdapter.getDevice(position).getAddress());

                Intent result = new Intent();
                result.putExtras(b);
                setResult(Activity.RESULT_OK, result);
                finish();
            } catch (Exception e) {
                Log.i(TAG, "---scan finish---" + e.toString());
            }
        }
    };

    //@Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
        Log.i(TAG, "onListItemClick device " + device);

        if (device == null) {
            return;
        }
        try {
            bleManager.stopFindAllDevices();
            Bundle b = new Bundle();

            b.putString(BluetoothDevice.EXTRA_DEVICE, mLeDeviceListAdapter.getDevice(position).getAddress());

            Intent result = new Intent();
            result.putExtras(b);
            setResult(Activity.RESULT_OK, result);
            finish();
        } catch (Exception e) {
            Log.i(TAG, "---scan finish---" + e.toString());
        }
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            Log.e(TAG, "enable: " + enable);
            bleManager.FindAllDevices(new BLEScanner.OnBLEScanListener() {

                @Override
                public void onScanResult(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    //runOnUiThread(new Runnable() {
                    //    @Override
                    //    public void run() {
                    mLeDeviceListAdapter.addDevice(device);
                    Log.e(TAG, "devices is " + device.getAddress());
                    mLeDeviceListAdapter.notifyDataSetChanged();
                    //    }
                    //});
                }

                @Override
                public void onScanFailed(BLEException bleException) {
                    Log.e(TAG, bleException.getMessage());
                }
            });
            mScanning = true;
        } else {
            mScanning = false;
            bleManager.stopFindAllDevices();
        }
        invalidateOptionsMenu();
    }

    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter(Context context) {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = LayoutInflater.from(context);
        }

        public void addDevice(BluetoothDevice device) {
            if (!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(i);

            if(!isrequestPermissions){
                if (ActivityCompat.checkSelfPermission(SelectDeviceActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        SelectDeviceActivity.this.requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT},0);
                    }
                }
            }

            String deviceName = device.getName();
            Log.e(TAG,"device.getName()"+deviceName);
            if (deviceName != null && deviceName.length() > 0) {
                viewHolder.deviceName.setText(deviceName);
            } else {
                viewHolder.deviceName.setText(R.string.unknown_device);
            }
            viewHolder.deviceAddress.setText(device.getAddress());
            return view;
        }
    }

    class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }


    private int getStatusBarHeight() {
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            return getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            Log.i(TAG, "get status bar height fail");
            e1.printStackTrace();
            return 75;
        }

    }
}
