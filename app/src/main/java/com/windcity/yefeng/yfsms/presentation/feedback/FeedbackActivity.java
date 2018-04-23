package com.windcity.yefeng.yfsms.presentation.feedback;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;

import com.windcity.yefeng.yfsms.R;
import com.windcity.yefeng.yfsms.base.BaseActivity;
import com.windcity.yefeng.yfsms.domain.usecase.feedback.FeedbackTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FeedbackActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.et_contact_way)
    TextInputEditText mEtContactWay;
    @BindView(R.id.et_feedback)
    TextInputEditText mEtFeedback;

    public static void startMe(Context context) {
        context.startActivity(new Intent(context, FeedbackActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        setSupportActionBar(mToolbar);
        if (null != getSupportActionBar()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.fab)
    void sendFeedback() {
        if (TextUtils.isEmpty(mEtFeedback.getText())) {
            return;
        }
        String contactWay = "";
        if (!TextUtils.isEmpty(mEtContactWay.getText())) {
            contactWay = mEtContactWay.getText().toString();
        }
        String feedbackMsg = mEtFeedback.getText().toString();
        FeedbackTask.start(feedbackMsg, contactWay, this);
        finish();
    }
}
