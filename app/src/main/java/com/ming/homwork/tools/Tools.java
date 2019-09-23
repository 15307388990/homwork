package com.ming.homwork.tools;

import android.content.Context;

/**
 * @author luoming
 * created at 2019-09-16 10:40
 */
public class Tools {
    /**
     * 获取版本名称
     */

    public static String getVersionName(Context context) {
        try {
            String pkName = context.getPackageName();
            String versionName = context.getPackageManager().getPackageInfo(
                    pkName, 0).versionName;
            return versionName;
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * 获取版本号
     */
    public static int getVersionCode(Context context) {
        try {
            int versionCode = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).versionCode;
            return versionCode;
        } catch (Exception e) {
        }
        return 0;

    }
}
