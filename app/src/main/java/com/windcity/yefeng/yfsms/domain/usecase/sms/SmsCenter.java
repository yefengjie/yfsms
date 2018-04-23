package com.windcity.yefeng.yfsms.domain.usecase.sms;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.windcity.yefeng.yfsms.data.db.DbHelper;
import com.windcity.yefeng.yfsms.data.model.Contact;
import com.windcity.yefeng.yfsms.data.model.Contact_;
import com.windcity.yefeng.yfsms.data.model.Conversation;
import com.windcity.yefeng.yfsms.data.model.Conversation_;
import com.windcity.yefeng.yfsms.data.model.Sms;
import com.windcity.yefeng.yfsms.data.model.Sms_;
import com.windcity.yefeng.yfsms.data.model.Tabs;
import com.windcity.yefeng.yfsms.domain.service.TaskService;
import com.yefeng.support.rxbus.RxBus;

import java.util.ArrayList;
import java.util.List;

import io.objectbox.Box;
import timber.log.Timber;

/**
 * Created by yefeng on 25/07/2017.
 */

public class SmsCenter {

    /**
     * new sms, save in draft
     */
    public static Uri newSentSms(Context context, String address, String content, String serviceCenter) {
        if (null == context || TextUtils.isEmpty(address) || TextUtils.isEmpty(content)) {
            return null;
        }
        address = handleCn(address);
        Uri newSmsUri = null;
        Cursor cursor = null;
        try {
            ContentResolver cr = context.getContentResolver();
            ContentValues values = new ContentValues();
            values.put(SmsConst.ADDRESS, address);
            values.put(SmsConst.BODY, content);
            values.put(SmsConst.READ, SmsConst.READ_read); //"0" for have not read sms and "1" for have read sms
            values.put(SmsConst.PROTOCOL, SmsConst.RPOTO_SMS);
            values.put(SmsConst.STATUS, SmsConst.STATUS_PENDING);
            values.put(SmsConst.TYPE, SmsConst.TYPE_QUEUED);
            if (!TextUtils.isEmpty(serviceCenter)) {
                values.put(SmsConst.SERVICE_CENTER, serviceCenter);
            }
            newSmsUri = cr.insert(SmsConst.SMS_OUTBOX, values);
            if (null != newSmsUri) {
                Timber.e("newSentSms:insert:" + newSmsUri.toString());
                cursor = cr.query(newSmsUri, null, null, null, null);
            } else {
                Timber.e("newSentSms:insert:null");
            }
            if (null != cursor && cursor.getCount() > 0) {
                cursor.moveToFirst();
                Sms sms = new Sms();
                sms._id = cursor.getLong(cursor.getColumnIndex(SmsConst._ID));
                sms.id = sms._id;
                sms.thread_id = cursor.getLong(cursor.getColumnIndex(SmsConst.THREAD_ID));
                sms.address = cursor.getString(cursor.getColumnIndex(SmsConst.ADDRESS));
                sms.person = cursor.getString(cursor.getColumnIndex(SmsConst.PERSON));
                sms.date = cursor.getLong(cursor.getColumnIndex(SmsConst.DATE));
                sms.protocol = cursor.getInt(cursor.getColumnIndex(SmsConst.PROTOCOL));
                sms.read = cursor.getInt(cursor.getColumnIndex(SmsConst.READ));
                sms.status = cursor.getInt(cursor.getColumnIndex(SmsConst.STATUS));
                sms.type = cursor.getInt(cursor.getColumnIndex(SmsConst.TYPE));
                sms.body = cursor.getString(cursor.getColumnIndex(SmsConst.BODY));
                sms.service_center = cursor.getString(cursor.getColumnIndex(SmsConst.SERVICE_CENTER));

                Box<Tabs> tabsBox = DbHelper.getInstance().getBoxStore().boxFor(Tabs.class);
                Box<Sms> smsBox = DbHelper.getInstance().getBoxStore().boxFor(Sms.class);
                Box<Contact> contactBox = DbHelper.getInstance().getBoxStore().boxFor(Contact.class);
                Box<Conversation> conversationBox = DbHelper.getInstance().getBoxStore().boxFor(Conversation.class);
                SmsCenter.syncSms(sms, tabsBox, conversationBox, smsBox, cr, contactBox);
                RxBus.getBus().send(new SmsEvent.New());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                try {  // 有可能cursor都没有创建成功
                    cursor.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return newSmsUri;
    }

    public static void receiveSms(Context context, String content, String address) {
        if (null == context || TextUtils.isEmpty(address) || TextUtils.isEmpty(content)) {
            return;
        }
        address = handleCn(address);
        Cursor cursor = null;
        try {
            ContentResolver cr = context.getContentResolver();
            ContentValues values = new ContentValues();
            values.put(SmsConst.ADDRESS, address);
            values.put(SmsConst.BODY, content);
            values.put(SmsConst.READ, SmsConst.READ_unread); //"0" for have not read sms and "1" for have read sms
            values.put(SmsConst.PROTOCOL, SmsConst.RPOTO_SMS);
            values.put(SmsConst.STATUS, SmsConst.STATUS_RECEIVED);
            values.put(SmsConst.TYPE, SmsConst.TYPE_INBOX);
            Uri newSmsUri = cr.insert(SmsConst.SMS_OUTBOX, values);
            if (null != newSmsUri) {
                Timber.e("receiveSms:insert:" + newSmsUri.toString());
                cursor = cr.query(newSmsUri, null, null, null, null);
            } else {
                Timber.e("receiveSms:insert:null");
            }
            if (null != cursor && cursor.getCount() > 0) {
                cursor.moveToFirst();
                Sms sms = new Sms();
                sms._id = cursor.getLong(cursor.getColumnIndex(SmsConst._ID));
                sms.id = sms._id;
                sms.thread_id = cursor.getLong(cursor.getColumnIndex(SmsConst.THREAD_ID));
                sms.address = cursor.getString(cursor.getColumnIndex(SmsConst.ADDRESS));
                sms.person = cursor.getString(cursor.getColumnIndex(SmsConst.PERSON));
                sms.date = cursor.getLong(cursor.getColumnIndex(SmsConst.DATE));
                sms.protocol = cursor.getInt(cursor.getColumnIndex(SmsConst.PROTOCOL));
                sms.read = cursor.getInt(cursor.getColumnIndex(SmsConst.READ));
                //可能因为执行了查询，所以消息变成了已读，这里我们还原为未读
                sms.read = SmsConst.READ_unread;

                sms.status = cursor.getInt(cursor.getColumnIndex(SmsConst.STATUS));
                sms.type = cursor.getInt(cursor.getColumnIndex(SmsConst.TYPE));
                sms.body = cursor.getString(cursor.getColumnIndex(SmsConst.BODY));
                sms.service_center = cursor.getString(cursor.getColumnIndex(SmsConst.SERVICE_CENTER));

                Box<Tabs> tabsBox = DbHelper.getInstance().getBoxStore().boxFor(Tabs.class);
                Box<Sms> smsBox = DbHelper.getInstance().getBoxStore().boxFor(Sms.class);
                Box<Contact> contactBox = DbHelper.getInstance().getBoxStore().boxFor(Contact.class);
                Box<Conversation> conversationBox = DbHelper.getInstance().getBoxStore().boxFor(Conversation.class);
                sms = SmsCenter.syncSms(sms, tabsBox, conversationBox, smsBox, cr, contactBox);
                TaskService.syncUnreadNum(context);
                SmsNotification.notify(context, sms, conversationBox);
                RxBus.getBus().send(new SmsEvent.New());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                try {  // 有可能cursor都没有创建成功
                    cursor.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static Sms syncSms(Sms sms, Box<Tabs> tabsBox, Box<Conversation> conversationBox, Box<Sms> smsBox, ContentResolver re, Box<Contact> contactBox) {
        Timber.d("syncSms:" + sms.person + "_" + sms.address + "_" + sms.body);
        SmsUtil.avoidNull(sms);
        sms.address = handleCn(sms.address);
        setName(sms, contactBox, re);
        Tabs tabs = setTab(sms, tabsBox, re);
        Conversation conversation = setConversation(tabs, sms, conversationBox);
        return setSms(sms, conversation, tabs, smsBox);
    }

    public static void updateSendSmsStatus(Context context, Uri newSmsUri, int pos, int size, String action, int resultCode) {
        Cursor cursor = null;
        try {
            ContentResolver cr = context.getContentResolver();
            if (SmsConst.ACTION_SEND_SMS_OK.equals(action)) {
                ContentValues values = new ContentValues();
                values.put(SmsConst.TYPE, SmsConst.TYPE_SENT);
                values.put(SmsConst.STATUS, SmsConst.STATUS_RECEIVED);
                int i = cr.update(newSmsUri, values, null, null);
                Timber.e("updateSendSmsStatus: update:" + newSmsUri + "_" + i);
            } else if (SmsConst.ACTION_SEND_SMS.equals(action)) {
                ContentValues values = new ContentValues();
                values.put(SmsConst.TYPE, SmsConst.TYPE_SENT);
                if (resultCode <= 1) {//发送成功
                    if (pos == size - 1) {
                        values.put(SmsConst.STATUS, SmsConst.STATUS_COMPLETE);
                    }
                } else {
                    if (pos == size - 1) {
                        values.put(SmsConst.STATUS, SmsConst.STATUS_FAILED);
                    }
                }
                int i = cr.update(newSmsUri, values, null, null);
                Timber.e("updateSendSmsStatus: update:" + newSmsUri + "_" + i);
            }
            if (SmsConst.ACTION_SEND_SMS.equals(action) || SmsConst.ACTION_SEND_SMS_OK.equals(action)) {
                cursor = cr.query(newSmsUri, null, null, null, null);
                if (null != cursor && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    long newSmsId = cursor.getLong(cursor.getColumnIndex(SmsConst._ID));
                    Box<Sms> smsBox = DbHelper.getInstance().getBoxStore().boxFor(Sms.class);
                    Sms sms = smsBox.get(newSmsId);
                    if (null != sms) {
                        sms.status = cursor.getInt(cursor.getColumnIndex(SmsConst.STATUS));
                        sms.type = cursor.getInt(cursor.getColumnIndex(SmsConst.TYPE));
                        smsBox.put(sms);
                        RxBus.getBus().send(new SmsEvent.Update());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                try {  // 有可能cursor都没有创建成功
                    cursor.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String handleCn(String address) {
        if (null != address && address.contains("+86")) {
            address = address.substring(3, address.length());
        }
        return address;
    }

    private static Sms setSms(Sms sms, Conversation conversation, Tabs tabs, Box<Sms> smsBox) {
        sms.tabsId = tabs.id;
        sms.conversationId = conversation.id;
        sms.isSpam = false;
        sms.isStar = false;
        if (sms.type != SmsConst.TYPE_INBOX) {
            sms.read = SmsConst.READ_read;
        }
        smsBox.put(sms);
        return sms;
    }

    private static Tabs setTab(Sms sms, Box<Tabs> tabsBox, ContentResolver re) {
        long tabsId;
        if (SmsUtil.isServiceSms(sms.address)) {
            //服务号
            tabsId = SmsType.TYPE_SERVICE;
        } else if (SmsUtil.isNotifySms(sms.address)) {
            //通知类
            tabsId = SmsType.TYPE_NOTIFICATION;
        } else {
            if (TextUtils.isEmpty(sms.name) || sms.address.equals(sms.name)) {
                //陌生人
                tabsId = SmsType.TYPE_STRANGER;
            } else {
                //联系人
                tabsId = SmsType.TYPE_CONTACT;
            }
        }
        Tabs tabs = tabsBox.get(tabsId);
        if (null == tabs) {
            tabs = new Tabs();
            tabs.id = tabsId;
        }
        tabsBox.put(tabs);
        return tabs;
    }

    private static Conversation setConversation(Tabs tabs, Sms sms, Box<Conversation> conversationBox) {
        String conversationName = SmsUtil.getConversationName(sms);
        Conversation conversation = conversationBox.query()
                .equal(Conversation_.address, sms.address)
                .build()
                .findFirst();
        if (null == conversation) {
            conversation = new Conversation();
            conversation.totalNum = 0;
            conversation.unreadNum = 0;
        }
        conversation.name = conversationName;
        conversation.address = sms.address;
        conversation.newestProtocol = sms.protocol;
        if (conversation.newestProtocol == SmsConst.PROTO_MMS) {
            conversation.newestContent = "[彩信]暂不支持";
        } else {
            conversation.newestContent = sms.body;
        }
        conversation.newestTime = sms.date;
        conversation.newestSmsId = sms._id;
        conversation.totalNum += 1;
        conversation.tabsId = tabs.id;
        conversationBox.put(conversation);
        return conversation;
    }


    private static void setName(Sms sms, Box<Contact> contactBox, ContentResolver re) {
        if (SmsUtil.isNotifySms(sms.address) || SmsUtil.isServiceSms(sms.address)) {
            //服务号或者通知短信
            int startNamePos = sms.body.indexOf("【");
            int endNamePos = sms.body.indexOf("】");
            if (startNamePos != -1 && endNamePos != -1 && endNamePos > startNamePos && endNamePos - startNamePos <= 15) {
                sms.name = sms.body.substring(startNamePos + 1, endNamePos);
            }
            if (TextUtils.isEmpty(sms.name)) {
                if (startNamePos < 0 || endNamePos < 0) {
                    startNamePos = sms.body.indexOf("[");
                    endNamePos = sms.body.indexOf("]");
                    if (startNamePos != -1 && endNamePos != -1 && endNamePos > startNamePos && endNamePos - startNamePos <= 15) {
                        sms.name = sms.body.substring(startNamePos + 1, endNamePos);
                    }
                }
            }
            Contact contact = contactBox.query().equal(Contact_.address, sms.address).build().findFirst();
            if (TextUtils.isEmpty(sms.name)) {
                if (null != contact) {
                    sms.name = contact.name;
                }
            } else {
                if (null != contact) {
                    if (!sms.name.equals(contact.name)) {
                        contact.name = sms.name;
                        contactBox.put(contact);
                    }
                } else {
                    contact = new Contact();
                    contact.name = sms.name;
                    contact.address = sms.address;
                    contactBox.put(contact);
                }
            }
        } else if (sms.address.length() >= 11) {
            //也许是联系人的短信
            sms.name = SmsUtil.getPeopleNameFromPerson(sms.address, re);
        }
    }


    public static void markSmsRead(Context context, ArrayList<Sms> smsList) {
        if (null == context || null == smsList || smsList.isEmpty()) {
            return;
        }
        try {
            ContentResolver cr = context.getContentResolver();
            ContentValues values = new ContentValues();
            values.put(SmsConst.READ, SmsConst.READ_read);
            for (Sms sms : smsList) {
                int i = cr.update(Uri.parse("content://sms/inbox/" + sms._id), values, null, null);
                Timber.e("markSmsRead: system db:" + sms._id + "_" + i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean deleteSmses(ArrayList<Sms> smses, Context context) {
        if (null == smses || smses.isEmpty() || null == context) {
            return true;
        }
        ContentResolver cr = context.getContentResolver();
        Box<Sms> smsBox = DbHelper.getInstance().getBoxStore().boxFor(Sms.class);
        Box<Conversation> conversationBox = DbHelper.getInstance().getBoxStore().boxFor(Conversation.class);
        for (Sms sms : smses) {
            //step one, delete sms in system db
            int i = cr.delete(Uri.parse("content://sms"), "_id=?", new String[]{String.valueOf(sms._id)});
            Timber.e("deleteSmses:" + sms._id + "_" + i);
            if (i < 0) {
                return false;
            }
            //step two, delete sms in app
            try {
                smsBox.remove(sms);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //step three, update conversation
            Conversation conversation = conversationBox.query()
                    .equal(Conversation_.address, sms.address)
                    .build()
                    .findFirst();
            if (null != conversation) {
                conversation.totalNum -= 1;
                if (conversation.totalNum <= 0) {
                    conversationBox.remove(conversation);
                } else {
                    //update newest sms
                    if (conversation.newestSmsId == sms._id) {
                        Sms newestSms = smsBox.query()
                                .equal(Sms_.address, conversation.address)
                                .orderDesc(Sms_._id)
                                .build()
                                .findFirst();
                        if (null != newestSms) {
                            conversation.newestProtocol = newestSms.protocol;
                            if (conversation.newestProtocol == SmsConst.PROTO_MMS) {
                                conversation.newestContent = "[彩信]暂不支持";
                            } else {
                                conversation.newestContent = newestSms.body;
                            }
                            conversation.newestTime = newestSms.date;
                            conversation.newestSmsId = newestSms._id;
                        }
                    }
                    conversationBox.put(conversation);
                }
            }
        }
        TaskService.syncUnreadNum(context);
        return true;
    }

    public static List<Tabs> initTabs() {
        Box<Tabs> tabsBox = DbHelper.getInstance().getBoxStore().boxFor(Tabs.class);
        if (tabsBox.count() <= 0) {
            ArrayList<Tabs> tabses = new ArrayList<>();
            for (int i = SmsType.TYPE_CONTACT; i <= SmsType.TYPE_NOTIFICATION; i++) {
                Tabs tabs = new Tabs();
                tabs.id = i;
                tabses.add(tabs);
            }
            tabsBox.put(tabses);
        }
        return tabsBox.getAll();
    }

    public static boolean deleteConversations(ArrayList<Conversation> conversations, Context context) {
        if (null == conversations || conversations.isEmpty() || null == context) {
            return true;
        }
        ContentResolver cr = context.getContentResolver();
        Box<Sms> smsBox = DbHelper.getInstance().getBoxStore().boxFor(Sms.class);
        Box<Conversation> conversationBox = DbHelper.getInstance().getBoxStore().boxFor(Conversation.class);
        for (Conversation conversation : conversations) {
            //delete sms in app,system
            List<Sms> smses = smsBox.find(Sms_.address, conversation.address);
            if (null != smses && !smses.isEmpty()) {
                for (Sms sms : smses) {
                    int i = cr.delete(Uri.parse("content://sms"), "_id=?", new String[]{String.valueOf(sms._id)});
                    Timber.e("deleteConversations,delete sms:" + sms._id + "_" + i);
                    if (i < 0) {
                        return false;
                    }
                }
                try {
                    smsBox.remove(smses);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //delete conversation
            conversationBox.remove(conversation);
        }
        TaskService.syncUnreadNum(context);
        return true;
    }

    public static List<Sms> search(String keyword) {
        if (TextUtils.isEmpty(keyword)) {
            return new ArrayList<>();
        }
        Box<Sms> smsBox = DbHelper.getInstance().getBoxStore().boxFor(Sms.class);
        List<Sms> list = smsBox.query()
                .contains(Sms_.body, keyword)
                .or()
                .contains(Sms_.address, keyword)
                .or()
                .contains(Sms_.name, keyword)
                .build()
                .find(0, 15);
        return list;
    }
}