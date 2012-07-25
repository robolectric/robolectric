package com.xtremelabs.robolectric.shadows;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.ParcelFormatException;
import android.os.Parcelable;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

@RunWith(WithTestDefaultsRunner.class)
public class ParcelTest {

    private Parcel parcel;
    private ShadowParcel shadowParcel;

    @Before
    public void setup() {
        parcel = Parcel.obtain();
        shadowParcel = Robolectric.shadowOf(parcel);
    }

    @Test
    public void testObtain() {
        assertThat(parcel, notNullValue());
    }

    @Test(expected = ParcelFormatException.class)
    public void testReadIntWhenEmpty() {
        parcel.readInt();
    }

    @Test(expected = ParcelFormatException.class)
    public void testReadLongWhenEmpty() {
        parcel.readLong();
    }

    @Test(expected = ParcelFormatException.class)
    public void testReadStringWhenEmpty() {
        parcel.readString();
    }

    @Test
    public void testReadWriteSingleString() {
        String val = "test";
        parcel.writeString(val);
        parcel.setDataPosition(0);
        assertThat(parcel.readString(), equalTo(val));
    }

    @Test
    public void testWriteNullString() {
        parcel.writeString(null);
        parcel.setDataPosition(0);
        assertThat(parcel.readString(), nullValue());
//        assertThat(shadowParcel.getIndex(), equalTo(0));
//        assertThat(shadowParcel.getParcelData().size(), equalTo(0));
    }

    @Test
    public void testReadWriteMultipleStrings() {
        for (int i = 0; i < 10; ++i) {
            parcel.writeString(Integer.toString(i));
        }
        parcel.setDataPosition(0);
        for (int i = 0; i < 10; ++i) {
            assertThat(parcel.readString(), equalTo(Integer.toString(i)));
        }
    }

    @Test
    public void testReadWriteSingleInt() {
        int val = 5;
        parcel.writeInt(val);
        parcel.setDataPosition(0);
        assertThat(parcel.readInt(), equalTo(val));
    }

    @Test
    public void testReadWriteIntArray() throws Exception {
        final int[] ints = {1, 2};
        parcel.writeIntArray(ints);
        parcel.setDataPosition(0);
        final int[] ints2 = new int[ints.length];
        parcel.readIntArray(ints2);
        assertTrue(Arrays.equals(ints, ints2));
    }

    @Test
    public void testReadWriteLongArray() throws Exception {
        final long[] longs = {1, 2};
        parcel.writeLongArray(longs);
        parcel.setDataPosition(0);
        final long[] longs2 = new long[longs.length];
        parcel.readLongArray(longs2);
        assertTrue(Arrays.equals(longs, longs2));
    }

    @Test
    public void testReadWriteSingleFloat() {
        float val = 5.2f;
        parcel.writeFloat(val);
        parcel.setDataPosition(0);
        assertThat(parcel.readFloat(), equalTo(val));
    }

    @Test
    public void testReadWriteFloatArray() throws Exception {
        final float[] floats = {1.1f, 2.0f};
        parcel.writeFloatArray(floats);
        parcel.setDataPosition(0);
        final float[] floats2 = new float[floats.length];
        parcel.readFloatArray(floats2);
        assertTrue(Arrays.equals(floats, floats2));
    }

    @Test
    public void testReadWriteDoubleArray() throws Exception {
        final double[] doubles = {1.1f, 2.0f};
        parcel.writeDoubleArray(doubles);
        parcel.setDataPosition(0);
        final double[] doubles2 = new double[doubles.length];
        parcel.readDoubleArray(doubles2);
        assertTrue(Arrays.equals(doubles, doubles2));
    }

    @Test
    public void testReadWriteStringArray() throws Exception {
        final String[] strings = {"foo", "bar"};
        parcel.writeStringArray(strings);
        parcel.setDataPosition(0);
        final String[] strings2 = new String[strings.length];
        parcel.readStringArray(strings2);
        assertTrue(Arrays.equals(strings, strings2));
    }

    @Test
    public void testReadWriteMultipleInts() {
        for (int i = 0; i < 10; ++i) {
            parcel.writeInt(i);
        }
        parcel.setDataPosition(0);
        for (int i = 0; i < 10; ++i) {
            assertThat(parcel.readInt(), equalTo(i));
        }
    }

    @Test
    public void testReadWriteSingleByte() {
        byte val = 1;
        parcel.writeByte(val);
        parcel.setDataPosition(0);
        assertThat(parcel.readByte(), equalTo(val));
    }

    @Test
    public void testReadWriteMultipleBytes() {
        for (byte i = Byte.MIN_VALUE; i < Byte.MAX_VALUE; ++i) {
            parcel.writeByte(i);
        }
        parcel.setDataPosition(0);
        for (byte i = Byte.MIN_VALUE; i < Byte.MAX_VALUE; ++i) {
            assertThat(parcel.readByte(), equalTo(i));
        }
    }


    @Test
    public void testReadWriteStringInt() {
        for (int i = 0; i < 10; ++i) {
            parcel.writeString(Integer.toString(i));
            parcel.writeInt(i);
        }
        parcel.setDataPosition(0);
        for (int i = 0; i < 10; ++i) {
            assertThat(parcel.readString(), equalTo(Integer.toString(i)));
            assertThat(parcel.readInt(), equalTo(i));
        }
    }

    @Test
    public void testReadWriteSingleLong() {
        long val = 5;
        parcel.writeLong(val);
        parcel.setDataPosition(0);
        assertThat(parcel.readLong(), equalTo(val));
    }

    @Test
    public void testReadWriteMultipleLongs() {
        for (long i = 0; i < 10; ++i) {
            parcel.writeLong(i);
        }
        parcel.setDataPosition(0);
        for (long i = 0; i < 10; ++i) {
            assertThat(parcel.readLong(), equalTo(i));
        }
    }

    @Test
    public void testReadWriteStringLong() {
        for (long i = 0; i < 10; ++i) {
            parcel.writeString(Long.toString(i));
            parcel.writeLong(i);
        }
        parcel.setDataPosition(0);
        for (long i = 0; i < 10; ++i) {
            assertThat(parcel.readString(), equalTo(Long.toString(i)));
            assertThat(parcel.readLong(), equalTo(i));
        }
    }

    @Test
    public void testReadWriteParcelable() {
        TestParcelable test = new TestParcelable();
        parcel.writeParcelable(test, 0);
        parcel.setDataPosition(0);

        TestParcelable test2 = parcel.readParcelable(TestParcelable.class.getClassLoader());
    }


    @Test
    public void testReadWriteBundle() {
        Bundle b1 = new Bundle();
        b1.putString("hello", "world");
        parcel.writeBundle(b1);
        parcel.setDataPosition(0);
        Bundle b2 = parcel.readBundle();

        assertEquals("world", b2.getString("hello"));
        assertEquals(b1.size(), b2.size());

        parcel.writeBundle(b1);
        parcel.setDataPosition(0);
        b2 = parcel.readBundle(null /* ClassLoader */);
        assertEquals(b1.size(), b2.size());
        assertEquals("world", b2.getString("hello"));
    }

    static class TestParcelable implements Parcelable {

        public static final Parcelable.Creator<TestParcelable> CREATOR
                = new Parcelable.Creator<TestParcelable>() {
            public TestParcelable createFromParcel(Parcel in) {
                return new TestParcelable(in);
            }

            public TestParcelable[] newArray(int size) {
                return new TestParcelable[size];
            }
        };

        private TestParcelable(Parcel in) {
            in.readInt();
            in.readString();
        }

        public TestParcelable() {

        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeInt(4);
            parcel.writeString("hi");
        }


    }
}
