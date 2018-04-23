package com.windcity.yefeng.yfsms.domain.usecase.conversation;

import com.windcity.yefeng.yfsms.data.db.DbHelper;
import com.windcity.yefeng.yfsms.data.model.Conversation;
import com.windcity.yefeng.yfsms.data.model.Conversation_;
import com.windcity.yefeng.yfsms.domain.usecase.sms.SmsType;
import com.yefeng.support.base.UseCase;
import com.yefeng.support.http.HttpSchedulersTransformer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.objectbox.Box;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;

/**
 * Created by yefeng on 19/07/2017.
 */

public class LoadConversationTask extends UseCase<LoadConversationTask.Req, LoadConversationTask.Res> {

    @Inject
    public LoadConversationTask() {
    }

    @Override
    protected Disposable executeUseCase(Req requestValues) {
        return Flowable.fromCallable(() -> {
            Box<Conversation> conversationBox = DbHelper.getInstance().getBoxStore().boxFor(Conversation.class);
            if (requestValues.tabsId == SmsType.TYPE_ALL) {
                Res res = new Res();
                res.unfoldConversations = new ArrayList<Conversation>();
                List<Conversation> list = conversationBox.query()
                        .orderDesc(Conversation_.newestSmsId)
                        .build()
                        .find();
                if (!list.isEmpty()) {
                    res.unfoldConversations.addAll(list);
                }
                return res;
            }
            List<Conversation> list = conversationBox.query()
                    .equal(Conversation_.tabsId, requestValues.tabsId)
                    .orderDesc(Conversation_.newestSmsId)
                    .build()
                    .find();
            return foldConversation(list);
        }).compose(new HttpSchedulersTransformer<>())
                .subscribe(res -> {
                    if (null != getUseCaseCallback()) {
                        getUseCaseCallback().onSuccess(res);
                    }
                }, throwable -> {
                    if (null != getUseCaseCallback()) {
                        getUseCaseCallback().onError(throwable.getMessage() + "");
                    }
                });
    }

    private Res foldConversation(List<Conversation> list) {
        Res res = new Res();
        if (null == list || list.isEmpty()) {
            return res;
        }
        LinkedHashMap<String, ArrayList<Conversation>> hashMap = new LinkedHashMap<>();
        for (Conversation conversation : list) {
            ArrayList<Conversation> conversations = hashMap.get(conversation.name);
            if (null == conversations) {
                conversations = new ArrayList<>();
            }
            conversations.add(conversation);
            hashMap.put(conversation.name, conversations);
        }
        ArrayList<ArrayList<Conversation>> foldConversations = new ArrayList<>();

        Iterator<Map.Entry<String, ArrayList<Conversation>>> iterator = hashMap.entrySet().iterator();
        int unReadPos = 0;
        while (iterator.hasNext()) {
            Map.Entry<String, ArrayList<Conversation>> entry = iterator.next();
            ArrayList<Conversation> tempList = entry.getValue();
            int unReadNum = 0;
            for (Conversation c : tempList) {
                unReadNum += c.unreadNum;
            }
            if (unReadNum > 0) {
                foldConversations.add(unReadPos, tempList);
                unReadPos++;
            } else {
                foldConversations.add(tempList);
            }
            res.unReadNum += unReadNum;
        }
        res.foldConversations = foldConversations;
        return res;
    }

    public static class Req implements UseCase.RequestValues {
        public int tabsId;

        public Req(int tabsId) {
            this.tabsId = tabsId;
        }
    }

    public static class Res implements UseCase.ResponseValue {
        public ArrayList<ArrayList<Conversation>> foldConversations;
        public int unReadNum = 0;
        public ArrayList<Conversation> unfoldConversations;

        public Res() {
            this.unReadNum = 0;
        }
    }
}
