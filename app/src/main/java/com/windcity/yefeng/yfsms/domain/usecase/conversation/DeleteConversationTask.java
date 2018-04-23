package com.windcity.yefeng.yfsms.domain.usecase.conversation;

import android.content.Context;

import com.windcity.yefeng.yfsms.data.db.DbHelper;
import com.windcity.yefeng.yfsms.data.model.Conversation;
import com.windcity.yefeng.yfsms.data.model.Conversation_;
import com.windcity.yefeng.yfsms.domain.usecase.sms.SmsCenter;
import com.windcity.yefeng.yfsms.domain.usecase.sms.SmsType;
import com.yefeng.support.base.UseCase;
import com.yefeng.support.http.HttpSchedulersTransformer;

import java.util.ArrayList;
import java.util.List;

import io.objectbox.Box;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;

/**
 * Created by yefeng on 17/08/2017.
 */

public class DeleteConversationTask extends UseCase<DeleteConversationTask.Req, DeleteConversationTask.Res> {

    private static final int NO_TAB = -1000;

    @Override
    protected Disposable executeUseCase(Req requestValues) {
        return Flowable.fromCallable(
                () -> SmsCenter.deleteConversations(
                        getNeedDeleteConversations(requestValues), requestValues.context))
                .compose(new HttpSchedulersTransformer<>())
                .subscribe(b -> {
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
                });
    }

    private ArrayList<Conversation> getNeedDeleteConversations(Req req) {
        if (req.tabsId == NO_TAB) {
            return req.conversations;
        }
        if (null == req.conversations) {
            req.conversations = new ArrayList<>();
        }
        req.conversations.clear();
        Box<Conversation> conversationBox = DbHelper.getInstance().getBoxStore().boxFor(Conversation.class);
        List<Conversation> list;
        switch (req.tabsId) {
            case SmsType.TYPE_ALL:
                list = conversationBox.getAll();
                break;
            default:
                list = conversationBox.query()
                        .equal(Conversation_.tabsId, req.tabsId)
                        .build()
                        .find();
        }
        if (null != list && !list.isEmpty()) {
            req.conversations.addAll(list);
        }
        return req.conversations;
    }

    public static class Req implements UseCase.RequestValues {
        public ArrayList<Conversation> conversations;
        public Context context;
        public int tabsId;

        public Req(ArrayList<Conversation> conversations, Context context) {
            this.conversations = conversations;
            this.context = context;
            this.tabsId = NO_TAB;
        }

        public Req(Context context, int tabsId) {
            this.context = context;
            this.conversations = null;
            this.tabsId = tabsId;
        }
    }

    public static class Res implements UseCase.ResponseValue {
        public boolean isDeleteSuccess;

        public Res(boolean deleteSuccess) {
            this.isDeleteSuccess = deleteSuccess;
        }
    }
}
