package com.windcity.yefeng.yfsms.domain.usecase.sms;

import android.content.Context;

import com.windcity.yefeng.yfsms.data.db.DbHelper;
import com.windcity.yefeng.yfsms.data.model.Sms;

import java.util.ArrayList;

import io.objectbox.Box;

/**
 * Created by yefeng on 14/08/2017.
 */

public class MarkSmsReadTask {
    public static void start(Context context, ArrayList<Sms> list) {
        if (null == context || null == list || list.isEmpty()) {
            return;
        }
        for (Sms sms : list) {
            sms.read = SmsConst.READ_read;
        }
        Box<Sms> smsBox = DbHelper.getInstance().getBoxStore().boxFor(Sms.class);
        smsBox.put(list);
        //mark read in system db
        SmsCenter.markSmsRead(context, list);
        SyncUnreadNumTask.start();
    }
}
