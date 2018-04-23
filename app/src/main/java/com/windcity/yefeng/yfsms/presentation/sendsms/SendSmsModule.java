package com.windcity.yefeng.yfsms.presentation.sendsms;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by yefeng on 03/08/2017.
 */
@Module
public class SendSmsModule {
    private SendSmsActivity mSendSmsActivity;

    public SendSmsModule(SendSmsActivity sendSmsActivity) {
        this.mSendSmsActivity = sendSmsActivity;
    }

    @Singleton
    @Provides
    SendSmsPresenter providerPresenter() {
        return new SendSmsPresenter(mSendSmsActivity);
    }
}
