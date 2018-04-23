package com.yefeng.support.base;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.util.DisplayMetrics;

import com.yefeng.support.R;


/**
 * Created by yee on 11/18/13.
 * app basic information
 *
 * @author yefeng
 */
public class AppInfo {

    public static String sBuildModel;
    public static int sBuildSdkInt;
    public static String sBuildRelease;


    public static int sAppCode;
    public static String sAppVersion;
    public static String sAppName;

    public static int sWidth;
    public static int sHeight;
    public static float sDensity;
    public static int sDensityDpi;

    public static void init(Context mContext) {
        sBuildModel = Build.MODEL;
        sBuildSdkInt = Build.VERSION.SDK_INT;
        sBuildRelease = Build.VERSION.RELEASE;

        PackageInfo pi = null;
        try {
            pi = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null != pi) {
            sAppVersion = pi.versionName;
            sAppCode = pi.versionCode;
        } else {
            sAppVersion = "";
            sAppCode = 0;
        }
        sAppName = mContext.getString(R.string.app_name);

        initDisplay(mContext);
    }

    private static void initDisplay(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        sWidth = metrics.widthPixels;
        sHeight = metrics.heightPixels;
        sDensity = metrics.density;
        sDensityDpi = metrics.densityDpi;
    }
}
