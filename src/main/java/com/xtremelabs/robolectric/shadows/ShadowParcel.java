package com.xtremelabs.robolectric.shadows;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Bundle;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.util.ArrayList;
import java.util.List;

@Implements(Parcel.class)
@SuppressWarnings("unchecked")
public class ShadowParcel {
    private ArrayList parcelData = new ArrayList();
    private int index = 0;

    @Implementation
    public static Parcel obtain() {
        return Robolectric.newInstanceOf(Parcel.class);
    }

    @Implementation
    public void writeString(String str) {
        if (str == null) {
            return;
        }
        parcelData.add(str);
    }

    @Implementation
    public void writeInt(int i) {
        parcelData.add(i);
    }

    @Implementation
    public void writeLong(long i) {
        parcelData.add(i);
    }

    @Implementation
    public void writeFloat(float f) {
        parcelData.add(f);
    }

    @Implementation
    public void writeDouble(double f) {
        parcelData.add(f);
    }

    @Implementation
    @SuppressWarnings("unchecked")
    public void writeByte( byte b ) {
    	parcelData.add( b );
    }

    @Implementation
    public String readString() {
        return index < parcelData.size() ? (String) parcelData.get(index++) : null;
    }

    @Implementation
    public int readInt() {
        return index < parcelData.size() ? (Integer) parcelData.get(index++) : 0;
    }

    @Implementation
    public float readFloat() {
        return index < parcelData.size() ? (Float) parcelData.get(index++) : 0;
    }

    @Implementation
    public double readDouble() {
        return index < parcelData.size() ? (Double) parcelData.get(index++) : 0;
    }

    @Implementation
    public byte readByte() {
        return index < parcelData.size() ? (Byte) parcelData.get(index++) : 0;
    }

    @Implementation
    public long readLong() {
        return index < parcelData.size() ? (Long) parcelData.get(index++) : 0;
    }

    @Implementation
    public Bundle readBundle() {
        return index < parcelData.size() ? (Bundle) parcelData.get(index++) : null;
    }

    @Implementation
    public Bundle readBundle(ClassLoader loader) {
        return readBundle();
    }

    @Implementation
    public void writeBundle(Bundle bundle) {
        parcelData.add(bundle);
    }

    @Implementation
    public void writeParcelable(Parcelable p, int flags) {
        parcelData.add(p);
    }

    @Implementation
    public Parcelable readParcelable(ClassLoader cl) {
        return index < parcelData.size() ? (Parcelable) parcelData.get(index++) : null;
    }

    @Implementation
    public void readFloatArray(float[] val) {
        int n = readInt();
        if (val.length != n) throw new RuntimeException("bad array lengths");
        for (int i = 0; i< val.length; i++) {
            val[i] = readFloat();
        }
    }

    @Implementation
    public void writeFloatArray(float[] val) {
        writeInt(val.length);
        for (float f : val) writeFloat(f);
    }

    @Implementation
    public void writeDoubleArray(double[] val) {
        writeInt(val.length);
        for (double f : val) writeDouble(f);
    }

    @Implementation
    public void readDoubleArray(double[] val) {
        int n = readInt();
        if (val.length != n) throw new RuntimeException("bad array lengths");
        for (int i = 0; i< val.length; i++) {
            val[i] = readDouble();
        }
    }

    @Implementation
    public void writeIntArray(int[] val) {
        writeInt(val.length);
        for (int f : val) writeInt(f);
    }

    @Implementation
    public void readIntArray(int[] val) {
        int n = readInt();
        if (val.length != n) throw new RuntimeException("bad array lengths");
        for (int i = 0; i< val.length; i++) {
            val[i] = readInt();
        }
    }

    @Implementation
    public void writeLongArray(long[] val) {
        writeInt(val.length);
        for (long f : val) writeLong(f);
    }

    @Implementation
    public void readLongArray(long[] val) {
        int n = readInt();
        if (val.length != n) throw new RuntimeException("bad array lengths");
        for (int i = 0; i< val.length; i++) {
            val[i] = readLong();
        }
    }

    @Implementation
    public void writeStringArray(String[] val) {
        writeInt(val.length);
        for (String f : val) writeString(f);
    }

    @Implementation
    public void readStringArray(String[] val) {
        int n = readInt();
        if (val.length != n) throw new RuntimeException("bad array lengths");
        for (int i = 0; i< val.length; i++) {
            val[i] = readString();
        }
    }





    public int getIndex() {
        return index;
    }

    public List getParcelData() {
        return parcelData;
    }

}
