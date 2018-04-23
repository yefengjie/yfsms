package com.windcity.yefeng.yfsms.domain.usecase.star;

import com.windcity.yefeng.yfsms.data.model.StarSms;

/**
 * Created by yefeng on 24/08/2017.
 */

public class UnstarSmsTask {
    public static void start(StarSms starSms) {
        StarCenter.unstarSms(starSms);
    }
}
