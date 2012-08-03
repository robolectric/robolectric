package com.xtremelabs.robolectric.shadows;

import android.os.Parcel;
import android.os.Parcelable;

class TestParcelable implements Parcelable {
    int contents;

    public TestParcelable(int contents) {
        this.contents = contents;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TestParcelable> CREATOR =
            new Creator<TestParcelable>() {
                @Override
                public TestParcelable createFromParcel(Parcel source) {
                    return new TestParcelable(source.readInt());
                }

                @Override
                public TestParcelable[] newArray(int size) {
                    return new TestParcelable[0];
                }
            };
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(contents);
    }
}
