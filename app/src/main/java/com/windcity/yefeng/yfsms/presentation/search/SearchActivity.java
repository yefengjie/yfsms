package com.windcity.yefeng.yfsms.presentation.search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.windcity.yefeng.yfsms.R;
import com.windcity.yefeng.yfsms.base.BaseActivity;
import com.windcity.yefeng.yfsms.data.model.Sms;
import com.windcity.yefeng.yfsms.domain.usecase.sms.SearchSmsTask;
import com.windcity.yefeng.yfsms.presentation.conversation.ConversationActivity;
import com.yefeng.support.base.UseCase;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.disposables.CompositeDisposable;

public class SearchActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.rv_search)
    RecyclerView mList;
    @BindView(R.id.tv_prompt)
    TextView mPrompt;
    @BindView(R.id.et_search_keyword)
    EditText mEtKeyword;
    SearchAdapter mAdapter;
    SearchSmsTask mSearchSmsTask;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    public static void startMe(Context context) {
        context.startActivity(new Intent(context, SearchActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);
        init();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mCompositeDisposable.clear();
    }

    @OnClick(R.id.fl_back)
    void clickBack() {
        finish();
    }

    private void init() {
        setSupportActionBar(mToolbar);
        mAdapter = new SearchAdapter(null);
        mAdapter.setOnClickListener(v -> openSms((Sms) v.getTag()));
        mList.setHasFixedSize(true);
        mList.setLayoutManager(new LinearLayoutManager(getCtx()));
        mList.setAdapter(mAdapter);
        mEtKeyword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                displaySearchPrompt();
                if (!TextUtils.isEmpty(editable)) {
                    String keyword = editable.toString();
                    search(keyword);
                }
            }
        });
    }

    private void search(String keyword) {
        if (null == mSearchSmsTask) {
            mSearchSmsTask = new SearchSmsTask();
        }
        if (null == mSearchSmsTask.getUseCaseCallback()) {
            mSearchSmsTask.setUseCaseCallback(new UseCase.UseCaseCallBackImpl<SearchSmsTask.Res>() {
                @Override
                public void onSuccess(SearchSmsTask.Res response) {
                    mAdapter.setData(response.result);
                    displaySearchPrompt();
                }

                @Override
                public void onError(String errorMsg) {
                    showToast(R.string.load_error);
                }
            });
        }
        mSearchSmsTask.setRequestValues(new SearchSmsTask.Req(keyword));
        mCompositeDisposable.add(mSearchSmsTask.run());
    }

    private void openSms(Sms sms) {
        if (null != sms) {
            ConversationActivity.startMe(this, sms);
        }
    }

    public void displaySearchPrompt() {
        if (null == mAdapter || null == mList || null == mPrompt || null == mEtKeyword) {
            return;
        }
        if (TextUtils.isEmpty(mEtKeyword.getText())) {
            mList.setVisibility(View.INVISIBLE);
            mPrompt.setVisibility(View.VISIBLE);
            mPrompt.setText("");
        } else {
            if (mAdapter.getItemCount() > 0) {
                mList.setVisibility(View.VISIBLE);
                mPrompt.setVisibility(View.INVISIBLE);
            } else {
                mList.setVisibility(View.INVISIBLE);
                mPrompt.setVisibility(View.VISIBLE);
                mPrompt.setText(R.string.prompt_no_search_result);
            }
        }
    }

}
