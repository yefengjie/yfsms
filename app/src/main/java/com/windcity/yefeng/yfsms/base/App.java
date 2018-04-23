package com.windcity.yefeng.yfsms.base;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;
import com.windcity.yefeng.yfsms.BuildConfig;
import com.windcity.yefeng.yfsms.R;
import com.windcity.yefeng.yfsms.data.db.DbHelper;
import com.yefeng.support.base.AppInfo;

import org.polaric.colorful.Colorful;

import timber.log.Timber;

/**
 * Created by yefeng on 19/07/2017.
 */

public class App extends Application {

    private Tracker mTracker;

    @Override
    public void onCreate() {
        super.onCreate();
        AppInfo.init(getApplicationContext());
        Beta.autoCheckUpgrade = false;
        Bugly.init(getApplicationContext(), "739bf0a135", true);
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
        DbHelper.getInstance().init(getApplicationContext());
        Colorful.init(this);
    }

    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     *
     * @return tracker
     */
    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(R.xml.global_tracker);
        }
        return mTracker;
    }
}
