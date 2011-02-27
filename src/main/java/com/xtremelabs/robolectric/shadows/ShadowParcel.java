/*
 * Copyright (C) 2006 The Android Open Source Project
 * Copyright (C) 2011 Eric Bowman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xtremelabs.robolectric.shadows;

import android.R;
import android.os.BadParcelableException;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.bytecode.ShadowWrangler;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Document me.
 *
 * @author Eric Bowman
 * @since 2011-02-25 14:20
 */
@SuppressWarnings({"JavaDoc"})
@Implements(Parcel.class)
public class ShadowParcel {
    private static final boolean DEBUG_RECYCLE = false;

    @RealObject private Parcel This;

    private byte[] buffer;
    private int cursor;
    private int highwater;

    private static final int VAL_NULL = -1;
    private static final int VAL_STRING = 0;
    private static final int VAL_INTEGER = 1;
    private static final int VAL_MAP = 2;
    private static final int VAL_BUNDLE = 3;
    private static final int VAL_PARCELABLE = 4;
    private static final int VAL_SHORT = 5;
    private static final int VAL_LONG = 6;
    private static final int VAL_FLOAT = 7;
    private static final int VAL_DOUBLE = 8;
    private static final int VAL_BOOLEAN = 9;
    private static final int VAL_CHARSEQUENCE = 10;
    private static final int VAL_LIST  = 11;
    private static final int VAL_SPARSEARRAY = 12;
    private static final int VAL_BYTEARRAY = 13;
    private static final int VAL_STRINGARRAY = 14;
    private static final int VAL_IBINDER = 15;
    private static final int VAL_PARCELABLEARRAY = 16;
    private static final int VAL_OBJECTARRAY = 17;
    private static final int VAL_INTARRAY = 18;
    private static final int VAL_LONGARRAY = 19;
    private static final int VAL_BYTE = 20;
    private static final int VAL_SERIALIZABLE = 21;
    private static final int VAL_SPARSEBOOLEANARRAY = 22;
    private static final int VAL_BOOLEANARRAY = 23;
    private static final int VAL_CHARSEQUENCEARRAY = 24;

    private static final int EX_SECURITY = -1;
    private static final int EX_BAD_PARCELABLE = -2;
    private static final int EX_ILLEGAL_ARGUMENT = -3;
    private static final int EX_NULL_POINTER = -4;
    private static final int EX_ILLEGAL_STATE = -5;

    public final static Parcelable.Creator<String> STRING_CREATOR
             = new Parcelable.Creator<String>() {
        public String createFromParcel(Parcel source) {
            return source.readString();
        }
        public String[] newArray(int size) {
            return new String[size];
        }
    };

    /**
     * Retrieve a new Parcel object from the pool.
     */
    @Implementation
    public static Parcel obtain() {
        try {
            Constructor<?>[] constructor = Parcel.class.getDeclaredConstructors();
            constructor[0].setAccessible(true);
            Parcel parcel = (Parcel) constructor[0].newInstance();
            ShadowParcel shadow = Robolectric.shadowOf(parcel);
            return shadow.This;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Put a Parcel object back into the pool.  You must not touch
     * the object after this call.
     */
    @Implementation
    public void recycle() {
        init();
    }

    /**
     * Returns the raw bytes of the parcel.
     *
     * <p class="note">The data you retrieve here <strong>must not</strong>
     * be placed in any kind of persistent storage (on local disk, across
     * a network, etc).  For that, you should use standard serialization
     * or another kind of general serialization mechanism.  The Parcel
     * marshalled representation is highly optimized for local IPC, and as
     * such does not attempt to maintain compatibility with data created
     * in different versions of the platform.
     */
    @Implementation
    public byte[] marshall() {
        byte[] bytes = new byte[highwater];
        System.arraycopy(buffer, 0, bytes, 0, highwater);
        return bytes;
    }

    /**
     * Set the bytes in data to be the raw bytes of this Parcel.
     */
    @Implementation
    public void unmarshall(byte[] data, int offset, int length) {
        if (cursor + length > buffer.length) {
            byte[] newBytes = new byte[cursor + length];
            System.arraycopy(buffer, 0, newBytes, 0, highwater);
            buffer = newBytes;
        }
        System.arraycopy(data, offset, buffer, cursor, length);
        cursor += length;
        highwater = Math.max(highwater, cursor);
    }

    @Implementation
    public void setDataPosition(int pos) {

        if (pos < 0) {
            throw new IllegalArgumentException("pos " + pos + " cannot be negative");
        }
        if (pos > buffer.length) {
            byte[] newBytes = new byte[pos];
            System.arraycopy(buffer, 0, newBytes, 0, highwater);
            buffer = newBytes;
        }
        cursor = pos;
        highwater = Math.max(highwater, cursor);
    }

    @Implementation
    public int dataPosition() {
        return cursor;
    }

    @Implementation
    public int dataSize() {
        return highwater;
    }

    @Implementation
    public int dataAvail() {
        return highwater - cursor;
    }

    @Implementation
    public void appendFrom(Parcel parcel, int offset, int length) {
        ShadowParcel shadow = (ShadowParcel) ShadowWrangler.getInstance().shadowOf(parcel);

        if (cursor + length > buffer.length) {
            byte[] newBytes = new byte[cursor + length];
            System.arraycopy(buffer, 0, newBytes, 0, buffer.length);
            buffer = newBytes;
        }
        System.arraycopy(shadow.buffer, offset, buffer, cursor, length);
        cursor += length;
        highwater = Math.max(cursor, highwater);
    }

    /**
     * Write a byte array into the parcel at the current {#link #dataPosition},
     * growing if needed.
     * @param b Bytes to place into the parcel.
     */
    @Implementation
    public void writeByteArray(byte[] b) {
        writeByteArray(b, 0, (b != null) ? b.length : 0);
    }

    /**
     * Write an byte array into the parcel at the current {#link #dataPosition},
     * growing if needed.
     * @param b Bytes to place into the parcel.
     * @param offset Index of first byte to be written.
     * @param len Number of bytes to write.
     */
    @Implementation
    public void writeByteArray(byte[] b, int offset, int len) {
        if (b == null) {
            writeInt(-1);
            return;
        }
        if (b.length < offset + len || len < 0 || offset < 0) {
            throw new ArrayIndexOutOfBoundsException();
        }
        writeNative(b, offset, len);
    }

    private void writeNative(byte[] b, int offset, int len) {
        writeInt(len);
        if (cursor + b.length > buffer.length) {
            byte[] newBytes = new byte[cursor + b.length];
            System.arraycopy(buffer, 0, newBytes, 0, cursor);
            buffer = newBytes;
        }
        System.arraycopy(b, offset, buffer, cursor, len);
        cursor += len;
        highwater = Math.max(highwater, cursor);
    }

    /**
     * Write an integer value into the parcel at the current dataPosition(),
     * growing dataCapacity() if needed.
     */
    @Implementation
    public void writeInt(int val) {
        int a = (val & (-1 << 24)) >>> 24;
        int b = (val & ((-1 << 24)>>>8)) >>> 16;
        int c = (val & ((-1 << 24)>>>16)) >>> 8;
        int d = (val & ((-1 << 24)>>>24));
        byte[] bytes = new byte[] { (byte) a, (byte) b, (byte) c, (byte) d };
        if (cursor + bytes.length > buffer.length) {
            byte[] newBytes = new byte[cursor + bytes.length];
            System.arraycopy(buffer, 0, newBytes, 0, cursor);
            buffer = newBytes;
        }
        System.arraycopy(bytes, 0, buffer, cursor, bytes.length);
        cursor += bytes.length;
        highwater = Math.max(highwater, cursor);
    }

    /**
     * Write a long integer value into the parcel at the current dataPosition(),
     * growing dataCapacity() if needed.
     */
    @Implementation
    public void writeLong(long val) {
        int high = (int) ((val & (-1L << 32)) >>> 32);
        int low = (int) (val & (-1L >>> 32));
        writeInt(high);
        writeInt(low);
    }

    /**
     * Write a floating point value into the parcel at the current
     * dataPosition(), growing dataCapacity() if needed.
     */
    @Implementation
    public void writeFloat(float val) {
        writeInt(Float.floatToIntBits(val));
    }

    /**
     * Write a double precision floating point value into the parcel at the
     * current dataPosition(), growing dataCapacity() if needed.
     */
    @Implementation
    public void writeDouble(double val) {
        writeLong(Double.doubleToLongBits(val));
    }

    /**
     * Write a string value into the parcel at the current dataPosition(),
     * growing dataCapacity() if needed.
     */
    @Implementation
    public void writeString(String val) {
        try {
            if (val == null) {
                writeInt(-1);
            } else {
                byte[] bytes = val.getBytes("ISO-8859-1");
                writeByteArray(bytes);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Write a CharSequence value into the parcel at the current dataPosition(),
     * growing dataCapacity() if needed.
     * @hide
     */
    public void writeCharSequence(CharSequence val) {
        writeString(val.toString());
    }

    /**
     * Write an byte value into the parcel at the current dataPosition(),
     * growing dataCapacity() if needed.
     */
    @Implementation
    public void writeByte(byte val) {
        writeInt(val);
    }

    /**
     * Please use {@link #writeBundle} instead.  Flattens a Map into the parcel
     * at the current dataPosition(),
     * growing dataCapacity() if needed.  The Map keys must be String objects.
     * The Map values are written using {@link #writeValue} and must follow
     * the specification there.
     *
     * <p>It is strongly recommended to use {@link #writeBundle} instead of
     * this method, since the Bundle class provides a type-safe API that
     * allows you to avoid mysterious type errors at the point of marshalling.
     */
    @Implementation
    public void writeMap(Map val) {
        //noinspection unchecked
        writeMapInternal((Map<String,Object>) val);
    }

    /**
     * Flatten a Map into the parcel at the current dataPosition(),
     * growing dataCapacity() if needed.  The Map keys must be String objects.
     */
    void writeMapInternal(Map<String,Object> val) {
        if (val == null) {
            writeInt(-1);
            return;
        }
        Set<Map.Entry<String,Object>> entries = val.entrySet();
        writeInt(entries.size());
        for (Map.Entry<String,Object> e : entries) {
            writeValue(e.getKey());
            writeValue(e.getValue());
        }
    }

    /**
     * Flatten a Bundle into the parcel at the current dataPosition(),
     * growing dataCapacity() if needed.
     */
    @Implementation
    public void writeBundle(Bundle val) {
        if (val == null) {
            writeInt(-1);
            return;
        }

        val.writeToParcel(This, 0);
    }

    /**
     * Flatten a List into the parcel at the current dataPosition(), growing
     * dataCapacity() if needed.  The List values are written using
     * {@link #writeValue} and must follow the specification there.
     */
    @Implementation
    public void writeList(List val) {
        if (val == null) {
            writeInt(-1);
            return;
        }
        int N = val.size();
        int i=0;
        writeInt(N);
        while (i < N) {
            writeValue(val.get(i));
            i++;
        }
    }

    /**
     * Flatten an Object array into the parcel at the current dataPosition(),
     * growing dataCapacity() if needed.  The array values are written using
     * {@link #writeValue} and must follow the specification there.
     */
    @Implementation
    public void writeArray(Object[] val) {
        if (val == null) {
            writeInt(-1);
            return;
        }
        int N = val.length;
        int i=0;
        writeInt(N);
        while (i < N) {
            writeValue(val[i]);
            i++;
        }
    }

    /**
     * Flatten a generic SparseArray into the parcel at the current
     * dataPosition(), growing dataCapacity() if needed.  The SparseArray
     * values are written using {@link #writeValue} and must follow the
     * specification there.
     */
    @Implementation
    public void writeSparseArray(SparseArray<Object> val) {
        if (val == null) {
            writeInt(-1);
            return;
        }
        int N = val.size();
        writeInt(N);
        int i=0;
        while (i < N) {
            writeInt(val.keyAt(i));
            writeValue(val.valueAt(i));
            i++;
        }
    }

    @Implementation
    public void writeSparseBooleanArray(SparseBooleanArray val) {
        if (val == null) {
            writeInt(-1);
            return;
        }
        int N = val.size();
        writeInt(N);
        int i=0;
        while (i < N) {
            writeInt(val.keyAt(i));
            writeByte((byte)(val.valueAt(i) ? 1 : 0));
            i++;
        }
    }

    @Implementation
    public void writeBooleanArray(boolean[] val) {
        if (val != null) {
            int N = val.length;
            writeInt(N);
            for (int i=0; i<N; i++) {
                writeInt(val[i] ? 1 : 0);
            }
        } else {
            writeInt(-1);
        }
    }

    @Implementation
    public boolean[] createBooleanArray() {
        int N = readInt();
        // >>2 as a fast divide-by-4 works in the create*Array() functions
        // because dataAvail() will never return a negative number.  4 is
        // the size of a stored boolean in the stream.
        if (N >= 0) {
            boolean[] val = new boolean[N];
            for (int i=0; i<N; i++) {
                val[i] = readInt() != 0;
            }
            return val;
        } else {
            return null;
        }
    }

    @Implementation
    public void readBooleanArray(boolean[] val) {
        int N = readInt();
        if (N == val.length) {
            for (int i=0; i<N; i++) {
                val[i] = readInt() != 0;
            }
        } else {
            throw new RuntimeException("bad array lengths");
        }
    }

    @Implementation
    public void writeCharArray(char[] val) {
        if (val != null) {
            int N = val.length;
            writeInt(N);
            for (int i=0; i<N; i++) {
                writeInt((int)val[i]);
            }
        } else {
            writeInt(-1);
        }
    }

    @Implementation
    public char[] createCharArray() {
        int N = readInt();
        if (N >= 0) {
            char[] val = new char[N];
            for (int i=0; i<N; i++) {
                val[i] = (char)readInt();
            }
            return val;
        } else {
            return null;
        }
    }

    @Implementation
    public void readCharArray(char[] val) {
        int N = readInt();
        if (N == val.length) {
            for (int i=0; i<N; i++) {
                val[i] = (char)readInt();
            }
        } else {
            throw new RuntimeException("bad array lengths");
        }
    }

    @Implementation
    public void writeIntArray(int[] val) {
        if (val != null) {
            int N = val.length;
            writeInt(N);
            for (int i=0; i<N; i++) {
                writeInt(val[i]);
            }
        } else {
            writeInt(-1);
        }
    }

    @Implementation
    public int[] createIntArray() {
        int N = readInt();
        if (N >= 0) {
            int[] val = new int[N];
            for (int i=0; i<N; i++) {
                val[i] = readInt();
            }
            return val;
        } else {
            return null;
        }
    }

    @Implementation
    public void readIntArray(int[] val) {
        int N = readInt();
        if (N == val.length) {
            for (int i=0; i<N; i++) {
                val[i] = readInt();
            }
        } else {
            throw new RuntimeException("bad array lengths");
        }
    }

    @Implementation
    public void writeLongArray(long[] val) {
        if (val != null) {
            int N = val.length;
            writeInt(N);
            for (int i=0; i<N; i++) {
                writeLong(val[i]);
            }
        } else {
            writeInt(-1);
        }
    }

    @Implementation
    public long[] createLongArray() {
        int N = readInt();
        if (N >= 0) {
            long[] val = new long[N];
            for (int i=0; i<N; i++) {
                val[i] = readLong();
            }
            return val;
        } else {
            return null;
        }
    }

    @Implementation
    public void readLongArray(long[] val) {
        int N = readInt();
        if (N == val.length) {
            for (int i=0; i<N; i++) {
                val[i] = readLong();
            }
        } else {
            throw new RuntimeException("bad array lengths");
        }
    }

    @Implementation
    public void writeFloatArray(float[] val) {
        if (val != null) {
            int N = val.length;
            writeInt(N);
            for (int i=0; i<N; i++) {
                writeFloat(val[i]);
            }
        } else {
            writeInt(-1);
        }
    }

    @Implementation
    public float[] createFloatArray() {
        int N = readInt();
        if (N >= 0 ) {
            float[] val = new float[N];
            for (int i=0; i<N; i++) {
                val[i] = readFloat();
            }
            return val;
        } else {
            return null;
        }
    }

    @Implementation
    public void readFloatArray(float[] val) {
        int N = readInt();
        if (N == val.length) {
            for (int i=0; i<N; i++) {
                val[i] = readFloat();
            }
        } else {
            throw new RuntimeException("bad array lengths");
        }
    }

    @Implementation
    public void writeDoubleArray(double[] val) {
        if (val != null) {
            int N = val.length;
            writeInt(N);
            for (int i=0; i<N; i++) {
                writeDouble(val[i]);
            }
        } else {
            writeInt(-1);
        }
    }

    @Implementation
    public double[] createDoubleArray() {
        int N = readInt();
        if (N >= 0) {
            double[] val = new double[N];
            for (int i=0; i<N; i++) {
                val[i] = readDouble();
            }
            return val;
        } else {
            return null;
        }
    }

    @Implementation
    public void readDoubleArray(double[] val) {
        int N = readInt();
        if (N == val.length) {
            for (int i=0; i<N; i++) {
                val[i] = readDouble();
            }
        } else {
            throw new RuntimeException("bad array lengths");
        }
    }

    @Implementation
    public void writeStringArray(String[] val) {
        if (val != null) {
            int N = val.length;
            writeInt(N);
            for (int i=0; i<N; i++) {
                writeString(val[i]);
            }
        } else {
            writeInt(-1);
        }
    }

    @Implementation
    public String[] createStringArray() {
        int N = readInt();
        if (N >= 0) {
            String[] val = new String[N];
            for (int i=0; i<N; i++) {
                val[i] = readString();
            }
            return val;
        } else {
            return null;
        }
    }

    @Implementation
    public void readStringArray(String[] val) {
        int N = readInt();
        if (N == val.length) {
            for (int i=0; i<N; i++) {
                val[i] = readString();
            }
        } else {
            throw new RuntimeException("bad array lengths");
        }
    }

    /**
     * @hide
     */
    public void writeCharSequenceArray(CharSequence[] val) {
        if (val != null) {
            int N = val.length;
            writeInt(N);
            for (int i=0; i<N; i++) {
                writeCharSequence(val[i]);
            }
        } else {
            writeInt(-1);
        }
    }

    /**
     * Flatten a List containing a particular object type into the parcel, at
     * the current dataPosition() and growing dataCapacity() if needed.  The
     * type of the objects in the list must be one that implements Parcelable.
     * Unlike the generic writeList() method, however, only the raw data of the
     * objects is written and not their type, so you must use the corresponding
     * readTypedList() to unmarshall them.
     *
     * @param val The list of objects to be written.
     *
     * @see #createTypedArrayList
     * @see #readTypedList
     * @see Parcelable
     */
    @Implementation
    public <T extends Parcelable> void writeTypedList(List<T> val) {
        if (val == null) {
            writeInt(-1);
            return;
        }
        int N = val.size();
        int i=0;
        writeInt(N);
        while (i < N) {
            T item = val.get(i);
            if (item != null) {
                writeInt(1);
                item.writeToParcel(This, 0);
            } else {
                writeInt(0);
            }
            i++;
        }
    }

    /**
     * Flatten a List containing String objects into the parcel, at
     * the current dataPosition() and growing dataCapacity() if needed.  They
     * can later be retrieved with {@link #createStringArrayList} or
     * {@link #readStringList}.
     *
     * @param val The list of strings to be written.
     *
     * @see #createStringArrayList
     * @see #readStringList
     */
    @Implementation
    public void writeStringList(List<String> val) {
        if (val == null) {
            writeInt(-1);
            return;
        }
        int N = val.size();
        int i=0;
        writeInt(N);
        while (i < N) {
            writeString(val.get(i));
            i++;
        }
    }

    /**
     * Flatten a heterogeneous array containing a particular object type into
     * the parcel, at
     * the current dataPosition() and growing dataCapacity() if needed.  The
     * type of the objects in the array must be one that implements Parcelable.
     * Unlike the {@link #writeParcelableArray} method, however, only the
     * raw data of the objects is written and not their type, so you must use
     * {@link #readTypedArray} with the correct corresponding
     * {@link Parcelable.Creator} implementation to unmarshall them.
     *
     * @param val The array of objects to be written.
     * @param parcelableFlags Contextual flags as per
     * {@link Parcelable#writeToParcel(Parcel, int) Parcelable.writeToParcel()}.
     *
     * @see #readTypedArray
     * @see #writeParcelableArray
     * @see Parcelable.Creator
     */
    @Implementation
    public <T extends Parcelable> void writeTypedArray(T[] val,
            int parcelableFlags) {
        if (val != null) {
            int N = val.length;
            writeInt(N);
            for (int i=0; i<N; i++) {
                T item = val[i];
                if (item != null) {
                    writeInt(1);
                    item.writeToParcel(This, parcelableFlags);
                } else {
                    writeInt(0);
                }
            }
        } else {
            writeInt(-1);
        }
    }

    /**
     * Flatten a generic object in to a parcel.  The given Object value may
     * currently be one of the following types:
     *
     * <ul>
     * <li> null
     * <li> String
     * <li> Byte
     * <li> Short
     * <li> Integer
     * <li> Long
     * <li> Float
     * <li> Double
     * <li> Boolean
     * <li> String[]
     * <li> boolean[]
     * <li> byte[]
     * <li> int[]
     * <li> long[]
     * <li> Object[] (supporting objects of the same type defined here).
     * <li> {@link Bundle}
     * <li> Map (as supported by {@link #writeMap}).
     * <li> Any object that implements the {@link Parcelable} protocol.
     * <li> Parcelable[]
     * <li> CharSequence (as supported by {@link TextUtils#writeToParcel}).
     * <li> List (as supported by {@link #writeList}).
     * <li> {@link SparseArray} (as supported by {@link #writeSparseArray}).
     * <li> {@link IBinder}
     * <li> Any object that implements Serializable (but see
     *      {@link #writeSerializable} for caveats).  Note that all of the
     *      previous types have relatively efficient implementations for
     *      writing to a Parcel; having to rely on the generic serialization
     *      approach is much less efficient and should be avoided whenever
     *      possible.
     * </ul>
     */
    @Implementation
    public void writeValue(Object v) {
        if (v == null) {
            writeInt(VAL_NULL);
        } else if (v instanceof String) {
            writeInt(VAL_STRING);
            writeString((String) v);
        } else if (v instanceof Integer) {
            writeInt(VAL_INTEGER);
            writeInt((Integer) v);
        } else if (v instanceof Map) {
            writeInt(VAL_MAP);
            writeMap((Map) v);
        } else if (v instanceof Bundle) {
            // Must be before Parcelable
            writeInt(VAL_BUNDLE);
            writeBundle((Bundle) v);
        } else if (v instanceof Parcelable) {
            writeInt(VAL_PARCELABLE);
            writeParcelable((Parcelable) v, 0);
        } else if (v instanceof Short) {
            writeInt(VAL_SHORT);
            writeInt(((Short) v).intValue());
        } else if (v instanceof Long) {
            writeInt(VAL_LONG);
            writeLong((Long) v);
        } else if (v instanceof Float) {
            writeInt(VAL_FLOAT);
            writeFloat((Float) v);
        } else if (v instanceof Double) {
            writeInt(VAL_DOUBLE);
            writeDouble((Double) v);
        } else if (v instanceof Boolean) {
            writeInt(VAL_BOOLEAN);
            writeInt((Boolean) v ? 1 : 0);
        } else if (v instanceof CharSequence) {
            // Must be after String
            writeInt(VAL_CHARSEQUENCE);
            writeCharSequence((CharSequence) v);
        } else if (v instanceof List) {
            writeInt(VAL_LIST);
            writeList((List) v);
        } else if (v instanceof SparseArray) {
            writeInt(VAL_SPARSEARRAY);
            //noinspection unchecked
            writeSparseArray((SparseArray) v);
        } else if (v instanceof boolean[]) {
            writeInt(VAL_BOOLEANARRAY);
            writeBooleanArray((boolean[]) v);
        } else if (v instanceof byte[]) {
            writeInt(VAL_BYTEARRAY);
            writeByteArray((byte[]) v);
        } else if (v instanceof String[]) {
            writeInt(VAL_STRINGARRAY);
            writeStringArray((String[]) v);
        } else if (v instanceof CharSequence[]) {
            // Must be after String[] and before Object[]
            writeInt(VAL_CHARSEQUENCEARRAY);
            writeCharSequenceArray((CharSequence[]) v);
        } else if (v instanceof IBinder) {
            writeInt(VAL_IBINDER);
            throw new RuntimeException("IBinder not supported by mock");
        } else if (v instanceof Parcelable[]) {
            writeInt(VAL_PARCELABLEARRAY);
            writeParcelableArray((Parcelable[]) v, 0);
        } else if (v instanceof Object[]) {
            writeInt(VAL_OBJECTARRAY);
            writeArray((Object[]) v);
        } else if (v instanceof int[]) {
            writeInt(VAL_INTARRAY);
            writeIntArray((int[]) v);
        } else if (v instanceof long[]) {
            writeInt(VAL_LONGARRAY);
            writeLongArray((long[]) v);
        } else if (v instanceof Byte) {
            writeInt(VAL_BYTE);
            writeInt((Byte) v);
        } else if (v instanceof Serializable) {
            // Must be last
            writeInt(VAL_SERIALIZABLE);
            writeSerializable((Serializable) v);
        } else {
            throw new RuntimeException("Parcel: unable to marshal value " + v);
        }
    }

    /**
     * Flatten the name of the class of the Parcelable and its contents
     * into the parcel.
     *
     * @param p The Parcelable object to be written.
     * @param parcelableFlags Contextual flags as per
     * {@link Parcelable#writeToParcel(Parcel, int) Parcelable.writeToParcel()}.
     */
    @Implementation
    public void writeParcelable(Parcelable p, int parcelableFlags) {
        if (p == null) {
            writeString(null);
            return;
        }
        String name = p.getClass().getName();
        writeString(name);
        p.writeToParcel(This, parcelableFlags);
    }

    /**
     * Write a generic serializable object in to a Parcel.  It is strongly
     * recommended that this method be avoided, since the serialization
     * overhead is extremely large, and this approach will be much slower than
     * using the other approaches to writing data in to a Parcel.
     */
    @Implementation
    public void writeSerializable(Serializable s) {
        if (s == null) {
            writeString(null);
            return;
        }
        String name = s.getClass().getName();
        writeString(name);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(s);
            oos.close();

            writeByteArray(baos.toByteArray());
        } catch (IOException ioe) {
            throw new RuntimeException("Parcelable encountered " +
                "IOException writing serializable object (name = " + name +
                ")", ioe);
        }
    }

    /**
     * Special function for writing an exception result at the header of
     * a parcel, to be used when returning an exception from a transaction.
     * Note that this currently only supports a few exception types; any other
     * exception will be re-thrown by this function as a RuntimeException
     * (to be caught by the system's last-resort exception handling when
     * dispatching a transaction).
     *
     * <p>The supported exception types are:
     * <ul>
     * <li>{@link BadParcelableException}
     * <li>{@link IllegalArgumentException}
     * <li>{@link IllegalStateException}
     * <li>{@link NullPointerException}
     * <li>{@link SecurityException}
     * </ul>
     *
     * @param e The Exception to be written.
     *
     * @see #writeNoException
     * @see #readException
     */
    @Implementation
    public void writeException(Exception e) {
        int code = 0;
        if (e instanceof SecurityException) {
            code = EX_SECURITY;
        } else if (e instanceof BadParcelableException) {
            code = EX_BAD_PARCELABLE;
        } else if (e instanceof IllegalArgumentException) {
            code = EX_ILLEGAL_ARGUMENT;
        } else if (e instanceof NullPointerException) {
            code = EX_NULL_POINTER;
        } else if (e instanceof IllegalStateException) {
            code = EX_ILLEGAL_STATE;
        }
        writeInt(code);
        if (code == 0) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
        writeString(e.getMessage());
    }

    /**
     * Special function for writing information at the front of the Parcel
     * indicating that no exception occurred.
     *
     * @see #writeException
     * @see #readException
     */
    @Implementation
    public void writeNoException() {
        writeInt(0);
    }

    /**
     * Special function for reading an exception result from the header of
     * a parcel, to be used after receiving the result of a transaction.  This
     * will throw the exception for you if it had been written to the Parcel,
     * otherwise return and let you read the normal result data from the Parcel.
     *
     * @see #writeException
     * @see #writeNoException
     */
    @Implementation
    public void readException() {
        int code = readInt();
        if (code == 0) return;
        String msg = readString();
        readException(code, msg);
    }

    /**
     * Use this function for customized exception handling.
     * customized method call this method for all unknown case
     * @param code exception code
     * @param msg exception message
     */
    @Implementation
    public void readException(int code, String msg) {
        switch (code) {
            case EX_SECURITY:
                throw new SecurityException(msg);
            case EX_BAD_PARCELABLE:
                throw new BadParcelableException(msg);
            case EX_ILLEGAL_ARGUMENT:
                throw new IllegalArgumentException(msg);
            case EX_NULL_POINTER:
                throw new NullPointerException(msg);
            case EX_ILLEGAL_STATE:
                throw new IllegalStateException(msg);
        }
        throw new RuntimeException("Unknown exception code: " + code
                + " msg " + msg);
    }

    /**
     * Read an integer value from the parcel at the current dataPosition().
     */
    @Implementation
    public int readInt() {
        if (cursor + 4 > highwater) {
            throw new IllegalStateException("Not enough water");
        }
        int a = byteToInt(buffer[cursor]) << 24;
        int b = byteToInt(buffer[cursor + 1]) << 16;
        int c = byteToInt(buffer[cursor + 2]) << 8;
        int d = byteToInt(buffer[cursor + 3]);
        cursor += 4;
        return a + b + c + d;
    }

    private int byteToInt(byte b) {
        if (b >= 0) {
            return b;
        } else {
            return 256 + b;
        }
    }

    /**
     * Read a long integer value from the parcel at the current dataPosition().
     */
    @Implementation
    public long readLong() {
        long high = readInt();
        long low = readInt();

        return (high << 32) + (low & 0x00000000FFFFFFFFL);
    }

    /**
     * Read a floating point value from the parcel at the current
     * dataPosition().
     */
    @Implementation
    public float readFloat() {
        return Float.intBitsToFloat(readInt());
    }

    /**
     * Read a double precision floating point value from the parcel at the
     * current dataPosition().
     */
    @Implementation
    public double readDouble() {
        return Double.longBitsToDouble(readLong());
    }

    /**
     * Read a string value from the parcel at the current dataPosition().
     */
    @Implementation
    public String readString() {
        try {
            byte[] array = createByteArray();
            if (array == null) {
                return null;
            }
            return new String(array, "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Read a CharSequence value from the parcel at the current dataPosition().
     * @hide
     */
    public CharSequence readCharSequence() {
        return readString();
    }

    /**
     * Read a byte value from the parcel at the current dataPosition().
     */
    @Implementation
    public byte readByte() {
        return (byte)(readInt() & 0xff);
    }

    /**
     * Please use {@link #readBundle(ClassLoader)} instead (whose data must have
     * been written with {@link #writeBundle}.  Read into an existing Map object
     * from the parcel at the current dataPosition().
     */
    @Implementation
    public void readMap(Map outVal, ClassLoader loader) {
        int N = readInt();
        readMapInternal(outVal, N, loader);
    }

    /**
     * Read into an existing List object from the parcel at the current
     * dataPosition(), using the given class loader to load any enclosed
     * Parcelables.  If it is null, the default class loader is used.
     */
    @Implementation
    public void readList(List outVal, ClassLoader loader) {
        int N = readInt();
        readListInternal(outVal, N, loader);
    }

    /**
     * Please use {@link #readBundle(ClassLoader)} instead (whose data must have
     * been written with {@link #writeBundle}.  Read and return a new HashMap
     * object from the parcel at the current dataPosition(), using the given
     * class loader to load any enclosed Parcelables.  Returns null if
     * the previously written map object was null.
     */
    @Implementation
    public HashMap readHashMap(ClassLoader loader)
    {
        int N = readInt();
        if (N < 0) {
            return null;
        }
        HashMap m = new HashMap(N);
        readMapInternal(m, N, loader);
        return m;
    }

    /**
     * Read and return a new Bundle object from the parcel at the current
     * dataPosition().  Returns null if the previously written Bundle object was
     * null.
     */
    @Implementation
    public Bundle readBundle() {
        return readBundle(null);
    }

    /**
     * Read and return a new Bundle object from the parcel at the current
     * dataPosition(), using the given class loader to initialize the class
     * loader of the Bundle for later retrieval of Parcelable objects.
     * Returns null if the previously written Bundle object was null.
     */
    @Implementation
    public Bundle readBundle(ClassLoader loader) {
        int length = readInt();
        if (length < 0) {
            return null;
        }

        try {

            Bundle bundle = (Bundle) Bundle.class.getDeclaredConstructors()[0].newInstance(Bundle.class.getClassLoader());
            ShadowWrangler.getInstance().methodInvoked(
                    Bundle.class, "<init>", bundle,
                    new String[] { },
                    new Object[] { } );
            if (loader != null) {
                bundle.setClassLoader(loader);
            }
            Robolectric.shadowOf(bundle).readFromParcelInner(This, length);
            return bundle;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Read and return a byte[] object from the parcel.
     */
    @Implementation
    public byte[] createByteArray() {
        int bytes = readInt();
        if (bytes == -1) {
            return null;
        }
        byte[] array = new byte[bytes];
        if (cursor + array.length > highwater) {
            throw new IllegalStateException("Not enough water");
        }
        System.arraycopy(buffer, cursor, array, 0, array.length);
        cursor += array.length;
        return array;
    }

    /**
     * Read a byte[] object from the parcel and copy it into the
     * given byte array.
     */
    @Implementation
    public void readByteArray(byte[] val) {
        byte[] ba = createByteArray();
        if (ba.length == val.length) {
           System.arraycopy(ba, 0, val, 0, ba.length);
        } else {
            // put it back in!
            writeByteArray(ba);
            throw new RuntimeException("bad array lengths");
        }
    }

    /**
     * Read and return a String[] object from the parcel.
     * {@hide}
     */
    public String[] readStringArray() {
        String[] array = null;

        int length = readInt();
        if (length >= 0)
        {
            array = new String[length];

            for (int i = 0 ; i < length ; i++)
            {
                array[i] = readString();
            }
        }

        return array;
    }

    /**
     * Read and return a CharSequence[] object from the parcel.
     * {@hide}
     */
    public CharSequence[] readCharSequenceArray() {
        CharSequence[] array = null;

        int length = readInt();
        if (length >= 0)
        {
            array = new CharSequence[length];

            for (int i = 0 ; i < length ; i++)
            {
                array[i] = readCharSequence();
            }
        }

        return array;
    }

    /**
     * Read and return a new ArrayList object from the parcel at the current
     * dataPosition().  Returns null if the previously written list object was
     * null.  The given class loader will be used to load any enclosed
     * Parcelables.
     */
    @Implementation
    public ArrayList readArrayList(ClassLoader loader) {
        int N = readInt();
        if (N < 0) {
            return null;
        }
        ArrayList l = new ArrayList(N);
        readListInternal(l, N, loader);
        return l;
    }

    /**
     * Read and return a new Object array from the parcel at the current
     * dataPosition().  Returns null if the previously written array was
     * null.  The given class loader will be used to load any enclosed
     * Parcelables.
     */
    @Implementation
    public Object[] readArray(ClassLoader loader) {
        int N = readInt();
        if (N < 0) {
            return null;
        }
        Object[] l = new Object[N];
        readArrayInternal(l, N, loader);
        return l;
    }

    /**
     * Read and return a new SparseArray object from the parcel at the current
     * dataPosition().  Returns null if the previously written list object was
     * null.  The given class loader will be used to load any enclosed
     * Parcelables.
     */
    @Implementation
    public SparseArray readSparseArray(ClassLoader loader) {
        int N = readInt();
        if (N < 0) {
            return null;
        }
        SparseArray sa = new SparseArray(N);
        readSparseArrayInternal(sa, N, loader);
        return sa;
    }

    /**
     * Read and return a new SparseBooleanArray object from the parcel at the current
     * dataPosition().  Returns null if the previously written list object was
     * null.
     */
    @Implementation
    public SparseBooleanArray readSparseBooleanArray() {
        int N = readInt();
        if (N < 0) {
            return null;
        }
        SparseBooleanArray sa = new SparseBooleanArray(N);
        readSparseBooleanArrayInternal(sa, N);
        return sa;
    }

    /**
     * Read and return a new ArrayList containing a particular object type from
     * the parcel that was written with {@link #writeTypedList} at the
     * current dataPosition().  Returns null if the
     * previously written list object was null.  The list <em>must</em> have
     * previously been written via {@link #writeTypedList} with the same object
     * type.
     *
     * @return A newly created ArrayList containing objects with the same data
     *         as those that were previously written.
     *
     * @see #writeTypedList
     */
    @Implementation
    public <T> ArrayList<T> createTypedArrayList(Parcelable.Creator<T> c) {
        int N = readInt();
        if (N < 0) {
            return null;
        }
        ArrayList<T> l = new ArrayList<T>(N);
        while (N > 0) {
            if (readInt() != 0) {
                l.add(c.createFromParcel(This));
            } else {
                l.add(null);
            }
            N--;
        }
        return l;
    }

    /**
     * Read into the given List items containing a particular object type
     * that were written with {@link #writeTypedList} at the
     * current dataPosition().  The list <em>must</em> have
     * previously been written via {@link #writeTypedList} with the same object
     * type.
     *
     * @return A newly created ArrayList containing objects with the same data
     *         as those that were previously written.
     *
     * @see #writeTypedList
     */
    @Implementation
    public <T> void readTypedList(List<T> list, Parcelable.Creator<T> c) {
        int M = list.size();
        int N = readInt();
        int i = 0;
        for (; i < M && i < N; i++) {
            if (readInt() != 0) {
                list.set(i, c.createFromParcel(This));
            } else {
                list.set(i, null);
            }
        }
        for (; i<N; i++) {
            if (readInt() != 0) {
                list.add(c.createFromParcel(This));
            } else {
                list.add(null);
            }
        }
        for (; i<M; i++) {
            list.remove(N);
        }
    }

    /**
     * Read and return a new ArrayList containing String objects from
     * the parcel that was written with {@link #writeStringList} at the
     * current dataPosition().  Returns null if the
     * previously written list object was null.
     *
     * @return A newly created ArrayList containing strings with the same data
     *         as those that were previously written.
     *
     * @see #writeStringList
     */
    @Implementation
    public ArrayList<String> createStringArrayList() {
        int N = readInt();
        if (N < 0) {
            return null;
        }
        ArrayList<String> l = new ArrayList<String>(N);
        while (N > 0) {
            l.add(readString());
            N--;
        }
        return l;
    }

    /**
     * Read into the given List items String objects that were written with
     * {@link #writeStringList} at the current dataPosition().
     *
     * @return A newly created ArrayList containing strings with the same data
     *         as those that were previously written.
     *
     * @see #writeStringList
     */
    @Implementation
    public void readStringList(List<String> list) {
        int M = list.size();
        int N = readInt();
        int i = 0;
        for (; i < M && i < N; i++) {
            list.set(i, readString());
        }
        for (; i<N; i++) {
            list.add(readString());
        }
        for (; i<M; i++) {
            list.remove(N);
        }
    }

    /**
     * Read and return a new array containing a particular object type from
     * the parcel at the current dataPosition().  Returns null if the
     * previously written array was null.  The array <em>must</em> have
     * previously been written via {@link #writeTypedArray} with the same
     * object type.
     *
     * @return A newly created array containing objects with the same data
     *         as those that were previously written.
     *
     * @see #writeTypedArray
     */
    @Implementation
    public <T> T[] createTypedArray(Parcelable.Creator<T> c) {
        int N = readInt();
        if (N < 0) {
            return null;
        }
        T[] l = c.newArray(N);
        for (int i=0; i<N; i++) {
            if (readInt() != 0) {
                l[i] = c.createFromParcel(This);
            }
        }
        return l;
    }

    @Implementation
    public <T> void readTypedArray(T[] val, Parcelable.Creator<T> c) {
        int N = readInt();
        if (N == val.length) {
            for (int i=0; i<N; i++) {
                if (readInt() != 0) {
                    val[i] = c.createFromParcel(This);
                } else {
                    val[i] = null;
                }
            }
        } else {
            throw new RuntimeException("bad array lengths");
        }
    }

    /**
     * Write a heterogeneous array of Parcelable objects into the Parcel.
     * Each object in the array is written along with its class name, so
     * that the correct class can later be instantiated.  As a result, this
     * has significantly more overhead than {@link #writeTypedArray}, but will
     * correctly handle an array containing more than one type of object.
     *
     * @param value The array of objects to be written.
     * @param parcelableFlags Contextual flags as per
     * {@link Parcelable#writeToParcel(Parcel, int) Parcelable.writeToParcel()}.
     *
     * @see #writeTypedArray
     */
    @Implementation
    public <T extends Parcelable> void writeParcelableArray(T[] value,
            int parcelableFlags) {
        if (value != null) {
            int N = value.length;
            writeInt(N);
            for (int i=0; i<N; i++) {
                writeParcelable(value[i], parcelableFlags);
            }
        } else {
            writeInt(-1);
        }
    }

    /**
     * Read a typed object from a parcel.  The given class loader will be
     * used to load any enclosed Parcelables.  If it is null, the default class
     * loader will be used.
     */
    @Implementation
    public Object readValue(ClassLoader loader) {
        int type = readInt();

        switch (type) {
        case VAL_NULL:
            return null;

        case VAL_STRING:
            return readString();

        case VAL_INTEGER:
            return readInt();

        case VAL_MAP:
            return readHashMap(loader);

        case VAL_PARCELABLE:
            return readParcelable(loader);

        case VAL_SHORT:
            return (short) readInt();

        case VAL_LONG:
            return readLong();

        case VAL_FLOAT:
            return readFloat();

        case VAL_DOUBLE:
            return readDouble();

        case VAL_BOOLEAN:
            return readInt() == 1;

        case VAL_CHARSEQUENCE:
            return readCharSequence();

        case VAL_LIST:
            return readArrayList(loader);

        case VAL_BOOLEANARRAY:
            return createBooleanArray();

        case VAL_BYTEARRAY:
            return createByteArray();

        case VAL_STRINGARRAY:
            return readStringArray();

        case VAL_CHARSEQUENCEARRAY:
            return readCharSequenceArray();

        case VAL_IBINDER:
            throw new IllegalStateException("Not supported by mock");

        case VAL_OBJECTARRAY:
            return readArray(loader);

        case VAL_INTARRAY:
            return createIntArray();

        case VAL_LONGARRAY:
            return createLongArray();

        case VAL_BYTE:
            return readByte();

        case VAL_SERIALIZABLE:
            return readSerializable();

        case VAL_PARCELABLEARRAY:
            return readParcelableArray(loader);

        case VAL_SPARSEARRAY:
            return readSparseArray(loader);

        case VAL_SPARSEBOOLEANARRAY:
            return readSparseBooleanArray();

        case VAL_BUNDLE:
            return readBundle(loader); // loading will be deferred

        default:
            int off = -4;   // dataPosition() - 4;
            throw new RuntimeException(
                "Parcel " + this + ": Unmarshalling unknown type code " + type + " at offset " + off);
        }
    }

    /**
     * Read and return a new Parcelable from the parcel.  The given class loader
     * will be used to load any enclosed Parcelables.  If it is null, the default
     * class loader will be used.
     * @param loader A ClassLoader from which to instantiate the Parcelable
     * object, or null for the default class loader.
     * @return Returns the newly created Parcelable, or null if a null
     * object has been written.
     * @throws BadParcelableException Throws BadParcelableException if there
     * was an error trying to instantiate the Parcelable.
     */
    @Implementation
    public <T extends Parcelable> T readParcelable(ClassLoader loader) {
        String name = readString();
        if (name == null) {
            return null;
        }
        Parcelable.Creator<T> creator;
        synchronized (mCreators) {
            HashMap<String,Parcelable.Creator> map = mCreators.get(loader);
            if (map == null) {
                map = new HashMap<String,Parcelable.Creator>();
                mCreators.put(loader, map);
            }
            //noinspection unchecked
            creator = map.get(name);
            if (creator == null) {
                try {
                    Class c = loader == null ?
                        Class.forName(name) : Class.forName(name, true, loader);
                    Field f = c.getField("CREATOR");
                    f.setAccessible(true);
                    //noinspection unchecked
                    creator = (Parcelable.Creator)f.get(null);
                    if (creator == null) {
                        // look to see, does the shadow class have the CREATOR?
                        c = ShadowWrangler.getInstance().getShadowClass(c, getClass().getClassLoader());
                        f = c.getField("CREATOR");
                        f.setAccessible(true);
                        //noinspection unchecked
                        creator = (Parcelable.Creator)f.get(null);
                    }
                }
                catch (IllegalAccessException e) {
                    Log.e("Parcel", "Class not found when unmarshalling: "
                            + name + ", e: " + e);
                    throw new BadParcelableException(
                            "IllegalAccessException when unmarshalling: " + name);
                }
                catch (ClassNotFoundException e) {
                    Log.e("Parcel", "Class not found when unmarshalling: "
                                        + name + ", e: " + e);
                    throw new BadParcelableException(
                            "ClassNotFoundException when unmarshalling: " + name);
                }
                catch (ClassCastException e) {
                    e.printStackTrace();
                    throw new BadParcelableException("Parcelable protocol requires a "
                                        + "Parcelable.Creator object called "
                                        + " CREATOR on class " + name);
                }
                catch (NoSuchFieldException e) {
                    e.printStackTrace();
                    throw new BadParcelableException("Parcelable protocol requires a "
                                        + "Parcelable.Creator object called "
                                        + " CREATOR on class " + name);
                }
                if (creator == null) {
                    throw new BadParcelableException("Parcelable protocol requires a "
                                        + "Parcelable.Creator object called "
                                        + " CREATOR on class " + name);
                }

                map.put(name, creator);
            }
        }

        return creator.createFromParcel(This);
    }

    /**
     * Read and return a new Parcelable array from the parcel.
     * The given class loader will be used to load any enclosed
     * Parcelables.
     * @return the Parcelable array, or null if the array is null
     */
    @Implementation
    public Parcelable[] readParcelableArray(ClassLoader loader) {
        int N = readInt();
        if (N < 0) {
            return null;
        }
        Parcelable[] p = new Parcelable[N];
        for (int i = 0; i < N; i++) {
            p[i] = readParcelable(loader);
        }
        return p;
    }

    /**
     * Read and return a new Serializable object from the parcel.
     * @return the Serializable object, or null if the Serializable name
     * wasn't found in the parcel.
     */
    @Implementation
    public Serializable readSerializable() {
        String name = readString();
        if (name == null) {
            // For some reason we were unable to read the name of the Serializable (either there
            // is nothing left in the Parcel to read, or the next value wasn't a String), so
            // return null, which indicates that the name wasn't found in the parcel.
            return null;
        }

        byte[] serializedData = createByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(serializedData);
        try {
            ObjectInputStream ois = new ObjectInputStream(bais);
            return (Serializable) ois.readObject();
        } catch (IOException ioe) {
            throw new RuntimeException("Parcelable encountered " +
                "IOException reading a Serializable object (name = " + name +
                ")", ioe);
        } catch (ClassNotFoundException cnfe) {
            throw new RuntimeException("Parcelable encountered" +
                "ClassNotFoundException reading a Serializable object (name = "
                + name + ")", cnfe);
        }
    }

    // Cache of previously looked up CREATOR.createFromParcel() methods for
    // particular classes.  Keys are the names of the classes, values are
    // Method objects.
    private static final HashMap<ClassLoader,HashMap<String,Parcelable.Creator>>
        mCreators = new HashMap<ClassLoader,HashMap<String,Parcelable.Creator>>();

    @Implementation
    static public Parcel obtain(int obj) {
        ShadowParcel parcel = new ShadowParcel();
        parcel.__constructor__();
        return parcel.This;
    }

    public ShadowParcel() {
        init();
    }

    public void __constructor__() {
        init();
    }

    private void init() {
        this.buffer = new byte[32];
        this.cursor = 0;
        this.highwater = 0;
    }

    void readMapInternal(Map outVal, int N,
        ClassLoader loader) {
        while (N > 0) {
            Object key = readValue(loader);
            Object value = readValue(loader);
            //noinspection unchecked
            outVal.put(key, value);
            N--;
        }
    }

    private void readListInternal(List outVal, int N,
        ClassLoader loader) {
        while (N > 0) {
            Object value = readValue(loader);
            //Log.d("Parcel", "Unmarshalling value=" + value);
            //noinspection unchecked
            outVal.add(value);
            N--;
        }
    }

    private void readArrayInternal(Object[] outVal, int N,
        ClassLoader loader) {
        for (int i = 0; i < N; i++) {
            Object value = readValue(loader);
            //Log.d("Parcel", "Unmarshalling value=" + value);
            outVal[i] = value;
        }
    }

    private void readSparseArrayInternal(SparseArray outVal, int N,
        ClassLoader loader) {
        while (N > 0) {
            int key = readInt();
            Object value = readValue(loader);
            //Log.i("Parcel", "Unmarshalling key=" + key + " value=" + value);
            //noinspection unchecked
            outVal.append(key, value);
            N--;
        }
    }


    private void readSparseBooleanArrayInternal(SparseBooleanArray outVal, int N) {
        while (N > 0) {
            int key = readInt();
            boolean value = this.readByte() == 1;
            //Log.i("Parcel", "Unmarshalling key=" + key + " value=" + value);
            outVal.append(key, value);
            N--;
        }
    }
}
