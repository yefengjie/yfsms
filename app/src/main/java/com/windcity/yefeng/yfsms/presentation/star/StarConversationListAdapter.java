package com.windcity.yefeng.yfsms.presentation.star;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.windcity.yefeng.yfsms.R;
import com.windcity.yefeng.yfsms.data.model.StarSms;
import com.windcity.yefeng.yfsms.widget.AvatarImageView;
import com.windcity.yefeng.yfsms.widget.ThemeTextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by yefeng on 24/08/2017.
 */

public class StarConversationListAdapter extends RecyclerView.Adapter<StarConversationListAdapter.ViewHolder> {
    private ArrayList<ArrayList<StarSms>> mData;
    private SimpleDateFormat mSdf = new SimpleDateFormat("MM月dd日 HH:mm", Locale.CHINA);
    private View.OnClickListener mOnClickListener;

    public StarConversationListAdapter(List<StarSms> smses) {
        handleData(smses);
    }

    public void setData(List<StarSms> smses) {
        handleData(smses);
        notifyDataSetChanged();
    }

    private void handleData(List<StarSms> smses) {
        if (null == mData) {
            mData = new ArrayList<>();
        }
        if (!mData.isEmpty()) {
            mData.clear();
        }
        LinkedHashMap<String, ArrayList<StarSms>> map = new LinkedHashMap<>();
        if (null != smses) {
            for (StarSms starSms : smses) {
                ArrayList<StarSms> values = map.get(starSms.address);
                if (null == values) {
                    values = new ArrayList<>();
                    values.add(starSms);
                    map.put(starSms.address, values);
                } else {
                    values.add(starSms);
                }
            }
        }
        if (!map.isEmpty()) {
            mData.addAll(map.values());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public StarConversationListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_conversation_list, parent, false);
        StarConversationListAdapter.ViewHolder holder = new StarConversationListAdapter.ViewHolder(view);
        view.setOnClickListener(mOnClickListener);
        return holder;
    }

    public void setOnClickListener(View.OnClickListener listener) {
        this.mOnClickListener = listener;
    }

    @Override
    public void onBindViewHolder(StarConversationListAdapter.ViewHolder holder, int position) {
        ArrayList<StarSms> list = mData.get(position);
        if (null == list || list.isEmpty()) {
            return;
        }
        StarSms starSms = list.get(0);
        if (null != starSms.name && !starSms.name.equals(starSms.address)) {
            holder.mAvatar.setText(starSms.name);
        } else {
            holder.mAvatar.setText(null);
        }

        holder.mTitle.setText(TextUtils.isEmpty(starSms.name) ? starSms.address + "" : starSms.name);
        holder.mNewestTime.setText(mSdf.format(new Date(starSms.date)));
        holder.mPreviewContent.setText(starSms.body + "");
        holder.mUnreadNum.setVisibility(View.GONE);
        holder.mTitle.setReadStatusPrimary();
        holder.mNewestTime.setReadStatusSecondary();
        holder.mPreviewContent.setReadStatusSecondary();
        holder.itemView.setTag(list);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_adapter_conversation_list_avatar)
        AvatarImageView mAvatar;
        @BindView(R.id.tv_adapter_conversation_list_title)
        ThemeTextView mTitle;
        @BindView(R.id.tv_adapter_conversation_list_newest_time)
        ThemeTextView mNewestTime;
        @BindView(R.id.tv_adapter_conversation_list_preview)
        ThemeTextView mPreviewContent;
        @BindView(R.id.tv_adapter_conversation_list_unread_num)
        TextView mUnreadNum;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
