package com.windcity.yefeng.yfsms.presentation.about;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.TextView;

import com.tencent.bugly.beta.Beta;
import com.windcity.yefeng.yfsms.R;
import com.windcity.yefeng.yfsms.base.BaseActivity;
import com.windcity.yefeng.yfsms.presentation.feedback.FeedbackActivity;
import com.yefeng.support.base.AppInfo;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AboutActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.tv_app_info)
    TextView mTvAppInfo;
    @BindView(R.id.tv_check_update)
    TextView mTvCheckUpdate;
    @BindView(R.id.tv_feedback)
    TextView mTvFeedback;

    public static void startMe(Context context) {
        context.startActivity(new Intent(context, AboutActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        setSupportActionBar(mToolbar);
        if (null != getSupportActionBar()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mTvAppInfo.setText(TextUtils.isEmpty(AppInfo.sAppVersion)
                ? getString(R.string.app_name)
                : getString(R.string.app_name) + " " + AppInfo.sAppVersion);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.tv_check_update)
    void checkUpdate() {
        Beta.checkUpgrade(true, false);
    }

    @OnClick(R.id.tv_feedback)
    void feedback() {
        FeedbackActivity.startMe(this);
    }

    @OnClick(R.id.tv_contact_author)
    void contactAuthor() {
        FeedbackActivity.startMe(this);
    }
}
