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

import com.bumptech.glide.Glide;
import com.ming.homwork.bean.ProjectTab;
import com.ming.homwork.bean.StationTab;
import com.ming.homwork.tools.Tools;
import com.tencent.bugly.beta.Beta;

import java.net.IDN;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;


public class MainActivity extends Activity {
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
        setContentView(R.layout.activity_main);
        initView();
        task = new TimerTask() {
            @Override
            public void run() {
                init();
            }
        };
        new Timer().schedule(task, 0, 5000);//5秒执行一次
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                init();
            }
        });
    }

    public void init() {
        //获取设备序列号
        androidId = android.os.Build.SERIAL;
        //查询
        ueryQ();

    }

    private void ueryQ() {
        //查询数据库里面是否有启用的项目
        BmobQuery<ProjectTab> bmobQuery = new BmobQuery<>();
        bmobQuery.findObjects(new FindListener<ProjectTab>() {
            @Override
            public void done(List<ProjectTab> list, BmobException e) {
                if (list != null) {
                    for (ProjectTab projectTab : list) {
                        if (projectTab.isEnable()) {
                            QueryStation(projectTab.getObjectId());
                            return;
                        }
                    }
                    text = "没有启动项目";
                    upload();
                }
            }
        });
    }

    private void QueryStation(String objectId) {
        BmobQuery<StationTab> bmobQuery = new BmobQuery<>();
        bmobQuery.addWhereEqualTo("projectId", objectId);
        bmobQuery.findObjects(new FindListener<StationTab>() {
            @Override
            public void done(List<StationTab> list, BmobException e) {
                if (list != null) {
                    for (StationTab stationTab : list) {
                        if (stationTab.getDeviceId().equals(androidId)) {
                            text = "";
                            upload(stationTab.getImgUrl());
                            return;
                        }
                    }
                    text = "没有你工位的作业指导图";
                    upload();
                }
            }
        });

    }

    /**
     * @param imgUrl 加载图片
     */
    private void upload(String imgUrl) {
        ll_layout.setVisibility(View.GONE);
        iv_img.setVisibility(View.VISIBLE);
        Glide.with(this).load(imgUrl).into(iv_img);
    }

    private void upload() {
        ll_layout.setVisibility(View.VISIBLE);
        iv_img.setVisibility(View.GONE);
        tv_text.setText(text);
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
                Beta.checkUpgrade();
            }
        });


    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // if ((System.currentTimeMillis() - mExitTime) > 2000)
            // {Toast.makeText(this,
            // getResources().getString(R.string.exit).toString(),
            // Toast.LENGTH_SHORT).show();
            // mExitTime = System.currentTimeMillis();}

            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();

            } else {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);// 注意
                intent.addCategory(Intent.CATEGORY_HOME);
                this.startActivity(intent);
                finish();
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        task.cancel();

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }
}
