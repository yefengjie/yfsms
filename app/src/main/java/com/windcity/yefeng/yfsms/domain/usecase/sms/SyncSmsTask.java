package com.windcity.yefeng.yfsms.domain.usecase.sms;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;

import com.windcity.yefeng.yfsms.data.db.DbHelper;
import com.windcity.yefeng.yfsms.data.model.Contact;
import com.windcity.yefeng.yfsms.data.model.Conversation;
import com.windcity.yefeng.yfsms.data.model.Conversation_;
import com.windcity.yefeng.yfsms.data.model.Sms;
import com.windcity.yefeng.yfsms.data.model.Tabs;
import com.yefeng.support.rxbus.RxBus;

import io.objectbox.Box;
import timber.log.Timber;

/**
 * Created by yefeng on 21/07/2017.
 * sms主要结构：
 * <p>
 * _id：短信序号，如100
 * <p>
 * thread_id：对话的序号，如100，与同一个手机号互发的短信，其序号是相同的
 * <p>
 * address：发件人地址，即手机号，如138138000
 * <p>
 * person：发件人，如果发件人在通讯录中则为具体姓名，陌生人为null
 * <p>
 * date：日期，long型，如1346988516，可以对日期显示格式进行设置
 * <p>
 * protocol：协议0SMS_RPOTO短信，1MMS_PROTO彩信
 * <p>
 * read：是否阅读0未读，1已读
 * <p>
 * status：短信状态-1接收，0complete,64pending,128failed
 * <p>
 * type：短信类型1是接收到的，2是已发出
 * <p>
 * body：短信具体内容
 */

public class SyncSmsTask {
    public static void start(Context context) {
        ContentResolver re = context.getContentResolver();
        Box<Conversation> conversationBox = DbHelper.getInstance().getBoxStore().boxFor(Conversation.class);
        Conversation lastestConversation = conversationBox.query().orderDesc(Conversation_.newestSmsId).build().findFirst();
        if (!isNeedSyncDb(re, lastestConversation)) {
            Timber.d("isNeedSyncDb:false");
            return;
        }
        Timber.d("isNeedSyncDb:true");
        Box<Tabs> tabsBox = DbHelper.getInstance().getBoxStore().boxFor(Tabs.class);
        Box<Sms> smsBox = DbHelper.getInstance().getBoxStore().boxFor(Sms.class);
        Box<Contact> contactBox = DbHelper.getInstance().getBoxStore().boxFor(Contact.class);
        RxBus.getBus().send(new SmsEvent.Sync(true));
        syncDb(re, lastestConversation, conversationBox, tabsBox, smsBox, contactBox, context);
        RxBus.getBus().send(new SmsEvent.Sync(false));
    }

    private static boolean isNeedSyncDb(ContentResolver re, Conversation conversation) {
        if (null == conversation) {
            return true;
        }
        Timber.d("conversation lastest sms id is :" + conversation.newestSmsId);
        //短信数据库最新一条数据
        long lastestSmsId = -1;
        Cursor cursor = null;
        try {
            cursor = re.query(SmsConst.SMS_ALL, new String[]{SmsConst._ID}, null, null, SmsConst._ID + " desc LIMIT 1");
            if (null != cursor && cursor.getCount() > 0) {
                Timber.d("sms count:" + cursor.getCount());
                cursor.moveToFirst();
                lastestSmsId = cursor.getLong(cursor.getColumnIndex(SmsConst._ID));
            }
        } catch (Exception e) {
            Timber.e(e);
        } finally {
            if (cursor != null) {
                try {  // 有可能cursor都没有创建成功
                    cursor.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        Timber.d("sms db lastest sms id is :" + lastestSmsId);
        if (lastestSmsId == -1) {
            return false;
        }
        return lastestSmsId > conversation.newestSmsId;
    }

    private static void syncDb(ContentResolver re, Conversation lastestConversation, Box<Conversation> conversationBox, Box<Tabs> tabsBox, Box<Sms> smsBox, Box<Contact> contactBox, Context context) {
        long lastestSmsId = null == lastestConversation ? -1 : lastestConversation.newestSmsId;
        Cursor cursor = null;
        try {
            cursor = re.query(SmsConst.SMS_ALL,
                    new String[]{SmsConst._ID, SmsConst.THREAD_ID, SmsConst.ADDRESS, SmsConst.PERSON, SmsConst.DATE,
                            SmsConst.PROTOCOL, SmsConst.READ, SmsConst.STATUS, SmsConst.TYPE, SmsConst.BODY, SmsConst.SERVICE_CENTER},
                    SmsConst._ID + ">?",
                    new String[]{"" + lastestSmsId},
                    SmsConst._ID + " asc");
            if (null != cursor && cursor.getCount() > 0) {
                Timber.e("syncDb count:" + cursor.getCount());
                int i = 0;
                Timber.e("");
                while (cursor.moveToNext()) {
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
                    SmsCenter.syncSms(sms, tabsBox, conversationBox, smsBox, re, contactBox);
                    i = i + 1;
                }
                Timber.e("syncDb count:" + smsBox.count());
                RxBus.getBus().send(new SmsEvent.New());
            }
        } catch (Exception e) {
            Timber.e(e);
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
}
