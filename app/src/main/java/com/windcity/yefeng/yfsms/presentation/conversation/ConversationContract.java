package com.windcity.yefeng.yfsms.presentation.conversation;

import android.content.Context;

import com.windcity.yefeng.yfsms.data.model.Conversation;
import com.windcity.yefeng.yfsms.data.model.ConversationItem;
import com.windcity.yefeng.yfsms.data.model.Sms;
import com.windcity.yefeng.yfsms.domain.usecase.conversation.DeleteConversationTask;
import com.windcity.yefeng.yfsms.domain.usecase.sms.DeleteSmsTask;
import com.windcity.yefeng.yfsms.domain.usecase.sms.LoadSmsTask;

import java.util.ArrayList;

/**
 * Created by yefeng on 27/07/2017.
 */

public class ConversationContract {

    public interface Presenter {
        void subscribe();

        void unSubscribe();

        void doSend(String address);

        void deleteSms(ArrayList<Sms> smses);

        void deleteConversation(ArrayList<Conversation> mConversations);
    }

    public interface View {
        Context getCtx();

        ArrayList<ConversationItem> getData();

        void setData(ArrayList<ConversationItem> items);

        void showToast(String msg);

        LoadSmsTask getLoadSmsTask();

        DeleteSmsTask getDeleteSmsTask();

        ArrayList<Conversation> getConversations();

        String getContent();

        void clearContent();

        DeleteConversationTask getDeleteConversationTask();

        void onDeleteConversationSuccess();
    }
}
