package com.windcity.yefeng.yfsms.presentation.classify;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.windcity.yefeng.yfsms.R;
import com.windcity.yefeng.yfsms.base.BaseFragment;
import com.windcity.yefeng.yfsms.data.model.Tabs;
import com.windcity.yefeng.yfsms.domain.usecase.conversation.DeleteConversationTask;
import com.windcity.yefeng.yfsms.domain.usecase.sms.SmsCenter;
import com.windcity.yefeng.yfsms.domain.usecase.sms.SmsType;
import com.windcity.yefeng.yfsms.domain.usecase.tabs.LoadTabsTask;
import com.windcity.yefeng.yfsms.presentation.drawer.DrawerActivity;
import com.windcity.yefeng.yfsms.presentation.search.SearchActivity;
import com.yefeng.support.util.SharedPreferenceUtil;

import org.polaric.colorful.ColorfulUtil;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by yefeng on 18/08/2017.
 */

public class ClassifyFragment extends BaseFragment implements ClassifyContract.View {

    private static final String MARK_READ_PROMPT = "MARK_READ_PROMPT";

    @BindView(R.id.container)
    ViewPager mVp;
    Unbinder mUnbinder;
    ClassifyAdapter mAdapter;
    @Inject
    ClassifyPresenter mPresenter;
    @Inject
    LoadTabsTask mLoadTabsTask;
    @Inject
    DeleteConversationTask mDeleteConversationTask;
    boolean mAutoLocate = true;

    public static ClassifyFragment newInstance() {
        return new ClassifyFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mAutoLocate = true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_classify, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_delete_multiple_conversation) {
            deleteConversation();
            return true;
        } else if (item.getItemId() == R.id.action_mark_all_read) {
            markRead();
            return true;
        } else if (item.getItemId() == R.id.action_search) {
            search();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_classified, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getParentActivity().updateTabs(null, View.GONE);
        mUnbinder.unbind();
        mPresenter.unSubscribe();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DaggerClassifyComponent.builder()
                .classifyModule(new ClassifyModule(this))
                .build()
                .inject(this);
        getParentActivity().updateTabs(mVp, View.VISIBLE);
        init();
    }

    private void init() {
        List<Tabs> list = SmsCenter.initTabs();
        if (null == list || list.isEmpty()) {
            return;
        }
        mAdapter = new ClassifyAdapter(getChildFragmentManager(), getContext(), list);
        mVp.setAdapter(mAdapter);
        mPresenter.subscribe();
    }

    public void updateTabsUnreadNum(List<Tabs> tabses) {
        if (null == tabses || tabses.isEmpty() || null == getParentActivity().getTabLayout() || null == mAdapter) {
            return;
        }
        mAdapter.setTabs(tabses);
        int size = tabses.size();
        for (int i = 0; i < size; i++) {
            TabLayout.Tab tab = getParentActivity().getTabLayout().getTabAt(i);
            if (null != tab) {
                tab.setText(mAdapter.getPageTitle(i));
            }
        }
    }


    public void autoLocate(int position) {
        if (null == mVp
                || null == mVp.getAdapter()
                || position >= mVp.getAdapter().getCount()) {
            return;
        }
        if (mVp.getCurrentItem() != position) {
            mVp.setCurrentItem(position, false);
        }
    }

    private DrawerActivity getParentActivity() {
        return (DrawerActivity) getActivity();
    }

    private void deleteConversation() {
        String[] choose = new String[]{
                getString(R.string.delete_all_conversation),
                getString(R.string.delete_contacts_conversation),
                getString(R.string.delete_stranger_conversation),
                getString(R.string.delete_servers_conversation),
                getString(R.string.delete_notification_conversation),
                getString(R.string.choose_to_delete_conversation),
        };
        int[] ids = new int[]{
                SmsType.TYPE_ALL,
                SmsType.TYPE_CONTACT,
                SmsType.TYPE_STRANGER,
                SmsType.TYPE_SERVICE,
                SmsType.TYPE_NOTIFICATION,
                -1
        };
        new MaterialDialog.Builder(getContext())
                .title(R.string.choose_action)
                .itemsIds(ids)
                .items(choose)
                .itemsCallback((dialog, itemView, position, text) -> confirmDelete(itemView.getId()))
                .titleColor(ColorfulUtil.getAccentColor(getContext()))
                .build()
                .show();
    }

    private void confirmDelete(int id) {
        if (id <= 0) {
            mPresenter.deleteConversation(id);
            return;
        }
        new MaterialDialog.Builder(getContext())
                .title(R.string.confirm_delete)
                .positiveText(R.string.confirm)
                .negativeText(R.string.cancel)
                .onPositive((dialog, which) -> mPresenter.deleteConversation(id))
                .build()
                .show();
    }

    private void search() {
        SearchActivity.startMe(getContext());
    }

    private void markRead() {
        if (null == getContext() || null == mPresenter) {
            return;
        }
        boolean notShowPrompt = SharedPreferenceUtil.getBoolean(getContext(), MARK_READ_PROMPT, false);
        if (notShowPrompt) {
            mPresenter.markAllSmsRead();
            return;
        }
        SharedPreferenceUtil.putBoolean(getContext(), MARK_READ_PROMPT, true);
        new MaterialDialog.Builder(getContext())
                .title(R.string.mark_all_sms_read)
                .positiveText(R.string.confirm)
                .negativeText(R.string.cancel)
                .onPositive((dialog, which) -> mPresenter.markAllSmsRead())
                .build()
                .show();
    }
}
