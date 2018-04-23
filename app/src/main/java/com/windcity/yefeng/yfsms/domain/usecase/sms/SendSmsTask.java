package com.windcity.yefeng.yfsms.domain.usecase.sms;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.telephony.SmsManager;
import android.text.TextUtils;

import com.yefeng.support.util.SharedPreferenceUtil;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by yefeng on 03/08/2017.
 */

public class SendSmsTask {
    public static boolean start(Context context, String address, String content) {
        if (null == content || TextUtils.isEmpty(address) || TextUtils.isEmpty(content)) {
            return false;
        }
        String serviceCenter = SharedPreferenceUtil.getString(context, SmsConst.SP_SERVICE_CENTER, null);
        //新消息进入待发送
        Uri newSmsUri = SmsCenter.newSentSms(context, address, content, serviceCenter);
        if (null == newSmsUri) {
            return false;
        }
        //发送消息
        SmsManager manager = SmsManager.getDefault();
        ArrayList<String> list = manager.divideMessage(content);
        int size = list.size();
        ArrayList<PendingIntent> sendIntents = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            Intent intent = new Intent(SmsConst.ACTION_SEND_SMS);
            intent.putExtra(SmsConst.ACTION_SEND_SMS_EXTRA_POSITION, i);
            intent.putExtra(SmsConst.ACTION_SEND_SMS_EXTRA_SMS_SIZE, size);
            intent.putExtra(SmsConst.ACTION_SEND_SMS_EXTRA_NEW_SMS_URI, newSmsUri);
            sendIntents.add(PendingIntent.getBroadcast(context, UUID.randomUUID().hashCode(), intent, 0));
        }
        ArrayList<PendingIntent> sendIntentsDelivery = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            Intent intent = new Intent(SmsConst.ACTION_SEND_SMS_OK);
            intent.putExtra(SmsConst.ACTION_SEND_SMS_EXTRA_POSITION, i);
            intent.putExtra(SmsConst.ACTION_SEND_SMS_EXTRA_SMS_SIZE, size);
            intent.putExtra(SmsConst.ACTION_SEND_SMS_EXTRA_NEW_SMS_URI, newSmsUri);
            sendIntentsDelivery.add(PendingIntent.getBroadcast(context, UUID.randomUUID().hashCode(), intent, 0));
        }
        try {
            manager.sendMultipartTextMessage(address, serviceCenter, list, sendIntents, sendIntentsDelivery);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void start(Context context, ArrayList<String> addresses, String content) {
        if (null == content || TextUtils.isEmpty(content) || null == addresses || addresses.isEmpty()) {
            return;
        }
        for (String address : addresses) {
            start(context, address, content);
        }
    }
}
