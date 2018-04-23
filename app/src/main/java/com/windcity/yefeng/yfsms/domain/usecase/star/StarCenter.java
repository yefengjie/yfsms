package com.windcity.yefeng.yfsms.domain.usecase.star;

import android.text.TextUtils;

import com.windcity.yefeng.yfsms.data.db.DbHelper;
import com.windcity.yefeng.yfsms.data.model.Sms;
import com.windcity.yefeng.yfsms.data.model.StarSms;
import com.windcity.yefeng.yfsms.data.model.StarSms_;

import java.util.List;

import io.objectbox.Box;

/**
 * Created by yefeng on 24/08/2017.
 */

public class StarCenter {
    public static boolean starSms(Sms sms) {
        if (null == sms) {
            return false;
        }
        Box<StarSms> starSmsBox = DbHelper.getInstance().getBoxStore().boxFor(StarSms.class);
        StarSms starSms = starSmsBox.get(sms.id);
        if (null != starSms) {
            return true;
        }
        starSms = new StarSms(sms.id, sms.tabsId, sms.conversationId, sms.isSpam, sms.isStar, sms.isDelete, sms._id, sms.thread_id, sms.address, sms.person, sms.date, sms.protocol, sms.read, sms.status, sms.type, sms.body, sms.service_center, sms.name);
        starSmsBox.put(starSms);
        return true;
    }

    public static List<StarSms> loadStartSms() {
        Box<StarSms> starSmsBox = DbHelper.getInstance().getBoxStore().boxFor(StarSms.class);
        List<StarSms> list = starSmsBox.query()
                .orderDesc(StarSms_._id)
                .build()
                .find();
        if (list.isEmpty()) {
            return list;
        }
        //从sms表中重新同步联系人的名字,以防用户更新联系人的名字
        Box<Sms> smsBox = DbHelper.getInstance().getBoxStore().boxFor(Sms.class);
        for (StarSms starSms : list) {
            Sms sms = smsBox.get(starSms.id);
            if (null != sms
                    && !TextUtils.isEmpty(sms.name)
                    && !sms.name.equals(starSms.name)) {
                starSms.name = sms.name;
                starSmsBox.put(starSms);
            }
        }
        return list;
    }

    public static void unstarSms(StarSms starSms) {
        if (null == starSms) {
            return;
        }
        Box<StarSms> starSmsBox = DbHelper.getInstance().getBoxStore().boxFor(StarSms.class);
        starSmsBox.remove(starSms.id);
    }
}
