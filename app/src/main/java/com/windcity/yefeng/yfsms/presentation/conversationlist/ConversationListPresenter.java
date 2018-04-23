package com.windcity.yefeng.yfsms.presentation.conversationlist;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import com.windcity.yefeng.yfsms.R;
import com.windcity.yefeng.yfsms.data.model.Conversation;
import com.windcity.yefeng.yfsms.domain.usecase.contact.SystemContactEvent;
import com.windcity.yefeng.yfsms.domain.usecase.conversation.DeleteConversationTask;
import com.windcity.yefeng.yfsms.domain.usecase.conversation.LoadConversationTask;
import com.windcity.yefeng.yfsms.domain.usecase.sms.SmsEvent;
import com.yefeng.support.base.UseCase;
import com.yefeng.support.rxbus.RxBus;

import java.util.ArrayList;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by yefeng on 19/07/2017.
 */

public class ConversationListPresenter implements ConversationListContract.Presenter {

    private ConversationListContract.View mView;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private boolean mLoadLock = false;

    @Inject
    public ConversationListPresenter(ConversationListContract.View view) {
        this.mView = view;
    }

    @Override
    public void subscribe() {
        mLoadLock = false;
        initRxBus();
        load();
    }

    private void load() {
        if (mLoadLock) {
            return;
        }
        mLoadLock = true;
        mView.getTask().setRequestValues(new LoadConversationTask.Req(mView.getType()));
        mView.getTask().setUseCaseCallback(new UseCase.UseCaseCallBackImpl<LoadConversationTask.Res>() {
            @Override
            public void onSuccess(LoadConversationTask.Res response) {
                mView.setData(response.foldConversations);
                mLoadLock = false;
            }

            @Override
            public void onError(String errorMsg) {
                mView.showToast(errorMsg + "");
                mLoadLock = false;
            }
        });
        mCompositeDisposable.add(mView.getTask().run());
    }

    @Override
    public void unSubscribe() {
        mLoadLock = false;
        mCompositeDisposable.clear();
    }

    private void initRxBus() {
        mCompositeDisposable.add(RxBus.getBus()
                .toObserverable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                    if (o instanceof SmsEvent.New) {
                        load();
                    } else if (o instanceof SmsEvent.UnreadNumChange) {
                        load();
                    } else if (o instanceof SmsEvent.Delete) {
                        load();
                    } else if (o instanceof SystemContactEvent.Update) {
                        load();
                    }
                }));
    }

    void openContact(String address) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(address));
        String[] projection = new String[]{ContactsContract.PhoneLookup._ID};
        Cursor cur = null;
        try {
            cur = mView.getCtx().getContentResolver().query(uri, projection, null, null, null);
            if (cur != null && cur.moveToNext()) {
                Long id = cur.getLong(0);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri contactUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(id));
                intent.setData(contactUri);
                mView.getCtx().startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != cur) {
                try {
                    cur.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void deleteConversation(ArrayList<Conversation> list) {
        mView.getDeleteConversationTask().setRequestValues(new DeleteConversationTask.Req(list, mView.getCtx().getApplicationContext()));
        mView.getDeleteConversationTask().setUseCaseCallback(new UseCase.UseCaseCallBackImpl<DeleteConversationTask.Res>() {
            @Override
            public void onSuccess(DeleteConversationTask.Res response) {
                super.onSuccess(response);
                if (response.isDeleteSuccess) {
                    RxBus.getBus().send(new SmsEvent.Delete());
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
}
