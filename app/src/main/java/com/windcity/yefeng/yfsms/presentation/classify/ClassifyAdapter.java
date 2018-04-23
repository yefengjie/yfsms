package com.windcity.yefeng.yfsms.presentation.classify;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;

import com.windcity.yefeng.yfsms.R;
import com.windcity.yefeng.yfsms.data.model.Tabs;
import com.windcity.yefeng.yfsms.domain.usecase.sms.SmsType;
import com.windcity.yefeng.yfsms.presentation.conversationlist.ConversationListFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yefeng on 20/07/2017.
 */

public class ClassifyAdapter extends FragmentPagerAdapter {

    private List<Tabs> mTabs;
    private SparseArray<String> mTabsLabels;
    private Context mContext;

    public ClassifyAdapter(FragmentManager fm, Context context, List<Tabs> list) {
        super(fm);
        this.mTabs = null == list ? new ArrayList<>() : list;
        this.mContext = context;
        getTabLabels();
    }

    public void setTabs(List<Tabs> list) {
        this.mTabs = null == list ? new ArrayList<>() : list;
    }

    @Override
    public Fragment getItem(int position) {
        return ConversationListFragment.newInstance((int) mTabs.get(position).id);
    }

    @Override
    public int getCount() {
        return mTabs.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Tabs tabs = mTabs.get(position);
        String text = getTabLabels().get((int) tabs.id);
        if (tabs.unreadNum > 9) {
            text += "(9+)";
        } else if (tabs.unreadNum > 0) {
            text += "(" + tabs.unreadNum + ")";
        }
        return text;
    }

    private SparseArray<String> getTabLabels() {
        if (null == mTabsLabels) {
            mTabsLabels = new SparseArray<>();
        }
        if (mTabsLabels.size() <= 0) {
            mTabsLabels.put(SmsType.TYPE_CONTACT, mContext.getString(R.string.type_contact));
            mTabsLabels.put(SmsType.TYPE_STRANGER, mContext.getString(R.string.type_stranger));
            mTabsLabels.put(SmsType.TYPE_SERVICE, mContext.getString(R.string.type_service));
            mTabsLabels.put(SmsType.TYPE_NOTIFICATION, mContext.getString(R.string.type_notification));
        }
        return mTabsLabels;
    }
}
