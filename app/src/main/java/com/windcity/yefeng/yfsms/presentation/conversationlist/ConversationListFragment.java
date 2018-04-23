package com.windcity.yefeng.yfsms.presentation.conversationlist;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.windcity.yefeng.yfsms.R;
import com.windcity.yefeng.yfsms.base.BaseFragment;
import com.windcity.yefeng.yfsms.data.model.Conversation;
import com.windcity.yefeng.yfsms.domain.usecase.conversation.DeleteConversationTask;
import com.windcity.yefeng.yfsms.domain.usecase.conversation.LoadConversationTask;
import com.windcity.yefeng.yfsms.domain.usecase.sms.SmsType;
import com.windcity.yefeng.yfsms.presentation.conversation.ConversationActivity;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by yefeng on 17/07/2017.
 */

public class ConversationListFragment extends BaseFragment implements ConversationListContract.View {

    public static final String TYPE = "TYPE";
    @BindView(R.id.list_conversation)
    RecyclerView mListConversation;
    @Inject
    ConversationListPresenter mPresenter;
    @Inject
    ConversationListAdapter mAdapter;
    @Inject
    LoadConversationTask mLoadConversationTask;
    @Inject
    DeleteConversationTask mDeleteConversationTask;
    private int mType = SmsType.TYPE_CONTACT;
    private int mPosition = 0;//position in main activity
    private Unbinder mUnbinder;

    public ConversationListFragment() {
    }

    public static ConversationListFragment newInstance(int type) {
        Bundle bundle = new Bundle();
        bundle.putInt(TYPE, type);
        ConversationListFragment conversationListFragment = new ConversationListFragment();
        conversationListFragment.setArguments(bundle);
        return conversationListFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (null != getArguments() && -1 != getArguments().getInt(TYPE, -1)) {
            mType = getArguments().getInt(TYPE, SmsType.TYPE_CONTACT);
        } else if (null != savedInstanceState) {
            mType = savedInstanceState.getInt(TYPE, SmsType.TYPE_CONTACT);
        } else {
            mType = SmsType.TYPE_CONTACT;
        }
        DaggerConversationListComponent
                .builder()
                .conversationListModule(new ConversationListModule(this))
                .build()
                .inject(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(TYPE, mType);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_conversation_list, container, false);
        mUnbinder = ButterKnife.bind(this, root);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPresenter.unSubscribe();
        mUnbinder.unbind();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
        mPresenter.subscribe();
    }

    private void init() {
        mAdapter.setLongClickListener(v -> {
            ArrayList<Conversation> list = (ArrayList<Conversation>) v.getTag();
            if (null != list && !list.isEmpty()) {
                longClick(list);
                return true;
            }
            return false;
        });
        mListConversation.setHasFixedSize(true);
        mListConversation.setLayoutManager(new LinearLayoutManager(getActivity()));
        mListConversation.setAdapter(mAdapter);
    }

    private void longClick(ArrayList<Conversation> list) {
        String[] actions = new String[]{
                getString(R.string.delete_conversation)};
        new MaterialDialog.Builder(getCtx())
                .items(actions)
                .itemsCallback((dialog, itemView, position, text) -> {
                    if (position == 0) {
                        new MaterialDialog.Builder(getCtx())
                                .title(R.string.confirm_delete_conversation)
                                .positiveText(R.string.delete)
                                .negativeText(R.string.cancel)
                                .onPositive((dialog1, which) -> mPresenter.deleteConversation(list))
                                .show();
                    }
                })
                .show();
    }

    @Override
    public Context getCtx() {
        return getActivity();
    }

    @Override
    public LoadConversationTask getTask() {
        return mLoadConversationTask;
    }

    @Override
    public int getType() {
        return mType;
    }

    @Override
    public void setData(ArrayList<ArrayList<Conversation>> list) {
        if (null != mAdapter) {
            mAdapter.setData(list);
        }
    }

    public void openConversation(ArrayList<Conversation> conversations) {
        if (null == conversations || conversations.isEmpty() || null == getContext()) {
            return;
        }
        ConversationActivity.startMe(getActivity(), conversations);
    }

    public void openContact(ArrayList<Conversation> conversations) {
        if (null == conversations || conversations.size() != 1 || mType != SmsType.TYPE_CONTACT) {
            return;
        }
        mPresenter.openContact(conversations.get(0).address);
    }

    public DeleteConversationTask getDeleteConversationTask() {
        return mDeleteConversationTask;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
