package org.daniel.andruino.app;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import org.daniel.andruino.app.utils.TU;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeActivity extends Activity {
    private List<ActivityInfo> mActivityList = new ArrayList<ActivityInfo>();
    private ListView mListView;
    private HomeAdapter mAdapter;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mListView = (ListView) findViewById(R.id.list);
        mAdapter = new HomeAdapter();
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(mAdapter);
        loadActivities();
    }

    private void loadActivities() {
        PackageInfo packageInfo = null;
        try {

            packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageInfo != null) {
            mActivityList.addAll(Arrays.asList(packageInfo.activities));
        }
        int myPos;
        for (myPos = 0; myPos < mActivityList.size(); myPos++) {
            ActivityInfo info = mActivityList.get(myPos);
            if (info.name.equals(getComponentName().getClassName())) {
                break;
            }
        }
        mActivityList.remove(myPos);
    }

    private class HomeAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {

        @Override
        public int getCount() {
            return mActivityList.size();
        }

        @Override
        public ActivityInfo getItem(int i) {
            return mActivityList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return getItem(i).name.hashCode();
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            TextView tv = (TextView) view;
            if (tv == null) {
                tv = new TextView(getApplicationContext());
                Resources res = getResources();
                int padding = res.getDimensionPixelSize(R.dimen.home_padding);
                tv.setPadding(padding, padding, padding, padding);
                tv.setTextSize(res.getDimensionPixelSize(R.dimen.home_text));
                tv.setTextColor(res.getColor(R.color.home_text));
            }
            String name = getItem(i).name;
            name = name.substring(name.lastIndexOf('.'));
            tv.setText(name);

            return tv;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ActivityInfo info = getItem(position);
            TU.j(position, info.packageName, info.name);
            Intent intent = new Intent();
            intent.setClassName(getApplicationContext(), info.name);
            startActivity(intent);
        }
    }
}
