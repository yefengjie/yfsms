package com.windcity.yefeng.yfsms.domain.usecase.sms;

import android.net.Uri;
import android.provider.Telephony;

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
 * type
 * ALL    = 0;
 * INBOX  = 1;
 * SENT   = 2;
 * DRAFT  = 3;
 * OUTBOX = 4;
 * FAILED = 5;
 * QUEUED = 6;
 * <p>
 * body：短信具体内容
 */

public class SmsConst {
    public static final String _ID = "_id";
    public static final String THREAD_ID = "thread_id";
    public static final String ADDRESS = "address";
    public static final String PERSON = "person";
    public static final String DATE = "date";
    public static final String PROTOCOL = "protocol";
    public static final String READ = "read";
    public static final String STATUS = "status";
    public static final String TYPE = "type";
    public static final String BODY = "body";
    public static final String SERVICE_CENTER = "service_center";
    public static final int TYPE_ALL = 0;
    public static final int TYPE_INBOX = 1;
    public static final int TYPE_SENT = 2;
    public static final int TYPE_DRAFT = 3;
    public static final int TYPE_OUTBOX = 4;
    public static final int TYPE_FAILED = 5;
    public static final int TYPE_QUEUED = 6;
    public static final int STATUS_RECEIVED = -1;
    public static final int STATUS_COMPLETE = 0;
    public static final int STATUS_PENDING = 64;
    public static final int STATUS_FAILED = 128;
    public static final int READ_unread = 0;
    public static final int READ_read = 1;
    public static final int RPOTO_SMS = 0;
    public static final int PROTO_MMS = 1;
    public static final String ACTION_SEND_SMS = "com.windcity.yefeng.yfsms.domain.usecase.sms.action.SEND_SMS";
    public static final String ACTION_SEND_SMS_OK = "com.windcity.yefeng.yfsms.domain.usecase.sms.action.SEND_SMS_OK";
    public static final String ACTION_SEND_SMS_EXTRA_POSITION = "ACTION_SEND_SMS_EXTRA_POSITION";
    public static final String ACTION_SEND_SMS_EXTRA_SMS_SIZE = "ACTION_SEND_SMS_EXTRA_SMS_SIZE";
    public static final String ACTION_SEND_SMS_EXTRA_NEW_SMS_URI = "ACTION_SEND_SMS_EXTRA_NEW_SMS_URI";
    public static final String SP_SERVICE_CENTER = "SP_SERVICE_CENTER";
    public static final String SP_CURRENT_CONVERSATION_ADDRESS = "SP_CURRENT_CONVERSATION_ADDRESS";
    public static Uri SMS_ALL = Telephony.Sms.CONTENT_URI;//全部
    public static Uri SMS_INBOX = Telephony.Sms.Inbox.CONTENT_URI;//收件箱
    public static Uri SMS_SENT = Telephony.Sms.Sent.CONTENT_URI;//已发送
    public static Uri SMS_DRAFT = Telephony.Sms.Draft.CONTENT_URI;//草稿
    public static Uri SMS_OUTBOX = Telephony.Sms.Outbox.CONTENT_URI;//发件箱

}
