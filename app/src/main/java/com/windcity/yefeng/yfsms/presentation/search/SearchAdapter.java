package com.windcity.yefeng.yfsms.presentation.search;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.windcity.yefeng.yfsms.R;
import com.windcity.yefeng.yfsms.data.model.Sms;
import com.windcity.yefeng.yfsms.widget.AvatarImageView;
import com.windcity.yefeng.yfsms.widget.ThemeTextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by yefeng on 25/08/2017.
 */

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    private List<Sms> mData;
    private View.OnClickListener mOnClickListener;
    private SimpleDateFormat mSdf = new SimpleDateFormat("MM月dd日 HH:mm", Locale.CHINA);

    public SearchAdapter(List<Sms> data) {
        this.mData = null == data ? new ArrayList<>() : data;
    }

    public void setData(List<Sms> data) {
        this.mData = null == data ? new ArrayList<>() : data;
        this.notifyDataSetChanged();
    }

    public void setOnClickListener(View.OnClickListener listener) {
        this.mOnClickListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_conversation_list, parent, false);
        view.setOnClickListener(mOnClickListener);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Sms sms = mData.get(position);
        if (null != sms.name && !sms.name.equals(sms.address)) {
            holder.mAvatar.setText(sms.name);
        } else {
            holder.mAvatar.setText(null);
        }
        holder.mTitle.setText(TextUtils.isEmpty(sms.name) ? sms.address : sms.name);
        holder.mTime.setText(mSdf.format(new Date(sms.date)));
        holder.mContent.setText(sms.body + "");
        holder.itemView.setTag(sms);
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
        ThemeTextView mTime;
        @BindView(R.id.tv_adapter_conversation_list_preview)
        ThemeTextView mContent;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
