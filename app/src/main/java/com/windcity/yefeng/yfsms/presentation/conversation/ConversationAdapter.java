package com.windcity.yefeng.yfsms.presentation.conversation;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.windcity.yefeng.yfsms.data.model.ConversationItem;
import com.windcity.yefeng.yfsms.data.model.Sms;
import com.windcity.yefeng.yfsms.widget.ConversationItemView;

import org.polaric.colorful.ColorfulUtil;

import java.util.ArrayList;

import javax.inject.Inject;

/**
 * Created by yefeng on 27/07/2017.
 */

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ViewHolder> {


    private int mConversationSize;
    private ArrayList<ConversationItem> mItems;
    private View.OnLongClickListener mLongClickListener;
    private Sms mSearchSms;

    @Inject
    public ConversationAdapter(ArrayList<ConversationItem> items, int conversationSize) {
        this.mItems = null == items ? new ArrayList<>() : items;
        this.mConversationSize = conversationSize;
    }

    public void setData(ArrayList<ConversationItem> items, int conversationSize, Sms searchSms) {
        this.mItems = null == items ? new ArrayList<>() : items;
        this.mConversationSize = conversationSize;
        this.mSearchSms = searchSms;
        this.notifyDataSetChanged();
    }

    public ArrayList<ConversationItem> getData() {
        return mItems;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ConversationItemView view = new ConversationItemView(parent.getContext());
        view.setOnLongClickListener(mLongClickListener);
        return new ViewHolder(view);
    }

    public void setOnLongClickListener(View.OnLongClickListener longClickListener) {
        this.mLongClickListener = longClickListener;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ConversationItemView itemView = (ConversationItemView) holder.itemView;
        ConversationItem item = mItems.get(position);
        itemView.setTag(item);
        itemView.bindData(item.sms, mConversationSize);
        if (null != mSearchSms && item.sms.id == mSearchSms.id) {
            itemView.setBackgroundColor(ColorfulUtil.getPrimaryLightColor(holder.itemView.getContext()));
        } else {
            itemView.setBackgroundColor(ColorfulUtil.getTransparentColor(holder.itemView.getContext()));
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public int getSearchSmsPosition(Sms searchSms) {
        if (null == mItems || mItems.isEmpty()) {
            return 0;
        }
        int size = mItems.size();
        for (int i = 0; i < size; i++) {
            if (mItems.get(i).sms.id == searchSms.id) {
                return i;
            }
        }
        return 0;
    }


    static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
