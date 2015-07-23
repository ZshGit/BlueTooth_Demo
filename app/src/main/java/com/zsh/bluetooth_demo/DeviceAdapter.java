package com.zsh.bluetooth_demo;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jash.bluetooth.R;

import java.util.Collection;
import java.util.List;

/**
 * Created by jash
 * Date: 15-7-18
 * Time: 上午10:14
 */
public class DeviceAdapter extends BaseAdapter{
    private Context context;
    private List<BluetoothDevice> list;

    public DeviceAdapter(Context context, List<BluetoothDevice> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item, parent, false);
            convertView.setTag(new ViewHolder(convertView));
        }
        BluetoothDevice device = list.get(position);
        ViewHolder holder = (ViewHolder) convertView.getTag();
        if (device.getBondState() == BluetoothDevice.BOND_BONDED){
            holder.name.setTextColor(Color.BLACK);
        } else {
            holder.name.setTextColor(Color.RED);
        }
        holder.name.setText(device.getName() + "");
        holder.address.setText(device.getAddress());
        return convertView;
    }

    public void add(BluetoothDevice device){
        if (!list.contains(device)){
            list.add(device);
            notifyDataSetChanged();
        }
    }

    public void addAll(Collection<? extends BluetoothDevice> collection){
        list.addAll(collection);
        notifyDataSetChanged();
    }
    public static class ViewHolder{
        private TextView name;
        private TextView address;

        public ViewHolder(View itemView) {
            name = ((TextView) itemView.findViewById(R.id.name));
            address = ((TextView) itemView.findViewById(R.id.address));
        }
    }
}
