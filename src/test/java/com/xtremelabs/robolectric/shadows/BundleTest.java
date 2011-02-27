/*
 * Copyright (C) 2008 The Android Open Source Project
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

import com.xtremelabs.robolectric.WithTestDefaultsRunner;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.SparseArray;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(WithTestDefaultsRunner.class)
public class BundleTest {
    private static final boolean BOOLEANKEYVALUE = false;

    private static final int INTKEYVALUE = 20;

    private static final String INTKEY = "intkey";

    private static final String BOOLEANKEY = "booleankey";

    public static final String KEY = "Bruce Lee";

    private static final String KEY2 = "key2";

    private Spannable mSpannable;

    private Bundle mBundle;

    @Before
    public void setUp() throws Exception {
        mBundle = new Bundle();
        mSpannable = new SpannableString("foo bar");
        mSpannable.setSpan(new ForegroundColorSpan(0x123456), 0, 3, 0);
    }

    @Test
    public void testBundle() {
        final Bundle b1 = new Bundle();
        assertTrue(b1.isEmpty());
        b1.putBoolean(KEY, true);
        assertFalse(b1.isEmpty());

        final Bundle b2 = new Bundle(b1);
        assertTrue(b2.getBoolean(KEY));

        new Bundle(1024);
        new Bundle(getClass().getClassLoader());
    }

    // first put sth into tested Bundle, it shouldn't be empty, then clear it and it should be empty
    @Test
    public void testClear() {
        mBundle.putBoolean("android", true);
        mBundle.putBoolean(KEY, true);
        assertFalse(mBundle.isEmpty());
        mBundle.clear();
        assertTrue(mBundle.isEmpty());
    }

    // first clone the tested Bundle, then compare the original Bundle with the
    // cloned Bundle, they should equal
    @Test
    public void testClone() {
        mBundle.putBoolean(BOOLEANKEY, BOOLEANKEYVALUE);
        mBundle.putInt(INTKEY, INTKEYVALUE);
        Bundle cloneBundle = (Bundle) mBundle.clone();
        assertEquals(mBundle.size(), cloneBundle.size());
        assertEquals(mBundle.getBoolean(BOOLEANKEY), cloneBundle.getBoolean(BOOLEANKEY));
        assertEquals(mBundle.getInt(INTKEY), cloneBundle.getInt(INTKEY));
    }

    // containsKey would return false if nothing has been put into the Bundle,
    // else containsKey would return true if any putXXX has been called before
    @Test
    public void testContainsKey() {
        assertFalse(mBundle.containsKey(KEY));
        mBundle.putBoolean(KEY, true);
        assertTrue(mBundle.containsKey(KEY));
        roundtrip();
        assertTrue(mBundle.containsKey(KEY));
    }

    // get would return null if nothing has been put into the Bundle,else get
    // would return the value set by putXXX
    @Test
    public void testGet() {
        assertNull(mBundle.get(KEY));
        mBundle.putBoolean(KEY, true);
        assertNotNull(mBundle.get(KEY));
        roundtrip();
        assertNotNull(mBundle.get(KEY));
    }

    @Test
    public void testGetBoolean1() {
        assertFalse(mBundle.getBoolean(KEY));
        mBundle.putBoolean(KEY, true);
        assertTrue(mBundle.getBoolean(KEY));
        roundtrip();
        assertTrue(mBundle.getBoolean(KEY));
    }

    @Test
    public void testGetBoolean2() {
        assertTrue(mBundle.getBoolean(KEY, true));
        mBundle.putBoolean(KEY, false);
        assertFalse(mBundle.getBoolean(KEY, true));
        roundtrip();
        assertFalse(mBundle.getBoolean(KEY, true));
    }

    @Test
    public void testGetBooleanArray() {
        assertNull(mBundle.getBooleanArray(KEY));
        mBundle.putBooleanArray(KEY, new boolean[] {
                true, false, true
        });
        boolean[] booleanArray = mBundle.getBooleanArray(KEY);
        assertNotNull(booleanArray);
        assertEquals(3, booleanArray.length);
        assertEquals(true, booleanArray[0]);
        assertEquals(false, booleanArray[1]);
        assertEquals(true, booleanArray[2]);
        roundtrip();
        booleanArray = mBundle.getBooleanArray(KEY);
        assertNotNull(booleanArray);
        assertEquals(3, booleanArray.length);
        assertEquals(true, booleanArray[0]);
        assertEquals(false, booleanArray[1]);
        assertEquals(true, booleanArray[2]);
    }

    @Test
    public void testGetBundle() {
        assertNull(mBundle.getBundle(KEY));
        final Bundle bundle = new Bundle();
        mBundle.putBundle(KEY, bundle);
        assertTrue(bundle.equals(mBundle.getBundle(KEY)));
        roundtrip();
        assertBundleEquals(bundle, mBundle.getBundle(KEY));
    }

    @Test
    public void testGetByte1() {
        final byte b = 7;

        assertEquals(0, mBundle.getByte(KEY));
        mBundle.putByte(KEY, b);
        assertEquals(b, mBundle.getByte(KEY));
        roundtrip();
        assertEquals(b, mBundle.getByte(KEY));
    }

    @Test
    public void testGetByte2() {
        final byte b1 = 6;
        final byte b2 = 7;

        assertEquals((Byte)b1, mBundle.getByte(KEY, b1));
        mBundle.putByte(KEY, b2);
        assertEquals((Byte)b2, mBundle.getByte(KEY, b1));
        roundtrip();
        assertEquals((Byte)b2, mBundle.getByte(KEY, b1));
    }

    @Test
    public void testGetByteArray() {
        assertNull(mBundle.getByteArray(KEY));
        mBundle.putByteArray(KEY, new byte[] {
                1, 2, 3
        });
        byte[] byteArray = mBundle.getByteArray(KEY);
        assertNotNull(byteArray);
        assertEquals(3, byteArray.length);
        assertEquals(1, byteArray[0]);
        assertEquals(2, byteArray[1]);
        assertEquals(3, byteArray[2]);
        roundtrip();
        byteArray = mBundle.getByteArray(KEY);
        assertNotNull(byteArray);
        assertEquals(3, byteArray.length);
        assertEquals(1, byteArray[0]);
        assertEquals(2, byteArray[1]);
        assertEquals(3, byteArray[2]);
    }

    @Test
    public void testGetChar1() {
        final char c = 'l';

        assertEquals((char)0, mBundle.getChar(KEY));
        mBundle.putChar(KEY, c);
        assertEquals(c, mBundle.getChar(KEY));
        roundtrip();
        assertEquals(c, mBundle.getChar(KEY));
    }

    @Test
    public void testGetChar2() {
        final char c1 = 'l';
        final char c2 = 'i';

        assertEquals(c1, mBundle.getChar(KEY, c1));
        mBundle.putChar(KEY, c2);
        assertEquals(c2, mBundle.getChar(KEY, c1));
        roundtrip();
        assertEquals(c2, mBundle.getChar(KEY, c1));
    }

    @Test
    public void testGetCharArray() {
        assertNull(mBundle.getCharArray(KEY));
        mBundle.putCharArray(KEY, new char[] {
                'h', 'i'
        });
        char[] charArray = mBundle.getCharArray(KEY);
        assertEquals('h', charArray[0]);
        assertEquals('i', charArray[1]);
        roundtrip();
        charArray = mBundle.getCharArray(KEY);
        assertEquals('h', charArray[0]);
        assertEquals('i', charArray[1]);
    }

    @Test
    public void testGetCharSequence() {
        final CharSequence cS = "Bruce Lee";

        assertNull(mBundle.getCharSequence(KEY));
        assertNull(mBundle.getCharSequence(KEY2));
        mBundle.putCharSequence(KEY, cS);
        mBundle.putCharSequence(KEY2, mSpannable);
        assertEquals(cS, mBundle.getCharSequence(KEY));
        // todo assertSpannableEquals(mSpannable, mBundle.getCharSequence(KEY2));
        roundtrip();
        assertEquals(cS, mBundle.getCharSequence(KEY));
        // todo assertSpannableEquals(mSpannable, mBundle.getCharSequence(KEY2));
    }

    @Test
    public void testGetCharSequenceArray() {
        assertNull(mBundle.getCharSequenceArray(KEY));
        mBundle.putCharSequenceArray(KEY, new CharSequence[] {
                "one", "two", "three", mSpannable
        });
        CharSequence[] ret = mBundle.getCharSequenceArray(KEY);
        assertEquals(4, ret.length);
        assertEquals("one", ret[0]);
        assertEquals("two", ret[1]);
        assertEquals("three", ret[2]);
        // todo assertSpannableEquals(mSpannable, ret[3]);
        roundtrip();
        ret = mBundle.getCharSequenceArray(KEY);
        assertEquals(4, ret.length);
        assertEquals("one", ret[0]);
        assertEquals("two", ret[1]);
        assertEquals("three", ret[2]);
        // todo assertSpannableEquals(mSpannable, ret[3]);
    }

    @Test
    public void testGetCharSequenceArrayList() {
        assertNull(mBundle.getCharSequenceArrayList(KEY));
        final ArrayList<CharSequence> list = new ArrayList<CharSequence>();
        list.add("one");
        list.add("two");
        list.add("three");
        list.add(mSpannable);
        mBundle.putCharSequenceArrayList(KEY, list);
        roundtrip();
        ArrayList<CharSequence> ret = mBundle.getCharSequenceArrayList(KEY);
        assertEquals(4, ret.size());
        assertEquals("one", ret.get(0));
        assertEquals("two", ret.get(1));
        assertEquals("three", ret.get(2));
        // todo assertSpannableEquals(mSpannable, ret.get(3));
        roundtrip();
        ret = mBundle.getCharSequenceArrayList(KEY);
        assertEquals(4, ret.size());
        assertEquals("one", ret.get(0));
        assertEquals("two", ret.get(1));
        assertEquals("three", ret.get(2));
        // todo assertSpannableEquals(mSpannable, ret.get(3));
    }

    @Test
    public void testGetDouble1() {
        final double d = 10.07;

        assertEquals(0.0, mBundle.getDouble(KEY), 0);
        mBundle.putDouble(KEY, d);
        assertEquals(d, mBundle.getDouble(KEY), 0);
        roundtrip();
        assertEquals(d, mBundle.getDouble(KEY), 0);
    }

    @Test
    public void testGetDouble2() {
        final double d1 = 10.06;
        final double d2 = 10.07;

        assertEquals(d1, mBundle.getDouble(KEY, d1), 0);
        mBundle.putDouble(KEY, d2);
        assertEquals(d2, mBundle.getDouble(KEY, d1), 0);
        roundtrip();
        assertEquals(d2, mBundle.getDouble(KEY, d1), 0);
    }

    @Test
    public void testGetDoubleArray() {
        assertNull(mBundle.getDoubleArray(KEY));
        mBundle.putDoubleArray(KEY, new double[] {
                10.06, 10.07
        });
        double[] doubleArray = mBundle.getDoubleArray(KEY);
        assertEquals(10.06, doubleArray[0], 0);
        assertEquals(10.07, doubleArray[1], 0);
        roundtrip();
        doubleArray = mBundle.getDoubleArray(KEY);
        assertEquals(10.06, doubleArray[0], 0);
        assertEquals(10.07, doubleArray[1], 0);
    }

    @Test
    public void testGetFloat1() {
        final float f = 10.07f;

        assertEquals(0.0f, mBundle.getFloat(KEY), 0);
        mBundle.putFloat(KEY, f);
        assertEquals(f, mBundle.getFloat(KEY), 0);
        roundtrip();
        assertEquals(f, mBundle.getFloat(KEY), 0);
    }

    @Test
    public void testGetFloat2() {
        final float f1 = 10.06f;
        final float f2 = 10.07f;

        assertEquals(f1, mBundle.getFloat(KEY, f1), 0);
        mBundle.putFloat(KEY, f2);
        assertEquals(f2, mBundle.getFloat(KEY, f1), 0);
        roundtrip();
        assertEquals(f2, mBundle.getFloat(KEY, f1), 0);
    }

    @Test
    public void testGetFloatArray() {
        assertNull(mBundle.getFloatArray(KEY));
        mBundle.putFloatArray(KEY, new float[] {
                10.06f, 10.07f
        });
        float[] floatArray = mBundle.getFloatArray(KEY);
        assertEquals(10.06f, floatArray[0], 0);
        assertEquals(10.07f, floatArray[1], 0);
        roundtrip();
        floatArray = mBundle.getFloatArray(KEY);
        assertEquals(10.06f, floatArray[0], 0);
        assertEquals(10.07f, floatArray[1], 0);
    }

    @Test
    public void testGetInt1() {
        final int i = 1007;

        assertEquals(0, mBundle.getInt(KEY));
        mBundle.putInt(KEY, i);
        assertEquals(i, mBundle.getInt(KEY));
        roundtrip();
        assertEquals(i, mBundle.getInt(KEY));
    }

    @Test
    public void testGetInt2() {
        final int i1 = 1006;
        final int i2 = 1007;

        assertEquals(i1, mBundle.getInt(KEY, i1));
        mBundle.putInt(KEY, i2);
        assertEquals(i2, mBundle.getInt(KEY, i2));
        roundtrip();
        assertEquals(i2, mBundle.getInt(KEY, i2));
    }

    @Test
    public void testGetIntArray() {
        assertNull(mBundle.getIntArray(KEY));
        mBundle.putIntArray(KEY, new int[] {
                1006, 1007
        });
        int[] intArray = mBundle.getIntArray(KEY);
        assertEquals(1006, intArray[0]);
        assertEquals(1007, intArray[1]);
        roundtrip();
        intArray = mBundle.getIntArray(KEY);
        assertEquals(1006, intArray[0]);
        assertEquals(1007, intArray[1]);
    }

    // getIntegerArrayList should only return the IntegerArrayList set by putIntegerArrayLis
    @Test
    public void testGetIntegerArrayList() {
        final int i1 = 1006;
        final int i2 = 1007;

        assertNull(mBundle.getIntegerArrayList(KEY));
        final ArrayList<Integer> arrayList = new ArrayList<Integer>();
        arrayList.add(i1);
        arrayList.add(i2);
        mBundle.putIntegerArrayList(KEY, arrayList);
        ArrayList<Integer> retArrayList = mBundle.getIntegerArrayList(KEY);
        assertNotNull(retArrayList);
        assertEquals(2, retArrayList.size());
        assertEquals((Integer)i1, retArrayList.get(0));
        assertEquals((Integer)i2, retArrayList.get(1));
        roundtrip();
        retArrayList = mBundle.getIntegerArrayList(KEY);
        assertNotNull(retArrayList);
        assertEquals(2, retArrayList.size());
        assertEquals((Integer)i1, retArrayList.get(0));
        assertEquals((Integer)i2, retArrayList.get(1));
    }

    @Test
    public void testGetLong1() {
        final long l = 1007;

        assertEquals(0, mBundle.getLong(KEY));
        mBundle.putLong(KEY, l);
        assertEquals(l, mBundle.getLong(KEY));
        roundtrip();
        assertEquals(l, mBundle.getLong(KEY));
    }

    @Test
    public void testGetLong2() {
        final long l1 = 1006;
        final long l2 = 1007;

        assertEquals(l1, mBundle.getLong(KEY, l1));
        mBundle.putLong(KEY, l2);
        assertEquals(l2, mBundle.getLong(KEY, l2));
        roundtrip();
        assertEquals(l2, mBundle.getLong(KEY, l2));
    }

    @Test
    public void testGetLongArray() {
        assertNull(mBundle.getLongArray(KEY));
        mBundle.putLongArray(KEY, new long[] {
                1006, 1007
        });
        long[] longArray = mBundle.getLongArray(KEY);
        assertEquals(1006, longArray[0]);
        assertEquals(1007, longArray[1]);
        roundtrip();
        longArray = mBundle.getLongArray(KEY);
        assertEquals(1006, longArray[0]);
        assertEquals(1007, longArray[1]);
    }

    @Test
    public void testGetParcelable() {
        assertNull(mBundle.getParcelable(KEY));
        final Bundle bundle = new Bundle();
        mBundle.putParcelable(KEY, bundle);
        assertTrue(bundle.equals(mBundle.getParcelable(KEY)));
        roundtrip();
        assertBundleEquals(bundle, (Bundle) mBundle.getParcelable(KEY));
    }

    // getParcelableArray should only return the ParcelableArray set by putParcelableArray
    @Test
    public void testGetParcelableArray() {
        assertNull(mBundle.getParcelableArray(KEY));
        final Bundle bundle1 = new Bundle();
        final Bundle bundle2 = new Bundle();
        mBundle.putParcelableArray(KEY, new Bundle[] {
                bundle1, bundle2
        });
        Parcelable[] parcelableArray = mBundle.getParcelableArray(KEY);
        assertEquals(2, parcelableArray.length);
        assertTrue(bundle1.equals(parcelableArray[0]));
        assertTrue(bundle2.equals(parcelableArray[1]));
        roundtrip();

        parcelableArray = mBundle.getParcelableArray(KEY);
        assertEquals(2, parcelableArray.length);
        assertBundleEquals(bundle1, (Bundle) parcelableArray[0]);
        assertBundleEquals(bundle2, (Bundle) parcelableArray[1]);
    }

    // getParcelableArrayList should only return the parcelableArrayList set by putParcelableArrayList
    @Test
    public void testGetParcelableArrayList() {
        assertNull(mBundle.getParcelableArrayList(KEY));
        final ArrayList<Parcelable> parcelableArrayList = new ArrayList<Parcelable>();
        final Bundle bundle1 = new Bundle();
        final Bundle bundle2 = new Bundle();
        parcelableArrayList.add(bundle1);
        parcelableArrayList.add(bundle2);
        mBundle.putParcelableArrayList(KEY, parcelableArrayList);
        ArrayList<Parcelable> ret = mBundle.getParcelableArrayList(KEY);
        assertEquals(2, ret.size());
        assertTrue(bundle1.equals(ret.get(0)));
        assertTrue(bundle2.equals(ret.get(1)));
        roundtrip();
        ret = mBundle.getParcelableArrayList(KEY);
        assertEquals(2, ret.size());
        assertBundleEquals(bundle1, (Bundle) ret.get(0));
        assertBundleEquals(bundle2, (Bundle) ret.get(1));
    }

    @Test
    public void testGetSerializable() {
        assertNull(mBundle.getSerializable(KEY));
        mBundle.putSerializable(KEY, "android");
        assertEquals("android", mBundle.getSerializable(KEY));
        roundtrip();
        assertEquals("android", mBundle.getSerializable(KEY));
    }

    @Test
    public void testGetShort1() {
        final short s = 1007;

        assertEquals(0, mBundle.getShort(KEY));
        mBundle.putShort(KEY, s);
        assertEquals(s, mBundle.getShort(KEY));
        roundtrip();
        assertEquals(s, mBundle.getShort(KEY));
    }

    @Test
    public void testGetShort2() {
        final short s1 = 1006;
        final short s2 = 1007;

        assertEquals(s1, mBundle.getShort(KEY, s1));
        mBundle.putShort(KEY, s2);
        assertEquals(s2, mBundle.getShort(KEY, s1));
        roundtrip();
        assertEquals(s2, mBundle.getShort(KEY, s1));
    }

    @Test
    public void testGetShortArray() {
        final short s1 = 1006;
        final short s2 = 1007;

        assertNull(mBundle.getShortArray(KEY));
        mBundle.putShortArray(KEY, new short[] {
                s1, s2
        });
        short[] shortArray = mBundle.getShortArray(KEY);
        assertEquals(s1, shortArray[0]);
        assertEquals(s2, shortArray[1]);
        roundtrip();
        shortArray = mBundle.getShortArray(KEY);
        assertEquals(s1, shortArray[0]);
        assertEquals(s2, shortArray[1]);
    }

    // getSparseParcelableArray should only return the SparseArray<Parcelable>
    // set by putSparseParcelableArray
    @Test
    public void testGetSparseParcelableArray() {
        assertNull(mBundle.getSparseParcelableArray(KEY));
        final SparseArray<Parcelable> sparseArray = new SparseArray<Parcelable>();
        final Bundle bundle = new Bundle();
        final Intent intent = new Intent();
        sparseArray.put(1006, bundle);
        sparseArray.put(1007, intent);
        mBundle.putSparseParcelableArray(KEY, sparseArray);
        SparseArray<Parcelable> ret = mBundle.getSparseParcelableArray(KEY);
        assertEquals(2, ret.size());
        assertNull(ret.get(1008));
        assertTrue(bundle.equals(ret.get(1006)));
        assertTrue(intent.equals(ret.get(1007)));
        roundtrip();
        ret = mBundle.getSparseParcelableArray(KEY);
        assertEquals(2, ret.size());
        assertNull(ret.get(1008));
        assertBundleEquals(bundle, (Bundle) ret.get(1006));
        assertIntentEquals(intent, (Intent) ret.get(1007));
    }

    @Test
    public void testGetString() {
        assertNull(mBundle.getString(KEY));
        mBundle.putString(KEY, "android");
        assertEquals("android", mBundle.getString(KEY));
        roundtrip();
        assertEquals("android", mBundle.getString(KEY));
    }

    @Test
    public void testGetStringArray() {
        assertNull(mBundle.getStringArray(KEY));
        mBundle.putStringArray(KEY, new String[] {
                "one", "two", "three"
        });
        String[] ret = mBundle.getStringArray(KEY);
        assertEquals("one", ret[0]);
        assertEquals("two", ret[1]);
        assertEquals("three", ret[2]);
        roundtrip();
        ret = mBundle.getStringArray(KEY);
        assertEquals("one", ret[0]);
        assertEquals("two", ret[1]);
        assertEquals("three", ret[2]);
    }

    // getStringArrayList should only return the StringArrayList set by putStringArrayList
    @Test
    public void testGetStringArrayList() {
        assertNull(mBundle.getStringArrayList(KEY));
        final ArrayList<String> stringArrayList = new ArrayList<String>();
        stringArrayList.add("one");
        stringArrayList.add("two");
        stringArrayList.add("three");
        mBundle.putStringArrayList(KEY, stringArrayList);
        ArrayList<String> ret = mBundle.getStringArrayList(KEY);
        assertEquals(3, ret.size());
        assertEquals("one", ret.get(0));
        assertEquals("two", ret.get(1));
        assertEquals("three", ret.get(2));
        roundtrip();
        ret = mBundle.getStringArrayList(KEY);
        assertEquals(3, ret.size());
        assertEquals("one", ret.get(0));
        assertEquals("two", ret.get(1));
        assertEquals("three", ret.get(2));
    }

    @Test
    public void testKeySet() {
        Set<String> setKey = mBundle.keySet();
        assertFalse(setKey.contains("one"));
        assertFalse(setKey.contains("two"));
        mBundle.putBoolean("one", true);
        mBundle.putChar("two", 't');
        setKey = mBundle.keySet();
        assertEquals(2, setKey.size());
        assertTrue(setKey.contains("one"));
        assertTrue(setKey.contains("two"));
        assertFalse(setKey.contains("three"));
        roundtrip();
        setKey = mBundle.keySet();
        assertEquals(2, setKey.size());
        assertTrue(setKey.contains("one"));
        assertTrue(setKey.contains("two"));
        assertFalse(setKey.contains("three"));
    }

    // same as hasFileDescriptors, the only difference is that describeContents
    // return 0 if no fd and return 1 if has fd for the tested Bundle
    /* we don't support file descriptors
    @Test
    public void testDescribeContents() {
        assertTrue((mBundle.describeContents()
                & Parcelable.CONTENTS_FILE_DESCRIPTOR) == 0);

        final Parcel parcel = Parcel.obtain();
        try {
            mBundle.putParcelable("foo", ParcelFileDescriptor.open(
                    new File("/system"), ParcelFileDescriptor.MODE_READ_ONLY));
        } catch (FileNotFoundException e) {
            throw new RuntimeException("can't open /system", e);
        }
        assertTrue((mBundle.describeContents()
                & Parcelable.CONTENTS_FILE_DESCRIPTOR) != 0);
        mBundle.writeToParcel(parcel, 0);
        mBundle.clear();
        assertTrue((mBundle.describeContents()
                & Parcelable.CONTENTS_FILE_DESCRIPTOR) == 0);
        parcel.setDataPosition(0);
        mBundle.readFromParcel(parcel);
        assertTrue((mBundle.describeContents()
                & Parcelable.CONTENTS_FILE_DESCRIPTOR) != 0);
        ParcelFileDescriptor pfd = (ParcelFileDescriptor)mBundle.getParcelable("foo");
        assertTrue((mBundle.describeContents()
                & Parcelable.CONTENTS_FILE_DESCRIPTOR) != 0);
    }
    */

    // case 1: The default bundle doesn't has FileDescriptor.
    // case 2: The tested Bundle should has FileDescriptor
    //  if it read data from a Parcel object, which is created with a FileDescriptor.
    // case 3: The tested Bundle should has FileDescriptor
    //  if put a Parcelable object, which is created with a FileDescriptor, into it.
    /* we don't support file descriptors
    @Test
    public void testHasFileDescriptors() {
        assertFalse(mBundle.hasFileDescriptors());

        final Parcel parcel = Parcel.obtain();
        assertFalse(parcel.hasFileDescriptors());
        try {
            mBundle.putParcelable("foo", ParcelFileDescriptor.open(
                    new File("/system"), ParcelFileDescriptor.MODE_READ_ONLY));
        } catch (FileNotFoundException e) {
            throw new RuntimeException("can't open /system", e);
        }
        assertTrue(mBundle.hasFileDescriptors());
        mBundle.writeToParcel(parcel, 0);
        assertTrue(parcel.hasFileDescriptors());
        mBundle.clear();
        assertFalse(mBundle.hasFileDescriptors());
        parcel.setDataPosition(0);
        mBundle.readFromParcel(parcel);
        assertTrue(mBundle.hasFileDescriptors());
        ParcelFileDescriptor pfd = (ParcelFileDescriptor)mBundle.getParcelable("foo");
        assertTrue(mBundle.hasFileDescriptors());
    }
    */

    @Test
    public void testSetClassLoader() {
        mBundle.setClassLoader(new MockClassLoader());
    }

    // Write the bundle(A) to a parcel(B), and then create a bundle(C) from B.
    // C should be same as A.
    @Test
    public void testWriteToParcel() {
        final String li = "Bruce Li";

        mBundle.putString(KEY, li);
        final Parcel parcel = Parcel.obtain();
        mBundle.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        final Bundle bundle = ShadowBundle.CREATOR.createFromParcel(parcel);
        assertEquals(li, bundle.getString(KEY));
    }

    // test the size should be right after add/remove key-value pair of the Bundle.
    @Test
    public void testSize() {
        assertEquals(0, mBundle.size());
        mBundle.putBoolean("one", true);
        assertEquals(1, mBundle.size());

        mBundle.putBoolean("two", true);
        assertEquals(2, mBundle.size());

        mBundle.putBoolean("three", true);
        assertEquals(3, mBundle.size());

        mBundle.putBoolean("four", true);
        mBundle.putBoolean("five", true);
        assertEquals(5, mBundle.size());
        mBundle.remove("six");
        assertEquals(5, mBundle.size());

        mBundle.remove("one");
        assertEquals(4, mBundle.size());
        mBundle.remove("one");
        assertEquals(4, mBundle.size());

        mBundle.remove("two");
        assertEquals(3, mBundle.size());

        mBundle.remove("three");
        mBundle.remove("four");
        mBundle.remove("five");
        assertEquals(0, mBundle.size());
    }

    // The return value of toString() should not be null.
    @Test
    public void testToString() {
        assertNotNull(mBundle.toString());
        mBundle.putString("foo", "this test is so stupid");
        assertNotNull(mBundle.toString());
    }

    // The tested Bundle should hold mappings from the given after putAll be invoked.
    @Test
    public void testPutAll() {
        assertEquals(0, mBundle.size());

        final Bundle map = new Bundle();
        map.putBoolean(KEY, true);
        assertEquals(1, map.size());
        mBundle.putAll(map);
        assertEquals(1, mBundle.size());
    }

    private void roundtrip() {
        Parcel out = Parcel.obtain();
        mBundle.writeToParcel(out, 0);
        Parcel in = roundtripParcel(out);
        mBundle = in.readBundle();
    }

    private Parcel roundtripParcel(Parcel out) {
        byte[] buf = out.marshall();
        Parcel in = Parcel.obtain();
        in.unmarshall(buf, 0, buf.length);
        in.setDataPosition(0);
        return in;
    }

    private void assertBundleEquals(Bundle expected, Bundle observed) {
        assertEquals(expected.size(), observed.size());
        for (String key : expected.keySet()) {
            assertEquals(expected.get(key), observed.get(key));
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    private void assertIntentEquals(Intent expected, Intent observed) {
        assertEquals(expected.toUri(0), observed.toUri(0));
    }

    @SuppressWarnings({"UnusedDeclaration"})
    private void assertSpannableEquals(Spannable expected, CharSequence observed) {
        Spannable s = (Spannable) observed;
        assertEquals(expected.toString(), observed.toString());
        Object[] expectedSpans = expected.getSpans(0, expected.length(), Object.class);
        Object[] observedSpans = expected.getSpans(0, expected.length(), Object.class);
        assertEquals(expectedSpans.length, observedSpans.length);
        for (int i = 0; i < expectedSpans.length; i++) {
            // Can't compare values of arbitrary objects
            assertEquals(expectedSpans[i].getClass(), observedSpans[i].getClass());
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    class MockClassLoader extends ClassLoader {
        MockClassLoader() {
            super();
        }

        MockClassLoader(ClassLoader parent) {
            super(parent);
        }
    }
}
