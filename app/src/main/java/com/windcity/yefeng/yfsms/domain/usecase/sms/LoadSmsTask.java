package com.windcity.yefeng.yfsms.domain.usecase.sms;

import com.windcity.yefeng.yfsms.data.db.DbHelper;
import com.windcity.yefeng.yfsms.data.model.Conversation;
import com.windcity.yefeng.yfsms.data.model.ConversationItem;
import com.windcity.yefeng.yfsms.data.model.Sms;
import com.windcity.yefeng.yfsms.data.model.Sms_;
import com.yefeng.support.base.UseCase;
import com.yefeng.support.http.HttpSchedulersTransformer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import io.objectbox.Box;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;

/**
 * Created by yefeng on 27/07/2017.
 */

public class LoadSmsTask extends UseCase<LoadSmsTask.Req, LoadSmsTask.Res> {

    @Inject
    public LoadSmsTask() {
    }

    @Override
    protected Disposable executeUseCase(Req requestValues) {
        return Flowable.fromCallable(() ->
                loadSms(requestValues.conversations))
                .compose(new HttpSchedulersTransformer<>())
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

    private ArrayList<ConversationItem> loadSms(ArrayList<Conversation> conversations) {
        if (null == conversations || conversations.isEmpty()) {
            return new ArrayList<>();
        }
        ArrayList<ConversationItem> resultList = new ArrayList<>();
        Box<Sms> smsBox = DbHelper.getInstance().getBoxStore().boxFor(Sms.class);
        if (conversations.size() == 1) {
            List<Sms> list = smsBox.query()
                    .equal(Sms_.address, conversations.get(0).address)
                    .orderDesc(Sms_._id)
                    .notEqual(Sms_.type, SmsConst.TYPE_DRAFT)
                    .build()
                    .find();
            if (list.isEmpty()) {
                return resultList;
            }
            int size = list.size();
            for (int i = 0; i < size; i++) {
                ConversationItem item = new ConversationItem();
                item.conversation = conversations.get(0);
                item.sms = list.get(i);
                resultList.add(item);
            }
            return resultList;
        }
        int size = conversations.size();
        for (int i = 0; i < size; i++) {
            List<Sms> list = smsBox.query()
                    .equal(Sms_.address, conversations.get(i).address)
                    .orderDesc(Sms_._id)
                    .notEqual(Sms_.type, SmsConst.TYPE_DRAFT)
                    .build()
                    .find();
            if (!list.isEmpty()) {
                for (Sms sms : list) {
                    ConversationItem item = new ConversationItem();
                    item.conversation = conversations.get(i);
                    item.sms = sms;
                    resultList.add(item);
                }
            }
        }
        Collections.sort(resultList, (item1, item2) -> (int) (item2.sms._id - item1.sms._id));
        return resultList;
    }


    public static class Req implements UseCase.RequestValues {
        public ArrayList<Conversation> conversations;

        public Req(ArrayList<Conversation> conversations) {
            this.conversations = conversations;
        }
    }

    public static class Res implements UseCase.ResponseValue {
        public ArrayList<ConversationItem> items;

        public Res(ArrayList<ConversationItem> items) {
            this.items = items;
        }
    }
}
