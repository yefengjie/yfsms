package com.windcity.yefeng.yfsms.data.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by yefeng on 02/08/2017.
 */

public class ConversationItem implements Parcelable {
    public static final Creator<ConversationItem> CREATOR = new Creator<ConversationItem>() {
        @Override
        public ConversationItem createFromParcel(Parcel source) {
            return new ConversationItem(source);
        }

        @Override
        public ConversationItem[] newArray(int size) {
            return new ConversationItem[size];
        }
    };
    public Conversation conversation;
    public Sms sms;

    public ConversationItem() {
    }

    protected ConversationItem(Parcel in) {
        this.conversation = in.readParcelable(Conversation.class.getClassLoader());
        this.sms = in.readParcelable(Sms.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.conversation, flags);
        dest.writeParcelable(this.sms, flags);
    }
}
