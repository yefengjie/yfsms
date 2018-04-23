package com.windcity.yefeng.yfsms.presentation.sendsms;

import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.TextUtils;

import com.windcity.yefeng.yfsms.domain.service.TaskService;
import com.windcity.yefeng.yfsms.domain.usecase.sms.SmsCenter;
import com.windcity.yefeng.yfsms.domain.usecase.sms.SmsUtil;
import com.yefeng.support.http.HttpSchedulersTransformer;

import java.util.ArrayList;

import javax.inject.Inject;

import io.reactivex.Flowable;
import timber.log.Timber;

/**
 * Created by yefeng on 03/08/2017.
 */

public class SendSmsPresenter implements SendSmsContract.Presenter {
    private SendSmsContract.View mView;

    @Inject
    public SendSmsPresenter(SendSmsContract.View view) {
        this.mView = view;
    }

    @Override
    public void subscribe() {
        if (mView.getContactChips().size() == 0) {
            getContactList();
        }
    }

    @Override
    public void unSubscribe() {

    }

    public void getContactList() {
        Cursor phones = mView.getCtx().getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        // loop over all contacts
        if (phones != null) {
            while (phones.moveToNext()) {
                // get contact info
                String phoneNumber = null;
                String id = phones.getString(phones.getColumnIndex(ContactsContract.Contacts._ID));
                String name = phones.getString(phones.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String avatarUriString = phones.getString(phones.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));
                Uri avatarUri = null;
                if (avatarUriString != null)
                    avatarUri = Uri.parse(avatarUriString);

                // get phone number
                if (Integer.parseInt(phones.getString(phones.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor pCur = mView.getCtx().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);

                    while (pCur != null && pCur.moveToNext()) {
                        phoneNumber = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    }

                    pCur.close();

                }

                ContactChip contactChip = new ContactChip(id, avatarUri, name, phoneNumber);
                // add contact to the list
                mView.getContactChips().add(contactChip);
            }
            phones.close();
        }
        mView.setContactChips(mView.getContactChips());
    }

    @Override
    public void sendSmses(ArrayList<ContactChip> contactChips, String s) {
        int size = contactChips.size();
        ArrayList<String> numbers = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            numbers.add(contactChips.get(i).getInfo());
        }
        //open conversation
        if (size == 1) {//single
            //发送完打开最新的发送短信就可以了
            TaskService.sendSms(mView.getCtx(), numbers.get(0), s, true);
            return;
        }
        //todo 发短信，增加群发分类tab
        TaskService.sendSms(mView.getCtx(), numbers, s);
    }

    @Override
    public void autoAddContact(Uri data) {
        Flowable.fromCallable(() -> {
            String address = data.getSchemeSpecificPart();
            if (TextUtils.isEmpty(address) || TextUtils.isEmpty(address.trim())) {
                return null;
            }
            String name = SmsUtil.getPeopleNameFromPerson(address, mView.getCtx().getContentResolver());
            address = address.trim();
            address = SmsCenter.handleCn(address);
            return new ContactChip(name, address);
        }).compose(new HttpSchedulersTransformer<>())
                .subscribe(
                        contactChip -> mView.addChip(contactChip),
                        throwable -> Timber.e(throwable));
    }
}
