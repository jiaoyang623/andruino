package org.daniel.hardware;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.*;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author jiaoyang<br>
 *         email: jiaoyang@360.cn
 * @version 1.0
 * @date Apr 23 2015 1:32 PM
 */
public class CommunicationActivity extends Activity {
    public static final String KEY_DEVICE_NAME = "deviceName";
    private static final String ACTION_USB_PERMISSION = "org.daniel.usbcomm.app.ACTION_USB_PERMISSION";
    private String mDeviceName = null;
    private UsbManager mUsbManager;
    private UsbDevice mDevice;
    private boolean mIsRunning = true;
    private ListView mListView;
    private CommunicationAdapter mAdapter = new CommunicationAdapter();
    private List<String> mContentList = new ArrayList<String>();
    private boolean mPaused = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsRunning = true;
        setContentView(R.layout.activity_communication);
        mListView = (ListView) findViewById(R.id.list);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(mAdapter);

        Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(KEY_DEVICE_NAME);
        logi("", "device name: " + mDeviceName);
        if (mDeviceName == null) {
            return;
        }
        mUsbManager = (UsbManager) getSystemService(USB_SERVICE);
        Map<String, UsbDevice> deviceMap = mUsbManager.getDeviceList();
        for (String name : deviceMap.keySet()) {
            logi("jy", "for: " + name);
            mDevice = deviceMap.get(name);
        }
        if (mDevice == null) {
            logi("", "device is null");
            return;
        }

        requestPermission();
    }

    @Override
    protected void onDestroy() {
        mIsRunning = false;
        unregisterReceiver(mUsbReceiver);
        super.onDestroy();
    }

    private void requestPermission() {
        PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        //完成授权
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        //设备删除
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);

        registerReceiver(mUsbReceiver, filter);
        mUsbManager.requestPermission(mDevice, permissionIntent);
    }

    private static int TIMEOUT = 10;
    private boolean forceClaim = true;
    private boolean isReading = false;

    public void sendData(final String message) {
        logi("jy", "sending message: " + message);
        logi("jy", "interfaceCount: " + mDevice.getInterfaceCount());
        UsbInterface usbInterface = mDevice.getInterface(1);
        logi("jy", usbInterface.toString());

        final UsbDeviceConnection connection = mUsbManager.openDevice(mDevice);

        // Arduino Serial usb Conv
//        connection.controlTransfer(0x21, 34, 0, 0, null, 0, 0);
//        connection.controlTransfer(0x21, 32, 0, 0, new byte[]{(byte) 0x80,
//                0x25, 0x00, 0x00, 0x00, 0x00, 0x08}, 7, 0);


        UsbEndpoint inEp = null, outEp = null;
        for (int i = 0; i < usbInterface.getEndpointCount(); i++) {
            if (usbInterface.getEndpoint(i).getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                if (usbInterface.getEndpoint(i).getDirection() == UsbConstants.USB_DIR_IN)
                    inEp = usbInterface.getEndpoint(i);
                else
                    outEp = usbInterface.getEndpoint(i);
            }
        }
        // read
        final UsbEndpoint readEndpoint = inEp;
        logi("jy", "direction: " + readEndpoint.getDirection() + ", in = " + UsbConstants.USB_DIR_IN);
        // write
        final UsbEndpoint writeEndpoint = outEp;

        connection.claimInterface(usbInterface, forceClaim);

        Thread readThread = new Thread(new Runnable() {
            private byte[] bytes = new byte[256];
            private int allCount = 0;

            @Override
            public void run() {
                StringBuilder sb = new StringBuilder();
                while (mIsRunning) {

                    int count;
                    synchronized (connection) {
                        count = connection.bulkTransfer(readEndpoint, bytes, bytes.length, TIMEOUT);
                    }
                    if (count > 0) {
                        isReading = true;
                        String text = new String(bytes, 0, count);
                        sb.append(text);
                        if (text.contains("\n")) {
                            logi("jy", "read[" + allCount++ + "]: {" + sb.toString().replace('\n', ' ') + "}");
                            sb.setLength(0);
                            isReading = false;
                        }
                    } else {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }
            }
        });
        readThread.start();

        Thread writeThread = new Thread(new Runnable() {
            private byte[] bytes = new byte[64];
            private int allCount = 0;

            @Override
            public void run() {
                while (mIsRunning) {
                    if (mPaused) {
                        try {
                            Thread.sleep(100);
                            continue;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    StringBuilder builder = new StringBuilder();
//                    for (int i = 0; i < 100; i++) {
                    builder.append("1234567890");
//                    }
                    builder.append('\n');
                    bytes = builder.toString().getBytes();
                    if (!isReading) {
                        synchronized (connection) {
                            logi("", "write[" + allCount++ + "]");
                            connection.bulkTransfer(writeEndpoint, bytes, bytes.length, TIMEOUT);
                        }
                    }
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        });
        writeThread.start();
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            //TODO call method to set up device communication
                            mDevice = device;
                            logi("jy", "UsbManager.EXTRA_PERMISSION_GRANTED");
                            sendData("hi, arduino");
                        }
                    } else {
                        logi("jy", "permission denied for device " + device);
                    }
                }
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null) {
                    //TODO call your method that cleans up and closes communication with the device
                    logi("jy", "UsbManager.ACTION_USB_DEVICE_DETACHED");
                    mDevice = null;
                    finish();
                }
            }
        }
    };

    private void logi(final String tag, final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mContentList.add(message);
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    private class CommunicationAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {

        @Override
        public int getCount() {
            return mContentList.size();
        }

        @Override
        public String getItem(int i) {
            return mContentList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return mContentList.get(i).hashCode();
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            TextView tv = (TextView) view;
            if (tv == null) {
                tv = new TextView(getApplicationContext());
                tv.setTextSize(20);
                tv.setPadding(10, 10, 10, 10);
                tv.setTextColor(0xff000000);
            }

            tv.setText(getItem(i));

            return tv;
        }

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            mPaused = !mPaused;
        }
    }
}
