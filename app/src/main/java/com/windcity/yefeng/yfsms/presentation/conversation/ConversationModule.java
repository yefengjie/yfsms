package com.windcity.yefeng.yfsms.presentation.conversation;

import com.windcity.yefeng.yfsms.domain.usecase.conversation.DeleteConversationTask;
import com.windcity.yefeng.yfsms.domain.usecase.sms.DeleteSmsTask;
import com.windcity.yefeng.yfsms.domain.usecase.sms.LoadSmsTask;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by yefeng on 27/07/2017.
 */
@Module
public class ConversationModule {

    private final ConversationActivity mConversationActivity;

    public ConversationModule(ConversationActivity conversationActivity) {
        this.mConversationActivity = conversationActivity;
    }

    @Singleton
    @Provides
    ConversationAdapter provideAdapter() {
        int size = 0;
        if (null != mConversationActivity.getConversations()) {
            size = mConversationActivity.getConversations().size();
        }
        return new ConversationAdapter(null, size);
    }

    @Singleton
    @Provides
    ConversationPresenter providePresenter() {
        return new ConversationPresenter(mConversationActivity);
    }

    @Singleton
    @Provides
    LoadSmsTask provideLoadSmsTask() {
        return new LoadSmsTask();
    }

    @Singleton
    @Provides
    DeleteSmsTask provideDeleteSmsTask() {
        return new DeleteSmsTask();
    }

    @Singleton
    @Provides
    DeleteConversationTask providerDeleteConversationTask() {
        return new DeleteConversationTask();
    }

}
