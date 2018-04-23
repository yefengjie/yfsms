package com.windcity.yefeng.yfsms.presentation.conversationlist;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.windcity.yefeng.yfsms.R;
import com.windcity.yefeng.yfsms.data.model.Conversation;
import com.windcity.yefeng.yfsms.widget.AvatarImageView;
import com.windcity.yefeng.yfsms.widget.ThemeTextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by yefeng on 19/07/2017.
 */

public class ConversationListAdapter extends RecyclerView.Adapter<ConversationListAdapter.ViewHolder> {

    private ArrayList<ArrayList<Conversation>> mData;
    private SimpleDateFormat mSdf = new SimpleDateFormat("MM月dd日 HH:mm", Locale.CHINA);
    private ConversationListFragment mFragment;
    private View.OnLongClickListener mLongClickListener;

    @Inject
    public ConversationListAdapter(ArrayList<ArrayList<Conversation>> data, ConversationListFragment fragment) {
        this.mData = data == null ? new ArrayList<>() : data;
        this.mFragment = fragment;
    }

    public void setData(ArrayList<ArrayList<Conversation>> data) {
        this.mData = data == null ? new ArrayList<>() : data;
        notifyDataSetChanged();
    }

    @SuppressWarnings("unchecked")
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_conversation_list, parent, false);
        ViewHolder holder = new ViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Conversation> list = (ArrayList<Conversation>) view.getTag();
                mFragment.openConversation(list);
            }
        });
        holder.mAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Conversation> list = (ArrayList<Conversation>) view.getTag();
                mFragment.openContact(list);
            }
        });
        view.setOnLongClickListener(mLongClickListener);
        return holder;
    }

    public void setLongClickListener(View.OnLongClickListener listener) {
        this.mLongClickListener = listener;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ArrayList<Conversation> list = mData.get(position);
        if (null == list || list.isEmpty()) {
            return;
        }
        Conversation conversation = list.get(0);

        if (null != conversation.name && !conversation.name.equals(conversation.address)) {
            holder.mAvatar.setText(conversation.name);
        } else {
            holder.mAvatar.setText(null);
        }

        holder.mTitle.setText(TextUtils.isEmpty(conversation.name) ? conversation.address : conversation.name);

        holder.mNewestTime.setText(mSdf.format(new Date(conversation.newestTime)));

        holder.mPreviewContent.setText(conversation.newestContent);

        int totalNum = 0;
        int unReadNum = 0;
        for (Conversation c : list) {
            totalNum += c.totalNum;
            unReadNum += c.unreadNum;
        }
        if (unReadNum > 0) {
            holder.mUnreadNum.setVisibility(View.VISIBLE);
            if (unReadNum > 9) {
                holder.mUnreadNum.setText("");
            } else {
                holder.mUnreadNum.setText(String.valueOf(unReadNum));
            }
            holder.mTitle.setUnreadStatusPrimaryHighlight();
            holder.mNewestTime.setReadStatusSecondary();
            holder.mPreviewContent.setUnreadStatusSecondaryHighlight(true);

        } else {
            holder.mUnreadNum.setVisibility(View.INVISIBLE);
            holder.mTitle.setReadStatusPrimary();
            holder.mNewestTime.setReadStatusSecondary();
            holder.mPreviewContent.setReadStatusSecondary();
        }
        holder.itemView.setTag(list);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_adapter_conversation_list_avatar)
        AvatarImageView mAvatar;
        @BindView(R.id.tv_adapter_conversation_list_unread_num)
        TextView mUnreadNum;
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
}
