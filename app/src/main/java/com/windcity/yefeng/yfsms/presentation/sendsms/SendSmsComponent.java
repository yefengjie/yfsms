package com.windcity.yefeng.yfsms.presentation.sendsms;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by yefeng on 03/08/2017.
 */
@Singleton
@Component(modules = SendSmsModule.class)
public interface SendSmsComponent {
    void inject(SendSmsActivity sendSmsActivity);
}
