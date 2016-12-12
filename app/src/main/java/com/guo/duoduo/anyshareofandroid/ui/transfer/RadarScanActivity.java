package com.guo.duoduo.anyshareofandroid.ui.transfer;


import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.guo.duoduo.anyshareofandroid.MyApplication;
import com.guo.duoduo.anyshareofandroid.R;
import com.guo.duoduo.anyshareofandroid.sdk.accesspoint.AccessPointManager;
import com.guo.duoduo.anyshareofandroid.sdk.accesspoint.ClientScanResult;
import com.guo.duoduo.anyshareofandroid.sdk.cache.Cache;
import com.guo.duoduo.anyshareofandroid.ui.common.BaseActivity;
import com.guo.duoduo.anyshareofandroid.ui.transfer.view.FileTransferAdapter;
import com.guo.duoduo.anyshareofandroid.utils.NetworkUtils;
import com.guo.duoduo.anyshareofandroid.utils.ToastUtils;
import com.guo.duoduo.p2pmanager.p2pconstant.P2PConstant;
import com.guo.duoduo.p2pmanager.p2pcore.P2PManager;
import com.guo.duoduo.p2pmanager.p2pentity.P2PFileInfo;
import com.guo.duoduo.p2pmanager.p2pentity.P2PNeighbor;
import com.guo.duoduo.p2pmanager.p2pinterface.Melon_Callback;
import com.guo.duoduo.p2pmanager.p2pinterface.SendFile_Callback;
import com.guo.duoduo.randomtextview.RandomTextView;
import com.guo.duoduo.rippleoutview.RippleView;


/**
 * Created by zeus on 2016/9/12.
 */
public class RadarScanActivity extends BaseActivity
{
    private static final String tag = RadarScanActivity.class.getSimpleName();

    private RandomTextView randomTextView;

    private WifiReceiver mWifiReceiver;

    private AccessPointManager mWifiApManager;

    private P2PManager p2PManager;
    private String alias;
    private RelativeLayout scanRelative;
    private TextView mClientDevices;
    private RelativeLayout scanRocket;
    private ListView fileSendListView;
    private List<P2PNeighbor> neighbors = new ArrayList<>();
    private P2PNeighbor curNeighbor;
    private FileTransferAdapter transferAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radarscan);

        mWifiReceiver = new WifiReceiver();

        mWifiApManager = new AccessPointManager(MyApplication.getInstance());

        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_radar_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        if (intent != null)
            alias = intent.getStringExtra("name");
        else
            alias = Build.DEVICE;

        registReceiver();

        TextView radar_scan_name = (TextView) findViewById(R.id.activity_radar_scan_name);
        radar_scan_name.setText(alias);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.activity_radar_scan_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view,
                    getResources().getString(R.string.file_transfering_exit),
                    Snackbar.LENGTH_LONG)
                        .setAction(getResources().getString(R.string.ok),
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    finish();
                                }
                            }).show();
            }
        });

        scanRelative = (RelativeLayout) findViewById(R.id.activity_radar_scan_relative);
        scanRelative.setVisibility(View.VISIBLE);
        scanRocket = (RelativeLayout) findViewById(R.id.activity_radar_rocket_layout);
        scanRocket.setVisibility(View.GONE);

        mClientDevices = (TextView)findViewById(R.id.tv_client_devices);
        mClientDevices.setVisibility(View.GONE);

        fileSendListView = (ListView) findViewById(R.id.activity_radar_scan_listview);
        fileSendListView.setVisibility(View.GONE);

        randomTextView = (RandomTextView) findViewById(R.id.activity_radar_rand_textview);
        randomTextView.setMode(RippleView.MODE_OUT);
        randomTextView.setOnRippleViewClickListener(new RandomTextView.OnRippleViewClickListener() {
                    @Override
                    public void onRippleViewClicked(View view) {
                        scanRelative.setVisibility(View.GONE);
                        scanRocket.setVisibility(View.VISIBLE);
                        //给对方发送文件传输的请求
                        String alias = ((RippleView) (view)).getText().toString();
                        for (int i = 0; i < neighbors.size(); i++) {
                            if (neighbors.get(i).alias.equals(alias)) {
                                curNeighbor = neighbors.get(i);
                                sendFile(curNeighbor);
                                break;
                            }
                        }
                    }
                });

        new Thread(scanTask).start();

        initP2P();
    }

    Runnable scanTask = new Runnable() {
        @Override
        public void run() {
            ArrayList<ClientScanResult> results = mWifiApManager.getClientList(true);
            String devices;
            if (results != null){
                devices = String.valueOf(results.size());
            }else{
                devices = "null";
            }
            Message msg =  new Message();
            Bundle data = new Bundle();
            data.putString("devices", devices);
            msg.setData(data);
            handler.sendMessage(msg);
        }
    };

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String counts = data.getString("devices");
            initClientDevicesCount(counts);
        }
    };

    private void initClientDevicesCount(String clients) {
        if (!TextUtils.equals(clients, "null")){
            mClientDevices.setText(clients);
            mClientDevices.setVisibility(View.VISIBLE);
        }
    }

    private void registReceiver() {
        // 注册Receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(mWifiReceiver, filter);
    }

    /* 监听热点变化 */
    private final class WifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
           switch (intent.getAction()){
               case WifiManager.WIFI_STATE_CHANGED_ACTION:
                   initP2P();
                   break;
               case WifiManager.NETWORK_STATE_CHANGED_ACTION:
                   break;
           }
        }
    }

    private void sendFile(P2PNeighbor neighbor) {
        P2PNeighbor[] neighbors = new P2PNeighbor[]{neighbor};
        P2PFileInfo[] fileArray = new P2PFileInfo[Cache.selectedList.size()];
        for (int i = 0; i < Cache.selectedList.size(); i++) {
            fileArray[i] = Cache.selectedList.get(i);
        }

        p2PManager.sendFile(neighbors, fileArray, new SendFile_Callback() {
            @Override
            public void BeforeSending() {
                Animation animation = AnimationUtils.loadAnimation(
                    getApplicationContext(), R.anim.out_to_up);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        scanRocket.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                scanRocket.startAnimation(animation);

                fileSendListView.setVisibility(View.VISIBLE);

                transferAdapter = new FileTransferAdapter(getApplicationContext());
                fileSendListView.setAdapter(transferAdapter);

                ActionBar actionBar = getSupportActionBar();
                if (actionBar != null)
                    actionBar.setTitle(getString(R.string.file_sending));
            }

            @Override
            public void OnSending(P2PFileInfo file, P2PNeighbor dest) {
                Log.d(tag, "onSending file percent = " + file.percent);

                int index = -1;
                if (Cache.selectedList.contains(file)) {
                    index = Cache.selectedList.indexOf(file);
                }
                if (index != -1) {
                    P2PFileInfo fileInfo = Cache.selectedList.get(index);
                    fileInfo.percent = file.percent;
                    transferAdapter.notifyDataSetChanged();
                }
                else {
                    Log.d(tag, "onSending index error");
                }
            }

            @Override
            public void AfterSending(P2PNeighbor dest) {
                ToastUtils.showTextToast(getApplicationContext(),
                    getString(R.string.file_send_complete));
                finish();
            }

            @Override
            public void AfterAllSending() {
                ToastUtils.showTextToast(getApplicationContext(),
                    getString(R.string.file_send_complete));
                finish();
            }

            @Override
            public void AbortSending(int error, P2PNeighbor dest) {
                String format = getString(R.string.send_abort_self);
                String toastMsg = "";
                switch (error) {
                    case P2PConstant.CommandNum.RECEIVE_ABORT_SELF :
                        toastMsg = String.format(format, dest.alias);
                        break;
                }

                ToastUtils.showTextToast(getApplicationContext(), toastMsg);

                finish();
            }
        });
    }

    private void initP2P() {
        p2PManager = new P2PManager(getApplicationContext());
        P2PNeighbor melonInfo = new P2PNeighbor();
        melonInfo.alias = alias;
        String ip = null;
        try {
            ip = AccessPointManager.getLocalIpAddress();
        }
        catch (UnknownHostException e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(ip))
            ip = NetworkUtils.getLocalIp(getApplicationContext());
        melonInfo.ip = ip;

        p2PManager.start(melonInfo, new Melon_Callback() {
            @Override
            public void Melon_Found(P2PNeighbor melon) {
                if (melon != null) {
                    if (!neighbors.contains(melon)){
                        neighbors.add(melon);
                    }
                    new Thread(scanTask).start();
                    randomTextView.addKeyWord(melon.alias);
                    randomTextView.show();
                }
            }

            @Override
            public void Melon_Removed(P2PNeighbor melon) {
                if (melon != null) {
                    neighbors.remove(melon);
                    randomTextView.removeKeyWord(melon.alias);
                    randomTextView.show();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (p2PManager != null) {
            if (curNeighbor != null)
                p2PManager.cancelSend(curNeighbor);
            p2PManager.stop();
        }
        for (int i = 0; i < Cache.selectedList.size(); i++) {
            Cache.selectedList.get(i).percent = 0;
        }
    }

}
