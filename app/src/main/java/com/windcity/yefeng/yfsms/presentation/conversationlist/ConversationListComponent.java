package com.windcity.yefeng.yfsms.presentation.conversationlist;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by yefeng on 19/07/2017.
 */
@Singleton
@Component(modules = ConversationListModule.class)
public interface ConversationListComponent {
    void inject(ConversationListFragment conversationListFragment);
}
