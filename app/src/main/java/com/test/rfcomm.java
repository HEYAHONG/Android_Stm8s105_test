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

public class rfcomm  {
    public static Context c;
    public static int slave_addr=1;
    public static BluetoothAdapter mBluetoothAdapter=null;
    public static BluetoothSocket con_socket=null;
    public static InputStream in_s=null;
    public static OutputStream out_s=null;
    public static BluetoothDevice device=null;
    public static boolean isInited=false,isConnected=false;
    public rfcomm(Context context_in) //初始化Context
    {
        c=context_in;
    }
    public void setting_init() //初始化设置
    {
        SharedPreferences read_settings = c.getSharedPreferences("settings", Context.MODE_PRIVATE);
        if (read_settings.getString("bluetooth_name", "") != "") {

        } else {
            SharedPreferences.Editor edit_settings = read_settings.edit();
            edit_settings.putString("bluetooth_name", "02:11:22:33:AC:32");
            edit_settings.commit();

        }
        if (read_settings.getString("bluetooth_addr", "") != "") {

        } else {
            SharedPreferences.Editor edit_settings = read_settings.edit();
            edit_settings.putString("bluetooth_addr", "1");
            edit_settings.commit();

        }
    }
    public void _Init() //初始化蓝牙设备
    {
        isInited=true;
        setting_init();
        slave_addr=Integer.parseInt(c.getSharedPreferences("settings",Context.MODE_PRIVATE).getString("bluetooth_addr", ""));
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {

            Toast.makeText(c, "本地蓝牙不可用", Toast.LENGTH_SHORT).show();
            isInited=false;
            return;
        }
        Toast.makeText(c, "打开蓝牙!", Toast.LENGTH_SHORT).show();
        mBluetoothAdapter.enable();
        if (!mBluetoothAdapter.isEnabled()) {
            Toast.makeText(c, "打开蓝牙失败！", Toast.LENGTH_SHORT).show();
            isInited=false;
            return;
        }
        Toast.makeText(c, "开始搜索!", Toast.LENGTH_SHORT).show();
        mBluetoothAdapter.startDiscovery();
        device = mBluetoothAdapter.getRemoteDevice(c.getSharedPreferences("settings", Context.MODE_PRIVATE).getString("bluetooth_name", ""));
        device.createBond();
        //取消发现设备
        mBluetoothAdapter.cancelDiscovery();
    }
    public  void _connect() //链接蓝牙设备
    {
        isConnected=true;
        if(!isInited) _Init();
        //取消发现设备
        mBluetoothAdapter.cancelDiscovery();
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
            Toast.makeText(c, "连接失败!", Toast.LENGTH_SHORT).show();
            isConnected=false;
            return;
        }
        Toast.makeText(c, "连接成功!", Toast.LENGTH_SHORT).show();

    }
    public void _close() //关闭连接
    {
        if(!isConnected) return;
        isConnected=false;
        try {
            con_socket.close();
        }
        catch (Exception e)
        {
            //isConnected=true;
        }

    }
    public InputStream _getInputStream(){
        if(isConnected) return in_s;
        else return null;

    }
    public OutputStream _getOutputStream(){
        if(isConnected) return out_s;
        else return  null;
    }

}
