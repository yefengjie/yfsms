package com.windcity.yefeng.yfsms.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Generated;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.apihint.Internal;

/**
 * Created by yefeng on 20/07/2017.
 */

@Entity
public class Tabs implements Parcelable {
    public static final Creator<Tabs> CREATOR = new Creator<Tabs>() {
        @Override
        public Tabs createFromParcel(Parcel source) {
            return new Tabs(source);
        }

        @Override
        public Tabs[] newArray(int size) {
            return new Tabs[size];
        }
    };
    @Id(assignable = true)
    public long id;
    public int unreadNum;
    public long newestUnreadSmsTime;

    @Generated(485229652)
    @Internal
    /** This constructor was generated by ObjectBox and may change any time. */
    public Tabs(long id, int unreadNum, long newestUnreadSmsTime) {
        this.id = id;
        this.unreadNum = unreadNum;
        this.newestUnreadSmsTime = newestUnreadSmsTime;
    }


    @Generated(1863161359)
    public Tabs() {
    }

    protected Tabs(Parcel in) {
        this.id = in.readLong();
        this.unreadNum = in.readInt();
        this.newestUnreadSmsTime = in.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeInt(this.unreadNum);
        dest.writeLong(this.newestUnreadSmsTime);
    }
}
