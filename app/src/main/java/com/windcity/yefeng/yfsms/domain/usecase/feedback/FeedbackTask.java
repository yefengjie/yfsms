package com.windcity.yefeng.yfsms.domain.usecase.feedback;

import android.text.TextUtils;

import com.windcity.yefeng.yfsms.R;
import com.windcity.yefeng.yfsms.base.BaseActivity;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by yefeng on 06/09/2017.
 */

public class FeedbackTask {
    private static final String URL = "https://oapi.dingtalk.com/robot/send?access_token=4d7c757a5c0fefef14f8278feb4900e1b94015d5244c71196534862af7df1f9c";

    public static void start(String msg, String contactName, BaseActivity activity) {
        if (TextUtils.isEmpty(msg)) {
            return;
        }
        activity.showToast(activity.getString(R.string.feedback_success));
        if (TextUtils.isEmpty(contactName)) {
            contactName = "黑衣人";
        }
        String title = contactName + "要吐槽:\n";
        String textMsg = "{ \"msgtype\": \"text\", \"text\": {\"content\": \"" + title + msg + "\"}}";
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), textMsg);
                Request request = new Request.Builder()
                        .url(URL)
                        .post(body)
                        .build();
                Response response = client.newCall(request).execute();
                response.body().string();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
