package com.windcity.yefeng.yfsms.domain.usecase.reward;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.afollestad.materialdialogs.MaterialDialog;
import com.windcity.yefeng.yfsms.R;
import com.windcity.yefeng.yfsms.base.BaseActivity;
import com.yefeng.support.util.SharedPreferenceUtil;

/**
 * Created by yefeng on 07/09/2017.
 */

public class RewardTask {
    public static final String ALIPAY_COMMAND = "#吱口令#长按复制此条消息，打开支付宝给我转账WOmN0K562J";
    private static final String SP_USED_TIME = "SP_USED_TIME";

    public static void start(BaseActivity activity) {
        if (null == activity) {
            return;
        }
        ClipboardManager cmb = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
        cmb.setPrimaryClip(ClipData.newPlainText("alipay_command", ALIPAY_COMMAND));
        new MaterialDialog.Builder(activity)
                .content(R.string.reward_prompt)
                .positiveText(R.string.go_to_reward)
                .negativeText(R.string.refuse)
                .onPositive((dialog, which) -> openAlipay(activity))
                .onNegative((dialog, which) -> {
                })
                .cancelable(false)
                .canceledOnTouchOutside(false)
                .build()
                .show();


    }

    private static void openAlipay(BaseActivity activity) {
        RewardUtil.recordReward(activity);
        try {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            intent.setComponent(new ComponentName("com.eg.android.AlipayGphone", "com.eg.android.AlipayGphone.AlipayLogin"));
            activity.startActivity(intent);
        } catch (Exception | Error e) {
            e.printStackTrace();
        }
    }

    public static void promptReward(BaseActivity activity) {
        if (null == activity) {
            return;
        }
        long firstTime = SharedPreferenceUtil.getLong(activity, SP_USED_TIME, 0);
        if (firstTime == 0) {
            SharedPreferenceUtil.putLong(activity, SP_USED_TIME, System.currentTimeMillis());
        }
        if (!RewardUtil.needPromptReward(activity)) {
            return;
        }
        int usedMonth = getUsedTime(activity);
        String promptString = activity.getString(R.string.reward_prompt_month, usedMonth + "");
        new MaterialDialog.Builder(activity)
                .content(promptString)
                .positiveText(R.string.go_to_reward)
                .negativeText(R.string.refuse)
                .onPositive((dialog, which) -> start(activity))
                .onNegative((dialog, which) -> {
                })
                .cancelable(false)
                .canceledOnTouchOutside(false)
                .build()
                .show();
    }

    private static int getUsedTime(Context context) {
        long firstTime = SharedPreferenceUtil.getLong(context, SP_USED_TIME, 0);
        long elapsedTime = System.currentTimeMillis() - firstTime;
        int month = (int) (elapsedTime / 1000 / 60 / 60 / 24 / 30);
        if (month < 1) {
            month = 1;
        }
        return month;
    }

}
