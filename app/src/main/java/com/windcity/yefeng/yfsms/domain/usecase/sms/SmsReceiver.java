package com.windcity.yefeng.yfsms.domain.usecase.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsMessage;

import timber.log.Timber;

import static android.provider.Telephony.Sms.Intents.SMS_DELIVER_ACTION;


public class SmsReceiver extends BroadcastReceiver {

    //    public static final String SMS_RECEIVED_ACTION = "android.provider.Telephony.SMS_RECEIVED";//只要注册声明权限即可收到、阻断
//    public static final String SMS_DELIVER_ACTION = "android.provider.Telephony.SMS_DELIVER";//默认短信应用才可以收到、阻断

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Timber.d(action + "_" + getResultCode());
        if (SMS_DELIVER_ACTION.equals(action)) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                if (pdus != null && pdus.length > 0) {
                    SmsMessage[] messages = new SmsMessage[pdus.length];
                    for (int i = 0; i < pdus.length; i++) {
                        byte[] pdu = (byte[]) pdus[i];
                        messages[i] = SmsMessage.createFromPdu(pdu);
                    }
                    String contentAll = "";
                    String sender = "";
                    for (SmsMessage message : messages) {
                        String content = message.getMessageBody();// 得到短信内容
                        sender = message.getOriginatingAddress();// 得到发信息的号码
                        contentAll += content;
                    }
                    SmsCenter.receiveSms(context, contentAll, sender);
                }
            }
        } else if (SmsConst.ACTION_SEND_SMS.equals(action) || SmsConst.ACTION_SEND_SMS_OK.equals(action)) {
            int pos = intent.getIntExtra(SmsConst.ACTION_SEND_SMS_EXTRA_POSITION, 0);
            int size = intent.getIntExtra(SmsConst.ACTION_SEND_SMS_EXTRA_SMS_SIZE, 1);
            Uri newSmsUri = intent.getParcelableExtra(SmsConst.ACTION_SEND_SMS_EXTRA_NEW_SMS_URI);
            if (null == newSmsUri) {
                return;
            }
            SmsCenter.updateSendSmsStatus(context, newSmsUri, pos, size, action, getResultCode());
        }
    }
}
