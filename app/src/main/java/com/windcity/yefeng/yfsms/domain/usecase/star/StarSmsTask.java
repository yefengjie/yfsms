package com.windcity.yefeng.yfsms.domain.usecase.star;

import com.windcity.yefeng.yfsms.data.model.Sms;

/**
 * Created by yefeng on 24/08/2017.
 */

public class StarSmsTask {
    public static boolean start(Sms sms) {
        return StarCenter.starSms(sms);
    }

}
