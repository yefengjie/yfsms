package com.windcity.yefeng.yfsms.presentation.classify;

import com.windcity.yefeng.yfsms.domain.usecase.conversation.DeleteConversationTask;
import com.windcity.yefeng.yfsms.domain.usecase.tabs.LoadTabsTask;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by yefeng on 21/08/2017.
 */
@Module
public class ClassifyModule {
    private ClassifyFragment mClassifyFragment;

    public ClassifyModule(ClassifyFragment classifyFragment) {
        this.mClassifyFragment = classifyFragment;
    }

    @Singleton
    @Provides
    ClassifyPresenter providerPresenter() {
        return new ClassifyPresenter(mClassifyFragment);
    }

    @Singleton
    @Provides
    LoadTabsTask providerLoadTabsUnreadNumTask() {
        return new LoadTabsTask();
    }

    @Singleton
    @Provides
    DeleteConversationTask proivderConversationTask() {
        return new DeleteConversationTask();
    }
}
