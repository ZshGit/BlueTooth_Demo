package com.zsh.bluetooth_demo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.jash.bluetooth.R;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private BluetoothAdapter defaultAdapter;
    private DeviceAdapter adapter;
    public static final String uuid = "2700abef-3062-44b9-b1dc-28910cf4940f";
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    BluetoothDevice device = msg.getData().getParcelable(BluetoothDevice.EXTRA_DEVICE);
                    adapter.add(device);
                    break;
                case 1:
                    Toast.makeText(MainActivity.this, ((String) msg.obj), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    private MyReceiver receiver;
    private BluetoothServerSocket serverSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView list = (ListView) findViewById(R.id.list);

        adapter = new DeviceAdapter(this, new ArrayList<BluetoothDevice>());
        list.setAdapter(adapter);
        list.setOnItemClickListener(this);
        //获取默认的蓝牙适配器
        defaultAdapter = BluetoothAdapter.getDefaultAdapter();
        if (defaultAdapter == null) {
            Toast.makeText(this, "本设备没有蓝牙适配器", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            if (!defaultAdapter.isEnabled()) {
                Toast.makeText(this, "蓝牙适配器没有开启", Toast.LENGTH_SHORT).show();
                //静默开启
//                defaultAdapter.enable();
                //询问启动
                startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 0);
            } else {
                startScan();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK){
            Toast.makeText(this, "开启成功", Toast.LENGTH_SHORT).show();
            startScan();
        } else {
            Toast.makeText(this, "开启失败", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    public void startScan(){
        //开始扫描，开启可见
        defaultAdapter.startDiscovery();
        //获得已绑定的蓝牙设备
        Set<BluetoothDevice> devices = defaultAdapter.getBondedDevices();
        adapter.addAll(devices);
        receiver = new MyReceiver(handler);
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
        UUID uuid1 = UUID.fromString(uuid);
        try {
            serverSocket = defaultAdapter.listenUsingRfcommWithServiceRecord(null, uuid1);
            new Thread(){
                @Override
                public void run() {
                    BluetoothSocket socket;
                    try {
                        while ((socket = serverSocket.accept()) != null){
                            //获取到连接的蓝牙设备
                            BluetoothDevice device = socket.getRemoteDevice();
                            DataInputStream stream = new DataInputStream(socket.getInputStream());
                            String s = stream.readUTF();
                            handler.obtainMessage(1, device.getName() + ":" + s).sendToTarget();
                            socket.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final BluetoothDevice device = (BluetoothDevice) adapter.getItem(position);

        new Thread(){
            @Override
            public void run() {
                BluetoothSocket socket = null;
                try {
                    //发起连接
                    socket = device.createRfcommSocketToServiceRecord(UUID.fromString(uuid));
                    //如果没有配对，则去配对
                    socket.connect();
                    DataOutputStream stream = new DataOutputStream(socket.getOutputStream());
                    stream.writeUTF("测试");
                    //这里不要关闭
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
