package com.windcity.yefeng.yfsms.presentation.star;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.windcity.yefeng.yfsms.R;
import com.windcity.yefeng.yfsms.base.BaseFragment;
import com.windcity.yefeng.yfsms.data.model.StarSms;
import com.windcity.yefeng.yfsms.domain.usecase.star.LoadStarSmsTask;
import com.yefeng.support.base.UseCase;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by yefeng on 18/08/2017.
 */

public class StarConversationListFragment extends BaseFragment {
    @BindView(R.id.list)
    RecyclerView mList;
    Unbinder mUnbinder;
    StarConversationListAdapter mAdapter;
    LoadStarSmsTask mLoadStarSmsTask;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    public static StarConversationListFragment newInstance() {
        return new StarConversationListFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_star, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void onStart() {
        super.onStart();
        load();
    }

    @Override
    public void onStop() {
        super.onStop();
        mCompositeDisposable.clear();
    }

    private void init() {
        mAdapter = new StarConversationListAdapter(null);
        mAdapter.setOnClickListener(v -> {
            ArrayList<StarSms> list = (ArrayList<StarSms>) v.getTag();
            openStartActivity(list);
        });
        mList.setHasFixedSize(true);
        mList.setLayoutManager(new LinearLayoutManager(getContext()));
        mList.setAdapter(mAdapter);
    }

    private void openStartActivity(ArrayList<StarSms> list) {
        StarActivity.startMe(getContext(), list);
    }

    private void load() {
        if (null == mLoadStarSmsTask) {
            mLoadStarSmsTask = new LoadStarSmsTask();
        }
        if (null == mLoadStarSmsTask.getUseCaseCallback()) {
            mLoadStarSmsTask.setUseCaseCallback(new UseCase.UseCaseCallBackImpl<LoadStarSmsTask.Res>() {
                @Override
                public void onSuccess(LoadStarSmsTask.Res response) {
                    mAdapter.setData(response.list);
                }

                @Override
                public void onError(String errorMsg) {
                    showToast(getString(R.string.loading_failed) + ":" + errorMsg);
                }
            });
        }
        mCompositeDisposable.add(mLoadStarSmsTask.run());
    }
}
