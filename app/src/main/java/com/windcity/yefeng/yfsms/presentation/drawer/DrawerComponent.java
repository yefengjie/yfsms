package com.windcity.yefeng.yfsms.presentation.drawer;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by yefeng on 18/08/2017.
 */
@Singleton
@Component(modules = DrawerModule.class)
public interface DrawerComponent {
    void inject(DrawerActivity drawerActivity);
}
