package com.windcity.yefeng.yfsms.domain.usecase.share;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.windcity.yefeng.yfsms.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Created by yefeng on 04/09/2017.
 */

public class ShareTask {
    public static final int REQUEST_CODE_SHARE = 2000;

    /**
     * 分享功能
     *
     * @param activity      上下文
     * @param activityTitle Activity的名字
     * @param file          图片
     */
    public static void start(Activity activity, String activityTitle,
                             File file) {
        if (file == null || TextUtils.isEmpty(file.getPath())) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/*");
        Uri u = Uri.fromFile(file);
        intent.putExtra(Intent.EXTRA_STREAM, u);
        activity.startActivityForResult(Intent.createChooser(intent, activityTitle), REQUEST_CODE_SHARE);
    }

    /**
     * 分享app
     *
     * @param activity activity
     */
    public static void shareApp(Activity activity) {
        String activityTitle = activity.getString(R.string.share_to);
        File f = new File(activity.getExternalCacheDir() + "/share.jpg");
        if (!f.exists()) {
            try {
                InputStream is = activity.getAssets().open("share.jpg");
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                FileOutputStream fos = new FileOutputStream(f);
                fos.write(buffer);
                fos.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        start(activity, activityTitle, f);
    }
}
