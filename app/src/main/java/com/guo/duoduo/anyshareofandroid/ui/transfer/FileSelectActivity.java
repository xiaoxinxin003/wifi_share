package com.guo.duoduo.anyshareofandroid.ui.transfer;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;

import com.guo.duoduo.anyshareofandroid.MyApplication;
import com.guo.duoduo.anyshareofandroid.R;
import com.guo.duoduo.anyshareofandroid.constant.Constant;
import com.guo.duoduo.anyshareofandroid.manager.CustomWifiManager;
import com.guo.duoduo.anyshareofandroid.sdk.accesspoint.AccessPointManager;
import com.guo.duoduo.anyshareofandroid.sdk.cache.Cache;
import com.guo.duoduo.anyshareofandroid.ui.common.BaseActivity;
import com.guo.duoduo.anyshareofandroid.ui.transfer.fragment.AppFragment;
import com.guo.duoduo.anyshareofandroid.ui.common.FragmentAdapter;
import com.guo.duoduo.anyshareofandroid.ui.transfer.fragment.OnSelectItemClickListener;
import com.guo.duoduo.anyshareofandroid.ui.transfer.fragment.PictureFragment;
import com.guo.duoduo.anyshareofandroid.ui.view.CommonProgressDialog;
import com.guo.duoduo.anyshareofandroid.utils.NetworkUtils;
import com.guo.duoduo.anyshareofandroid.utils.ToastUtils;


/**
 * Created by zeus.
 */
public class FileSelectActivity extends BaseActivity implements OnSelectItemClickListener, AccessPointManager.OnWifiApStateChangeListener {
    private String userName = Build.DEVICE;
    private Toolbar toolbar;
    private String title;

    private WifiManager mWifiManager;
    private AccessPointManager mWifiApManager = null;
    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_file);

        mWifiManager = (WifiManager)this.getSystemService(Context.WIFI_SERVICE);
        mWifiApManager = new AccessPointManager(MyApplication.getInstance());

        toolbar = (Toolbar) findViewById(R.id.activity_file_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        title = toolbar.getTitle().toString();
        if (TextUtils.isEmpty(title))
            title = getString(R.string.file_select);

        Intent intent = getIntent();
        if (intent != null) {
            userName = intent.getStringExtra("name");
        }
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.activity_file_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if (Cache.selectedList.size() > 0)
                    onSendFileBtnClick();
                else
                    ToastUtils.showTextToast(getApplicationContext(),
                        getString(R.string.please_select_file));
            }
        });

        List<String> titles = new ArrayList<>();
        titles.add(getString(R.string.app));
        titles.add(getString(R.string.picture));

        TabLayout tabLayout = (TabLayout) findViewById(R.id.activity_file_tabLayout);
        tabLayout.addTab(tabLayout.newTab().setText(titles.get(0)));
        tabLayout.addTab(tabLayout.newTab().setText(titles.get(1)));

        ViewPager viewPager = (ViewPager) findViewById(R.id.activity_file_viewpager);
        List<android.support.v4.app.Fragment> fragments = new ArrayList<>();
        fragments.add(new AppFragment());
        fragments.add(new PictureFragment());

        FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager(),
            fragments, titles);
        viewPager.setAdapter(adapter);

        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabsFromPagerAdapter(adapter);
    }

    private void onSendFileBtnClick() {
        //TODO 是否已经在可用热点中，是的话扫描就直接显示、发送，否则创建热点。
        if (NetworkUtils.isWifiConnected(this)){
            if (mWifiManager.getConnectionInfo().getSSID().contains("zeus")){
                //直接扫描就可以了
            }else {
                mWifiManager.setWifiEnabled(false);
                intWifiHotSpot();
            }
        }else {
            if (mWifiApManager.isWifiApEnabled()){
                WifiConfiguration apConfig = mWifiApManager.getWifiApConfiguration();
                String ssid = apConfig.SSID;
                if (ssid != null && ssid.contains("zeus")){

                }
            }else {
                intWifiHotSpot();
            }

        }
        startActivity(new Intent(FileSelectActivity.this,
                RadarScanActivity.class).putExtra("name", userName));
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Cache.selectedList.clear();
    }

    @Override
    public void onItemClicked(int type) {
        updateTitle();
    }

    private void updateTitle() {
        toolbar.setTitle(title + " / " + Cache.selectedList.size());
    }

    private void intWifiHotSpot() {

        mWifiApManager.setWifiApStateChangeListener(this);
        createAccessPoint();
    }

    private void createAccessPoint() {
        mWifiApManager.createWifiApSSID(Constant.WIFI_HOT_SPOT_SSID_PREFIX
                + android.os.Build.MODEL + "-" + random.nextInt(1000));

        if (!mWifiApManager.startWifiAp()) {
            ToastUtils.showTextToast(MyApplication.getInstance(),
                    getString(R.string.wifi_hotspot_fail));
            onBackPressed();
        }
    }

    @Override
    public void onWifiStateChanged(int state) {
        if (state == AccessPointManager.WIFI_AP_STATE_ENABLED) {
            onBuildWifiApSuccess();
        }
        else if (AccessPointManager.WIFI_AP_STATE_FAILED == state) {
            onBuildWifiApFailed();
        }
    }

    private void onBuildWifiApFailed() {
        ToastUtils.showTextToast(MyApplication.getInstance(),
                getString(R.string.wifi_hotspot_fail));


        onBackPressed();
    }

    private void onBuildWifiApSuccess() {

//        wifiName.setText(String.format(getString(R.string.send_connect_to),
//                mWifiApManager.getWifiApSSID()));
    }
}
