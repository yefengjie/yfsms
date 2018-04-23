package com.windcity.yefeng.yfsms.presentation.drawer;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by yefeng on 18/08/2017.
 */
@Module
public class DrawerModule {

    private DrawerActivity mDrawerActivity;

    public DrawerModule(DrawerActivity drawerActivity) {
        this.mDrawerActivity = drawerActivity;
    }

    @Singleton
    @Provides
    DrawerPresenter providerPresenter() {
        return new DrawerPresenter(mDrawerActivity);
    }
}
