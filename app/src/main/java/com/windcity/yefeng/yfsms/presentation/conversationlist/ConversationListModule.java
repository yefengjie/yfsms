package com.windcity.yefeng.yfsms.presentation.conversationlist;


import com.windcity.yefeng.yfsms.domain.usecase.conversation.DeleteConversationTask;
import com.windcity.yefeng.yfsms.domain.usecase.conversation.LoadConversationTask;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by yefeng on 19/07/2017.
 */
@Module
public class ConversationListModule {
    private final ConversationListFragment mConversationListFragment;

    public ConversationListModule(ConversationListFragment conversationListFragment) {
        this.mConversationListFragment = conversationListFragment;
    }

    @Provides
    @Singleton
    ConversationListAdapter providerAdapter() {
        return new ConversationListAdapter(null, mConversationListFragment);
    }

    @Provides
    @Singleton
    ConversationListPresenter providerPresenter() {
        return new ConversationListPresenter(mConversationListFragment);
    }

    @Provides
    @Singleton
    LoadConversationTask providerLoadConversationTask() {
        return new LoadConversationTask();
    }

    @Provides
    @Singleton
    DeleteConversationTask providerDeleteConversationTask() {
        return new DeleteConversationTask();
    }
}

