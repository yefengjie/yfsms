package com.windcity.yefeng.yfsms.domain.usecase.tabs;

import com.windcity.yefeng.yfsms.data.db.DbHelper;
import com.windcity.yefeng.yfsms.data.model.Tabs;
import com.yefeng.support.base.UseCase;
import com.yefeng.support.http.HttpSchedulersTransformer;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;

/**
 * Created by yefeng on 16/08/2017.
 */

public class LoadTabsTask extends UseCase<LoadTabsTask.Req, LoadTabsTask.Res> {

    @Override
    protected Disposable executeUseCase(Req requestValues) {
        return Flowable.fromCallable(
                this::load)
                .compose(new HttpSchedulersTransformer<>())
                .subscribe(tabses -> {
                    if (null != getUseCaseCallback()) {
                        getUseCaseCallback().onSuccess(new Res(tabses));
                    }
                }, throwable -> {
                    if (null != getUseCaseCallback()) {
                        getUseCaseCallback().onError(throwable.getMessage() + "");
                    }
                });
    }

    private List<Tabs> load() {
        return DbHelper.getInstance().getBoxStore().boxFor(Tabs.class).getAll();
    }

    public static class Req implements UseCase.RequestValues {
    }

    public static class Res implements UseCase.ResponseValue {
        public List<Tabs> tabses;

        public Res(List<Tabs> tabses) {
            this.tabses = tabses;
        }
    }
}
