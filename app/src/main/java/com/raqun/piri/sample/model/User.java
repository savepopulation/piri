package com.raqun.piri.sample.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by tyln on 13/06/2017.
 */

public class User implements Parcelable {
    private long userId;
    private String userName;


    protected User(Parcel in) {
        userId = in.readLong();
        userName = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(userId);
        dest.writeString(userName);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
