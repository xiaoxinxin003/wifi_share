package com.guo.duoduo.anyshareofandroid.ui.transfer.view;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.guo.duoduo.anyshareofandroid.R;

import java.util.List;

/**
 * Created by focus on 16-12-7.
 */

public class NearbyWifiAdapter extends BaseAdapter {

    private Context mCcontext;
    private LayoutInflater layoutInflater;
    private List<ScanResult> mList;

    public NearbyWifiAdapter(Context context, List<ScanResult> list) {
        this.mCcontext = context;
        this.mList = list;
        layoutInflater = layoutInflater.from(mCcontext);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
       ViewHolder holder;

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.view_file_transfer_item, null);
            holder = new ViewHolder();
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.wifiName.setText(mList.get(position).SSID);
        return convertView;
    }

    private static class ViewHolder {
        public TextView wifiName;
    }
}
