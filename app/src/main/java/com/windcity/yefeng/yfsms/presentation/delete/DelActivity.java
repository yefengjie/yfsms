package com.windcity.yefeng.yfsms.presentation.delete;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.afollestad.materialdialogs.MaterialDialog;
import com.windcity.yefeng.yfsms.R;
import com.windcity.yefeng.yfsms.base.BaseActivity;
import com.windcity.yefeng.yfsms.domain.usecase.conversation.DeleteConversationTask;
import com.windcity.yefeng.yfsms.domain.usecase.conversation.LoadConversationTask;
import com.windcity.yefeng.yfsms.domain.usecase.sms.SmsEvent;
import com.windcity.yefeng.yfsms.domain.usecase.sms.SmsType;
import com.yefeng.support.base.UseCase;
import com.yefeng.support.rxbus.RxBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class DelActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.list)
    RecyclerView mList;
    @BindView(R.id.fab)
    FloatingActionButton mFab;
    DelConversationAdapter mDelConversationAdapter;
    LoadConversationTask mLoadConversationTask;
    DeleteConversationTask mDelConversationTask;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();


    public static void startMe(Context context) {
        context.startActivity(new Intent(context, DelActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_del);
        ButterKnife.bind(this);
        init();
        loadData();
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_select_all);
        if (null != mDelConversationAdapter && mDelConversationAdapter.isAllCheck()) {
            item.setTitle(R.string.unselect_all);
        } else {
            item.setTitle(R.string.select_all);
        }
        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_del, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_select_all) {
            if (null != mDelConversationAdapter) {
                mDelConversationAdapter.selectAll();
            }
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
        mDelConversationAdapter = new DelConversationAdapter();
        mList.setHasFixedSize(true);
        mList.setLayoutManager(new LinearLayoutManager(this));
        mList.setAdapter(mDelConversationAdapter);

        mCompositeDisposable.add(
                RxBus.getBus()
                        .toObserverable()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(o -> {
                            if (o instanceof DelConversationAdapter.OnCheckChangeEvent) {
                                invalidateOptionsMenu();
                            }
                        })
        );
    }

    private void loadData() {
        if (null == mLoadConversationTask) {
            mLoadConversationTask = new LoadConversationTask();
        }
        showProgress(R.string.loading);
        mLoadConversationTask.setRequestValues(new LoadConversationTask.Req(SmsType.TYPE_ALL));
        mLoadConversationTask.setUseCaseCallback(new UseCase.UseCaseCallBackImpl<LoadConversationTask.Res>() {
            @Override
            public void onSuccess(LoadConversationTask.Res response) {
                dismissProgress();
                if (null != mDelConversationAdapter) {
                    mDelConversationAdapter.setData(response.unfoldConversations);
                }
            }

            @Override
            public void onError(String errorMsg) {
                dismissProgress();
                showToast(getString(R.string.loading_failed));
            }
        });
        mCompositeDisposable.add(mLoadConversationTask.run());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.clear();
        mDelConversationAdapter = null;
        mDelConversationTask = null;
        mLoadConversationTask = null;
    }

    @OnClick(R.id.fab)
    void delete() {
        if (null == mDelConversationAdapter
                || mDelConversationAdapter.getCheckedData() == null
                || mDelConversationAdapter.getCheckedData().isEmpty()) {
            return;
        }
        new MaterialDialog.Builder(this)
                .title(R.string.confirm_delete_conversation)
                .positiveText(R.string.delete)
                .negativeText(R.string.cancel)
                .onPositive((dialog1, which) -> doDelete())
                .show();

    }

    private void doDelete() {
        if (null == mDelConversationTask) {
            mDelConversationTask = new DeleteConversationTask();
        }
        mDelConversationTask.setRequestValues(new DeleteConversationTask.Req(
                mDelConversationAdapter.getCheckedData(), getCtx()
        ));
        mDelConversationTask.setUseCaseCallback(new UseCase.UseCaseCallBackImpl<DeleteConversationTask.Res>() {
            @Override
            public void onSuccess(DeleteConversationTask.Res response) {
                dismissProgress();
                if (response.isDeleteSuccess) {
                    showToast(R.string.delete_ok);
                    loadData();
                    RxBus.getBus().send(new SmsEvent.Delete());
                } else {
                    onError("");
                }
            }

            @Override
            public void onError(String errorMsg) {
                dismissProgress();
            }
        });
        mCompositeDisposable.add(mDelConversationTask.run());
        showProgress(R.string.deleting);
    }
}
