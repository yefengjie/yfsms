package com.windcity.yefeng.yfsms.presentation.delete;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.windcity.yefeng.yfsms.R;
import com.windcity.yefeng.yfsms.data.model.Conversation;
import com.windcity.yefeng.yfsms.widget.AvatarImageView;
import com.windcity.yefeng.yfsms.widget.ThemeTextView;
import com.yefeng.support.rxbus.RxBus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by yefeng on 23/08/2017.
 */

public class DelConversationAdapter extends RecyclerView.Adapter<DelConversationAdapter.ViewHolder> {

    private ArrayList<Conversation> mDatas;
    private SimpleDateFormat mSdf = new SimpleDateFormat("MM月dd日 HH:mm", Locale.CHINA);

    public DelConversationAdapter() {
        this.mDatas = new ArrayList<>();
    }

    public void setData(ArrayList<Conversation> data) {
        this.mDatas = null == data ? new ArrayList<>() : data;
        notifyDateChange();
    }

    public ArrayList<Conversation> getCheckedData() {
        if (null == mDatas || mDatas.isEmpty()) {
            return null;
        }
        ArrayList<Conversation> list = new ArrayList<>();
        for (Conversation conversation : mDatas) {
            if (conversation.isCheck) {
                list.add(conversation);
            }
        }
        return list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_del_conversation, parent, false);
        ViewHolder vh = new ViewHolder(view);
        vh.itemView.setOnClickListener(v -> {
            Conversation conversation = (Conversation) v.getTag();
            conversation.isCheck = !conversation.isCheck;
            notifyDateChange();
        });
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Conversation conversation = mDatas.get(position);
        if (null != conversation.name && !conversation.name.equals(conversation.address)) {
            holder.mAvatar.setText(conversation.name);
        } else {
            holder.mAvatar.setText(null);
        }
        holder.mTitle.setText(TextUtils.isEmpty(conversation.name) ? conversation.address : conversation.name);
        holder.mNewestTime.setText(mSdf.format(new Date(conversation.newestTime)));
        holder.mPreviewContent.setText(conversation.newestContent);
        holder.mTitle.setReadStatusPrimary();
        holder.mNewestTime.setReadStatusSecondary();
        holder.mPreviewContent.setReadStatusSecondary();
        holder.itemView.setTag(conversation);
        holder.mCb.setChecked(conversation.isCheck);
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    public void selectAll() {
        if (null == mDatas) {
            return;
        }
        boolean isAllChecked = isAllCheck();
        for (Conversation conversation : mDatas) {
            conversation.isCheck = !isAllChecked;
        }
        notifyDateChange();
    }

    public boolean isAllCheck() {
        if (null == mDatas || mDatas.isEmpty()) {
            return false;
        }
        boolean isAllChecked = true;
        for (Conversation conversation : mDatas) {
            if (!conversation.isCheck) {
                isAllChecked = false;
                break;
            }
        }
        return isAllChecked;
    }

    private void notifyDateChange() {
        this.notifyDataSetChanged();
        RxBus.getBus().send(new OnCheckChangeEvent());
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.cb_check)
        CheckBox mCb;
        @BindView(R.id.iv_adapter_conversation_list_avatar)
        AvatarImageView mAvatar;
        @BindView(R.id.tv_adapter_conversation_list_title)
        ThemeTextView mTitle;
        @BindView(R.id.tv_adapter_conversation_list_newest_time)
        ThemeTextView mNewestTime;
        @BindView(R.id.tv_adapter_conversation_list_preview)
        ThemeTextView mPreviewContent;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public static class OnCheckChangeEvent {
    }
}
