package com.test;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.bluetooth.*;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;


public class MainActivity extends AppCompatActivity {
    static rfcomm mrfcomm=null;
    String log;
    byte  [] read_buff=new byte[1];
    Handler mhander;
    Runnable run_reflash;
    public void write_stm8(int addr,int data)
    {
        try{
            if(mrfcomm != null ) mrfcomm._getOutputStream().write(modbus.modbus_write(mrfcomm.slave_addr,addr,(byte)(data & 0xff)));

        }catch (Exception e)
        {
            Toast.makeText(getApplicationContext(),"写失败!",Toast.LENGTH_SHORT).show();
            return;
        }
        //Toast.makeText(getApplicationContext(),"写成功!",Toast.LENGTH_SHORT).show();
    }
    public int read_stm8(int addr)
    {
        int value=0;
        try {


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
                    value=buff[4] & 0xff;

                }
            }


        }
        catch (Exception e)
        {
            Toast.makeText(getApplicationContext(),"读失败!",Toast.LENGTH_SHORT).show();
            return 0;
        }
        //Toast.makeText(getApplicationContext(),"读成功!",Toast.LENGTH_SHORT).show();
        return value;
    }
    public int DS_Time_IntToHex(int Int)
    {
        return Int/16*10+Int%16;
    }
    public int DS_Time_HexToInt(int Hex)
    {
        return Hex/10*16+Hex%10;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.app_name);

        mrfcomm=new rfcomm(getApplicationContext());

        //if(mrfcomm!=null) mrfcomm._connect();
        log =((TextView)findViewById(R.id.textView_log)).getText().toString();
        mhander=new Handler();
        run_reflash=new Runnable() {
            @Override
            public void run() {
                if(mrfcomm._getInputStream()!=null)
                {
                    log="";
                    try {
                    log+="时间："+DS_Time_IntToHex(read_stm8(1030))+":"+DS_Time_IntToHex(read_stm8(1031))+":"+DS_Time_IntToHex(read_stm8(1032))+"\r\n";
                    log+="温度："+(read_stm8(1052)+0.1*(read_stm8(1053)&0xf))+"C ";
                    log+="湿度："+(read_stm8(1050)+0.1*(read_stm8(1051)&0xf))+"% \r\n";
                    log+="ADC:"+(read_stm8(1040)*256+read_stm8(1041))+"\r\n";
                    log+="数字输入："+(read_stm8(1042)==0?"关":"开")+"\r\n";
                    log+="报警:"+(read_stm8(1070)==0?"关":"开")+"\r\n";
                    log+="继电器:"+(read_stm8(1080)==0?"关":"开")+"\r\n";
                    log+="规则："+(read_stm8(4)==0?"关":"开")+"\r\n";
                        ((TextView)findViewById(R.id.textView_log)).setText(log);




                    }
                    catch(Exception e)
                    {
                        Toast.makeText(getApplicationContext(),"读取失败", Toast.LENGTH_SHORT).show();
                    }
                }
                mhander.postDelayed(this,3000);

            }
        };
        // mhander.postDelayed(run_reflash,1000);
        ((TextView)findViewById(R.id.textView_log)).setMovementMethod(ScrollingMovementMethod.getInstance());
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "正在连接!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                try {
                    mrfcomm._close();
                    Thread.sleep(500);
                    mrfcomm._connect();
                    mhander.postDelayed(run_reflash,1000);
                }catch(Exception e)
                {

                }


            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) { //菜单跳转
           if(mrfcomm!=null) mrfcomm._close();
            mhander.removeCallbacks(run_reflash);
            Intent startnew_act = new Intent(MainActivity.this,menu.class);
            startActivity(startnew_act);
            //mhander.postDelayed(run_reflash,1000);
            return true;
        }
        if(id==R.id.action_rw)
        {
            mhander.removeCallbacks(run_reflash);
            if(mrfcomm!=null) mrfcomm._close();
            Intent startnew_act = new Intent(MainActivity.this,manual_rw.class);
            startActivity(startnew_act);
            //mhander.postDelayed(run_reflash,1000);
            return true;

        }
        if(id==R.id.rule_on)
        {
            write_stm8(4,1);
            return true;
        }
        if(id==R.id.rule_off)
        {
            write_stm8(4,0);
            return true;
        }
        if(id==R.id.beep_on)
        {
            write_stm8(1070,1);
            return true;
        }
        if(id==R.id.beep_off)
        {
            write_stm8(1070,0);
            return true;
        }
        if(id==R.id.relay_on)
        {
            write_stm8(1080,1);
            return true;
        }
        if(id==R.id.relay_off)
        {
            write_stm8(1080,0);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



}
