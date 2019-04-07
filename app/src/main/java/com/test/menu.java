package com.test;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.*;
import android.util.Log;
import android.widget.Toast;
import android.view.*;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.bluetooth.*;
import android.widget.EditText;
import android.content.Intent;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;


public class menu extends AppCompatActivity {
    int slave_addr=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);
        setTitle(R.string.action_settings);
        SharedPreferences read_settings = getSharedPreferences("settings", Context.MODE_PRIVATE);
        if (read_settings.getString("bluetooth_name", "") != "") {
            ((EditText) findViewById(R.id.editText_mingcheng)).setText(read_settings.getString("bluetooth_name", ""));
        } else {
            SharedPreferences.Editor edit_settings = read_settings.edit();
            edit_settings.putString("bluetooth_name", "02:11:22:33:AC:32");
            edit_settings.commit();
            ((EditText) findViewById(R.id.editText_mingcheng)).setText(read_settings.getString("bluetooth_name", ""));
        }
        if (read_settings.getString("bluetooth_addr", "") != "") {
            ((EditText) findViewById(R.id.editText_addr)).setText(read_settings.getString("bluetooth_addr", ""));
        } else {
            SharedPreferences.Editor edit_settings = read_settings.edit();
            edit_settings.putString("bluetooth_addr", "1");
            edit_settings.commit();
            ((EditText) findViewById(R.id.editText_addr)).setText(read_settings.getString("bluetooth_addr", ""));
        }
        slave_addr=Integer.parseInt(read_settings.getString("bluetooth_addr", ""));
    }

    public void save_button(View v) {
        SharedPreferences write_settings = getSharedPreferences("settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit_settings = write_settings.edit();
        edit_settings.putString("bluetooth_name", ((EditText) findViewById(R.id.editText_mingcheng)).getText().toString());
        edit_settings.putString("bluetooth_addr", ((EditText) findViewById(R.id.editText_addr)).getText().toString());
        edit_settings.commit();
        Toast.makeText(getApplicationContext(), "保存成功！", Toast.LENGTH_SHORT).show();
    }

    public void test_button(View v) {
        slave_addr=Integer.parseInt(getSharedPreferences("settings",Context.MODE_PRIVATE).getString("bluetooth_addr", ""));
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {

            Toast.makeText(this, "本地蓝牙不可用", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(getApplicationContext(), "打开蓝牙!", Toast.LENGTH_SHORT).show();
        mBluetoothAdapter.enable();
        if (!mBluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "打开蓝牙失败！", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(getApplicationContext(), "开始搜索!", Toast.LENGTH_SHORT).show();
        mBluetoothAdapter.startDiscovery();
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(getSharedPreferences("settings", Context.MODE_PRIVATE).getString("bluetooth_name", ""));
        device.setPin(getSharedPreferences("settings", Context.MODE_PRIVATE).getString("bluetooth_pin", "").getBytes());
        device.createBond();
        Set<BluetoothDevice> bonded=mBluetoothAdapter.getBondedDevices();
        Iterator<BluetoothDevice> bonded_it=bonded.iterator();
        while (bonded_it.hasNext())
        {

            Toast.makeText(this, "已配对地址："+bonded_it.next().getAddress(), Toast.LENGTH_LONG).show();
        }
        //取消发现设备
        mBluetoothAdapter.cancelDiscovery();
        UUID con=UUID.randomUUID();
        final BluetoothSocket con_socket;
        InputStream in_s;
        OutputStream out_s;
        try {
            //con_socket = device.createInsecureRfcommSocketToServiceRecord(con);
            con_socket =(BluetoothSocket) device.getClass()
                    .getDeclaredMethod("createRfcommSocket",new Class[]{int.class})
                    .invoke(device,1);

            con_socket.connect();
            in_s=con_socket.getInputStream();
            out_s=con_socket.getOutputStream();
        }
        catch(Exception e)
        {
            Log.w("Bluetooth:",e);
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "连接失败!", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(getApplicationContext(), "连接成功!", Toast.LENGTH_SHORT).show();

        try {
            Thread.sleep(1000);
            out_s.write(modbus.modbus_write(slave_addr,1070,(byte)1));
            Thread.sleep(3000);
            out_s.write(modbus.modbus_write(slave_addr,1070,(byte)0));
            Thread.sleep(1000);
            con_socket.close();
        }
        catch (Exception e)
        {
            Log.w("Bluetooth:",e);
            e.printStackTrace();
            return;
        }
        Toast.makeText(getApplicationContext(), "写测试成功!", Toast.LENGTH_SHORT).show();

    }

}
