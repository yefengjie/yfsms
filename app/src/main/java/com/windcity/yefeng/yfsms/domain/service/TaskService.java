package com.windcity.yefeng.yfsms.domain.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.windcity.yefeng.yfsms.data.db.DbHelper;
import com.windcity.yefeng.yfsms.data.model.Conversation;
import com.windcity.yefeng.yfsms.data.model.Conversation_;
import com.windcity.yefeng.yfsms.data.model.Sms;
import com.windcity.yefeng.yfsms.domain.usecase.contact.SyncContactNameTask;
import com.windcity.yefeng.yfsms.domain.usecase.sms.MarkSmsReadTask;
import com.windcity.yefeng.yfsms.domain.usecase.sms.SendSmsTask;
import com.windcity.yefeng.yfsms.domain.usecase.sms.SmsNotification;
import com.windcity.yefeng.yfsms.domain.usecase.sms.SyncSmsTask;
import com.windcity.yefeng.yfsms.domain.usecase.sms.SyncUnreadNumTask;
import com.windcity.yefeng.yfsms.presentation.conversation.ConversationActivity;

import java.util.ArrayList;

import io.objectbox.Box;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * helper methods.
 */
public class TaskService extends IntentService {
    public static final String ACTION_SYNC_SMS = "com.windcity.yefeng.yfsms.domain.service.action.SYNC_SMS";
    public static final String ACTION_SEND_SMS = "com.windcity.yefeng.yfsms.domain.service.action.SEND_SMS";
    public static final String ACTION_SEND_SMSES = "com.windcity.yefeng.yfsms.domain.service.action.SEND_SMSES";
    public static final String ACTION_MARK_SMS_READ = "com.windcity.yefeng.yfsms.domain.service.action.MARK_SMS_READ";
    public static final String ACTION_COPY_VERIFY_CODE = "com.windcity.yefeng.yfsms.domain.service.action.COPY_VERIFY_CODE";
    public static final String ACTION_SYNC_UNREAD_NUM = "com.windcity.yefeng.yfsms.domain.service.action.ACTION_SYNC_UNREAD_NUM";
    public static final String ACTION_SYNC_CONTACT_NAME = "com.windcity.yefeng.yfsms.domain.service.action.ACTION_SYNC_CONTACT_NAME";

    private static final String EXTRA_PARAM1 = "com.windcity.yefeng.yfsms.domain.service.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.windcity.yefeng.yfsms.domain.service.extra.PARAM2";
    private static final String EXTRA_PARAM3 = "com.windcity.yefeng.yfsms.domain.service.extra.PARAM3";

    public TaskService() {
        super("TaskService");
    }

    public static Intent getCopyVerifyCodeIntent(Context context, String verifyCode) {
        if (null == context || TextUtils.isEmpty(verifyCode)) {
            return new Intent();
        }
        Intent intent = new Intent(context, TaskService.class);
        intent.setAction(ACTION_COPY_VERIFY_CODE);
        intent.putExtra(EXTRA_PARAM1, verifyCode);
        return intent;
    }

    /**
     * mark sms read
     */
    public static void markRead(Context context, ArrayList<Sms> smses) {
        if (null == context || null == smses || smses.isEmpty()) {
            return;
        }
        Intent intent = new Intent(context, TaskService.class);
        intent.setAction(ACTION_MARK_SMS_READ);
        intent.putParcelableArrayListExtra(EXTRA_PARAM1, smses);
        context.startService(intent);
    }

    /**
     * mark sms read intent
     */
    public static Intent getMarkSmsReadIntent(Context context, Sms sms) {
        Intent intent = new Intent(context, TaskService.class);
        intent.setAction(ACTION_MARK_SMS_READ);
        ArrayList<Sms> list = new ArrayList<>();
        if (null != sms) {
            list.add(sms);
        }
        intent.putParcelableArrayListExtra(EXTRA_PARAM1, list);
        return intent;
    }

    /**
     * start sync sms
     *
     * @param context context
     */
    public static void startSyncSms(Context context) {
        if (null == context) {
            return;
        }
        Intent intent = new Intent(context, TaskService.class);
        intent.setAction(ACTION_SYNC_SMS);
        context.startService(intent);
    }

    /**
     * send sms
     *
     * @param address              address
     * @param content              content
     * @param openSendConversation open send conversation
     */
    public static void sendSms(Context context, String address, String content, boolean openSendConversation) {
        if (null == content || TextUtils.isEmpty(address) || TextUtils.isEmpty(content)) {
            return;
        }
        Intent intent = new Intent(context, TaskService.class);
        intent.setAction(ACTION_SEND_SMS);
        intent.putExtra(EXTRA_PARAM1, address);
        intent.putExtra(EXTRA_PARAM2, content);
        intent.putExtra(EXTRA_PARAM3, openSendConversation);
        context.startService(intent);
    }

    /**
     * send sms
     *
     * @param context   context
     * @param addresses address
     * @param content   content
     */
    public static void sendSms(Context context, ArrayList<String> addresses, String content) {
        if (null == content || null == addresses || addresses.isEmpty() || TextUtils.isEmpty(content)) {
            return;
        }
        Intent intent = new Intent(context, TaskService.class);
        intent.setAction(ACTION_SEND_SMSES);
        intent.putExtra(EXTRA_PARAM1, addresses);
        intent.putExtra(EXTRA_PARAM2, content);
        context.startService(intent);
    }

    /**
     * sync unread sms num
     *
     * @param context context
     */
    public static void syncUnreadNum(Context context) {
        if (null == context) {
            return;
        }
        Intent intent = new Intent(context, TaskService.class);
        intent.setAction(ACTION_SYNC_UNREAD_NUM);
        context.startService(intent);
    }

    public static void syncContactName(Context context, String address) {
        if (null == context) {
            return;
        }
        Intent intent = new Intent(context, TaskService.class);
        intent.setAction(ACTION_SYNC_CONTACT_NAME);
        if (!TextUtils.isEmpty(address)) {
            intent.putExtra(EXTRA_PARAM1, address);
        }
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (null == intent) {
            return;
        }
        //clear notification
        int notificationId = intent.getIntExtra(SmsNotification.EXTRA_NOTIFICATION_ID, -1);
        if (notificationId != -1) {
            NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            mNotifyMgr.cancel(notificationId);
        }

        final String action = intent.getAction();
        if (ACTION_SYNC_SMS.equals(action)) {
            SyncSmsTask.start(getApplicationContext());
        } else if (ACTION_SEND_SMS.equals(action)) {
            final String param1 = intent.getStringExtra(EXTRA_PARAM1);
            final String param2 = intent.getStringExtra(EXTRA_PARAM2);
            final boolean openSendConversation = intent.getBooleanExtra(EXTRA_PARAM3, false);
            doSendSms(param1, param2, openSendConversation);
        } else if (ACTION_SEND_SMSES.equals(action)) {
            final ArrayList<String> param1 = intent.getStringArrayListExtra(EXTRA_PARAM1);
            final String param2 = intent.getStringExtra(EXTRA_PARAM2);
            SendSmsTask.start(getApplicationContext(), param1, param2);
        } else if (ACTION_MARK_SMS_READ.equals(action)) {
            final ArrayList<Sms> list = intent.getParcelableArrayListExtra(EXTRA_PARAM1);
            MarkSmsReadTask.start(this, list);
        } else if (ACTION_COPY_VERIFY_CODE.equals(action)) {
            final String params1 = intent.getStringExtra(EXTRA_PARAM1);
            copyVerifyCodeToClipboard(params1);
        } else if (ACTION_SYNC_UNREAD_NUM.equals(action)) {
            SyncUnreadNumTask.start();
        } else if (ACTION_SYNC_CONTACT_NAME.equals(action)) {
            String address = intent.getStringExtra(EXTRA_PARAM1);
            SyncContactNameTask.start(address, getApplicationContext());
        }
    }

    private void copyVerifyCodeToClipboard(String verifyCode) {
        if (TextUtils.isEmpty(verifyCode)) {
            return;
        }
        ClipboardManager cmb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        cmb.setPrimaryClip(ClipData.newPlainText("验证码", verifyCode));
    }

    private void doSendSms(String address, String content, boolean openSendConversation) {
        boolean sended = SendSmsTask.start(getApplicationContext(), address, content);
        if (!sended || !openSendConversation || TextUtils.isEmpty(address)) {
            return;
        }
        Box<Conversation> conversationBox = DbHelper.getInstance().getBoxStore().boxFor(Conversation.class);
        Conversation conversation = conversationBox.query()
                .equal(Conversation_.address, address)
                .build()
                .findFirst();
        if (null == conversation) {
            return;
        }
        ArrayList<Conversation> conversationArrayList = new ArrayList<>();
        conversationArrayList.add(conversation);
        ConversationActivity.startMeWithFlags(this, conversationArrayList, Intent.FLAG_ACTIVITY_NEW_TASK);
    }

}
