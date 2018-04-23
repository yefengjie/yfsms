package com.windcity.yefeng.yfsms.presentation.drawer;

import com.windcity.yefeng.yfsms.R;
import com.windcity.yefeng.yfsms.domain.usecase.sms.SmsEvent;
import com.yefeng.support.rxbus.RxBus;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by yefeng on 18/08/2017.
 */

public class DrawerPresenter implements DrawerContract.Presenter {

    private DrawerActivity mView;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    @Inject
    public DrawerPresenter(DrawerActivity drawerActivity) {
        this.mView = drawerActivity;
    }

    @Override
    public void subscribe() {
        initRxBus();
    }

    private void initRxBus() {
        mCompositeDisposable.add(RxBus.getBus()
                .toObserverable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                    if (o instanceof SmsEvent.Sync) {
                        onSyncSms(((SmsEvent.Sync) o).status);
                    }
                }));
    }

    private void onSyncSms(boolean status) {
        if (status) {
            mView.showProgress(R.string.sync_sms_db);
        } else {
            mView.dismissProgress();
        }
    }

    @Override
    public void unSubscribe() {
        mCompositeDisposable.clear();
    }
}
