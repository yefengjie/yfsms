package com.windcity.yefeng.yfsms.presentation.classify;

import android.text.TextUtils;

import com.windcity.yefeng.yfsms.R;
import com.windcity.yefeng.yfsms.data.db.DbHelper;
import com.windcity.yefeng.yfsms.data.model.Sms;
import com.windcity.yefeng.yfsms.data.model.Sms_;
import com.windcity.yefeng.yfsms.data.model.Tabs;
import com.windcity.yefeng.yfsms.domain.service.TaskService;
import com.windcity.yefeng.yfsms.domain.usecase.conversation.DeleteConversationTask;
import com.windcity.yefeng.yfsms.domain.usecase.sms.SmsConst;
import com.windcity.yefeng.yfsms.domain.usecase.sms.SmsEvent;
import com.windcity.yefeng.yfsms.domain.usecase.tabs.LoadTabsTask;
import com.windcity.yefeng.yfsms.presentation.delete.DelActivity;
import com.yefeng.support.base.UseCase;
import com.yefeng.support.rxbus.RxBus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.objectbox.Box;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by yefeng on 21/08/2017.
 */

public class ClassifyPresenter implements ClassifyContract.Presenter {

    private ClassifyFragment mView;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    @Inject
    public ClassifyPresenter(ClassifyFragment classifyFragment) {
        this.mView = classifyFragment;
    }

    @Override
    public void subscribe() {
        initRxBus();
        TaskService.syncUnreadNum(mView.getContext());
    }

    private void initRxBus() {
        mCompositeDisposable.add(RxBus.getBus()
                .toObserverable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                    if (o instanceof SmsEvent.UnreadNumChange) {
                        reloadTabs();
                    }
                }));
    }

    @Override
    public void unSubscribe() {
        mCompositeDisposable.clear();
    }

    private void reloadTabs() {
        if (null == mView.mAdapter) {
            return;
        }
        mView.mLoadTabsTask.setUseCaseCallback(new UseCase.UseCaseCallBackImpl<LoadTabsTask.Res>() {
            @Override
            public void onSuccess(LoadTabsTask.Res res) {
                mView.updateTabsUnreadNum(res.tabses);
                autoLocate(res.tabses);
            }

            @Override
            public void onError(String errorMsg) {
                Timber.e(errorMsg);
            }
        });
        mCompositeDisposable.add(mView.mLoadTabsTask.run());
    }

    private void autoLocate(List<Tabs> tabses) {
        if (null == tabses || tabses.isEmpty() || !mView.mAutoLocate) {
            return;
        }
        mView.mAutoLocate = false;
        int newestUnreadSmsTabsLocation = -1;
        long newestUnreadSmsTime = 0;
        int size = tabses.size();
        for (int i = 0; i < size; i++) {
            if (tabses.get(i).unreadNum > 0 &&
                    tabses.get(i).newestUnreadSmsTime > newestUnreadSmsTime) {
                newestUnreadSmsTime = tabses.get(i).newestUnreadSmsTime;
                newestUnreadSmsTabsLocation = i;
            }
        }
        if (newestUnreadSmsTabsLocation != -1) {
            mView.autoLocate(newestUnreadSmsTabsLocation);
        }
    }

    public void deleteConversation(int id) {
        if (id <= 0) {
            //自由删除
            DelActivity.startMe(mView.getContext());
            return;
        }
        mView.mDeleteConversationTask.setRequestValues(
                new DeleteConversationTask.Req(mView.getContext(), id)
        );
        mView.mDeleteConversationTask.setUseCaseCallback(new UseCase.UseCaseCallBackImpl<DeleteConversationTask.Res>() {
            @Override
            public void onSuccess(DeleteConversationTask.Res response) {
                super.onSuccess(response);
                mView.dismissProgress();
                if (response.isDeleteSuccess) {
                    mView.showToast(mView.getString(R.string.delete_ok));
                    RxBus.getBus().send(new SmsEvent.Delete());
                } else {
                    onError("");
                }
            }

            @Override
            public void onError(String errorMsg) {
                mView.dismissProgress();
                super.onError(errorMsg);
                if (TextUtils.isEmpty(errorMsg)) {
                    mView.showToast(mView.getString(R.string.delete_failed));
                } else {
                    mView.showToast(mView.getString(R.string.delete_failed) + ":" + errorMsg);
                }
            }
        });
        mCompositeDisposable.add(mView.mDeleteConversationTask.run());
        mView.showProgress(R.string.deleting);
    }

    public void markAllSmsRead() {
        mView.showToast(mView.getString(R.string.has_mark_all_sms_read));
        Box<Sms> smsBox = DbHelper.getInstance().getBoxStore().boxFor(Sms.class);
        List<Sms> list = smsBox.query()
                .equal(Sms_.read, SmsConst.READ_unread)
                .build()
                .find();
        if (!list.isEmpty()) {
            ArrayList<Sms> arrayList = new ArrayList<>();
            arrayList.addAll(list);
            TaskService.markRead(mView.getContext(), arrayList);
        }
    }
}
