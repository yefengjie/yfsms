package com.windcity.yefeng.yfsms.domain.usecase.contact;

import android.content.ContentResolver;
import android.content.Context;
import android.text.TextUtils;

import com.windcity.yefeng.yfsms.data.db.DbHelper;
import com.windcity.yefeng.yfsms.data.model.Conversation;
import com.windcity.yefeng.yfsms.data.model.Conversation_;
import com.windcity.yefeng.yfsms.data.model.Sms;
import com.windcity.yefeng.yfsms.data.model.Sms_;
import com.windcity.yefeng.yfsms.domain.usecase.sms.SmsType;
import com.windcity.yefeng.yfsms.domain.usecase.sms.SmsUtil;
import com.yefeng.support.rxbus.RxBus;

import java.util.ArrayList;
import java.util.List;

import io.objectbox.Box;

/**
 * Created by yefeng on 23/08/2017.
 */

public class SyncContactNameTask {
    public static void start(String address, Context context) {
        if (null == context) {
            return;
        }
        Box<Sms> smsBox = DbHelper.getInstance().getBoxStore().boxFor(Sms.class);
        List<Sms> list;
        if (!TextUtils.isEmpty(address)) {
            Sms sms = smsBox.query()
                    .equal(Sms_.address, address)
                    .build()
                    .findFirst();
            if (null == sms) {
                return;
            }
            list = new ArrayList<>();
            list.add(sms);
        } else {
            list = smsBox.query()
                    .equal(Sms_.tabsId, SmsType.TYPE_CONTACT)
                    .or()
                    .equal(Sms_.tabsId, SmsType.TYPE_STRANGER)
                    .build()
                    .find();
        }
        if (list.isEmpty()) {
            return;
        }
        doSync(list, context, smsBox);
    }

    private static void doSync(List<Sms> list, Context context, Box<Sms> smsBox) {
        Box<Conversation> conversationBox = DbHelper.getInstance().getBoxStore().boxFor(Conversation.class);
        ContentResolver cr = context.getContentResolver();
        boolean hasChange = false;
        for (Sms sms : list) {
            String name = SmsUtil.getPeopleNameFromPerson(sms.address, cr);
            if (!TextUtils.isEmpty(name)
                    && !name.equals(sms.name)) {
                sms.name = name;
                smsBox.put(sms);
                Conversation conversation = conversationBox.query()
                        .equal(Conversation_.address, sms.address)
                        .build()
                        .findFirst();
                if (null != conversation) {
                    conversation.name = sms.name;
                    conversationBox.put(conversation);
                }
                hasChange = true;
            }
        }
        if (hasChange) {
            RxBus.getBus().send(new SystemContactEvent.Update());
        }
    }
}
