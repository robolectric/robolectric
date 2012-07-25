/*
 * Copyright (C) 2006 The Android Open Source Project
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

import android.os.BadParcelableException;
import android.os.Bundle;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.ParcelFormatException;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

/**
 *
 */
@Implements(Parcel.class)
public class ShadowParcel {
    private static final boolean DEBUG_RECYCLE = false;
    private static final String TAG = "Parcel";

    private RuntimeException mStack;

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

    // The initial int32 in a Binder call's reply Parcel header:
    private static final int EX_SECURITY = -1;
    private static final int EX_BAD_PARCELABLE = -2;
    private static final int EX_ILLEGAL_ARGUMENT = -3;
    private static final int EX_NULL_POINTER = -4;
    private static final int EX_ILLEGAL_STATE = -5;
    private static final int EX_HAS_REPLY_HEADER = -128;  // special; see below

    private ByteBuffer byteBuffer;
    private byte[] bytes;
    private int dataSize;

    @RealObject
    private Parcel realParcel;

    public final static Parcelable.Creator<String> STRING_CREATOR
            = new Parcelable.Creator<String>() {
        public String createFromParcel(Parcel source) {
            return source.readString();
        }
        public String[] newArray(int size) {
            return new String[size];
        }
    };

    @Implementation
    public static Parcel obtain() {
        return Robolectric.newInstanceOf(Parcel.class);
    }

    public void __constructor__() {
        if (DEBUG_RECYCLE) {
            mStack = new RuntimeException();
        }
        bytes = new byte[32];
        byteBuffer = ByteBuffer.wrap(bytes);
    }

    /**
     * Returns the total amount of data contained in the parcel.
     */
    @Implementation
    public final int dataSize() {
        return dataSize;
    }

    /**
     * Returns the amount of data remaining to be read from the
     * parcel.  That is, {@link #dataSize}-{@link #dataPosition}.
     */
    @Implementation
    public final int dataAvail() {
        return dataSize() - dataPosition();
    }

    /**
     * Returns the current position in the parcel data.  Never
     * more than {@link #dataSize}.
     */
    @Implementation
    public final int dataPosition() {
        return byteBuffer.position();
    }

    /**
     * Returns the total amount of space in the parcel.  This is always
     * >= {@link #dataSize}.  The difference between it and dataSize() is the
     * amount of room left until the parcel needs to re-allocate its
     * data buffer.
     */
    @Implementation
    public final int dataCapacity() {
        return byteBuffer.capacity();
    }

    /**
     * Change the amount of data in the parcel.  Can be either smaller or
     * larger than the current size.  If larger than the current capacity,
     * more memory will be allocated.
     *
     * @param size The new number of bytes in the Parcel.
     */
    @Implementation
    public final void setDataSize(int size) {
        if (size > bytes.length) {
            reallocate(size);
        } else {
            throw new RuntimeException();
        }
    }

    private void reallocate(int newSize) {
        byte[] newBytes = new byte[newSize];
        System.arraycopy(bytes, 0, newBytes, 0, bytes.length);
        bytes = newBytes;
        int position = byteBuffer.position();
        byteBuffer = ByteBuffer.wrap(bytes);
        byteBuffer.position(position);
    }

    /**
     * Move the current read/write position in the parcel.
     * @param pos New offset in the parcel; must be between 0 and
     * {@link #dataSize}.
     */
    @Implementation
    public final void setDataPosition(int pos) {
        byteBuffer.position(pos);
    }

    /**
     * Change the capacity (current available space) of the parcel.
     *
     * @param size The new capacity of the parcel, in bytes.  Can not be
     * less than {@link #dataSize} -- that is, you can not drop existing data
     * with this method.
     */
    @Implementation
    public final void setDataCapacity(int size) {
        if (size > bytes.length) {
            reallocate(size);
        }
    }

    private void ensureDataCapacity(int additionalBytes) {
        setDataCapacity(dataPosition() + additionalBytes);
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
    public final byte[] marshall() {
        byte[] newBytes = new byte[byteBuffer.position()];
        System.arraycopy(bytes, 0, newBytes, 0, byteBuffer.position());
        return newBytes;
    }

    /**
     * Set the bytes in data to be the raw bytes of this Parcel.
     */
    @Implementation
    public final void unmarshall(byte[] data, int offest, int length) {
        byte[] newBytes = new byte[length];
        System.arraycopy(data, offest, newBytes, 0, length);
        bytes = newBytes;
        byteBuffer = ByteBuffer.wrap(bytes);
    }

    @Implementation
    public final void appendFrom(Parcel parcel, int offset, int length) {
        ensureDataCapacity(length);
        ShadowParcel other = shadowOf(parcel);
        byteBuffer.put(other.bytes, offset, length);
        dataSize = Math.max(dataPosition(), dataSize());
    }

    /**
     * Report whether the parcel contains any marshalled file descriptors.
     */
    @Implementation
    public final boolean hasFileDescriptors() {
        return false;
    }

    /**
     * Write a byte array into the parcel at the current {@link #dataPosition},
     * growing {@link #dataCapacity} if needed.
     * @param b Bytes to place into the parcel.
     */
    @Implementation
    public final void writeByteArray(byte[] b) {
        writeByteArray(b, 0, (b != null) ? b.length : 0);
    }

    /**
     * Write an byte array into the parcel at the current {@link #dataPosition},
     * growing {@link #dataCapacity} if needed.
     * @param b Bytes to place into the parcel.
     * @param offset Index of first byte to be written.
     * @param len Number of bytes to write.
     */
    @Implementation
    public final void writeByteArray(byte[] b, int offset, int len) {
        if (b == null) {
            writeInt(-1);
            return;
        }
        writeNative(b, offset, len);
    }

    private final void writeNative(byte[] b, int offset, int len) {
        ensureDataCapacity(len + 4);
        byteBuffer.putInt(len - offset);
        byteBuffer.put(b, offset, len);
        dataSize = Math.max(dataPosition(), dataSize());
    }

    /**
     * Write an integer value into the parcel at the current dataPosition(),
     * growing dataCapacity() if needed.
     */
    @Implementation
    public final void writeInt(int val) {
        ensureDataCapacity(4);
        byteBuffer.putInt(val);
        dataSize = Math.max(dataPosition(), dataSize());
    }

    /**
     * Write a long integer value into the parcel at the current dataPosition(),
     * growing dataCapacity() if needed.
     */
    @Implementation
    public final void writeLong(long val) {
        ensureDataCapacity(8);
        byteBuffer.putLong(val);
        dataSize = Math.max(dataPosition(), dataSize());
    }

    /**
     * Write a floating point value into the parcel at the current
     * dataPosition(), growing dataCapacity() if needed.
     */
    @Implementation
    public final void writeFloat(float val) {
        ensureDataCapacity(4);
        byteBuffer.putFloat(val);
        dataSize = Math.max(dataPosition(), dataSize());
    }

    /**
     * Write a double precision floating point value into the parcel at the
     * current dataPosition(), growing dataCapacity() if needed.
     */
    @Implementation
    public final void writeDouble(double val) {
        ensureDataCapacity(8);
        byteBuffer.putDouble(val);
        dataSize = Math.max(dataPosition(), dataSize());
    }

    /**
     * Write a string value into the parcel at the current dataPosition(),
     * growing dataCapacity() if needed.
     */
    @Implementation
    public final void writeString(String val) {
        if (val == null) {
            ensureDataCapacity(4);
            byteBuffer.putInt(0);
        } else {
            ensureDataCapacity(4);
            byteBuffer.putInt(1);
            byte[] bytes = val.getBytes();
            ensureDataCapacity(4 + bytes.length);
            byteBuffer.putInt(bytes.length);
            byteBuffer.put(bytes);
        }
        dataSize = Math.max(dataPosition(), dataSize());
    }

    /**
     * Write a CharSequence value into the parcel at the current dataPosition(),
     * growing dataCapacity() if needed.
     * @hide
     */
    public final void writeCharSequence(CharSequence val) {
        TextUtils.writeToParcel(val, realParcel, 0);
    }

    /**
     * Write an byte value into the parcel at the current dataPosition(),
     * growing dataCapacity() if needed.
     */
    @Implementation
    public final void writeByte(byte val) {
        ensureDataCapacity(1);
        byteBuffer.put(val);
        dataSize = Math.max(dataPosition(), dataSize());
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
    public final void writeMap(Map val) {
        writeMapInternal((Map<String, Object>) val);
    }

    /**
     * Flatten a Map into the parcel at the current dataPosition(),
     * growing dataCapacity() if needed.  The Map keys must be String objects.
     */
    /* package */ void writeMapInternal(Map<String,Object> val) {
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
    public final void writeBundle(Bundle val) {
        if (val == null) {
            writeInt(-1);
            return;
        }

        val.writeToParcel(realParcel, 0);
    }

    /**
     * Flatten a List into the parcel at the current dataPosition(), growing
     * dataCapacity() if needed.  The List values are written using
     * {@link #writeValue} and must follow the specification there.
     */
    @Implementation
    public final void writeList(List val) {
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
    public final void writeArray(Object[] val) {
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
    public final void writeSparseArray(SparseArray<Object> val) {
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
    public final void writeSparseBooleanArray(SparseBooleanArray val) {
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
    public final void writeBooleanArray(boolean[] val) {
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
    public final boolean[] createBooleanArray() {
        int N = readInt();
        // >>2 as a fast divide-by-4 works in the create*Array() functions
        // because dataAvail() will never return a negative number.  4 is
        // the size of a stored boolean in the stream.
        if (N >= 0 && N <= (dataAvail() >> 2)) {
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
    public final void readBooleanArray(boolean[] val) {
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
    public final void writeCharArray(char[] val) {
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
    public final char[] createCharArray() {
        int N = readInt();
        if (N >= 0 && N <= (dataAvail() >> 2)) {
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
    public final void readCharArray(char[] val) {
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
    public final void writeIntArray(int[] val) {
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
    public final int[] createIntArray() {
        int N = readInt();
        if (N >= 0 && N <= (dataAvail() >> 2)) {
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
    public final void readIntArray(int[] val) {
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
    public final void writeLongArray(long[] val) {
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
    public final long[] createLongArray() {
        int N = readInt();
        // >>3 because stored longs are 64 bits
        if (N >= 0 && N <= (dataAvail() >> 3)) {
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
    public final void readLongArray(long[] val) {
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
    public final void writeFloatArray(float[] val) {
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
    public final float[] createFloatArray() {
        int N = readInt();
        // >>2 because stored floats are 4 bytes
        if (N >= 0 && N <= (dataAvail() >> 2)) {
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
    public final void readFloatArray(float[] val) {
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
    public final void writeDoubleArray(double[] val) {
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
    public final double[] createDoubleArray() {
        int N = readInt();
        // >>3 because stored doubles are 8 bytes
        if (N >= 0 && N <= (dataAvail() >> 3)) {
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
    public final void readDoubleArray(double[] val) {
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
    public final void writeStringArray(String[] val) {
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
    public final String[] createStringArray() {
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
    public final void readStringArray(String[] val) {
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
    public final void writeCharSequenceArray(CharSequence[] val) {
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
    public final <T extends Parcelable> void writeTypedList(List<T> val) {
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
                item.writeToParcel(realParcel, 0);
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
    public final void writeStringList(List<String> val) {
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
    public final <T extends Parcelable> void writeTypedArray(T[] val,
                                                             int parcelableFlags) {
        if (val != null) {
            int N = val.length;
            writeInt(N);
            for (int i=0; i<N; i++) {
                T item = val[i];
                if (item != null) {
                    writeInt(1);
                    item.writeToParcel(realParcel, parcelableFlags);
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
     * <li> {@link SparseArray} (as supported by {@link #writeSparseArray(SparseArray)}).
     * <li> Any object that implements Serializable (but see
     *      {@link #writeSerializable} for caveats).  Note that all of the
     *      previous types have relatively efficient implementations for
     *      writing to a Parcel; having to rely on the generic serialization
     *      approach is much less efficient and should be avoided whenever
     *      possible.
     * </ul>
     *
     * <p class="caution">{@link Parcelable} objects are written with
     * {@link Parcelable#writeToParcel} using contextual flags of 0.  When
     * serializing objects containing {@link ParcelFileDescriptor}s,
     * this may result in file descriptor leaks when they are returned from
     * Binder calls (where {@link Parcelable#PARCELABLE_WRITE_RETURN_VALUE}
     * should be used).</p>
     */
    @Implementation
    public final void writeValue(Object v) {
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
    public final void writeParcelable(Parcelable p, int parcelableFlags) {
        if (p == null) {
            writeString(null);
            return;
        }
        String name = p.getClass().getName();
        writeString(name);
        p.writeToParcel(realParcel, parcelableFlags);
    }

    /**
     * Write a generic serializable object in to a Parcel.  It is strongly
     * recommended that this method be avoided, since the serialization
     * overhead is extremely large, and this approach will be much slower than
     * using the other approaches to writing data in to a Parcel.
     */
    @Implementation
    public final void writeSerializable(Serializable s) {
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
    public final void writeException(Exception e) {
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
    public final void writeNoException() {
        // Despite the name of this function ("write no exception"),
        // it should instead be thought of as "write the RPC response
        // header", but because this function name is written out by
        // the AIDL compiler, we're not going to rename it.
        //
        // The response header, in the non-exception case (see also
        // writeException above, also called by the AIDL compiler), is
        // either a 0 (the default case), or EX_HAS_REPLY_HEADER if
        // StrictMode has gathered up violations that have occurred
        // during a Binder call, in which case we write out the number
        // of violations and their details, serialized, before the
        // actual RPC respons data.  The receiving end of this is
        // readException(), below.
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
    public final void readException() {
        int code = readExceptionCode();
        if (code != 0) {
            String msg = readString();
            readException(code, msg);
        }
    }

    /**
     * Parses the header of a Binder call's response Parcel and
     * returns the exception code.  Deals with lite or fat headers.
     * In the common successful case, this header is generally zero.
     * In less common cases, it's a small negative number and will be
     * followed by an error string.
     *
     * This exists purely for android.database.DatabaseUtils and
     * insulating it from having to handle fat headers as returned by
     * e.g. StrictMode-induced RPC responses.
     *
     * @hide
     */
    public final int readExceptionCode() {
        int code = readInt();
        if (code == EX_HAS_REPLY_HEADER) {
            int headerSize = readInt();
            if (headerSize == 0) {
                Log.e(TAG, "Unexpected zero-sized Parcel reply header.");
            } else {
                // Currently the only thing in the header is StrictMode stacks,
                // but discussions around event/RPC tracing suggest we might
                // put that here too.  If so, switch on sub-header tags here.
                // But for now, just parse out the StrictMode stuff.
            }
            // And fat response headers are currently only used when
            // there are no exceptions, so return no error:
            return 0;
        }
        return code;
    }

    /**
     * Use this function for customized exception handling.
     * customized method call this method for all unknown case
     * @param code exception code
     * @param msg exception message
     */
    @Implementation
    public final void readException(int code, String msg) {
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
    public final int readInt() {
        try {
            int ret = byteBuffer.getInt();
            if (byteBuffer.position() > dataSize()) {
                throw new ParcelFormatException();
            }
            return ret;
        } catch (BufferUnderflowException e) {
            throw new ParcelFormatException();
        }
    }

    /**
     * Read a long integer value from the parcel at the current dataPosition().
     */
    @Implementation
    public final long readLong() {
        try {
            long ret = byteBuffer.getLong();
            if (byteBuffer.position() > dataSize()) {
                throw new ParcelFormatException();
            }
            return ret;
        } catch (BufferUnderflowException e) {
            throw new ParcelFormatException();
        }
    }

    /**
     * Read a floating point value from the parcel at the current
     * dataPosition().
     */
    @Implementation
    public final float readFloat() {
        try {
            float ret = byteBuffer.getFloat();
            if (byteBuffer.position() > dataSize()) {
                throw new ParcelFormatException();
            }
            return ret;
        } catch (BufferUnderflowException e) {
            throw new ParcelFormatException();
        }
    }

    /**
     * Read a double precision floating point value from the parcel at the
     * current dataPosition().
     */
    @Implementation
    public final double readDouble() {
        try {
            double ret = byteBuffer.getDouble();
            if (byteBuffer.position() > dataSize()) {
                throw new ParcelFormatException();
            }
            return ret;
        } catch (BufferUnderflowException e) {
            throw new ParcelFormatException();
        }
    }

    /**
     * Read a string value from the parcel at the current dataPosition().
     */
    @Implementation
    public final String readString() {
        try {
            int isNotNull = byteBuffer.getInt();
            if (byteBuffer.position() > dataSize()) {
                throw new ParcelFormatException();
            }
            if (isNotNull == 0) {
                return null;
            }
            int length = byteBuffer.getInt();
            byte[] bytes = new byte[length];
            byteBuffer.get(bytes);
            if (byteBuffer.position() > dataSize()) {
                throw new ParcelFormatException();
            }
            return new String(bytes);
        } catch (BufferUnderflowException e) {
            throw new ParcelFormatException();
        }
    }

    /**
     * Read a CharSequence value from the parcel at the current dataPosition().
     * @hide
     */
    public final CharSequence readCharSequence() {
        return TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(realParcel);
    }


    /**
     * Read a byte value from the parcel at the current dataPosition().
     */
    @Implementation
    public final byte readByte() {
        try {
            byte ret = byteBuffer.get();
            if (byteBuffer.position() > dataSize()) {
                throw new ParcelFormatException();
            }
            return ret;
        } catch (BufferUnderflowException e) {
            throw new ParcelFormatException();
        }
    }

    /**
     * Please use {@link #readBundle(ClassLoader)} instead (whose data must have
     * been written with {@link #writeBundle}.  Read into an existing Map object
     * from the parcel at the current dataPosition().
     */
    @Implementation
    public final void readMap(Map outVal, ClassLoader loader) {
        int N = readInt();
        readMapInternal(outVal, N, loader);
    }

    /**
     * Read into an existing List object from the parcel at the current
     * dataPosition(), using the given class loader to load any enclosed
     * Parcelables.  If it is null, the default class loader is used.
     */
    @Implementation
    public final void readList(List outVal, ClassLoader loader) {
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
    public final HashMap readHashMap(ClassLoader loader)
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
    public final Bundle readBundle() {
        return readBundle(null);
    }

    /**
     * Read and return a new Bundle object from the parcel at the current
     * dataPosition(), using the given class loader to initialize the class
     * loader of the Bundle for later retrieval of Parcelable objects.
     * Returns null if the previously written Bundle object was null.
     */
    @Implementation
    public final Bundle readBundle(ClassLoader loader) {
        int length = readInt();
        if (length < 0) {
            return null;
        }

        final Bundle bundle = new Bundle();
        shadowOf(bundle).initFromParcel(realParcel, length);
        if (loader != null) {
            bundle.setClassLoader(loader);
        }
        return bundle;
    }

    /**
     * Read and return a byte[] object from the parcel.
     */
    @Implementation
    public final byte[] createByteArray() {
        try {
            int length = byteBuffer.getInt();
            byte[] bytes = new byte[length];
            byteBuffer.get(bytes);
            if (byteBuffer.position() > dataSize()) {
                throw new ParcelFormatException();
            }
            return bytes;
        } catch (BufferUnderflowException e) {
            throw new ParcelFormatException();
        }

    }

    /**
     * Read a byte[] object from the parcel and copy it into the
     * given byte array.
     */
    @Implementation
    public final void readByteArray(byte[] val) {
        // TODO: make this a native method to avoid the extra copy.
        byte[] ba = createByteArray();
        if (ba.length == val.length) {
            System.arraycopy(ba, 0, val, 0, ba.length);
        } else {
            throw new RuntimeException("bad array lengths");
        }
    }

    /**
     * Read and return a String[] object from the parcel.
     * {@hide}
     */
    public final String[] readStringArray() {
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
    public final CharSequence[] readCharSequenceArray() {
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
    public final ArrayList readArrayList(ClassLoader loader) {
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
    public final Object[] readArray(ClassLoader loader) {
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
    public final SparseArray readSparseArray(ClassLoader loader) {
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
    public final SparseBooleanArray readSparseBooleanArray() {
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
    public final <T> ArrayList<T> createTypedArrayList(Parcelable.Creator<T> c) {
        int N = readInt();
        if (N < 0) {
            return null;
        }
        ArrayList<T> l = new ArrayList<T>(N);
        while (N > 0) {
            if (readInt() != 0) {
                l.add(c.createFromParcel(realParcel));
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
    public final <T> void readTypedList(List<T> list, Parcelable.Creator<T> c) {
        int M = list.size();
        int N = readInt();
        int i = 0;
        for (; i < M && i < N; i++) {
            if (readInt() != 0) {
                list.set(i, c.createFromParcel(realParcel));
            } else {
                list.set(i, null);
            }
        }
        for (; i<N; i++) {
            if (readInt() != 0) {
                list.add(c.createFromParcel(realParcel));
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
    public final ArrayList<String> createStringArrayList() {
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
    public final void readStringList(List<String> list) {
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
    public final <T> T[] createTypedArray(Parcelable.Creator<T> c) {
        int N = readInt();
        if (N < 0) {
            return null;
        }
        T[] l = c.newArray(N);
        for (int i=0; i<N; i++) {
            if (readInt() != 0) {
                l[i] = c.createFromParcel(realParcel);
            }
        }
        return l;
    }

    @Implementation
    public final <T> void readTypedArray(T[] val, Parcelable.Creator<T> c) {
        int N = readInt();
        if (N == val.length) {
            for (int i=0; i<N; i++) {
                if (readInt() != 0) {
                    val[i] = c.createFromParcel(realParcel);
                } else {
                    val[i] = null;
                }
            }
        } else {
            throw new RuntimeException("bad array lengths");
        }
    }

    /**
     * @deprecated
     * @hide
     */
    @Deprecated
    public final <T> T[] readTypedArray(Parcelable.Creator<T> c) {
        return createTypedArray(c);
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
    public final <T extends Parcelable> void writeParcelableArray(T[] value,
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
    public final Object readValue(ClassLoader loader) {
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
                int off = dataPosition() - 4;
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
    public final <T extends Parcelable> T readParcelable(ClassLoader loader) {
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
            creator = map.get(name);
            if (creator == null) {
                try {
                    Class c = loader == null ?
                            Class.forName(name) : Class.forName(name, true, loader);
                    Field f = c.getField("CREATOR");
                    creator = (Parcelable.Creator)f.get(null);
                }
                catch (IllegalAccessException e) {
                    Log.e(TAG, "Class not found when unmarshalling: "
                            + name + ", e: " + e);
                    throw new BadParcelableException(
                            "IllegalAccessException when unmarshalling: " + name);
                }
                catch (ClassNotFoundException e) {
                    Log.e(TAG, "Class not found when unmarshalling: "
                            + name + ", e: " + e);
                    throw new BadParcelableException(
                            "ClassNotFoundException when unmarshalling: " + name);
                }
                catch (ClassCastException e) {
                    throw new BadParcelableException("Parcelable protocol requires a "
                            + "Parcelable.Creator object called "
                            + " CREATOR on class " + name);
                }
                catch (NoSuchFieldException e) {
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

        return creator.createFromParcel(realParcel);
    }

    /**
     * Read and return a new Parcelable array from the parcel.
     * The given class loader will be used to load any enclosed
     * Parcelables.
     * @return the Parcelable array, or null if the array is null
     */
    @Implementation
    public final Parcelable[] readParcelableArray(ClassLoader loader) {
        int N = readInt();
        if (N < 0) {
            return null;
        }
        Parcelable[] p = new Parcelable[N];
        for (int i = 0; i < N; i++) {
            p[i] = (Parcelable) readParcelable(loader);
        }
        return p;
    }

    /**
     * Read and return a new Serializable object from the parcel.
     * @return the Serializable object, or null if the Serializable name
     * wasn't found in the parcel.
     */
    @Implementation
    public final Serializable readSerializable() {
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
    @Override
    public void finalize() throws Throwable {
        if (DEBUG_RECYCLE) {
            if (mStack != null) {
                Log.w(TAG, "Client did not call Parcel.recycle()", mStack);
            }
        }
    }

    /* package */ void readMapInternal(Map outVal, int N,
                                       ClassLoader loader) {
        while (N > 0) {
            Object key = readValue(loader);
            Object value = readValue(loader);
            outVal.put(key, value);
            N--;
        }
    }

    private void readListInternal(List outVal, int N,
                                  ClassLoader loader) {
        while (N > 0) {
            Object value = readValue(loader);
            //Log.d(TAG, "Unmarshalling value=" + value);
            outVal.add(value);
            N--;
        }
    }

    private void readArrayInternal(Object[] outVal, int N,
                                   ClassLoader loader) {
        for (int i = 0; i < N; i++) {
            Object value = readValue(loader);
            //Log.d(TAG, "Unmarshalling value=" + value);
            outVal[i] = value;
        }
    }

    private void readSparseArrayInternal(SparseArray outVal, int N,
                                         ClassLoader loader) {
        while (N > 0) {
            int key = readInt();
            Object value = readValue(loader);
            //Log.i(TAG, "Unmarshalling key=" + key + " value=" + value);
            outVal.append(key, value);
            N--;
        }
    }


    private void readSparseBooleanArrayInternal(SparseBooleanArray outVal, int N) {
        while (N > 0) {
            int key = readInt();
            boolean value = this.readByte() == 1;
            //Log.i(TAG, "Unmarshalling key=" + key + " value=" + value);
            outVal.append(key, value);
            N--;
        }
    }
}
