package com.windcity.yefeng.yfsms.base;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.yefeng.support.util.ToastUtil;

import org.polaric.colorful.ColorfulActivity;

/**
 * Created by yefeng on 17/07/2017.
 */

public class BaseActivity extends ColorfulActivity {

    private MaterialDialog mProgressDialog;
    /**
     * The {@link Tracker} used to record screen views.
     */
    private Tracker mTracker;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Obtain the shared Tracker instance.
        App application = (App) getApplication();
        mTracker = application.getDefaultTracker();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTracker.setScreenName(this.getClass().getSimpleName());
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public void showToast(String msg) {
        ToastUtil.showToast(this, msg);
    }

    public void showToast(int msg) {
        ToastUtil.showToast(this, msg);
    }

    public Activity getCtx() {
        return this;
    }

    public void showProgress(int msg) {
        if (null == mProgressDialog) {
            mProgressDialog = new MaterialDialog.Builder(this)
                    .progress(true, 0)
                    .build();
        }
        mProgressDialog.setContent(msg);
        mProgressDialog.show();
    }

    public void dismissProgress() {
        if (null != mProgressDialog && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        dismissProgress();
    }
}