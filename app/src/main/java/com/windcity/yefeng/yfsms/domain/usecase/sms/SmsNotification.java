package com.windcity.yefeng.yfsms.domain.usecase.sms;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.windcity.yefeng.yfsms.R;
import com.windcity.yefeng.yfsms.data.model.Conversation;
import com.windcity.yefeng.yfsms.data.model.Conversation_;
import com.windcity.yefeng.yfsms.data.model.Sms;
import com.windcity.yefeng.yfsms.domain.service.TaskService;
import com.windcity.yefeng.yfsms.presentation.conversation.ConversationActivity;
import com.yefeng.support.util.SharedPreferenceUtil;

import java.util.ArrayList;
import java.util.Set;

import io.objectbox.Box;

import static android.content.Context.NOTIFICATION_SERVICE;
import static android.support.v4.app.NotificationCompat.PRIORITY_MAX;
import static android.support.v4.app.NotificationCompat.VISIBILITY_PUBLIC;

/**
 * Created by yefeng on 10/08/2017.
 */

public class SmsNotification {

    public static final String EXTRA_NOTIFICATION_ID = "EXTRA_NOTIFICATION_ID";

    public static void notify(Context context, Sms sms, Box<Conversation> conversationBox) {
        if (null == context || null == sms
                || TextUtils.isEmpty(sms.address)
                || null == conversationBox) {
            return;
        }
        if (sms.type != SmsConst.TYPE_INBOX) {
            return;
        }
        Set<String> set = SharedPreferenceUtil.getStringSet(context, SmsConst.SP_CURRENT_CONVERSATION_ADDRESS, null);
        if (null != set && !set.isEmpty()) {
            if (set.contains(sms.address)) {
                return;
            }
        }
        String numbers = SmsUtil.extractVerifyCode(sms.body);
        if (TextUtils.isEmpty(numbers)) {
            numbers = SmsUtil.extractNumbers(sms.body);
        }
        String title = TextUtils.isEmpty(sms.name) ? sms.address + "" : sms.name;
        String content = TextUtils.isEmpty(sms.body) ? "" : sms.body;
        Conversation conversation = conversationBox.query()
                .equal(Conversation_.address, sms.address)
                .build()
                .findFirst();
        NotificationCompat.Builder mBuilder = new NotificationCompat
                .Builder(context)
                .setSmallIcon(R.drawable.ic_launcher_black_24dp)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(content))
                .setContentIntent(getOpenConversationIntent(context, sms, conversation))
                .setFullScreenIntent(null, true)
                .setVisibility(VISIBILITY_PUBLIC)
                .setPriority(PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true);
        if (!TextUtils.isEmpty(numbers)) {
            mBuilder.addAction(R.drawable.ic_mark_read, context.getString(R.string.mark_read), getMarkSmsReadIntent(context, sms));
            mBuilder.addAction(R.drawable.ic_content_copy_black_24dp, context.getString(R.string.copy) + ":" + numbers, getCopyVerifyCodeIntent(context, numbers, sms));
        } else {
            mBuilder.addAction(R.drawable.ic_open_in_new_black_24dp, context.getString(R.string.open), getOpenConversationIntent(context, sms, conversation));
            mBuilder.addAction(R.drawable.ic_mark_read, context.getString(R.string.mark_read), getMarkSmsReadIntent(context, sms));
        }
        Notification notification = mBuilder.build();
        NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify((int) sms._id, notification);
    }

    private static PendingIntent getOpenConversationIntent(Context context, Sms sms, Conversation conversation) {
        Intent intent;
        if (null == conversation) {
            intent = new Intent();
        } else {
            ArrayList<Conversation> list = new ArrayList<>();
            list.add(conversation);
            intent = ConversationActivity.getIntent(context, list);
        }
        intent.putExtra(EXTRA_NOTIFICATION_ID, (int) sms._id);
        return PendingIntent.getActivity(context, (int) sms._id, intent, 0);
    }

    private static PendingIntent getMarkSmsReadIntent(Context context, Sms sms) {
        Intent intent = TaskService.getMarkSmsReadIntent(context, sms);
        intent.putExtra(EXTRA_NOTIFICATION_ID, (int) sms._id);
        return PendingIntent.getService(context, (int) sms._id, intent, 0);
    }

    private static PendingIntent getCopyVerifyCodeIntent(Context context, String verifyCode, Sms sms) {
        Intent intent = TaskService.getCopyVerifyCodeIntent(context, verifyCode);
        intent.putExtra(EXTRA_NOTIFICATION_ID, (int) sms._id);
        return PendingIntent.getService(context, (int) sms._id, intent, 0);
    }
}
