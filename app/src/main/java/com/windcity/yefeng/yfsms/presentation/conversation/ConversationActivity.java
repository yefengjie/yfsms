package com.windcity.yefeng.yfsms.presentation.conversation;

import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.afollestad.materialdialogs.MaterialDialog;
import com.windcity.yefeng.yfsms.R;
import com.windcity.yefeng.yfsms.base.BaseActivity;
import com.windcity.yefeng.yfsms.data.db.DbHelper;
import com.windcity.yefeng.yfsms.data.model.Conversation;
import com.windcity.yefeng.yfsms.data.model.ConversationItem;
import com.windcity.yefeng.yfsms.data.model.Conversation_;
import com.windcity.yefeng.yfsms.data.model.Sms;
import com.windcity.yefeng.yfsms.domain.service.TaskService;
import com.windcity.yefeng.yfsms.domain.usecase.conversation.DeleteConversationTask;
import com.windcity.yefeng.yfsms.domain.usecase.sms.DeleteSmsTask;
import com.windcity.yefeng.yfsms.domain.usecase.sms.LoadSmsTask;
import com.windcity.yefeng.yfsms.domain.usecase.sms.SmsUtil;
import com.windcity.yefeng.yfsms.domain.usecase.star.StarSmsTask;
import com.yefeng.support.util.KeyboardUtil;
import com.yefeng.support.util.SharedPreferenceUtil;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.objectbox.Box;

/**
 * Created by yefeng on 27/07/2017.
 */

public class ConversationActivity extends BaseActivity implements ConversationContract.View {

    private static final String CONVERSATIONS = "conversations";
    private static final String SEARCH_SMS = "search_sms";
    private static final int REQ_CODE_ADD_CONTACT = 1000;
    private static final String TEMP_MSG = "TEMP_MSG_CONVERSATION";
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.list_sms)
    RecyclerView mList;
    @BindView(R.id.et_send_content)
    EditText mEtSendContent;
    @Inject
    ConversationAdapter mAdapter;
    @Inject
    ConversationPresenter mPresenter;
    @Inject
    LoadSmsTask mLoadSmsTask;
    @Inject
    DeleteSmsTask mDeleteSmsTask;
    @Inject
    DeleteConversationTask mDeleteConversationTask;
    private ArrayList<Conversation> mConversations;
    private String mAddContactAddress;
    private Sms mSearchSms;

    public static void startMe(Context context, ArrayList<Conversation> list) {
        Intent intent = new Intent(context, ConversationActivity.class);
        intent.putParcelableArrayListExtra(CONVERSATIONS, list);
        context.startActivity(intent);
    }

    public static Intent getIntent(Context context, ArrayList<Conversation> list) {
        Intent intent = new Intent(context, ConversationActivity.class);
        intent.putParcelableArrayListExtra(CONVERSATIONS, list);
        return intent;
    }

    public static void startMe(Context context, Sms sms) {
        Box<Conversation> conversationBox = DbHelper.getInstance().getBoxStore().boxFor(Conversation.class);
        Conversation conversation = conversationBox.query()
                .equal(Conversation_.address, sms.address)
                .build()
                .findFirst();
        if (null == conversation) {
            return;
        }
        ArrayList<Conversation> list = new ArrayList<>();
        list.add(conversation);
        Intent intent = new Intent(context, ConversationActivity.class);
        intent.putParcelableArrayListExtra(CONVERSATIONS, list);
        intent.putExtra(SEARCH_SMS, sms);
        context.startActivity(intent);
    }

    public static void startMeWithFlags(Context context, ArrayList<Conversation> list, int flag) {
        Intent intent = new Intent(context, ConversationActivity.class);
        intent.putParcelableArrayListExtra(CONVERSATIONS, list);
        intent.addFlags(flag);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        ButterKnife.bind(this);
        DaggerConversationComponent
                .builder()
                .conversationModule(new ConversationModule(this))
                .build()
                .inject(this);
        //clear notification
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancelAll();
        if (!initConversation(savedInstanceState)) {
            finish();
            return;
        }
        autoInputTempMsg();
        init();
    }

    private void autoInputTempMsg() {
        String cacheTempMsg = readTempMsgFromSp();
        if (!TextUtils.isEmpty(cacheTempMsg) && null != mEtSendContent) {
            mEtSendContent.setText(cacheTempMsg);
            mEtSendContent.setSelection(mEtSendContent.length());
        }
    }

    private boolean initConversation(Bundle savedInstanceState) {
        if (null != getIntent()) {
            mConversations = getIntent().getParcelableArrayListExtra(CONVERSATIONS);
            mSearchSms = getIntent().getParcelableExtra(SEARCH_SMS);
        } else if (null != savedInstanceState) {
            mConversations = savedInstanceState.getParcelableArrayList(CONVERSATIONS);
        }
        if (null == mConversations || mConversations.isEmpty()) {
            return false;
        }
        return true;
    }

    private void init() {
        setSupportActionBar(mToolbar);
        Conversation conversation = mConversations.get(0);
        if (!conversation.name.equals(conversation.address)
                && mConversations.size() == 1) {
            mToolbar.setSubtitle(conversation.address);
        }
        setTitle(conversation.name);
        ActionBar actionBar = getSupportActionBar();
        if (null != actionBar) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mList.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        mList.setLayoutManager(layoutManager);
        mAdapter.setOnLongClickListener(v -> {
            ConversationItem item = (ConversationItem) v.getTag();
            if (null != item) {
                longClickItem(item);
                return true;
            }
            return false;
        });
        mList.setAdapter(mAdapter);
    }

    private void longClickItem(ConversationItem item) {
        if (null == item) {
            return;
        }
        String verifyCode = SmsUtil.extractVerifyCode(item.sms.body);
        new MaterialDialog.Builder(this)
                .items(mPresenter.getLongClickActions(verifyCode))
                .itemsIds(mPresenter.getLongClickIds(verifyCode))
                .itemsCallback((dialog, itemView, position, text) -> {
                    int id = itemView.getId();
                    if (id == R.string.delete_single_sms) {
                        ArrayList<Sms> smses = new ArrayList<>();
                        smses.add(item.sms);
                        new MaterialDialog.Builder(getCtx())
                                .title(R.string.confirm_delete_sms)
                                .positiveText(R.string.delete)
                                .negativeText(R.string.cancel)
                                .onPositive((dialog1, which) -> mPresenter.deleteSms(smses))
                                .show();
                    } else if (id == R.string.copy_sms) {
                        copySms(item.sms.address, item.sms.body);
                    } else if (id == R.string.copy_verify_code) {
                        copySms(item.sms.address, verifyCode);
                    } else if (id == R.string.star_sms) {
                        starSms(item.sms);
                    }
                })
                .show();
    }

    private void starSms(Sms sms) {
        if (StarSmsTask.start(sms)) {
            showToast(getString(R.string.star_success));
        } else {
            showToast(getString(R.string.star_failed));
        }
    }

    private void copySms(String address, String body) {
        ClipboardManager cmb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        cmb.setPrimaryClip(ClipData.newPlainText(address, body));
        showToast(getString(R.string.copy_success));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_conversation, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_phone:
                call();
                return true;
            case R.id.action_add_to_contact:
                addToContact();
                return true;
            case R.id.action_delete_conversation:
                new MaterialDialog.Builder(getCtx())
                        .title(R.string.confirm_delete_conversation)
                        .positiveText(R.string.delete)
                        .negativeText(R.string.cancel)
                        .onPositive((dialog1, which) -> mPresenter.deleteConversation(mConversations))
                        .show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void call() {
        if (null == mConversations || mConversations.isEmpty()) {
            return;
        }
        if (mConversations.size() == 1) {
            doCall(mConversations.get(0).address);
            return;
        }
        ArrayList<String> addresses = new ArrayList<>();
        for (Conversation conversation : mConversations) {
            addresses.add(conversation.address);
        }
        new MaterialDialog.Builder(this)
                .title(R.string.call_to)
                .items(addresses)
                .itemsCallback((dialog, itemView, position, text) -> doCall(addresses.get(position)))
                .show();
    }

    private void doCall(String number) {
        if (TextUtils.isEmpty(number)) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + number));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void addToContact() {
        if (null == mConversations || mConversations.isEmpty()) {
            return;
        }
        if (mConversations.size() == 1) {
            doAddToContact(mConversations.get(0).address);
            return;
        }
        ArrayList<String> addresses = new ArrayList<>();
        for (Conversation conversation : mConversations) {
            addresses.add(conversation.address);
        }
        new MaterialDialog.Builder(this)
                .title(R.string.choose_contact)
                .items(addresses)
                .itemsCallback((dialog, itemView, position, text) -> doAddToContact(addresses.get(position)))
                .show();
    }

    private void doAddToContact(String address) {
        if (TextUtils.isEmpty(address)) {
            return;
        }
        mAddContactAddress = address;
        try {
            Intent oldConstantIntent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
            oldConstantIntent.setType(ContactsContract.Contacts.CONTENT_ITEM_TYPE);
            oldConstantIntent.putExtra(ContactsContract.Intents.Insert.PHONE, mAddContactAddress);
            startActivityForResult(oldConstantIntent, REQ_CODE_ADD_CONTACT);
        } catch (Exception | Error e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_ADD_CONTACT && !TextUtils.isEmpty(mAddContactAddress)) {
            TaskService.syncContactName(this, mAddContactAddress);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(CONVERSATIONS, mConversations);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mPresenter.subscribe();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mPresenter.unSubscribe();
    }

    @Override
    public LoadSmsTask getLoadSmsTask() {
        return mLoadSmsTask;
    }

    @Override
    public ArrayList<Conversation> getConversations() {
        return mConversations;
    }

    @OnClick(R.id.fab_send)
    void send(View view) {
        KeyboardUtil.hideKeyboard(this);
        if (null == mConversations || mConversations.isEmpty()) {
            finish();
            return;
        }
        if (TextUtils.isEmpty(getContent())) {
            return;
        }
        String address;
        if (mConversations.size() > 1) {
            pickAddress(mConversations);
            return;
        } else {
            address = mConversations.get(0).address;
        }
        mPresenter.doSend(address);
    }

    private void pickAddress(ArrayList<Conversation> conversations) {
        ArrayList<String> address = new ArrayList<>();
        for (Conversation conversation : conversations) {
            address.add(conversation.address);
        }
        new MaterialDialog.Builder(this)
                .title(R.string.choose_receiver)
                .items(address)
                .itemsCallback((dialog, itemView, position, text) -> {
                    if (!TextUtils.isEmpty(text)) {
                        mPresenter.doSend(text.toString());
                    }
                })
                .show();
    }

    public String getContent() {
        if (null == mEtSendContent) {
            return "";
        }
        if (TextUtils.isEmpty(mEtSendContent.getText())) {
            return "";
        }
        return mEtSendContent.getText().toString();
    }

    public void clearContent() {
        if (null != mEtSendContent) {
            mEtSendContent.setText("");
        }
    }

    @Override
    public DeleteSmsTask getDeleteSmsTask() {
        return mDeleteSmsTask;
    }

    public DeleteConversationTask getDeleteConversationTask() {
        return mDeleteConversationTask;
    }

    @Override
    public void onDeleteConversationSuccess() {
        finish();
    }

    @Override
    public ArrayList<ConversationItem> getData() {
        if (null == mAdapter) {
            return null;
        }
        return mAdapter.getData();
    }

    @Override
    public void setData(ArrayList<ConversationItem> items) {
        if (null == items || items.isEmpty()) {
            finish();
            return;
        }
        if (null != mConversations && !mConversations.isEmpty()) {
            mAdapter.setData(items, mConversations.size(), mSearchSms);
            if (null != mSearchSms) {
                mList.smoothScrollToPosition(mAdapter.getSearchSmsPosition(mSearchSms));
            }
        }
    }

    @Override
    protected void onDestroy() {
        cacheTempMsgToSp();
        super.onDestroy();
    }

    private void cacheTempMsgToSp() {
        if (null == mEtSendContent || TextUtils.isEmpty(mEtSendContent.getText())) {
            SharedPreferenceUtil.remove(this, TEMP_MSG);
            return;
        }
        String tempMsg = mEtSendContent.getText().toString();
        SharedPreferenceUtil.putString(this, TEMP_MSG, tempMsg);
    }

    private String readTempMsgFromSp() {
        return SharedPreferenceUtil.getString(this, TEMP_MSG, "");
    }
}
