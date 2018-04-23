package com.windcity.yefeng.yfsms.presentation.classify;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by yefeng on 21/08/2017.
 */
@Singleton
@Component(modules = ClassifyModule.class)
public interface ClassifyComponent {
    void inject(ClassifyFragment classifyFragment);
}
