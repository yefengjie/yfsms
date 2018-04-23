package com.windcity.yefeng.yfsms.domain.usecase.sms;

/**
 * Created by yefeng on 25/07/2017.
 */

public class SmsType {
    //联系人
    public static final int TYPE_CONTACT = 11;
    //陌生人
    public static final int TYPE_STRANGER = 12;
    //服务号
    public static final int TYPE_SERVICE = 13;
    //通知类
    public static final int TYPE_NOTIFICATION = 14;
    //全部
    public static final int TYPE_ALL = 21;
    //重要
    public static final int TYPE_STAR = 22;
    //垃圾
    public static final int TYPE_SPAM = 23;
    //已删除
    public static final int TYPE_DELETED = 24;
}
