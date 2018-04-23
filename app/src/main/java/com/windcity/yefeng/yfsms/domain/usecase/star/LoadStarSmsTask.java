package com.windcity.yefeng.yfsms.domain.usecase.star;

import com.windcity.yefeng.yfsms.data.model.StarSms;
import com.yefeng.support.base.UseCase;
import com.yefeng.support.http.HttpSchedulersTransformer;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;

/**
 * Created by yefeng on 24/08/2017.
 */

public class LoadStarSmsTask extends UseCase<LoadStarSmsTask.Req, LoadStarSmsTask.Res> {


    @Override
    protected Disposable executeUseCase(Req requestValues) {
        return Flowable.fromCallable(
                () -> StarCenter.loadStartSms())
                .compose(new HttpSchedulersTransformer<>())
                .subscribe(
                        starSmses -> {
                            if (null != getUseCaseCallback()) {
                                getUseCaseCallback().onSuccess(new Res(starSmses));
                            }
                        }
                        , throwable -> {
                            if (null != getUseCaseCallback()) {
                                getUseCaseCallback().onError(throwable.getMessage() + "");
                            }
                        });
    }

    public static class Req implements UseCase.RequestValues {
    }

    public static class Res implements UseCase.ResponseValue {
        public List<StarSms> list;

        public Res(List<StarSms> list) {
            this.list = list;
        }
    }
}
