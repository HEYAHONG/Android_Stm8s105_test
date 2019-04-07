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

public class manual_rw extends AppCompatActivity {
    rfcomm mrfcomm;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manual_rw);
        setTitle("手动读写");
        mrfcomm=new rfcomm(getApplicationContext());
        mrfcomm._connect();



    }
    @Override
    protected  void onDestroy()
    {
        if(mrfcomm != null) mrfcomm._close();
        super.onDestroy();
    }
    public void write_button(View v)
    {
        try{
        if(((EditText)findViewById(R.id.reg_addr)).getText().toString() == ""
        || ((EditText)findViewById(R.id.reg_value)).getText().toString() == "")
        {
          Toast.makeText(getApplicationContext(),"输入不合法!",Toast.LENGTH_SHORT).show();
          return;
         }
         else
        {
         int addr=Integer.parseInt(((EditText) findViewById(R.id.reg_addr)).getText().toString());
         int value=Integer.parseInt(((EditText) findViewById(R.id.reg_value)).getText().toString());
         if(mrfcomm != null ) mrfcomm._getOutputStream().write(modbus.modbus_write(mrfcomm.slave_addr,addr,(byte)value));
         }
        }catch (Exception e)
        {
            Toast.makeText(getApplicationContext(),"写失败!",Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(getApplicationContext(),"写成功!",Toast.LENGTH_SHORT).show();
    }
    public void read_button(View v)
    {
      try {

          if(((EditText)findViewById(R.id.reg_addr)).getText().toString() == "")
          {
              Toast.makeText(getApplicationContext(),"输入不合法!",Toast.LENGTH_SHORT).show();
              return;
          }
          int addr=Integer.parseInt(((EditText) findViewById(R.id.reg_addr)).getText().toString());
          if(mrfcomm != null)
          {
              byte [] buff=new byte[7],buff_1=new byte[1];
              while (mrfcomm._getInputStream().available() !=0)
                  mrfcomm._getInputStream().read(buff_1);
              mrfcomm._getOutputStream().write(modbus.modbus_read(mrfcomm.slave_addr,addr));
              //Thread.sleep(4);
              byte i=0;
              while(i != 7)
              {
                  mrfcomm._getInputStream().read(buff_1);
                  buff[i++]=buff_1[0];
              }
              if(((int)buff[1]) == 3)
              {
                  int value=buff[4] & 0xff;
                  ((EditText)findViewById(R.id.reg_value)).setText(String.valueOf(value));

              }
          }


      }
      catch (Exception e)
      {
          Toast.makeText(getApplicationContext(),"读失败!",Toast.LENGTH_SHORT).show();
          return;
      }
        Toast.makeText(getApplicationContext(),"读成功!",Toast.LENGTH_SHORT).show();
    }
}
