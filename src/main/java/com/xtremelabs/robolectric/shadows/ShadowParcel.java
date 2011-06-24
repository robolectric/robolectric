package com.xtremelabs.robolectric.shadows;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(Parcel.class)
public class ShadowParcel {

    private ArrayList parcelData = new ArrayList();
    private int index = 0;

    @Implementation
    public static Parcel obtain() {
        return Robolectric.newInstanceOf(Parcel.class);
    }

    @Implementation
    @SuppressWarnings("unchecked")
    public void writeString(String str) {
        if (str == null) {
            return;
        }

        parcelData.add(str);
    }

    @Implementation
    @SuppressWarnings("unchecked")
    public void writeInt(int i) {
        parcelData.add(i);
    }

    @Implementation
    public String readString() {
        return index < parcelData.size() ? (String) parcelData.get(index++) : null;
    }

    @Implementation
    public int readInt() {
        return index < parcelData.size() ? (Integer) parcelData.get(index++) : 0;
    }

    public int getIndex() {
        return index;
    }

    public List getParcelData() {
        return parcelData;
    }
}
