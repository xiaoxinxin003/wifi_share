package com.guo.duoduo.anyshareofandroid.ui.transfer.view;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.guo.duoduo.anyshareofandroid.MyApplication;
import com.guo.duoduo.anyshareofandroid.R;
import com.guo.duoduo.anyshareofandroid.sdk.accesspoint.AccessPointManager;
import com.guo.duoduo.anyshareofandroid.sdk.cache.Cache;
import com.guo.duoduo.anyshareofandroid.ui.common.BaseActivity;
import com.guo.duoduo.anyshareofandroid.utils.ToastUtils;
import com.guo.duoduo.randomtextview.RandomTextView;
import com.guo.duoduo.rippleoutlayout.RippleOutLayout;
import com.guo.duoduo.rippleoutview.RippleView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 扫描附近热点并展示
 */
public class ScanApActivity extends BaseActivity implements AccessPointManager.OnWifiApStateChangeListener {

    private static final String tag = ScanApActivity.class.getSimpleName();

    private WifiManager mWifiManager;
    private WifiReceiver mWifiReceiver;

    private List<ScanResult> mWifiList;
    private List<String> mPassableHotsPot;

    private AccessPointManager mWifiApManager = null;
    private Random random = new Random();
    private TextView wifiName;

    private RippleOutLayout rippleOutLayout;
    private RandomTextView randomTextView;

    private String alias;

    private RelativeLayout receiveLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_ap);
        mWifiReceiver = new WifiReceiver();mWifiManager = (WifiManager)this.getSystemService(Context.WIFI_SERVICE);
        mWifiApManager = new AccessPointManager(MyApplication.getInstance());

        WifiInfo info = mWifiManager.getConnectionInfo();
        if (info != null && info.getSSID().contains("zeus")){
        }else {
            if (mWifiManager.isWifiEnabled()){
                mWifiManager.setWifiEnabled(false);
                mWifiManager.setWifiEnabled(true);
            }else {
                mWifiManager.setWifiEnabled(true);
            }
        }
        registReceiver();

        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_scan_ap_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.activity_scan_ap_fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Snackbar.make(view,
                    getResources().getString(R.string.file_transfering_exit),
                    Snackbar.LENGTH_LONG)
                        .setAction(getResources().getString(R.string.ok),
                            new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View view)
                                {
                                    finish();
                                }
                            }).show();
            }
        });
        Intent intent = getIntent();
        if (intent != null)
        {
            alias = intent.getStringExtra("name");
        } else{
            alias = Build.DEVICE;
        }

        initView();

        if (mWifiManager.isWifiEnabled()){
            mWifiManager.startScan();
        }

    }

    private void initView() {
        ((TextView)findViewById(R.id.activity_scan_ap_my_name)).setText(alias);

        wifiName = (TextView) findViewById(R.id.activity_receive_radar_wifi);

        receiveLayout = (RelativeLayout) findViewById(R.id.activity_scan_ap_layout);
        receiveLayout.setVisibility(View.VISIBLE);

        rippleOutLayout = (RippleOutLayout) findViewById(R.id.activity_scan_ap_ripple_layout);
        rippleOutLayout.startRippleAnimation();

        randomTextView = (RandomTextView) findViewById(R.id.activity_scan_ap_rand_textview);
        randomTextView.setMode(RippleView.MODE_IN);
        randomTextView.setOnRippleViewClickListener(new RandomTextView.OnRippleViewClickListener()
                {
                    @Override
                    public void onRippleViewClicked(View view)
                    {
                        //点击了热点，去连接，连接成功跳转到哪里？
                        if (mPassableHotsPot == null){
                            return;
                        }
                        int position = randomTextView.getPosition();
                        connect2Ap(mPassableHotsPot.get(position));
//                        ToastUtils.showTextToast(getApplicationContext(), "连接到：" + mPassableHotsPot.get(position));
                    }
                });
    }

    private void registReceiver() {
        // 注册Receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(mWifiReceiver, filter);
    }

    /* 监听热点变化 */
    private final class WifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION:
                    if (mWifiList != null){
                        mWifiList.clear();
                    }
                    mWifiList = mWifiManager.getScanResults();
                    onReceiveNewNetworks(mWifiList);
                    break;
                default:
                    break;
            }

        }
    }

    /*当搜索到新的wifi热点时判断该热点是否符合规格*/
    public void onReceiveNewNetworks(List<ScanResult> wifiList){
        mPassableHotsPot =new ArrayList<String>();
        int postion = 0;
        for( int i = 0; i < wifiList.size(); i++){
            ScanResult result = wifiList.get(i);
            System.out.println(result.SSID);
            if((result.SSID).contains("zeus")){
                mPassableHotsPot.add(result.SSID);
                randomTextView.addKeyWord(result.SSID);
                randomTextView.setPosition(postion++);
                randomTextView.show();
            }
        }
        synchronized (this) {
            showNearbyWifi();
        }
    }

    private void showNearbyWifi() {
        //random textview
        if (mPassableHotsPot == null || mPassableHotsPot.size() == 0){
            //附近没有任何WiFi可用（符合的不符合的都没有）
            rippleOutLayout.setVisibility(View.GONE);
            receiveLayout.clearAnimation();
            mPassableHotsPot = null;
            mWifiList = null;
            findViewById(R.id.activity_scan_ap_none).setVisibility(View.VISIBLE);
            return;
        }

    }

    /*连接到热点*/
    public void connectToHotpot(){
        WifiInfo info = mWifiManager.getConnectionInfo();
        if (info != null && info.getSSID().contains("zeus")){
            return;
        }
        List<ScanResult> results = mWifiManager.getScanResults();
        for (ScanResult result : results){
            if (result.SSID.contains("zeus")){
                WifiConfiguration wifiConfig = new WifiConfiguration();
                wifiConfig.SSID = result.SSID;
                wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                wifiConfig.wepTxKeyIndex = 0;
                int wcgID = mWifiManager.addNetwork(wifiConfig);
                mWifiManager.enableNetwork(wcgID, true);

                break;
            }
        }

    }

    public void connect2Ap(String ssid){
        WifiInfo info = mWifiManager.getConnectionInfo();
        if (info != null && TextUtils.equals(info.getSSID(), ssid)){
            ToastUtils.showTextToast(getApplicationContext(), "已连接");
            return;
        }
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = ssid;
        wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        wifiConfig.wepTxKeyIndex = 0;
        int wcgID = mWifiManager.addNetwork(wifiConfig);
        mWifiManager.enableNetwork(wcgID, true);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        //stop wifi hot spot
        closeAccessPoint();
        if (rippleOutLayout != null)
            rippleOutLayout.stopRippleAnimation();

        Cache.selectedList.clear();

        if (mWifiReceiver != null){
            unregisterReceiver(mWifiReceiver);
        }

        mWifiList = null;
        mPassableHotsPot = null;

    }

    private void closeAccessPoint()
    {
        try
        {
            if (mWifiApManager != null && mWifiApManager.isWifiApEnabled())
            {
                mWifiApManager.stopWifiAp(false);
                mWifiApManager.destroy(this);
            }
        }
        catch (IllegalArgumentException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onWifiStateChanged(int state)
    {
        if (state == AccessPointManager.WIFI_AP_STATE_ENABLED)
        {
            onBuildWifiApSuccess();
        }
        else if (AccessPointManager.WIFI_AP_STATE_FAILED == state)
        {
            onBuildWifiApFailed();
        }
    }

    private void onBuildWifiApFailed()
    {
        ToastUtils.showTextToast(MyApplication.getInstance(),
            getString(R.string.wifi_hotspot_fail));

        onBackPressed();
    }

    private void onBuildWifiApSuccess()
    {
        wifiName.setText(String.format(getString(R.string.send_connect_to),
            mWifiApManager.getWifiApSSID()));
    }

}
