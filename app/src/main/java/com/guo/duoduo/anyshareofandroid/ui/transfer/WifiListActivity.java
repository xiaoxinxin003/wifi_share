package com.guo.duoduo.anyshareofandroid.ui.transfer;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.guo.duoduo.anyshareofandroid.R;
import com.guo.duoduo.anyshareofandroid.manager.CustomWifiManager;
import com.guo.duoduo.anyshareofandroid.ui.common.BaseActivity;
import com.guo.duoduo.anyshareofandroid.ui.transfer.view.NearbyWifiAdapter;

import java.util.List;

/**
 * Created by zeus on 16-12-7.
 */

public class WifiListActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    private WifiManager mWifiManager;
    private ListView mGuysList;
    private NearbyWifiAdapter mNearbyWifiAdapter;
    private List<ScanResult> mNearbyWifiList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_list);
        init();

        initView();
    }

    private void initView() {
        mGuysList = (ListView)findViewById(R.id.activity_guys_listview);
        if (mNearbyWifiList != null && mNearbyWifiList.size() > 0){
            mNearbyWifiAdapter = new NearbyWifiAdapter(this, mNearbyWifiList);
            mGuysList.setAdapter(mNearbyWifiAdapter);
        }else {
            mGuysList.setVisibility(View.GONE);
        }
        mGuysList.setOnItemClickListener(this);
    }

    private void initNearbyWifiData() {
        List<ScanResult> results = mWifiManager.getScanResults();
        for (ScanResult result : results){
            if (result.SSID.contains("zeus")){
                mNearbyWifiList.add(result);
            }
        }
    }

    private void init() {
        mWifiManager = (WifiManager)this.getSystemService(Context.WIFI_SERVICE);
        CustomWifiManager.getInstance().setWifiApEnabled(mWifiManager, false);
        mWifiManager.setWifiEnabled(true);
//        mWifiManager.startScan();
        initNearbyWifiData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        connectToClickedWifi();
    }

    private void connectToClickedWifi() {


    }
}
