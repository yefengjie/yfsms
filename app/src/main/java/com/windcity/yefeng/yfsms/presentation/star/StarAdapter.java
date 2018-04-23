package com.windcity.yefeng.yfsms.presentation.star;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.windcity.yefeng.yfsms.data.model.StarSms;
import com.windcity.yefeng.yfsms.widget.ConversationItemView;

import java.util.ArrayList;

/**
 * Created by yefeng on 24/08/2017.
 */

public class StarAdapter extends RecyclerView.Adapter<StarAdapter.ViewHolder> {

    private ArrayList<StarSms> mDatas;
    private View.OnLongClickListener mLongClickListener;

    public StarAdapter(ArrayList<StarSms> smses) {
        this.mDatas = null == smses ? new ArrayList<>() : smses;
    }

    public void setData(ArrayList<StarSms> smses) {
        this.mDatas = null == smses ? new ArrayList<>() : smses;
        this.notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ConversationItemView view = new ConversationItemView(parent.getContext());
        view.setOnLongClickListener(mLongClickListener);
        return new StarAdapter.ViewHolder(view);
    }

    public void setOnLongClickListener(View.OnLongClickListener longClickListener) {
        this.mLongClickListener = longClickListener;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ConversationItemView itemView = (ConversationItemView) holder.itemView;
        StarSms sms = mDatas.get(position);
        itemView.setTag(sms);
        itemView.bindData(sms);
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
