package com.windcity.yefeng.yfsms.widget;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.windcity.yefeng.yfsms.R;
import com.yefeng.support.base.AppInfo;

import org.polaric.colorful.ColorfulUtil;

/**
 * Created by yefeng on 17/07/2017.
 */

public class AvatarImageView extends FrameLayout {

    private AppCompatImageView mDefaultAvatar;
    private AppCompatTextView mName;

    public AvatarImageView(Context context) {
        super(context);
        init(context);
    }

    public AvatarImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AvatarImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setBackgroundResource(R.drawable.bg_avatar);
        int padding = (int) (8 * AppInfo.sDensity);
        mDefaultAvatar = new AppCompatImageView(context);
        mName = new AppCompatTextView(context);
        Resources res = context.getResources();
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mName.setLayoutParams(lp);
        mName.setTextColor(ColorfulUtil.getTextColorHoloWhite(context));
        mName.setTextSize(32);
        mName.setGravity(Gravity.CENTER);


        FrameLayout.LayoutParams lp2 = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        lp2.gravity = Gravity.CENTER;
        mDefaultAvatar.setLayoutParams(lp2);
        mDefaultAvatar.setScaleType(ImageView.ScaleType.FIT_XY);
        mDefaultAvatar.setImageResource(R.drawable.ic_person_holo_light_white_24dp);
        mDefaultAvatar.setPadding(padding, padding, padding, padding);


        addView(mDefaultAvatar);
        addView(mName);
        mDefaultAvatar.setVisibility(View.VISIBLE);
        mName.setVisibility(View.GONE);
    }

    public void setTextSize(int textSize) {
        mName.setTextSize(textSize);
    }

    public void setText(String text) {
        if (TextUtils.isEmpty(text)) {
            mDefaultAvatar.setVisibility(View.VISIBLE);
            mName.setVisibility(View.GONE);
            return;
        }
        String name = text.substring(0, 1);
        mDefaultAvatar.setVisibility(View.GONE);
        mName.setVisibility(View.VISIBLE);
        mName.setText(name);
    }
}
