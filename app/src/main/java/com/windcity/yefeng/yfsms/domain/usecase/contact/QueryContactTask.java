package com.windcity.yefeng.yfsms.domain.usecase.contact;

import android.content.ContentResolver;

import com.windcity.yefeng.yfsms.domain.usecase.sms.SmsUtil;
import com.yefeng.support.base.UseCase;
import com.yefeng.support.http.HttpSchedulersTransformer;

import java.util.HashMap;

import javax.inject.Inject;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;

/**
 * Created by yefeng on 03/08/2017.
 */

public class QueryContactTask extends UseCase<QueryContactTask.Req, QueryContactTask.Res> {

    @Inject
    public QueryContactTask() {

    }

    @Override
    protected Disposable executeUseCase(Req requestValues) {
        return Flowable
                .fromCallable(() ->
                        SmsUtil.queryContact(requestValues.queryStr, requestValues.resolver))
                .compose(new HttpSchedulersTransformer<>())
                .subscribe(map -> {
                    if (null != getUseCaseCallback()) {
                        getUseCaseCallback().onSuccess(new Res(map));
                    }
                }, throwable -> {
                    if (null != getUseCaseCallback()) {
                        getUseCaseCallback().onError(throwable.getMessage() + "");
                    }
                });
    }

    public static class Req implements UseCase.RequestValues {
        public String queryStr;
        public ContentResolver resolver;

        public Req(String queryStr, ContentResolver resolver) {
            this.queryStr = queryStr;
            this.resolver = resolver;
        }
    }

    public static class Res implements UseCase.ResponseValue {
        public HashMap<String, String> map;

        public Res(HashMap<String, String> map) {
            this.map = null == map ? new HashMap<>() : map;
        }
    }
}
