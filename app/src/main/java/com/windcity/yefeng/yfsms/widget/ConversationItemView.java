package com.windcity.yefeng.yfsms.widget;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.windcity.yefeng.yfsms.R;
import com.windcity.yefeng.yfsms.data.model.Sms;
import com.windcity.yefeng.yfsms.data.model.StarSms;
import com.windcity.yefeng.yfsms.domain.usecase.sms.SmsConst;
import com.yefeng.support.base.AppInfo;

import org.polaric.colorful.ColorfulUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * Created by yefeng on 02/08/2017.
 */

public class ConversationItemView extends RelativeLayout {

    private AvatarImageView mAvatar;
    private AppCompatTextView mAddress;
    private AppCompatTextView mContent;
    private AppCompatTextView mTime;
    private Context mContext;
    private SimpleDateFormat mSdf = new SimpleDateFormat("MM月dd日 HH:mm", Locale.CHINA);

    public ConversationItemView(Context context) {
        super(context);
        init(context);
    }

    public ConversationItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ConversationItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mAvatar = new AvatarImageView(context);
        mAddress = new AppCompatTextView(context);
        mContent = new AppCompatTextView(context);
        mTime = new AppCompatTextView(context);
        mAvatar.setId(R.id.adapter_conversation_avatar);
        mAddress.setId(R.id.adapter_conversation_address);
        mContent.setId(R.id.adapter_conversation_content);
        mTime.setId(R.id.adapter_conversation_time);
        mAvatar.setTextSize(24);
        mAddress.setTextColor(ColorfulUtil.getTextColorSecondary(context));
        mTime.setTextColor(ColorfulUtil.getTextColorSecondary(context));
        mAddress.setTextSize(12);
        mTime.setTextSize(12);
        mContent.setAutoLinkMask(Linkify.ALL);
        addView(mAvatar);
        addView(mAddress);
        addView(mContent);
        addView(mTime);
        int padding = (int) (AppInfo.sDensity * 4);
        setPadding(0, padding * 2, 0, padding * 2);

        mContent.setOnCreateContextMenuListener((menu, v, menuInfo) -> ConversationItemView.this.performLongClick());

        setLayoutParams(new RecyclerView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    }

    public void bindData(Sms sms, int conversationSize) {
        int avatarSize = (int) (AppInfo.sDensity * 40);
        int padding4 = (int) (AppInfo.sDensity * 4);
        int contentPadding = (int) (AppInfo.sDensity * 46);
        if (sms.type == SmsConst.TYPE_INBOX) {
            //receive
            setReceiveLayout(avatarSize, padding4, contentPadding);
        } else {
            //send
            setSendLayout(padding4, contentPadding);
        }

        if (!TextUtils.isEmpty(sms.name) && !sms.name.equals(sms.address)) {
            mAvatar.setText(sms.name);
        } else {
            mAvatar.setText(null);
        }

        mAddress.setText(null == sms.address ? "" : sms.address);
        if (conversationSize <= 1) {
            mAddress.setText("");
        } else {
            mAddress.setText(sms.type == SmsConst.TYPE_INBOX
                    ? mContext.getString(R.string.address_receive, sms.address)
                    : mContext.getString(R.string.address_send, sms.address));
        }

        mContent.setText(null == sms.body ? "" : sms.body);

        String timeText = sms.date > 0 ? mSdf.format(new Date(sms.date)) : "";
        if (sms.type != SmsConst.TYPE_INBOX) {
            switch (sms.status) {
                case SmsConst.STATUS_COMPLETE:
                    timeText += "    已发送";
                    break;
                case SmsConst.STATUS_PENDING:
                    timeText += "    发送中";
                    break;
                case SmsConst.STATUS_FAILED:
                    timeText += "    发送失败";
                    break;
                case SmsConst.STATUS_RECEIVED:
                    timeText += "    已接收";
                    break;
            }
        }
        mTime.setText(timeText);
    }

    public void bindData(StarSms sms) {
        int avatarSize = (int) (AppInfo.sDensity * 40);
        int padding4 = (int) (AppInfo.sDensity * 4);
        int contentPadding = (int) (AppInfo.sDensity * 46);
        if (sms.type == SmsConst.TYPE_INBOX) {
            //receive
            setReceiveLayout(avatarSize, padding4, contentPadding);
        } else {
            //send
            setSendLayout(padding4, contentPadding);
        }

        if (!TextUtils.isEmpty(sms.name) && !sms.name.equals(sms.address)) {
            mAvatar.setText(sms.name);
        } else {
            mAvatar.setText(null);
        }
        mAddress.setText("");
        mAddress.setVisibility(View.GONE);
        mContent.setText(null == sms.body ? "" : sms.body);
        String timeText = sms.date > 0 ? mSdf.format(new Date(sms.date)) : "";
        mTime.setText(timeText);
    }

    private void setSendLayout(int padding4, int contentPadding) {
        mAvatar.setVisibility(View.GONE);

        mContent.setTextColor(ColorfulUtil.getTextColorHoloWhite(getContext()));
        RelativeLayout.LayoutParams lpContent = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lpContent.addRule(ALIGN_PARENT_RIGHT, TRUE);
        lpContent.addRule(ALIGN_PARENT_TOP, TRUE);
        lpContent.setMargins(contentPadding, 0, padding4 * 2, 0);
        mContent.setLayoutParams(lpContent);
        mContent.setBackgroundResource(R.drawable.bg_primary_rounded_rectangle);
        mContent.setLinkTextColor(ColorfulUtil.getTextColorWhite(getContext()));


        RelativeLayout.LayoutParams lpTime = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lpTime.addRule(ALIGN_PARENT_RIGHT, TRUE);
        lpTime.addRule(RelativeLayout.BELOW, R.id.adapter_conversation_content);
        lpTime.setMargins(padding4 * 2, 0, padding4 * 2, 0);
        mTime.setLayoutParams(lpTime);

        RelativeLayout.LayoutParams lpAddress = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lpAddress.addRule(RelativeLayout.LEFT_OF, R.id.adapter_conversation_time);
        lpAddress.addRule(RelativeLayout.BELOW, R.id.adapter_conversation_content);
        mAddress.setLayoutParams(lpAddress);
    }

    private void setReceiveLayout(int avatarSize, int padding4, int contentPadding) {
        RelativeLayout.LayoutParams lpAvatar = new RelativeLayout.LayoutParams(avatarSize, avatarSize);
        lpAvatar.addRule(ALIGN_PARENT_LEFT, TRUE);
        lpAvatar.addRule(ALIGN_PARENT_TOP, TRUE);
        mAvatar.setLayoutParams(lpAvatar);
        mAvatar.setVisibility(View.VISIBLE);

        mContent.setTextColor(ColorfulUtil.getTextColorPrimary(getContext()));
        RelativeLayout.LayoutParams lpContent = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lpContent.addRule(RelativeLayout.RIGHT_OF, R.id.adapter_conversation_avatar);
        lpContent.addRule(ALIGN_PARENT_TOP, TRUE);
        lpContent.setMargins(padding4 * 2, 0, contentPadding, 0);
        mContent.setLayoutParams(lpContent);
        mContent.setBackgroundResource(R.drawable.bg_white_rounded_rectangle);
        mContent.setLinkTextColor(ColorfulUtil.getPrimaryColor(getContext()));

        RelativeLayout.LayoutParams lpAddress = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lpAddress.addRule(RelativeLayout.RIGHT_OF, R.id.adapter_conversation_avatar);
        lpAddress.addRule(RelativeLayout.BELOW, R.id.adapter_conversation_content);
        lpAddress.setMargins(padding4 * 2, 0, 0, 0);
        mAddress.setLayoutParams(lpAddress);

        RelativeLayout.LayoutParams lpTime = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lpTime.addRule(RelativeLayout.RIGHT_OF, R.id.adapter_conversation_address);
        lpTime.addRule(RelativeLayout.BELOW, R.id.adapter_conversation_content);
        lpTime.setMargins(padding4 * 2, 0, 0, 0);
        mTime.setLayoutParams(lpTime);
    }
}
