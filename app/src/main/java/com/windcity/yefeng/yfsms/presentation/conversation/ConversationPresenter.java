package com.windcity.yefeng.yfsms.presentation.conversation;

import android.text.TextUtils;

import com.windcity.yefeng.yfsms.R;
import com.windcity.yefeng.yfsms.data.model.Conversation;
import com.windcity.yefeng.yfsms.data.model.ConversationItem;
import com.windcity.yefeng.yfsms.data.model.Sms;
import com.windcity.yefeng.yfsms.domain.service.TaskService;
import com.windcity.yefeng.yfsms.domain.usecase.conversation.DeleteConversationTask;
import com.windcity.yefeng.yfsms.domain.usecase.sms.DeleteSmsTask;
import com.windcity.yefeng.yfsms.domain.usecase.sms.LoadSmsTask;
import com.windcity.yefeng.yfsms.domain.usecase.sms.SmsConst;
import com.windcity.yefeng.yfsms.domain.usecase.sms.SmsEvent;
import com.yefeng.support.base.UseCase;
import com.yefeng.support.rxbus.RxBus;
import com.yefeng.support.util.SharedPreferenceUtil;

import java.util.ArrayList;
import java.util.HashSet;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by yefeng on 27/07/2017.
 */

public class ConversationPresenter implements ConversationContract.Presenter {

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private ConversationContract.View mView;

    @Inject
    public ConversationPresenter(ConversationContract.View view) {
        this.mView = view;
    }

    @Override
    public void subscribe() {
        initRxBus();
        load();
        setCurrentConversation();
    }

    @Override
    public void unSubscribe() {
        mCompositeDisposable.clear();
        clearCurrentConversation();
        markSmsRead();
    }

    private void markSmsRead() {
        if (null == mView.getCtx() || null == mView.getData()) {
            return;
        }
        ArrayList<Sms> list = new ArrayList<>();
        for (ConversationItem item : mView.getData()) {
            if (item.sms.read == SmsConst.READ_unread) {
                list.add(item.sms);
            }
        }
        TaskService.markRead(mView.getCtx(), list);
    }


    private void setCurrentConversation() {
        if (null == mView.getConversations()) {
            return;
        }
        HashSet<String> set = new HashSet<>();
        for (Conversation conversation : mView.getConversations()) {
            set.add(conversation.address);
        }
        SharedPreferenceUtil.putStringSet(mView.getCtx(), SmsConst.SP_CURRENT_CONVERSATION_ADDRESS, set);
    }

    private void clearCurrentConversation() {
        SharedPreferenceUtil.remove(mView.getCtx(), SmsConst.SP_CURRENT_CONVERSATION_ADDRESS);
    }


    private void initRxBus() {
        mCompositeDisposable.add(RxBus.getBus()
                .toObserverable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                    if (o instanceof SmsEvent.New) {
                        load();
                    } else if (o instanceof SmsEvent.Update) {
                        load();
                    } else if (o instanceof SmsEvent.Delete) {
                        load();
                    }

                }));
    }

    public void load() {
        mView.getLoadSmsTask().setRequestValues(new LoadSmsTask.Req(mView.getConversations()));
        mView.getLoadSmsTask().setUseCaseCallback(new UseCase.UseCaseCallBackImpl<LoadSmsTask.Res>() {
            @Override
            public void onSuccess(LoadSmsTask.Res response) {
                mView.setData(response.items);
            }

            @Override
            public void onError(String errorMsg) {
                mView.showToast("" + errorMsg);
            }
        });
        mCompositeDisposable.add(mView.getLoadSmsTask().run());
    }

    @Override
    public void doSend(String address) {
        if (!TextUtils.isEmpty(address)) {
            address = address.replaceAll(" +", "");
        }
        if (TextUtils.isEmpty(address)) {
            return;
        }
        TaskService.sendSms(mView.getCtx(), address, mView.getContent(), false);
        mView.clearContent();
    }

    @Override
    public void deleteSms(ArrayList<Sms> smses) {
        mView.getDeleteSmsTask().setRequestValues(new DeleteSmsTask.Req(mView.getCtx().getApplicationContext(), smses));
        mView.getDeleteSmsTask().setUseCaseCallback(new UseCase.UseCaseCallBackImpl<DeleteSmsTask.Res>() {
            @Override
            public void onSuccess(DeleteSmsTask.Res response) {
                super.onSuccess(response);
                if (response.isDeleteSuccess) {
                    RxBus.getBus().send(new SmsEvent.Delete());
                    mView.showToast(mView.getCtx().getString(R.string.delete_ok));
                } else {
                    onError("");
                }
            }

            @Override
            public void onError(String errorMsg) {
                super.onError(errorMsg);
                mView.showToast(mView.getCtx().getString(R.string.delete_failed));
            }
        });
        mCompositeDisposable.add(mView.getDeleteSmsTask().run());
    }

    @Override
    public void deleteConversation(ArrayList<Conversation> mConversations) {
        mView.getDeleteConversationTask().setRequestValues(new DeleteConversationTask.Req(mConversations, mView.getCtx().getApplicationContext()));
        mView.getDeleteConversationTask().setUseCaseCallback(new UseCase.UseCaseCallBackImpl<DeleteConversationTask.Res>() {
            @Override
            public void onSuccess(DeleteConversationTask.Res response) {
                super.onSuccess(response);
                if (response.isDeleteSuccess) {
                    RxBus.getBus().send(new SmsEvent.Delete());
                    mView.showToast(mView.getCtx().getString(R.string.delete_ok));
                    mView.onDeleteConversationSuccess();
                } else {
                    onError("");
                }
            }

            @Override
            public void onError(String errorMsg) {
                super.onError(errorMsg);
                mView.showToast(mView.getCtx().getString(R.string.delete_failed));
            }
        });
        mCompositeDisposable.add(mView.getDeleteConversationTask().run());
    }

    public ArrayList<String> getLongClickActions(String verifyCode) {
        ArrayList<String> actions = new ArrayList<>();
        actions.add(mView.getCtx().getString(R.string.delete_single_sms));
        actions.add(mView.getCtx().getString(R.string.star_sms));
        actions.add(mView.getCtx().getString(R.string.copy_sms));
        if (!TextUtils.isEmpty(verifyCode)) {
            actions.add(mView.getCtx().getString(R.string.copy_verify_code) + verifyCode);
        }
        return actions;
    }

    public int[] getLongClickIds(String verifyCode) {
        return TextUtils.isEmpty(verifyCode) ?
                new int[]{
                        R.string.delete_single_sms,
                        R.string.star_sms,
                        R.string.copy_sms,
                } :
                new int[]{
                        R.string.delete_single_sms,
                        R.string.star_sms,
                        R.string.copy_sms,
                        R.string.copy_verify_code,
                };
    }
}
