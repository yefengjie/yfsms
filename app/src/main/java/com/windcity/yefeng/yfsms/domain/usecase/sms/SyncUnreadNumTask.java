package com.windcity.yefeng.yfsms.domain.usecase.sms;

import com.windcity.yefeng.yfsms.data.db.DbHelper;
import com.windcity.yefeng.yfsms.data.model.Conversation;
import com.windcity.yefeng.yfsms.data.model.Conversation_;
import com.windcity.yefeng.yfsms.data.model.Sms;
import com.windcity.yefeng.yfsms.data.model.Sms_;
import com.windcity.yefeng.yfsms.data.model.Tabs;
import com.yefeng.support.rxbus.RxBus;

import java.util.ArrayList;
import java.util.List;

import io.objectbox.Box;

/**
 * Created by yefeng on 21/08/2017.
 */

public class SyncUnreadNumTask {
    public static void start() {
        Box<Tabs> tabsBox = DbHelper.getInstance().getBoxStore().boxFor(Tabs.class);
        List<Tabs> tabsList = tabsBox.getAll();
        if (null == tabsList) {
            tabsList = new ArrayList<>();
        }
        if (!tabsList.isEmpty()) {
            for (Tabs tabs : tabsList) {
                tabs.unreadNum = 0;
            }
        }

        Box<Conversation> conversationBox = DbHelper.getInstance().getBoxStore().boxFor(Conversation.class);
        List<Conversation> conversationList = conversationBox.query()
                .greater(Conversation_.unreadNum, 0)
                .build()
                .find();
        for (Conversation conversation : conversationList) {
            conversation.unreadNum = 0;
        }
        conversationBox.put(conversationList);

        Box<Sms> smsBox = DbHelper.getInstance().getBoxStore().boxFor(Sms.class);
        List<Sms> smsList = smsBox.query()
                .equal(Sms_.read, SmsConst.READ_unread)
                .build()
                .find();

        for (Sms sms : smsList) {
            for (Tabs tabs : tabsList) {
                if (tabs.id == sms.tabsId) {
                    tabs.unreadNum += 1;
                    if (tabs.newestUnreadSmsTime < sms.date) {
                        tabs.newestUnreadSmsTime = sms.date;
                    }
                    break;
                }
            }

            Conversation conversation = conversationBox.query()
                    .equal(Conversation_.address, sms.address)
                    .build()
                    .findFirst();
            if (null != conversation) {
                conversation.unreadNum += 1;
                conversationBox.put(conversation);
            }
        }
        tabsBox.put(tabsList);
        RxBus.getBus().send(new SmsEvent.UnreadNumChange());
    }
}
