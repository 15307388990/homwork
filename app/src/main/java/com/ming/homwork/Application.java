package com.ming.homwork;

import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;

import cn.bmob.v3.Bmob;

public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //BmobSdk
        Bmob.initialize(this, "4a812405cf82336b7940a53c422561e0");
        //bugly 参数3：是否开启debug模式，true表示打开debug模式，false表示关闭调试模式
        Bugly.init(getApplicationContext(), "d69ebdfc4c", false);
        Beta.autoCheckUpgrade = true;//自动检查更新
        Beta.upgradeCheckPeriod = 60 * 1000;
    }
}
