package com.windcity.yefeng.yfsms.data.db;

import android.content.Context;

import com.windcity.yefeng.yfsms.data.model.MyObjectBox;

import io.objectbox.BoxStore;

/**
 * Created by yefeng on 19/07/2017.
 */

public class DbHelper {
    private static volatile DbHelper mInstance;
    private static BoxStore mBoxStore;

    private DbHelper() {
    }

    public static DbHelper getInstance() {
        if (mInstance == null) {
            synchronized (DbHelper.class) {
                if (mInstance == null) {
                    mInstance = new DbHelper();
                }
            }
        }
        return mInstance;
    }

    public void init(Context context) {
        mBoxStore = MyObjectBox.builder().androidContext(context).build();
    }

    public BoxStore getBoxStore() {
        return mBoxStore;
    }
}
