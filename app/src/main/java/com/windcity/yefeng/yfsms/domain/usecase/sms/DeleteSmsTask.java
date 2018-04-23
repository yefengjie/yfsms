package com.windcity.yefeng.yfsms.domain.usecase.sms;

import android.content.Context;

import com.windcity.yefeng.yfsms.data.model.Sms;
import com.yefeng.support.base.UseCase;
import com.yefeng.support.http.HttpSchedulersTransformer;

import java.util.ArrayList;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;

/**
 * Created by yefeng on 17/08/2017.
 */

public class DeleteSmsTask extends UseCase<DeleteSmsTask.Req, DeleteSmsTask.Res> {

    @Override
    protected Disposable executeUseCase(Req req) {
        return Flowable.fromCallable(
                () -> SmsCenter.deleteSmses(req.smses, req.context))
                .compose(new HttpSchedulersTransformer<>())
                .subscribe(
                        b -> {
                            if (null != getUseCaseCallback()) {
                                if (b) {
                                    getUseCaseCallback().onSuccess(new Res(b));
                                } else {
                                    getUseCaseCallback().onError("delete failed");
                                }
                            }
                        }, throwable -> {
                            if (null != getUseCaseCallback()) {
                                getUseCaseCallback().onError(throwable.getMessage() + "");
                            }
                        }
                );
    }

    public static class Req implements UseCase.RequestValues {
        public Context context;
        public ArrayList<Sms> smses;

        public Req(Context context, ArrayList<Sms> smses) {
            this.context = context;
            this.smses = smses;
        }

        public Req(Context context, Sms sms) {
            this.context = context;
            this.smses = new ArrayList<>();
            if (null != sms) {
                this.smses.add(sms);
            }
        }
    }

    public static class Res implements UseCase.ResponseValue {
        public boolean isDeleteSuccess;

        public Res(boolean deleteSuccess) {
            this.isDeleteSuccess = deleteSuccess;
        }
    }
}
