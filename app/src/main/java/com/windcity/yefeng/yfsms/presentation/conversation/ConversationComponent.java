package com.windcity.yefeng.yfsms.presentation.conversation;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by yefeng on 27/07/2017.
 */
@Singleton
@Component(modules = ConversationModule.class)
public interface ConversationComponent {
    void inject(ConversationActivity conversationActivity);
}
