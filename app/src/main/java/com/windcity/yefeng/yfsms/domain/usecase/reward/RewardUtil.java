package com.windcity.yefeng.yfsms.domain.usecase.reward;

import android.content.Context;

import com.yefeng.support.util.SharedPreferenceUtil;

/**
 * Created by yefeng on 14/09/2017.
 */

public class RewardUtil {

    private static final String SP_REWARD = "SP_REWARD";
    private static final String LAST_PROMPT_TIME = "LAST_PROMPT_TIME";

    public static void recordReward(Context context) {
        SharedPreferenceUtil.putBoolean(context, SP_REWARD, true);
    }

    public static boolean isReward(Context context) {
        return SharedPreferenceUtil.getBoolean(context, SP_REWARD, false);
    }

    public static boolean needPromptReward(Context context) {
        if (isReward(context)) {
            return false;
        }
        long lastPromptTime = SharedPreferenceUtil.getLong(context, LAST_PROMPT_TIME, 0);
        if (lastPromptTime == 0) {
            SharedPreferenceUtil.putLong(context, LAST_PROMPT_TIME, System.currentTimeMillis());
            lastPromptTime = SharedPreferenceUtil.getLong(context, LAST_PROMPT_TIME, 0);
        }
        long elapsedTime = System.currentTimeMillis() - lastPromptTime;
        int month = (int) (elapsedTime / 1000 / 60 / 60 / 24 / 30);
        if (month >= 1) {
            //只提醒一次
            if (!SharedPreferenceUtil.getBoolean(context, LAST_PROMPT_TIME + lastPromptTime, false)) {
                SharedPreferenceUtil.putBoolean(context, LAST_PROMPT_TIME + lastPromptTime, true);
                SharedPreferenceUtil.putLong(context, LAST_PROMPT_TIME, System.currentTimeMillis());
                return true;
            }
        }
        return false;
    }
}