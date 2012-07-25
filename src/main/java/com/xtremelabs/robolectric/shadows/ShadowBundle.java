/*
 * Copyright (C) 2007 The Android Open Source Project
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

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.util.SparseArray;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

/**
 * A mapping from String values to various Parcelable types.
 *
 */
@Implements(Bundle.class)
public final class ShadowBundle {
    private static final String LOG_TAG = "Bundle";
    public static final Bundle EMPTY;

    static {
        EMPTY = new Bundle();
        ShadowBundle shadow = shadowOf(EMPTY);
        shadow.mMap = Collections.unmodifiableMap(new HashMap<String, Object>());
    }

    @RealObject
    private Bundle realBundle;

    // Invariant - exactly one of mMap / mParcelledData will be null
    // (except inside a call to unparcel)

    /* package */ Map<String, Object> mMap = null;

    /*
     * If mParcelledData is non-null, then mMap will be null and the
     * data are stored as a Parcel containing a Bundle.  When the data
     * are unparcelled, mParcelledData willbe set to null.
     */
    /* package */ Parcel mParcelledData = null;

    private boolean mHasFds = false;
    private boolean mFdsKnown = true;
    private boolean mAllowFds = true;

    /**
     * The ClassLoader used when unparcelling data from mParcelledData.
     */
    private ClassLoader mClassLoader;

    /**
     * Constructs a new, empty Bundle.
     */
    public void __constructor__() {
        mMap = new HashMap<String, Object>();
        mClassLoader = getClass().getClassLoader();
    }

    /**
     * Constructs a Bundle whose data is stored as a Parcel.  The data
     * will be unparcelled on first contact, using the assigned ClassLoader.
     *
     * @param parcelledData a Parcel containing a Bundle
     */
    void initFromParcel(Parcel parcelledData) {
        readFromParcel(parcelledData);
    }

    void initFromParcel(Parcel parcelledData, int length) {
        readFromParcelInner(parcelledData, length);
    }

    /**
     * Constructs a new, empty Bundle that uses a specific ClassLoader for
     * instantiating Parcelable and Serializable objects.
     *
     * @param loader An explicit ClassLoader to use when instantiating objects
     * inside of the Bundle.
     */
    public void __constructor__(ClassLoader loader) {
        mMap = new HashMap<String, Object>();
        mClassLoader = loader;
    }

    /**
     * Constructs a new, empty Bundle sized to hold the given number of
     * elements. The Bundle will grow as needed.
     *
     * @param capacity the initial capacity of the Bundle
     */
    public void __constructor__(int capacity) {
        mMap = new HashMap<String, Object>(capacity);
        mClassLoader = getClass().getClassLoader();
    }

    /**
     * Constructs a Bundle containing a copy of the mappings from the given
     * Bundle.
     *
     * @param b a Bundle to be copied.
     */
    public void __constructor__(Bundle b) {
        ShadowBundle shadow = shadowOf(b);
        if (shadow.mParcelledData != null) {
            mParcelledData = Parcel.obtain();
            mParcelledData.appendFrom(shadow.mParcelledData, 0, shadow.mParcelledData.dataSize());
            mParcelledData.setDataPosition(0);
        } else {
            mParcelledData = null;
        }

        if (shadow.mMap != null) {
            mMap = new HashMap<String, Object>(shadow.mMap);
        } else {
            mMap = null;
        }

        mHasFds = shadow.mHasFds;
        mFdsKnown = shadow.mFdsKnown;
        mClassLoader = shadow.mClassLoader;
    }

    /**
     * Make a Bundle for a single key/value pair.
     *
     * @hide
     */
    public static Bundle forPair(String key, String value) {
        // TODO: optimize this case.
        Bundle b = new Bundle(1);
        b.putString(key, value);
        return b;
    }

    /**
     * TODO: optimize this later (getting just the value part of a Bundle
     * with a single pair) once Bundle.forPair() above is implemented
     * with a special single-value Map implementation/serialization.
     *
     * Note: value in single-pair Bundle may be null.
     *
     * @hide
     */
    public String getPairValue() {
        unparcel();
        int size = mMap.size();
        if (size > 1) {
            Log.w(LOG_TAG, "getPairValue() used on Bundle with multiple pairs.");
        }
        if (size == 0) {
            return null;
        }
        Object o = mMap.values().iterator().next();
        try {
            return (String) o;
        } catch (ClassCastException e) {
            typeWarning("getPairValue()", o, "String", e);
            return null;
        }
    }

    /**
     * Changes the ClassLoader this Bundle uses when instantiating objects.
     *
     * @param loader An explicit ClassLoader to use when instantiating objects
     * inside of the Bundle.
     */
    @Implementation
    public void setClassLoader(ClassLoader loader) {
        mClassLoader = loader;
    }

    /**
     * Return the ClassLoader currently associated with this Bundle.
     */
    public ClassLoader getClassLoader() {
        return mClassLoader;
    }

    /** @hide */
    public boolean setAllowFds(boolean allowFds) {
        boolean orig = mAllowFds;
        mAllowFds = allowFds;
        return orig;
    }

    /**
     * Clones the current Bundle. The internal map is cloned, but the keys and
     * values to which it refers are copied by reference.
     */
    @Override
    @Implementation
    public Object clone() {
        return new Bundle(realBundle);
    }

    /**
     * If the underlying data are stored as a Parcel, unparcel them
     * using the currently assigned class loader.
     */
    /* package */ synchronized void unparcel() {
        if (mParcelledData == null) {
            return;
        }

        int N = mParcelledData.readInt();
        if (N < 0) {
            return;
        }
        if (mMap == null) {
            mMap = new HashMap<String, Object>();
        }
        ShadowParcel shadowParcel = shadowOf(mParcelledData);
        shadowParcel.readMapInternal(mMap, N, mClassLoader);
        mParcelledData.recycle();
        mParcelledData = null;
    }

    /**
     * Returns the number of mappings contained in this Bundle.
     *
     * @return the number of mappings as an int.
     */
    @Implementation
    public int size() {
        unparcel();
        return mMap.size();
    }

    /**
     * Returns true if the mapping of this Bundle is empty, false otherwise.
     */
    @Implementation
    public boolean isEmpty() {
        unparcel();
        return mMap.isEmpty();
    }

    /**
     * Removes all elements from the mapping of this Bundle.
     */
    @Implementation
    public void clear() {
        unparcel();
        mMap.clear();
        mHasFds = false;
        mFdsKnown = true;
    }

    /**
     * Returns true if the given key is contained in the mapping
     * of this Bundle.
     *
     * @param key a String key
     * @return true if the key is part of the mapping, false otherwise
     */
    @Implementation
    public boolean containsKey(String key) {
        unparcel();
        return mMap.containsKey(key);
    }

    /**
     * Returns the entry with the given key as an object.
     *
     * @param key a String key
     * @return an Object, or null
     */
    @Implementation
    public Object get(String key) {
        unparcel();
        return mMap.get(key);
    }

    /**
     * Removes any entry with the given key from the mapping of this Bundle.
     *
     * @param key a String key
     */
    @Implementation
    public void remove(String key) {
        unparcel();
        mMap.remove(key);
    }

    /**
     * Inserts all mappings from the given Bundle into this Bundle.
     *
     * @param map a Bundle
     */
    @Implementation
    public void putAll(Bundle map) {
        unparcel();
        ShadowBundle shadowBundle = shadowOf(map);
        shadowBundle.unparcel();
        mMap.putAll(shadowBundle.mMap);

        // fd state is now known if and only if both bundles already knew
        mHasFds |= shadowBundle.mHasFds;
        mFdsKnown = mFdsKnown && shadowBundle.mFdsKnown;
    }

    /**
     * Returns a Set containing the Strings used as keys in this Bundle.
     *
     * @return a Set of String keys
     */
    @Implementation
    public Set<String> keySet() {
        unparcel();
        return mMap.keySet();
    }

    /**
     * Reports whether the bundle contains any parcelled file descriptors.
     */
    @Implementation
    public boolean hasFileDescriptors() {
        if (!mFdsKnown) {
            boolean fdFound = false;    // keep going until we find one or run out of data

            if (mParcelledData != null) {
                if (mParcelledData.hasFileDescriptors()) {
                    fdFound = true;
                }
            } else {
                // It's been unparcelled, so we need to walk the map
                Iterator<Map.Entry<String, Object>> iter = mMap.entrySet().iterator();
                while (!fdFound && iter.hasNext()) {
                    Object obj = iter.next().getValue();
                    if (obj instanceof Parcelable) {
                        if ((((Parcelable)obj).describeContents()
                                & Parcelable.CONTENTS_FILE_DESCRIPTOR) != 0) {
                            fdFound = true;
                            break;
                        }
                    } else if (obj instanceof Parcelable[]) {
                        Parcelable[] array = (Parcelable[]) obj;
                        for (int n = array.length - 1; n >= 0; n--) {
                            if ((array[n].describeContents()
                                    & Parcelable.CONTENTS_FILE_DESCRIPTOR) != 0) {
                                fdFound = true;
                                break;
                            }
                        }
                    } else if (obj instanceof SparseArray) {
                        SparseArray<? extends Parcelable> array =
                                (SparseArray<? extends Parcelable>) obj;
                        for (int n = array.size() - 1; n >= 0; n--) {
                            if ((array.get(n).describeContents()
                                    & Parcelable.CONTENTS_FILE_DESCRIPTOR) != 0) {
                                fdFound = true;
                                break;
                            }
                        }
                    } else if (obj instanceof ArrayList) {
                        ArrayList array = (ArrayList) obj;
                        // an ArrayList here might contain either Strings or
                        // Parcelables; only look inside for Parcelables
                        if ((array.size() > 0)
                                && (array.get(0) instanceof Parcelable)) {
                            for (int n = array.size() - 1; n >= 0; n--) {
                                Parcelable p = (Parcelable) array.get(n);
                                if (p != null && ((p.describeContents()
                                        & Parcelable.CONTENTS_FILE_DESCRIPTOR) != 0)) {
                                    fdFound = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            mHasFds = fdFound;
            mFdsKnown = true;
        }
        return mHasFds;
    }

    /**
     * Inserts a Boolean value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a Boolean, or null
     */
    @Implementation
    public void putBoolean(String key, boolean value) {
        unparcel();
        mMap.put(key, value);
    }

    /**
     * Inserts a byte value into the mapping of this Bundle, replacing
     * any existing value for the given key.
     *
     * @param key a String, or null
     * @param value a byte
     */
    @Implementation
    public void putByte(String key, byte value) {
        unparcel();
        mMap.put(key, value);
    }

    /**
     * Inserts a char value into the mapping of this Bundle, replacing
     * any existing value for the given key.
     *
     * @param key a String, or null
     * @param value a char, or null
     */
    @Implementation
    public void putChar(String key, char value) {
        unparcel();
        mMap.put(key, value);
    }

    /**
     * Inserts a short value into the mapping of this Bundle, replacing
     * any existing value for the given key.
     *
     * @param key a String, or null
     * @param value a short
     */
    @Implementation
    public void putShort(String key, short value) {
        unparcel();
        mMap.put(key, value);
    }

    /**
     * Inserts an int value into the mapping of this Bundle, replacing
     * any existing value for the given key.
     *
     * @param key a String, or null
     * @param value an int, or null
     */
    @Implementation
    public void putInt(String key, int value) {
        unparcel();
        mMap.put(key, value);
    }

    /**
     * Inserts a long value into the mapping of this Bundle, replacing
     * any existing value for the given key.
     *
     * @param key a String, or null
     * @param value a long
     */
    @Implementation
    public void putLong(String key, long value) {
        unparcel();
        mMap.put(key, value);
    }

    /**
     * Inserts a float value into the mapping of this Bundle, replacing
     * any existing value for the given key.
     *
     * @param key a String, or null
     * @param value a float
     */
    @Implementation
    public void putFloat(String key, float value) {
        unparcel();
        mMap.put(key, value);
    }

    /**
     * Inserts a double value into the mapping of this Bundle, replacing
     * any existing value for the given key.
     *
     * @param key a String, or null
     * @param value a double
     */
    @Implementation
    public void putDouble(String key, double value) {
        unparcel();
        mMap.put(key, value);
    }

    /**
     * Inserts a String value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a String, or null
     */
    @Implementation
    public void putString(String key, String value) {
        unparcel();
        mMap.put(key, value);
    }

    /**
     * Inserts a CharSequence value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a CharSequence, or null
     */
    @Implementation
    public void putCharSequence(String key, CharSequence value) {
        unparcel();
        mMap.put(key, value);
    }

    /**
     * Inserts a Parcelable value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a Parcelable object, or null
     */
    @Implementation
    public void putParcelable(String key, Parcelable value) {
        unparcel();
        mMap.put(key, value);
        mFdsKnown = false;
    }

    /**
     * Inserts an array of Parcelable values into the mapping of this Bundle,
     * replacing any existing value for the given key.  Either key or value may
     * be null.
     *
     * @param key a String, or null
     * @param value an array of Parcelable objects, or null
     */
    @Implementation
    public void putParcelableArray(String key, Parcelable[] value) {
        unparcel();
        mMap.put(key, value);
        mFdsKnown = false;
    }

    /**
     * Inserts a List of Parcelable values into the mapping of this Bundle,
     * replacing any existing value for the given key.  Either key or value may
     * be null.
     *
     * @param key a String, or null
     * @param value an ArrayList of Parcelable objects, or null
     */
    @Implementation
    public void putParcelableArrayList(String key,
                                       ArrayList<? extends Parcelable> value) {
        unparcel();
        mMap.put(key, value);
        mFdsKnown = false;
    }

    /**
     * Inserts a SparceArray of Parcelable values into the mapping of this
     * Bundle, replacing any existing value for the given key.  Either key
     * or value may be null.
     *
     * @param key a String, or null
     * @param value a SparseArray of Parcelable objects, or null
     */
    @Implementation
    public void putSparseParcelableArray(String key,
                                         SparseArray<? extends Parcelable> value) {
        unparcel();
        mMap.put(key, value);
        mFdsKnown = false;
    }

    /**
     * Inserts an ArrayList<Integer> value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String, or null
     * @param value an ArrayList<Integer> object, or null
     */
    @Implementation
    public void putIntegerArrayList(String key, ArrayList<Integer> value) {
        unparcel();
        mMap.put(key, value);
    }

    /**
     * Inserts an ArrayList<String> value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String, or null
     * @param value an ArrayList<String> object, or null
     */
    @Implementation
    public void putStringArrayList(String key, ArrayList<String> value) {
        unparcel();
        mMap.put(key, value);
    }

    /**
     * Inserts an ArrayList<CharSequence> value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String, or null
     * @param value an ArrayList<CharSequence> object, or null
     */
    @Implementation
    public void putCharSequenceArrayList(String key, ArrayList<CharSequence> value) {
        unparcel();
        mMap.put(key, value);
    }

    /**
     * Inserts a Serializable value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a Serializable object, or null
     */
    @Implementation
    public void putSerializable(String key, Serializable value) {
        unparcel();
        mMap.put(key, value);
    }

    /**
     * Inserts a boolean array value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a boolean array object, or null
     */
    @Implementation
    public void putBooleanArray(String key, boolean[] value) {
        unparcel();
        mMap.put(key, value);
    }

    /**
     * Inserts a byte array value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a byte array object, or null
     */
    @Implementation
    public void putByteArray(String key, byte[] value) {
        unparcel();
        mMap.put(key, value);
    }

    /**
     * Inserts a short array value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a short array object, or null
     */
    @Implementation
    public void putShortArray(String key, short[] value) {
        unparcel();
        mMap.put(key, value);
    }

    /**
     * Inserts a char array value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a char array object, or null
     */
    @Implementation
    public void putCharArray(String key, char[] value) {
        unparcel();
        mMap.put(key, value);
    }

    /**
     * Inserts an int array value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String, or null
     * @param value an int array object, or null
     */
    @Implementation
    public void putIntArray(String key, int[] value) {
        unparcel();
        mMap.put(key, value);
    }

    /**
     * Inserts a long array value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a long array object, or null
     */
    @Implementation
    public void putLongArray(String key, long[] value) {
        unparcel();
        mMap.put(key, value);
    }

    /**
     * Inserts a float array value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a float array object, or null
     */
    @Implementation
    public void putFloatArray(String key, float[] value) {
        unparcel();
        mMap.put(key, value);
    }

    /**
     * Inserts a double array value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a double array object, or null
     */
    @Implementation
    public void putDoubleArray(String key, double[] value) {
        unparcel();
        mMap.put(key, value);
    }

    /**
     * Inserts a String array value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a String array object, or null
     */
    @Implementation
    public void putStringArray(String key, String[] value) {
        unparcel();
        mMap.put(key, value);
    }

    /**
     * Inserts a CharSequence array value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a CharSequence array object, or null
     */
    @Implementation
    public void putCharSequenceArray(String key, CharSequence[] value) {
        unparcel();
        mMap.put(key, value);
    }

    /**
     * Inserts a Bundle value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a Bundle object, or null
     */
    @Implementation
    public void putBundle(String key, Bundle value) {
        unparcel();
        mMap.put(key, value);
    }

    /**
     * Returns the value associated with the given key, or false if
     * no mapping of the desired type exists for the given key.
     *
     * @param key a String
     * @return a boolean value
     */
    @Implementation
    public boolean getBoolean(String key) {
        unparcel();
        return getBoolean(key, false);
    }

    // Log a message if the value was non-null but not of the expected type
    private void typeWarning(String key, Object value, String className,
                             Object defaultValue, ClassCastException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Key ");
        sb.append(key);
        sb.append(" expected ");
        sb.append(className);
        sb.append(" but value was a ");
        sb.append(value.getClass().getName());
        sb.append(".  The default value ");
        sb.append(defaultValue);
        sb.append(" was returned.");
        Log.w(LOG_TAG, sb.toString());
        Log.w(LOG_TAG, "Attempt to cast generated internal exception:", e);
    }

    private void typeWarning(String key, Object value, String className,
                             ClassCastException e) {
        typeWarning(key, value, className, "<null>", e);
    }

    /**
     * Returns the value associated with the given key, or defaultValue if
     * no mapping of the desired type exists for the given key.
     *
     * @param key a String
     * @return a boolean value
     */
    @Implementation
    public boolean getBoolean(String key, boolean defaultValue) {
        unparcel();
        Object o = mMap.get(key);
        if (o == null) {
            return defaultValue;
        }
        try {
            return (Boolean) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "Boolean", defaultValue, e);
            return defaultValue;
        }
    }

    /**
     * Returns the value associated with the given key, or (byte) 0 if
     * no mapping of the desired type exists for the given key.
     *
     * @param key a String
     * @return a byte value
     */
    @Implementation
    public byte getByte(String key) {
        unparcel();
        return getByte(key, (byte) 0);
    }

    /**
     * Returns the value associated with the given key, or defaultValue if
     * no mapping of the desired type exists for the given key.
     *
     * @param key a String
     * @return a byte value
     */
    @Implementation
    public Byte getByte(String key, byte defaultValue) {
        unparcel();
        Object o = mMap.get(key);
        if (o == null) {
            return defaultValue;
        }
        try {
            return (Byte) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "Byte", defaultValue, e);
            return defaultValue;
        }
    }

    /**
     * Returns the value associated with the given key, or false if
     * no mapping of the desired type exists for the given key.
     *
     * @param key a String
     * @return a char value
     */
    @Implementation
    public char getChar(String key) {
        unparcel();
        return getChar(key, (char) 0);
    }

    /**
     * Returns the value associated with the given key, or (char) 0 if
     * no mapping of the desired type exists for the given key.
     *
     * @param key a String
     * @return a char value
     */
    @Implementation
    public char getChar(String key, char defaultValue) {
        unparcel();
        Object o = mMap.get(key);
        if (o == null) {
            return defaultValue;
        }
        try {
            return (Character) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "Character", defaultValue, e);
            return defaultValue;
        }
    }

    /**
     * Returns the value associated with the given key, or (short) 0 if
     * no mapping of the desired type exists for the given key.
     *
     * @param key a String
     * @return a short value
     */
    @Implementation
    public short getShort(String key) {
        unparcel();
        return getShort(key, (short) 0);
    }

    /**
     * Returns the value associated with the given key, or defaultValue if
     * no mapping of the desired type exists for the given key.
     *
     * @param key a String
     * @return a short value
     */
    @Implementation
    public short getShort(String key, short defaultValue) {
        unparcel();
        Object o = mMap.get(key);
        if (o == null) {
            return defaultValue;
        }
        try {
            return (Short) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "Short", defaultValue, e);
            return defaultValue;
        }
    }

    /**
     * Returns the value associated with the given key, or 0 if
     * no mapping of the desired type exists for the given key.
     *
     * @param key a String
     * @return an int value
     */
    @Implementation
    public int getInt(String key) {
        unparcel();
        return getInt(key, 0);
    }

    /**
     * Returns the value associated with the given key, or defaultValue if
     * no mapping of the desired type exists for the given key.
     *
     * @param key a String
     * @return an int value
     */
    @Implementation
    public int getInt(String key, int defaultValue) {
        unparcel();
        Object o = mMap.get(key);
        if (o == null) {
            return defaultValue;
        }
        try {
            return (Integer) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "Integer", defaultValue, e);
            return defaultValue;
        }
    }

    /**
     * Returns the value associated with the given key, or 0L if
     * no mapping of the desired type exists for the given key.
     *
     * @param key a String
     * @return a long value
     */
    @Implementation
    public long getLong(String key) {
        unparcel();
        return getLong(key, 0L);
    }

    /**
     * Returns the value associated with the given key, or defaultValue if
     * no mapping of the desired type exists for the given key.
     *
     * @param key a String
     * @return a long value
     */
    @Implementation
    public long getLong(String key, long defaultValue) {
        unparcel();
        Object o = mMap.get(key);
        if (o == null) {
            return defaultValue;
        }
        try {
            return (Long) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "Long", defaultValue, e);
            return defaultValue;
        }
    }

    /**
     * Returns the value associated with the given key, or 0.0f if
     * no mapping of the desired type exists for the given key.
     *
     * @param key a String
     * @return a float value
     */
    @Implementation
    public float getFloat(String key) {
        unparcel();
        return getFloat(key, 0.0f);
    }

    /**
     * Returns the value associated with the given key, or defaultValue if
     * no mapping of the desired type exists for the given key.
     *
     * @param key a String
     * @return a float value
     */
    @Implementation
    public float getFloat(String key, float defaultValue) {
        unparcel();
        Object o = mMap.get(key);
        if (o == null) {
            return defaultValue;
        }
        try {
            return (Float) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "Float", defaultValue, e);
            return defaultValue;
        }
    }

    /**
     * Returns the value associated with the given key, or 0.0 if
     * no mapping of the desired type exists for the given key.
     *
     * @param key a String
     * @return a double value
     */
    @Implementation
    public double getDouble(String key) {
        unparcel();
        return getDouble(key, 0.0);
    }

    /**
     * Returns the value associated with the given key, or defaultValue if
     * no mapping of the desired type exists for the given key.
     *
     * @param key a String
     * @return a double value
     */
    @Implementation
    public double getDouble(String key, double defaultValue) {
        unparcel();
        Object o = mMap.get(key);
        if (o == null) {
            return defaultValue;
        }
        try {
            return (Double) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "Double", defaultValue, e);
            return defaultValue;
        }
    }

    /**
     * Returns the value associated with the given key, or null if
     * no mapping of the desired type exists for the given key or a null
     * value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return a String value, or null
     */
    @Implementation
    public String getString(String key) {
        unparcel();
        Object o = mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (String) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "String", e);
            return null;
        }
    }

    /**
     * Returns the value associated with the given key, or defaultValue if
     * no mapping of the desired type exists for the given key.
     *
     * @param key a String, or null
     * @param defaultValue Value to return if key does not exist
     * @return a String value, or null
     */
    public String getString(String key, String defaultValue) {
        unparcel();
        Object o = mMap.get(key);
        if (o == null) {
            return defaultValue;
        }
        try {
            return (String) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "String", e);
            return defaultValue;
        }
    }

    /**
     * Returns the value associated with the given key, or null if
     * no mapping of the desired type exists for the given key or a null
     * value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return a CharSequence value, or null
     */
    @Implementation
    public CharSequence getCharSequence(String key) {
        unparcel();
        Object o = mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (CharSequence) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "CharSequence", e);
            return null;
        }
    }

    /**
     * Returns the value associated with the given key, or defaultValue if
     * no mapping of the desired type exists for the given key.
     *
     * @param key a String, or null
     * @param defaultValue Value to return if key does not exist
     * @return a CharSequence value, or null
     */
    public CharSequence getCharSequence(String key, CharSequence defaultValue) {
        unparcel();
        Object o = mMap.get(key);
        if (o == null) {
            return defaultValue;
        }
        try {
            return (CharSequence) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "CharSequence", e);
            return defaultValue;
        }
    }

    /**
     * Returns the value associated with the given key, or null if
     * no mapping of the desired type exists for the given key or a null
     * value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return a Bundle value, or null
     */
    @Implementation
    public Bundle getBundle(String key) {
        unparcel();
        Object o = mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (Bundle) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "Bundle", e);
            return null;
        }
    }

    /**
     * Returns the value associated with the given key, or null if
     * no mapping of the desired type exists for the given key or a null
     * value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return a Parcelable value, or null
     */
    @Implementation
    public <T extends Parcelable> T getParcelable(String key) {
        unparcel();
        Object o = mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (T) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "Parcelable", e);
            return null;
        }
    }

    /**
     * Returns the value associated with the given key, or null if
     * no mapping of the desired type exists for the given key or a null
     * value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return a Parcelable[] value, or null
     */
    @Implementation
    public Parcelable[] getParcelableArray(String key) {
        unparcel();
        Object o = mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (Parcelable[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "Parcelable[]", e);
            return null;
        }
    }

    /**
     * Returns the value associated with the given key, or null if
     * no mapping of the desired type exists for the given key or a null
     * value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return an ArrayList<T> value, or null
     */
    @Implementation
    public <T extends Parcelable> ArrayList<T> getParcelableArrayList(String key) {
        unparcel();
        Object o = mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (ArrayList<T>) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "ArrayList", e);
            return null;
        }
    }

    /**
     * Returns the value associated with the given key, or null if
     * no mapping of the desired type exists for the given key or a null
     * value is explicitly associated with the key.
     *
     * @param key a String, or null
     *
     * @return a SparseArray of T values, or null
     */
    @Implementation
    public <T extends Parcelable> SparseArray<T> getSparseParcelableArray(String key) {
        unparcel();
        Object o = mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (SparseArray<T>) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "SparseArray", e);
            return null;
        }
    }

    /**
     * Returns the value associated with the given key, or null if
     * no mapping of the desired type exists for the given key or a null
     * value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return a Serializable value, or null
     */
    @Implementation
    public Serializable getSerializable(String key) {
        unparcel();
        Object o = mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (Serializable) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "Serializable", e);
            return null;
        }
    }

    /**
     * Returns the value associated with the given key, or null if
     * no mapping of the desired type exists for the given key or a null
     * value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return an ArrayList<String> value, or null
     */
    @Implementation
    public ArrayList<Integer> getIntegerArrayList(String key) {
        unparcel();
        Object o = mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (ArrayList<Integer>) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "ArrayList<Integer>", e);
            return null;
        }
    }

    /**
     * Returns the value associated with the given key, or null if
     * no mapping of the desired type exists for the given key or a null
     * value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return an ArrayList<String> value, or null
     */
    @Implementation
    public ArrayList<String> getStringArrayList(String key) {
        unparcel();
        Object o = mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (ArrayList<String>) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "ArrayList<String>", e);
            return null;
        }
    }

    /**
     * Returns the value associated with the given key, or null if
     * no mapping of the desired type exists for the given key or a null
     * value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return an ArrayList<CharSequence> value, or null
     */
    @Implementation
    public ArrayList<CharSequence> getCharSequenceArrayList(String key) {
        unparcel();
        Object o = mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (ArrayList<CharSequence>) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "ArrayList<CharSequence>", e);
            return null;
        }
    }

    /**
     * Returns the value associated with the given key, or null if
     * no mapping of the desired type exists for the given key or a null
     * value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return a boolean[] value, or null
     */
    @Implementation
    public boolean[] getBooleanArray(String key) {
        unparcel();
        Object o = mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (boolean[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "byte[]", e);
            return null;
        }
    }

    /**
     * Returns the value associated with the given key, or null if
     * no mapping of the desired type exists for the given key or a null
     * value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return a byte[] value, or null
     */
    @Implementation
    public byte[] getByteArray(String key) {
        unparcel();
        Object o = mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (byte[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "byte[]", e);
            return null;
        }
    }

    /**
     * Returns the value associated with the given key, or null if
     * no mapping of the desired type exists for the given key or a null
     * value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return a short[] value, or null
     */
    @Implementation
    public short[] getShortArray(String key) {
        unparcel();
        Object o = mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (short[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "short[]", e);
            return null;
        }
    }

    /**
     * Returns the value associated with the given key, or null if
     * no mapping of the desired type exists for the given key or a null
     * value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return a char[] value, or null
     */
    @Implementation
    public char[] getCharArray(String key) {
        unparcel();
        Object o = mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (char[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "char[]", e);
            return null;
        }
    }

    /**
     * Returns the value associated with the given key, or null if
     * no mapping of the desired type exists for the given key or a null
     * value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return an int[] value, or null
     */
    @Implementation
    public int[] getIntArray(String key) {
        unparcel();
        Object o = mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (int[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "int[]", e);
            return null;
        }
    }

    /**
     * Returns the value associated with the given key, or null if
     * no mapping of the desired type exists for the given key or a null
     * value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return a long[] value, or null
     */
    @Implementation
    public long[] getLongArray(String key) {
        unparcel();
        Object o = mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (long[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "long[]", e);
            return null;
        }
    }

    /**
     * Returns the value associated with the given key, or null if
     * no mapping of the desired type exists for the given key or a null
     * value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return a float[] value, or null
     */
    @Implementation
    public float[] getFloatArray(String key) {
        unparcel();
        Object o = mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (float[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "float[]", e);
            return null;
        }
    }

    /**
     * Returns the value associated with the given key, or null if
     * no mapping of the desired type exists for the given key or a null
     * value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return a double[] value, or null
     */
    @Implementation
    public double[] getDoubleArray(String key) {
        unparcel();
        Object o = mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (double[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "double[]", e);
            return null;
        }
    }

    /**
     * Returns the value associated with the given key, or null if
     * no mapping of the desired type exists for the given key or a null
     * value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return a String[] value, or null
     */
    @Implementation
    public String[] getStringArray(String key) {
        unparcel();
        Object o = mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (String[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "String[]", e);
            return null;
        }
    }

    /**
     * Returns the value associated with the given key, or null if
     * no mapping of the desired type exists for the given key or a null
     * value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return a CharSequence[] value, or null
     */
    @Implementation
    public CharSequence[] getCharSequenceArray(String key) {
        unparcel();
        Object o = mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (CharSequence[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "CharSequence[]", e);
            return null;
        }
    }

    public static final Parcelable.Creator<Bundle> CREATOR =
            new Parcelable.Creator<Bundle>() {
                public Bundle createFromParcel(Parcel in) {
                    return in.readBundle();
                }

                public Bundle[] newArray(int size) {
                    return new Bundle[size];
                }
            };

    /**
     * Report the nature of this Parcelable's contents
     */
    @Implementation
    public int describeContents() {
        int mask = 0;
        if (hasFileDescriptors()) {
            mask |= Parcelable.CONTENTS_FILE_DESCRIPTOR;
        }
        return mask;
    }

    /**
     * Writes the Bundle contents to a Parcel, typically in order for
     * it to be passed through an IBinder connection.
     * @param parcel The parcel to copy this bundle to.
     */
    @Implementation
    public void writeToParcel(Parcel parcel, int flags) {
        ShadowParcel shadowParcel = shadowOf(parcel);
        if (mParcelledData != null) {
            int length = mParcelledData.dataSize();
            parcel.writeInt(length);
            parcel.writeInt(0x4C444E42); // 'B' 'N' 'D' 'L'
            parcel.appendFrom(mParcelledData, 0, length);
        } else {
            Parcel tmpParcel = Parcel.obtain();
            ShadowParcel tmpShadowParcel = shadowOf(tmpParcel);
            tmpShadowParcel.writeMapInternal(mMap);

            int length = tmpParcel.dataSize();
            parcel.writeInt(length);
            parcel.writeInt(0x4C444E42); // 'B' 'N' 'D' 'L'
            parcel.appendFrom(tmpParcel, 0, length);
        }
    }

    /**
     * Reads the Parcel contents into this Bundle, typically in order for
     * it to be passed through an IBinder connection.
     * @param parcel The parcel to overwrite this bundle from.
     */
    @Implementation
    public void readFromParcel(Parcel parcel) {
        int length = parcel.readInt();
        if (length < 0) {
            throw new RuntimeException("Bad length in parcel: " + length);
        }
        readFromParcelInner(parcel, length);
    }

    void readFromParcelInner(Parcel parcel, int length) {
        int magic = parcel.readInt();
        if (magic != 0x4C444E42) {
            //noinspection ThrowableInstanceNeverThrown
            String st = Log.getStackTraceString(new RuntimeException());
            Log.e("Bundle", "readBundle: bad magic number");
            Log.e("Bundle", "readBundle: trace = " + st);
        }

        // Advance within this Parcel
        int offset = parcel.dataPosition();
        parcel.setDataPosition(offset + length);

        Parcel p = Parcel.obtain();
        p.setDataPosition(0);
        p.appendFrom(parcel, offset, length);
        p.setDataPosition(0);

        mParcelledData = p;
        mHasFds = p.hasFileDescriptors();
        mFdsKnown = true;
    }

    @Implementation
    @Override
    public synchronized String toString() {
        if (mParcelledData != null) {
            return "Bundle[mParcelledData.dataSize=" +
                    mParcelledData.dataSize() + "]";
        }
        return "Bundle[" + mMap.toString() + "]";
    }
}
