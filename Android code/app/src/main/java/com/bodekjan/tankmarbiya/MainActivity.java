package com.bodekjan.tankmarbiya;

import android.content.Context;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import cz.msebera.android.httpclient.Header;

import com.bodekjan.tankmarbiya.widget.MyButton;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.AsyncHttpClient;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class MainActivity extends AppCompatActivity {
    String mIp;
    int mIpEnd;
    String mIpStart;
    String sIp;
    int speed=2;
    TextView myIp;
    TextView sheIp;
    0
    EditText manualInput;
    Button find;
    Button manual;
    MyButton left;
    MyButton right;
    MyButton up;
    MyButton down;
    SeekBar seekBar;
    WebWorker wWorker;
    Handler uiHandler;
    class WebWorker extends Thread {
        public Handler workerHand;
        boolean hasShe=true;
        int end=0;
        int myEnd;
        String sIp;
        public boolean getHtmlContent(String rUrl, String encode) {
            URL url = null;
            try {
                url = new URL(rUrl);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            boolean reBool=false;
            StringBuffer contentBuffer = new StringBuffer();
            int responseCode = -1;
            HttpURLConnection con = null;
            try {
                con = (HttpURLConnection) url.openConnection();
                con.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");// IE代理进行下载
                con.setConnectTimeout(100);
                con.setReadTimeout(100);
                // 获得网页返回信息码
                responseCode = con.getResponseCode();
                if (responseCode == -1) {
                    System.out.println(url.toString() + " : connection is failure...");
                    con.disconnect();
                    return reBool;
                }
                if (responseCode >= 400) // 请求失败
                {
                    System.out.println("请求失败:get response code: " + responseCode);
                    con.disconnect();
                }
            } catch (ConnectException e){
            } catch (IOException e) {
                reBool=true;
                contentBuffer = null;
            } finally {
                con.disconnect();
            }
            return reBool;
        }
        public void run() {
            Looper.prepare();
            workerHand=new Handler(){
                @Override
                public void handleMessage(Message msg){
                    if(msg.what==0x111){
                        myEnd=msg.getData().getInt("ipend");
                        while (hasShe){
                            if(end>254) {
                                Message msgg=new Message();
                                msgg.what=0x121;
                                Bundle bundle=new Bundle();
                                bundle.putString("ip","0.0.0.0");
                                msgg.setData(bundle);
                                uiHandler.sendMessage(msgg);
                                hasShe=false;
                                break;
                            }
                            if(end==myEnd){
                                end++;
                                continue;
                            }
                            try {
                                boolean results=getHtmlContent("http://"+mIpStart+end,"utf-8");
                                //boolean results=getHtmlContent("http://"+mIpStart+117,"utf-8");
                                if(results){
                                    hasShe=false;
                                    sIp=mIpStart+end;
                                    Message msgg=new Message();
                                    msgg.what=0x120;
                                    Bundle bundle=new Bundle();
                                    bundle.putString("ip",sIp);
                                    msgg.setData(bundle);
                                    uiHandler.sendMessage(msgg);
                                    continue;
                                }
                                Message msgg=new Message();
                                msgg.what=0x122;
                                Bundle bundle=new Bundle();
                                bundle.putString("ip",mIpStart+end);
                                msgg.setData(bundle);
                                uiHandler.sendMessage(msgg);
                                Log.d("搜索中",mIpStart+end);
                                end++;
                            } catch (Exception e) {
                                end++;
                                continue;
                            }
                        }
                    }
                    if(msg.what==0x112){
                        // 向左拐
                        getHtmlContent("http://"+sIp+"?action=left&speed="+seekBar.getProgress(),"utf-8");
                        Log.e("asfasf","往左");
                    }
                    if(msg.what==0x113){
                        // 向右拐
                        getHtmlContent("http://"+sIp+"?action=right&speed="+seekBar.getProgress(),"utf-8");
                        Log.e("asfasf","往右");
                    }
                    if(msg.what==0x114){
                        // 向上
                        getHtmlContent("http://"+sIp+"?action=moveup&speed="+seekBar.getProgress(),"utf-8");
                        Log.e("asfasf","往上");
                    }
                    if(msg.what==0x115){
                        // 向下
                        getHtmlContent("http://"+sIp+"?action=movedown&speed="+seekBar.getProgress(),"utf-8");
                        Log.e("asfasf","往下");
                    }
                    if(msg.what==0x119){
                        // 手动设置ip
                        sIp=msg.getData().getString("ip");
                        Log.e("asfasf","手动设置ip");
                    }
                    if(msg.what==0x110){
                        // 停止
                        getHtmlContent("http://"+sIp+"?action=stop","utf-8");
                        Log.e("asfasf","停止命令");
                    }
                }
            };
            Looper.loop();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 线程开启
        wWorker=new WebWorker();
        wWorker.start();
        myIp=(TextView)findViewById(R.id.iptext);
        sheIp=(TextView)findViewById(R.id.sheiptext);
        manualInput=(EditText)findViewById(R.id.manualinput);
        manual=(Button)findViewById(R.id.manual);
        manual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sheIp.setText(manualInput.getText());
                Message msg=new Message();
                msg.what=0x119;
                Bundle bundle=new Bundle();
                bundle.putString("ip",manualInput.getText().toString());
                msg.setData(bundle);
                wWorker.workerHand.sendMessage(msg);
            }
        });
        find=(Button)findViewById(R.id.searchbtn);
        find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                find.setText("搜索中");
                Message msg=new Message();
                msg.what=0x111;
                Bundle bundle=new Bundle();
                bundle.putInt("ipend",mIpEnd);
                msg.setData(bundle);
                wWorker.workerHand.sendMessage(msg);
            }
        });
        left=(MyButton)findViewById(R.id.turnleft);
        left.setMyOnClickListener(new MyButton.MyOnClickListener() {
            @Override
            public void onPressed() {
                Message msg=new Message();
                msg.what=0x112;
                wWorker.workerHand.sendMessage(msg);
            }

            @Override
            public void onNortmal() {
                Message msg=new Message();
                msg.what=0x110;
                wWorker.workerHand.sendMessage(msg);
            }
        });
        right=(MyButton)findViewById(R.id.turnright);
        right.setMyOnClickListener(new MyButton.MyOnClickListener() {
            @Override
            public void onPressed() {
                Message msg=new Message();
                msg.what=0x113;
                wWorker.workerHand.sendMessage(msg);
            }

            @Override
            public void onNortmal() {
                Message msg=new Message();
                msg.what=0x110;
                wWorker.workerHand.sendMessage(msg);
            }
        });
        up=(MyButton)findViewById(R.id.upbtn);
        up.setMyOnClickListener(new MyButton.MyOnClickListener() {
            @Override
            public void onPressed() {
                Message msg=new Message();
                msg.what=0x114;
                wWorker.workerHand.sendMessage(msg);
            }

            @Override
            public void onNortmal() {
                Message msg=new Message();
                msg.what=0x110;
                wWorker.workerHand.sendMessage(msg);
            }
        });
        down=(MyButton)findViewById(R.id.downbtn);
        down.setMyOnClickListener(new MyButton.MyOnClickListener() {
            @Override
            public void onPressed() {
                Message msg=new Message();
                msg.what=0x115;
                wWorker.workerHand.sendMessage(msg);
            }

            @Override
            public void onNortmal() {
                Message msg=new Message();
                msg.what=0x110;
                wWorker.workerHand.sendMessage(msg);
            }
        });
        seekBar=(SeekBar)findViewById(R.id.seekBar);
        seekBar.setMax(4);
        seekBar.setProgress(speed);
        //获取本机ip地址
        //获取wifi服务
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        //判断wifi是否开启
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        myIp.setText(intToIp(ipAddress));
        mIp=intToIp(ipAddress);
        uiHandler=new Handler(){
            @Override
            public void handleMessage(Message msg){
                if(msg.what==0x120){
                    // 停止
                    find.setText("搜索完毕");
                    sheIp.setText(msg.getData().getString("ip"));
                    sheIp.setTextColor(Color.GREEN);
                }
                if(msg.what==0x121){
                    // 停止
                    find.setText("找不到，请重新搜索");
                    sheIp.setText(msg.getData().getString("ip"));
                }
                if(msg.what==0x122){
                    // 停止
                    sheIp.setText(msg.getData().getString("ip"));
                    sheIp.setTextColor(Color.YELLOW);
                }
            }
        };
    }
    private String intToIp(int i) {
        mIpEnd=( i >> 24 & 0xFF);
        mIpStart=(i & 0xFF ) + "." +
                ((i >> 8 ) & 0xFF) + "." +
                ((i >> 16 ) & 0xFF) + ".";
        return (i & 0xFF ) + "." +
                ((i >> 8 ) & 0xFF) + "." +
                ((i >> 16 ) & 0xFF) + "." +
                ( i >> 24 & 0xFF) ;
    }
}
