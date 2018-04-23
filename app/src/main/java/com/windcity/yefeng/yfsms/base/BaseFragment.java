package com.windcity.yefeng.yfsms.base;


import android.support.v4.app.Fragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.yefeng.support.util.ToastUtil;

/**
 * Created by yefeng on 17/07/2017.
 */

public class BaseFragment extends Fragment {

    private MaterialDialog mProgressDialog;

    public void showToast(String msg) {
        ToastUtil.showToast(getContext(), msg);
    }

    public void showProgress(int msg) {
        if (null == mProgressDialog) {
            mProgressDialog = new MaterialDialog.Builder(getContext())
                    .progress(true, 0)
                    .build();
        }
        mProgressDialog.setContent(msg);
        mProgressDialog.show();
    }

    public void dismissProgress() {
        if (null != mProgressDialog && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        dismissProgress();
    }
}

