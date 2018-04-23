package com.windcity.yefeng.yfsms.widget;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import org.polaric.colorful.ColorfulUtil;

/**
 * Created by yefeng on 17/07/2017.
 */

public class ThemeTextView extends AppCompatTextView {
    public ThemeTextView(Context context) {
        super(context);
    }

    public ThemeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ThemeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setUnreadStatusPrimaryHighlight() {
        setTextColor(ColorfulUtil.getTextColorBlack(getContext()));
        getPaint().setFakeBoldText(true);
    }

    public void setUnreadStatusSecondaryHighlight(boolean bold) {
        setTextColor(ColorfulUtil.getTextColorPrimary(getContext()));
        getPaint().setFakeBoldText(bold);
    }

    public void setReadStatusPrimary() {
        setTextColor(ColorfulUtil.getTextColorPrimary(getContext()));
        getPaint().setFakeBoldText(false);
    }

    public void setReadStatusSecondary() {
        setTextColor(ColorfulUtil.getTextColorSecondary(getContext()));
        getPaint().setFakeBoldText(false);
    }

}
