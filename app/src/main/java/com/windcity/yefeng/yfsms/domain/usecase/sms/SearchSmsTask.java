package com.windcity.yefeng.yfsms.domain.usecase.sms;

import com.windcity.yefeng.yfsms.data.model.Sms;
import com.yefeng.support.base.UseCase;
import com.yefeng.support.http.HttpSchedulersTransformer;

import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;

/**
 * Created by yefeng on 25/08/2017.
 */

public class SearchSmsTask extends UseCase<SearchSmsTask.Req, SearchSmsTask.Res> {
    @Override
    protected Disposable executeUseCase(Req requestValues) {
        return Flowable.fromCallable(new Callable<List<Sms>>() {
            @Override
            public List<Sms> call() throws Exception {
                return SmsCenter.search(requestValues.keyword);
            }
        }).compose(new HttpSchedulersTransformer<>())
                .subscribe(smses -> {
                    if (null != getUseCaseCallback()) {
                        getUseCaseCallback().onSuccess(new Res(smses));
                    }
                }, throwable -> {
                    if (null != getUseCaseCallback()) {
                        getUseCaseCallback().onError(throwable.getMessage() + "");
                    }
                });
    }

    public static class Req implements UseCase.RequestValues {
        public String keyword;

        public Req(String keyword) {
            this.keyword = keyword;
        }
    }

    public static class Res implements UseCase.ResponseValue {
        public List<Sms> result;

        public Res(List<Sms> result) {
            this.result = result;
        }
    }
}
