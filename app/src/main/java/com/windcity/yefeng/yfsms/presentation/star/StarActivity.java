package com.windcity.yefeng.yfsms.presentation.star;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;

import com.afollestad.materialdialogs.MaterialDialog;
import com.windcity.yefeng.yfsms.R;
import com.windcity.yefeng.yfsms.base.BaseActivity;
import com.windcity.yefeng.yfsms.data.model.StarSms;
import com.windcity.yefeng.yfsms.domain.usecase.star.StarCenter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StarActivity extends BaseActivity {

    private static final String STAR_SMS = "star_sms";
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.list_sms)
    RecyclerView mList;
    ArrayList<StarSms> mDatas;
    StarAdapter mAdapter;

    public static void startMe(Context context, ArrayList<StarSms> list) {
        Intent intent = new Intent(context, StarActivity.class);
        intent.putParcelableArrayListExtra(STAR_SMS, list);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_star);
        ButterKnife.bind(this);
        if (!handleData(savedInstanceState)) {
            finish();
            return;
        }
        init();
    }

    private boolean handleData(Bundle savedInstanceState) {
        if (getIntent() != null) {
            mDatas = getIntent().getParcelableArrayListExtra(STAR_SMS);
        }
        if (null == mDatas || mDatas.isEmpty()) {
            if (null != savedInstanceState) {
                mDatas = savedInstanceState.getParcelableArrayList(STAR_SMS);
            }
        }
        return !(null == mDatas || mDatas.isEmpty());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (null != mDatas && !mDatas.isEmpty()) {
            outState.putParcelableArrayList(STAR_SMS, mDatas);
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

    private void init() {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (null != actionBar) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        StarSms starSms = mDatas.get(0);
        String title = TextUtils.isEmpty(starSms.name) ? starSms.address : starSms.name;
        setTitle(title);
        mAdapter = new StarAdapter(mDatas);
        mAdapter.setOnLongClickListener(v -> {
            StarSms sms = (StarSms) v.getTag();
            if (null != sms) {
                longClickItem(sms);
                return true;
            }
            return false;
        });
        mList.setHasFixedSize(true);
        mList.setLayoutManager(new LinearLayoutManager(getCtx()));
        mList.setAdapter(mAdapter);
    }

    private void longClickItem(StarSms sms) {
        new MaterialDialog.Builder(this)
                .title(R.string.ask_if_unstar_sms)
                .positiveText(R.string.unstar_sms)
                .negativeText(R.string.cancel)
                .onPositive((dialog, which) -> unstar(sms))
                .build()
                .show();

    }

    private void unstar(StarSms sms) {
        if (null == sms) {
            return;
        }
        StarCenter.unstarSms(sms);
        mDatas.remove(sms);
        showToast(R.string.unstar_ok);
        if (null == mDatas || mDatas.isEmpty()) {
            finish();
            return;
        }
        mAdapter.setData(mDatas);
    }

}
