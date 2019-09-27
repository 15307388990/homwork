package com.ming.homwork;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import androidx.core.app.ActivityCompat;


public class DemoActivity extends Activity {
    private String tag = "MainActivity";
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    checkScreenOn(null);
                    break;
                case 2:

                    break;
            }
        }
    };
    private DevicePolicyManager policyManager;
    private ComponentName adminReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        checkPermission();
        upgradeRootPermission(getPackageCodePath());
        adminReceiver = new ComponentName(this, ScreenOffAdminReceiver.class);
        mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
        policyManager = (DevicePolicyManager) this.getSystemService(Context.DEVICE_POLICY_SERVICE);
        checkAndTurnOnDeviceManager(null);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        isOpen();
    }


    private void isOpen() {
        if (policyManager.isAdminActive(adminReceiver)) {//判断超级管理员是否激活
            showToast("设备已被激活");
        } else {
            showToast("设备没有被激活");

        }
    }

    private PowerManager mPowerManager;
    private PowerManager.WakeLock mWakeLock;


    /**
     * @param view 检测屏幕状态
     */
    public void checkScreen(View view) {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        boolean screenOn = pm.isScreenOn();
        if (!screenOn) {//如果灭屏
            //相关操作
            showToast("屏幕是息屏");
        } else {
            showToast("屏幕是亮屏");

        }
    }


    /**
     * @param view 亮屏
     */
    @SuppressLint("InvalidWakeLockTag")
    public void checkScreenOn(View view) {
        mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "tag");
        mWakeLock.acquire();
        mWakeLock.release();
    }

    /**
     * @param view 熄屏
     */
    public void checkScreenOff(View view) {
        boolean admin = policyManager.isAdminActive(adminReceiver);
        if (admin) {
            policyManager.lockNow();
        } else {
            showToast("没有设备管理权限");
        }
//        WindowManager.LayoutParams lp = this.getWindow().getAttributes();
//        lp.screenBrightness = 0;
//        this.getWindow().setAttributes(lp);
        //setLCD("1");
    }

    /**
     * @param view 熄屏并延时亮屏
     */
    public void checkScreenOffAndDelayOn(View view) {
        boolean admin = policyManager.isAdminActive(adminReceiver);
        if (admin) {
            policyManager.lockNow();
            handler.sendEmptyMessageDelayed(1, 3000);
        } else {
            showToast("没有设备管理权限");
        }
    }

    /**
     * @param view 检测并去激活设备管理器权限
     */
    public void checkAndTurnOnDeviceManager(View view) {
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminReceiver);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "开启后就可以使用锁屏功能了...");//显示位置见图二
        startActivityForResult(intent, 0);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        checkScreenOn(null);
        return super.onTouchEvent(event);
    }


    private void showToast(String Str) {
        Toast.makeText(this, Str, Toast.LENGTH_SHORT).show();
    }


    private void checkPermission() {
        //检查权限（NEED_PERMISSION）是否被授权 PackageManager.PERMISSION_GRANTED表示同意授权
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //用户已经拒绝过一次，再次弹出权限申请对话框需要给用户一个解释
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission
                    .WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "请开通相关权限，否则无法正常使用本应用！", Toast.LENGTH_SHORT).show();
            }
            //申请权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        } else {
            Toast.makeText(this, "授权成功！", Toast.LENGTH_SHORT).show();
        }
    }

    public void Write2File(File file, String mode) {
        //Log.d(TAG,"========Write2File,write mode = "+mode);
        if ((file == null) || (!file.exists()) || (mode == null)) return;

        try {
            FileOutputStream fout = new FileOutputStream(file);
            PrintWriter pWriter = new PrintWriter(fout);
            pWriter.println(mode);
            pWriter.flush();
            pWriter.close();
            fout.close();
        } catch (IOException re) {
            Log.d("penglei", "write error:" + re);
            return;
        }
    }

    public void setLCD(String value) {
        FileWriter fileWriter = null;
        File file = new File("/sys/gpio_test_attr/lcd_power");
        if (!file.exists()) {
            Log.e("penglei", "setLCD is not exists!");
            return;
        }
        try {
            fileWriter = new FileWriter(file);
            fileWriter.write(value);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 应用程序运行命令获取 Root权限，设备必须已破解(获得ROOT权限)
     *
     * @return 应用程序是/否获取Root权限
     */
    public  boolean upgradeRootPermission(String pkgCodePath) {
        Process process = null;
        DataOutputStream os = null;
        try {
            String cmd="chmod 777 " + pkgCodePath;
            process = Runtime.getRuntime().exec("su"); //切换到root帐号
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
            }
        }
        return true;
    }


}