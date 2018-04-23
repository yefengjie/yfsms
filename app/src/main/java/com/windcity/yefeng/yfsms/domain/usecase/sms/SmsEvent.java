package com.windcity.yefeng.yfsms.domain.usecase.sms;

/**
 * Created by yefeng on 26/07/2017.
 */

public class SmsEvent {
    public static class New {
    }

    public static class Delete {
    }

    public static class Update {
    }

    public static class UnreadNumChange {
    }

    public static class Sync {
        public boolean status;

        public Sync(boolean startOrFinish) {
            this.status = startOrFinish;
        }
    }
}
