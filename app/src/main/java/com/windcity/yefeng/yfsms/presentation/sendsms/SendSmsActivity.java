package com.windcity.yefeng.yfsms.presentation.sendsms;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.pchmn.materialchips.ChipsInput;
import com.pchmn.materialchips.model.ChipInterface;
import com.windcity.yefeng.yfsms.R;
import com.windcity.yefeng.yfsms.base.BaseActivity;
import com.windcity.yefeng.yfsms.domain.usecase.contact.QueryContactTask;
import com.yefeng.support.util.KeyboardUtil;
import com.yefeng.support.util.SharedPreferenceUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class SendSmsActivity extends BaseActivity implements SendSmsContract.View {

    private static final String TEMP_MSG = "TEMP_MSG";

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.et_send_content)
    EditText mEtSendContent;
    @BindView(R.id.ci_send_contact)
    ChipsInput mInputContacts;
    @Inject
    SendSmsPresenter mPresenter;
    @Inject
    QueryContactTask mQueryContactTask;
    private List<ContactChip> mContactList;

    public static void startMe(Context context) {
        Intent intent = new Intent(context, SendSmsActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_sms);
        ButterKnife.bind(this);
        DaggerSendSmsComponent.builder()
                .sendSmsModule(new SendSmsModule(this))
                .build()
                .inject(this);
        autoInputTempMsg();
        init();
        mPresenter.subscribe();
        handleSendtoIntent(getIntent());
    }

    private void autoInputTempMsg() {
        String cacheTempMsg = readTempMsgFromSp();
        if (!TextUtils.isEmpty(cacheTempMsg) && null != mEtSendContent) {
            mEtSendContent.setText(cacheTempMsg);
            mEtSendContent.setSelection(mEtSendContent.length());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleSendtoIntent(getIntent());
    }

    private void handleSendtoIntent(Intent intent) {
        if (null == intent || intent.getAction() == null || intent.getData() == null) {
            return;
        }
        if (!Intent.ACTION_SEND.equals(intent.getAction())
                && !Intent.ACTION_SENDTO.equals(intent.getAction())) {
            return;
        }
        mPresenter.autoAddContact(intent.getData());
    }

    @Override
    protected void onDestroy() {
        cacheTempMsgToSp();
        super.onDestroy();
        mPresenter.unSubscribe();
    }

    private void init() {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (null != actionBar) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // chips listener
        mInputContacts.addChipsListener(new ChipsInput.ChipsListener() {
            @Override
            public void onChipAdded(ChipInterface chip, int newSize) {
                Timber.e("chip added, " + newSize);
            }

            @Override
            public void onChipRemoved(ChipInterface chip, int newSize) {
                Timber.e("chip removed, " + newSize);
            }

            @Override
            public void onTextChanged(CharSequence text) {
                Timber.e("text changed: " + text.toString());
                if (TextUtils.isEmpty(text) || text.length() <= 0) {
                    return;
                }
                if (text.toString().lastIndexOf(",") != -1 || text.toString().lastIndexOf("，") != -1) {
                    String content = text.toString().substring(0, text.length() - 1);
                    if (TextUtils.isEmpty(content) || TextUtils.isEmpty(content.trim())) {
                        return;
                    }
                    content = content.trim();
                    ContactChip contactChip = new ContactChip(content, null, content, content);
                    mInputContacts.addChip(contactChip);
                    if (null != mInputContacts.getEditText()) {
                        mInputContacts.getEditText().setText("");
                    }
                }
            }
        });
        mEtSendContent.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    mInputContacts.hideFilterableListView();
                }
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("unchecked")
    @OnClick(R.id.fab_send)
    void send(View view) {
        KeyboardUtil.hideKeyboard(this);
        if (TextUtils.isEmpty(mEtSendContent.getText())) {
            return;
        }
        String strangerNumber = null;
        if (null != mInputContacts && null != mInputContacts.getEditText() && null != mInputContacts.getEditText().getText()) {
            strangerNumber = mInputContacts.getEditText().getText().toString();
        }
        ArrayList<ContactChip> numbers = new ArrayList<>();
        if (!TextUtils.isEmpty(strangerNumber)) {
            strangerNumber = strangerNumber.replaceAll(" +", "");
            if (!TextUtils.isEmpty(strangerNumber)) {
                String reg = "^[0-9]{4,20}$";
                if (!Pattern.matches(reg, strangerNumber)) {
                    showToast(getString(R.string.prompt_input_right_phone_number));
                    return;
                } else {
                    ContactChip c = new ContactChip("", strangerNumber);
                    numbers.add(c);
                }
            }
        }
        for (ContactChip chip : (List<ContactChip>) mInputContacts.getSelectedChipList()) {
            if (!TextUtils.isEmpty(chip.getInfo()) && !TextUtils.isEmpty(chip.getInfo().replaceAll(" +", ""))) {
                ContactChip c = new ContactChip(chip.getLabel() + "", chip.getInfo().replaceAll(" +", ""));
                numbers.add(c);
            }
        }
        if (numbers.isEmpty()) {
            return;
        }
        mPresenter.sendSmses(numbers, mEtSendContent.getText().toString());
        finish();
    }

    @Override
    public List<ContactChip> getContactChips() {
        if (null == mContactList) {
            mContactList = new ArrayList<>();
        }
        return mContactList;
    }

    @Override
    public void setContactChips(List<ContactChip> list) {
        mInputContacts.setFilterableList(list);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (null != mInputContacts && mInputContacts.onBackPress()) {
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void addChip(ContactChip contactChip) {
        if (null == contactChip || null == mInputContacts) {
            return;
        }
        mInputContacts.addChip(contactChip);
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
