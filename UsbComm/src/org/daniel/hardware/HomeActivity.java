package org.daniel.hardware;

import android.app.Activity;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class HomeActivity extends Activity {
    private ListView mListView;
    private ContentAdapter mAdapter;
    private List<String> mDeviceList = new ArrayList<String>();
    private HashMap<String, UsbDevice> mDeviceMap;
    private UsbManager mUsbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mListView = (ListView) findViewById(R.id.list);
        mAdapter = new ContentAdapter();
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(mAdapter);
        mUsbManager = (UsbManager) getSystemService(USB_SERVICE);
        findUsb();
    }

    private void findUsb() {
        mDeviceList.clear();
        mDeviceMap = mUsbManager.getDeviceList();
        for (String name : mDeviceMap.keySet()) {
            mDeviceList.add(name + ", " + mDeviceMap.get(name).getDeviceName());
        }
        mAdapter.notifyDataSetChanged();
    }


    private class ContentAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {

        @Override
        public int getCount() {
            return mDeviceList.size();
        }

        @Override
        public String getItem(int position) {
            return mDeviceList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).hashCode();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView tv = (TextView) convertView;
            if (tv == null) {
                tv = new TextView(getApplicationContext());
                tv.setTextColor(0xff000000);
                tv.setTextSize(20);
                tv.setPadding(10, 20, 10, 20);
            }

            tv.setText(getItem(position));

            return tv;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(getApplicationContext(), CommunicationActivity.class);
            intent.putExtra(CommunicationActivity.KEY_DEVICE_NAME, getItem(position));
            startActivity(intent);
        }
    }
}
