package com.yefeng.support.util;

import android.content.Context;
import android.telephony.TelephonyManager;

/**
 * Created by yefeng on 18/08/2017.
 */

public class SimCardUtil {
    public static boolean hasSimCard(Context context) {
        TelephonyManager telMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        int simState = telMgr.getSimState();
        boolean result = true;
        switch (simState) {
            case TelephonyManager.SIM_STATE_ABSENT:
                result = false; // 没有SIM卡
                break;
            case TelephonyManager.SIM_STATE_UNKNOWN:
                result = false;
                break;
        }
        return result;
    }
}
