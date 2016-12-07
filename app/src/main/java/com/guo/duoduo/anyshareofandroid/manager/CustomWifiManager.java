package com.guo.duoduo.anyshareofandroid.manager;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import java.lang.reflect.Method;

/**
 * Created by sw_01 on 2016/10/14.
 */

public class CustomWifiManager {

    public CustomWifiManager() {


    }

    private static CustomWifiManager sInstance;

    public static CustomWifiManager getInstance() {
        if (null == sInstance) {
            synchronized (CustomWifiManager.class) {
                if (null == sInstance) {
                    sInstance = new CustomWifiManager();
                }
            }
        }
        return sInstance;
    }

    public void createWifiAp(){

    }


    // wifi热点开关
    public boolean setWifiApEnabled(WifiManager wifiManager, boolean enabled) {
        wifiManager.setWifiEnabled(false);

//        if (enabled) { // disable WiFi in any case
//            //wifi和热点不能同时打开，所以打开热点的时候需要关闭wifi
//            wifiManager.setWifiEnabled(false);
//        }
        try {
            //热点的配置类
            WifiConfiguration apConfig = new WifiConfiguration();
            //配置热点的名称(可以在名字后面加点随机数什么的)
            apConfig.SSID = "闪电YRCCONNECTION";
            //配置热点的密码
            apConfig.preSharedKey="12122112";
            //通过反射调用设置热点
            Method method = wifiManager.getClass().getMethod(
                    "setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
            //返回热点打开状态
            return (Boolean) method.invoke(wifiManager, apConfig, enabled);
        } catch (Exception e) {
            return false;
        }
    }















}
