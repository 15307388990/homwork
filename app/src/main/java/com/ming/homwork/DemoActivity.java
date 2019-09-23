package com.ming.homwork;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ming.homwork.bean.ProjectTab;
import com.ming.homwork.bean.StationTab;
import com.ming.homwork.tools.Tools;
import com.ming.homwork.util.NtpTrustedTime;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;


public class DemoActivity extends Activity {
    private String androidId;
    private String text;
    private ImageView iv_img;
    private TextView tv_text;
    private Button btn;
    private LinearLayout ll_layout;
    private String imgUrl;
    private long mExitTime;
    private TimerTask task;
    private TextView tv_version;
    private String versionName;//版本名称
    private int versionCode;//版本号

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        initView();

        init();
    }

    public void init() {

        NtpTrustedTime ntpTrustedTime = NtpTrustedTime.getInstance(this);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                new Thread() {
//                    @Override
//                    public void run() {
//                        //这里写入子线程需要做的工作
//                        ntpTrustedTime.forceRefresh();
//                        long timer = ntpTrustedTime.currentTimeMillis();
//                        String time = getDateformat2(timer);
//                        //tv_version.setText();
//                    }
//                }.start();
                Calendar c = Calendar.getInstance();
                Toast.makeText(DemoActivity.this, String.valueOf(c.getTime()), Toast.LENGTH_LONG).show();
            }
        });


    }

    /**
     * 时间戳转时间
     */
    public static String getDateformat2(long time) {
        if (time == 0) {
            return "";
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(time);

    }


    private void initView() {
        iv_img = (ImageView) findViewById(R.id.iv_img);
        tv_text = (TextView) findViewById(R.id.tv_text);
        btn = (Button) findViewById(R.id.btn);
        ll_layout = (LinearLayout) findViewById(R.id.ll_layout);
        tv_version = (TextView) findViewById(R.id.tv_version);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        versionName = Tools.getVersionName(this);
        versionCode = Tools.getVersionCode(this);
        tv_version.setText("V." + versionName);
        tv_version.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


    }

}
