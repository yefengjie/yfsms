package com.windcity.yefeng.yfsms.presentation.sendsms;

import android.app.Activity;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yefeng on 03/08/2017.
 */

public class SendSmsContract {
    public interface Presenter {
        void subscribe();

        void unSubscribe();

        void sendSmses(ArrayList<ContactChip> contactChips, String s);

        void autoAddContact(Uri data);
    }

    public interface View {

        void showToast(String msg);

        Activity getCtx();

        List<ContactChip> getContactChips();

        void setContactChips(List<ContactChip> list);

        void addChip(ContactChip contactChip);
    }
}
