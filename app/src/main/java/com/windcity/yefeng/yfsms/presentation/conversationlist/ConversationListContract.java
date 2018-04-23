package com.windcity.yefeng.yfsms.presentation.conversationlist;

import android.content.Context;

import com.windcity.yefeng.yfsms.data.model.Conversation;
import com.windcity.yefeng.yfsms.domain.usecase.conversation.DeleteConversationTask;
import com.windcity.yefeng.yfsms.domain.usecase.conversation.LoadConversationTask;

import java.util.ArrayList;

/**
 * Created by yefeng on 19/07/2017.
 */

public class ConversationListContract {
    public interface Presenter {
        void subscribe();

        void unSubscribe();

        void deleteConversation(ArrayList<Conversation> list);
    }

    public interface View {
        Context getCtx();

        LoadConversationTask getTask();

        int getType();

        void setData(ArrayList<ArrayList<Conversation>> list);

        void showToast(String msg);

        DeleteConversationTask getDeleteConversationTask();
    }
}
