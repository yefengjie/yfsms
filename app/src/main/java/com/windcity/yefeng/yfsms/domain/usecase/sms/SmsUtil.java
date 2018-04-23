package com.windcity.yefeng.yfsms.domain.usecase.sms;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.TextUtils;

import com.windcity.yefeng.yfsms.data.model.Sms;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

/**
 * Created by yefeng on 27/07/2017.
 */

public class SmsUtil {
    public static boolean isServiceSms(String address) {
        return address.length() <= 10;
    }

    public static boolean isNotifySms(String address) {
        return address.startsWith("106");
    }

    // 通过address手机号关联Contacts联系人的显示名字
    public static String getPeopleNameFromPerson(String address, ContentResolver re) {
        if (TextUtils.isEmpty(address)) {
            return null;
        }
        String strPerson = null;
        String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER};
        Uri uri_Person = Uri.withAppendedPath(ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI, address);  // address 手机号过滤
        Cursor cursor = null;
        try {
            cursor = re.query(uri_Person, projection, null, null, null);
            if (null != cursor && cursor.getCount() > 0) {
                if (cursor.moveToFirst()) {
                    int index_PeopleName = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                    strPerson = cursor.getString(index_PeopleName);
                }
            }
        } catch (Exception e) {
            Timber.e(e);
        } finally {
            if (cursor != null) {
                try {  // 有可能cursor都没有创建成功
                    cursor.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return strPerson;
    }

    public static void avoidNull(Sms sms) {
        if (null == sms.person) {
            sms.person = "";
        }
        if (null == sms.address) {
            sms.address = "";
        }
        if (null == sms.body) {
            sms.body = "";
        }
    }

    public static String getConversationName(Sms sms) {
        return TextUtils.isEmpty(sms.name) ? sms.address : sms.name;
    }

    // 查询联系人
    public static HashMap<String, String> queryContact(String queryStr, ContentResolver re) {
        if (TextUtils.isEmpty(queryStr) || null == re) {
            return null;
        }
        HashMap<String, String> map = new HashMap<>();
        String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER};
        Uri uri_Person = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;  // address 手机号过滤
        Cursor cursor = null;
        try {
            cursor = re.query(uri_Person, projection, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " like? ", new String[]{queryStr}, "  limit 5");
            if (null != cursor && cursor.getCount() > 0) {
                if (cursor.moveToFirst()) {
                    int index_PeopleName = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                    int index_PeopleNumber = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    map.put(cursor.getString(index_PeopleNumber), cursor.getString(index_PeopleName));
                }
            }
        } catch (Exception e) {
            Timber.e(e);
        } finally {
            if (cursor != null) {
                try {  // 有可能cursor都没有创建成功
                    cursor.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            cursor = re.query(uri_Person, projection, ContactsContract.CommonDataKinds.Phone.NUMBER + " like? ", new String[]{queryStr}, "  limit 5");
            if (null != cursor && cursor.getCount() > 0) {
                if (cursor.moveToFirst()) {
                    int index_PeopleName = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                    int index_PeopleNumber = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    map.put(cursor.getString(index_PeopleNumber), cursor.getString(index_PeopleName));
                }
            }
        } catch (Exception e) {
            Timber.e(e);
        } finally {
            if (cursor != null) {
                try {  // 有可能cursor都没有创建成功
                    cursor.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return map;
    }

    public static String extractVerifyCode(String content) {
        if (TextUtils.isEmpty(content) || !content.contains("验证码")) {
            return null;
        }
        try {
            content = "哈" + content + "哈";//增加无用字符用于分割
            String[] cc = content.split("验证码");
            if (cc.length != 2) {
                return extractVerifyCode2(cc);
            } else {
                return extractVerifyCode1(cc);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 提取存在且仅存在一个的数字串，可能是验证码，也有可能是其他
     *
     * @param content
     * @return
     */
    public static String extractNumbers(String content) {
        if (TextUtils.isEmpty(content)) {
            return null;
        }
        try {
            Pattern p = Pattern.compile("[0-9]{4,20}");
            Matcher m = p.matcher(content);
            if (m.find()) {
                return m.group();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String extractVerifyCode1(String[] cc) throws Exception {
        String left = new StringBuilder(cc[0]).reverse().toString();
        String right = cc[1];
        String leftNumber = null;
        String rightNumber = null;
        Pattern p;
        Matcher m;
        p = Pattern.compile("[0-9]{3,8}");
        m = p.matcher(left);
        if (m.find()) {
            leftNumber = m.group();
        }
        m = p.matcher(right);
        if (m.find()) {
            rightNumber = m.group();
        }
        if (!TextUtils.isEmpty(leftNumber) && !TextUtils.isEmpty(rightNumber)) {
            int leftPos = left.indexOf(leftNumber);
            int rightPos = right.indexOf(rightNumber);
            leftNumber = new StringBuilder(leftNumber).reverse().toString();
            return leftPos < rightPos ? leftNumber : rightNumber;

        }
        if (!TextUtils.isEmpty(leftNumber)) {
            leftNumber = new StringBuilder(leftNumber).reverse().toString();
            return leftNumber;
        }
        if (!TextUtils.isEmpty(rightNumber)) {
            return rightNumber;
        }
        return null;
    }

    public static String extractVerifyCode2(String[] cc) throws Exception {
        int len = cc.length;
        if (len <= 2) {
            return null;
        }
        for (String s : cc) {
            if (!TextUtils.isEmpty(s)) {
                Pattern p;
                Matcher m;
                p = Pattern.compile("[0-9]{3,8}");
                m = p.matcher(s);
                if (m.find()) {
                    return m.group();
                }
            }
        }
        return null;
    }
}
