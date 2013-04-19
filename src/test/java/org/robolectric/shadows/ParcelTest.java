package org.robolectric.shadows;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import android.accounts.Account;
import android.os.Binder;
import android.os.Bundle;
import android.os.Parcel;

@RunWith(TestRunners.WithDefaults.class)
public class ParcelTest {

    private Parcel parcel;

    @Before
    public void setup() {
        parcel = Parcel.obtain();
    }

    @Test
    public void testObtain() {
        assertThat(parcel).isNotNull();
        assertThat(parcel.dataPosition()).isEqualTo(0);
        assertThat(parcel.dataSize()).isEqualTo(0);
    }

    @Test
    public void testReadIntWhenEmpty() {
        assertThat(parcel.readInt()).isEqualTo(0);
    }

    @Test
    public void testReadLongWhenEmpty() {
        assertThat(parcel.readLong()).isEqualTo(0l);
    }

    @Test
    public void testReadStringWhenEmpty() {
        assertThat(parcel.readString()).isNull();
    }

    @Test
    public void testReadStrongBinderWhenEmpty() {
        assertThat(parcel.readStrongBinder()).isNull();
    }

    @Test
    public void testReadWriteSingleString() {
        String val = "test";
        parcel.writeString(val);
        parcel.setDataPosition(0);
        assertThat(parcel.readString()).isEqualTo(val);
    }

    @Test
    public void testWriteNullString() {
        parcel.writeString(null);
        parcel.setDataPosition(0);
        assertThat(parcel.readString()).isNull();
    }

    @Test
    public void testReadWriteMultipleStrings() {
        for (int i = 0; i < 10; ++i) {
            parcel.writeString(Integer.toString(i));
        }
        parcel.setDataPosition(0);
        for (int i = 0; i < 10; ++i) {
            assertThat(parcel.readString()).isEqualTo(Integer.toString(i));
        }
        // now try to read past the number of items written and see what happens
        assertThat(parcel.readString()).isNull();
    }

    @Test
    public void testReadWriteSingleInt() {
        int val = 5;
        parcel.writeInt(val);
        parcel.setDataPosition(0);
        assertThat(parcel.readInt()).isEqualTo(val);
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
        assertThat(parcel.readFloat()).isEqualTo(val);
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
            assertThat(parcel.readInt()).isEqualTo(i);
        }
        // now try to read past the number of items written and see what happens
        assertThat(parcel.readInt()).isEqualTo(0);
    }

    @Test
    public void testReadWriteSingleByte() {
        byte val = 1;
        parcel.writeByte(val);
        parcel.setDataPosition(0);
        assertThat(parcel.readByte()).isEqualTo(val);
    }

    @Test
    public void testReadWriteMultipleBytes() {
        for (byte i = Byte.MIN_VALUE; i < Byte.MAX_VALUE; ++i) {
            parcel.writeByte(i);
        }
        parcel.setDataPosition(0);
        for (byte i = Byte.MIN_VALUE; i < Byte.MAX_VALUE; ++i) {
            assertThat(parcel.readByte()).isEqualTo(i);
        }
        // now try to read past the number of items written and see what happens
        assertThat(parcel.readByte()).isEqualTo((byte) 0);
    }


    @Test
    public void testReadWriteStringInt() {
        for (int i = 0; i < 10; ++i) {
            parcel.writeString(Integer.toString(i));
            parcel.writeInt(i);
        }
        parcel.setDataPosition(0);
        for (int i = 0; i < 10; ++i) {
            assertThat(parcel.readString()).isEqualTo(Integer.toString(i));
            assertThat(parcel.readInt()).isEqualTo(i);
        }
        // now try to read past the number of items written and see what happens
        assertThat(parcel.readString()).isNull();
        assertThat(parcel.readInt()).isEqualTo(0);
    }

    @Test
    public void testReadWriteSingleLong() {
        long val = 5;
        parcel.writeLong(val);
        parcel.setDataPosition(0);
        assertThat(parcel.readLong()).isEqualTo(val);
    }

    @Test
    public void testReadWriteMultipleLongs() {
        for (long i = 0; i < 10; ++i) {
            parcel.writeLong(i);
        }
        parcel.setDataPosition(0);
        for (long i = 0; i < 10; ++i) {
            assertThat(parcel.readLong()).isEqualTo(i);
        }
        // now try to read past the number of items written and see what happens
        assertThat(parcel.readLong()).isEqualTo(0l);
    }

    @Test
    public void testReadWriteStringLong() {
        for (long i = 0; i < 10; ++i) {
            parcel.writeString(Long.toString(i));
            parcel.writeLong(i);
        }
        parcel.setDataPosition(0);
        for (long i = 0; i < 10; ++i) {
            assertThat(parcel.readString()).isEqualTo(Long.toString(i));
            assertThat(parcel.readLong()).isEqualTo(i);
        }
        // now try to read past the number of items written and see what happens
        assertThat(parcel.readString()).isNull();
        assertThat(parcel.readLong()).isEqualTo(0l);
    }

    @Test
    public void testReadWriteParcelable() {
        Account a1 = new Account("name", "type");
        parcel.writeParcelable(a1, 0);
        parcel.setDataPosition(0);

        Account a2 = parcel.readParcelable(Account.class.getClassLoader());
        assertEquals(a1, a2);
    }

    @Test
    public void testReadWriteBundle() {
        Bundle b1 = new Bundle();
        b1.putString("hello", "world");

        parcel.writeBundle(b1);
        parcel.setDataPosition(0);
        Bundle b2 = parcel.readBundle();
        assertEquals("world", b2.getString("hello"));

        parcel.writeBundle(b1);
        parcel.setDataPosition(0);
        b2 = parcel.readBundle(null /* ClassLoader */);
        assertEquals("world", b2.getString("hello"));
    }

    @Test
    public void testCreateStringArrayList() throws Exception {
        parcel.writeInt(2);
        parcel.writeString("str1");
        parcel.writeString("str2");
        parcel.setDataPosition(0);
        List<String> actual = parcel.createStringArrayList();
        assertEquals(2, actual.size());
        assertEquals("str1", actual.get(0));
        assertEquals("str2", actual.get(1));
    }

    @Test
    public void testWriteTypedListAndCreateTypedArrayList() throws Exception {
        TestParcelable normal = new TestParcelable(23);
        ArrayList<TestParcelable> normals = new ArrayList<TestParcelable>();
        normals.add(normal);

        parcel.writeTypedList(normals);
        parcel.setDataPosition(0);
        ArrayList<TestParcelable> rehydrated = parcel.createTypedArrayList(TestParcelable.CREATOR);

        assertEquals(1, rehydrated.size());
        assertEquals(23, rehydrated.get(0).contents);
    }

    @Test
    public void testReadAndWriteStringList() throws Exception {
        ArrayList<String> original = new ArrayList<String>();
        ArrayList<String> rehydrated = new ArrayList<String>();
        original.add("str1");
        original.add("str2");
        parcel.writeStringList(original);
        parcel.setDataPosition(0);
        parcel.readStringList(rehydrated);
        assertEquals(2, rehydrated.size());
        assertEquals("str1", rehydrated.get(0));
        assertEquals("str2", rehydrated.get(1));
    }

    @Test
    public void testReadWriteStrongBinder() throws Exception {
        Binder expected = new Binder();
        parcel.writeStrongBinder(expected);
        parcel.setDataPosition(0);
        assertEquals(expected, parcel.readStrongBinder());
    }

    @Test
    public void testReadWriteMap() throws Exception {
        HashMap<String, String> original = new HashMap<String, String>();
        original.put("key", "value");
        parcel.writeMap(original);
        parcel.setDataPosition(0);
        HashMap<String, String> rehydrated = parcel.readHashMap(null);

        assertEquals("value", rehydrated.get("key"));
    }

    @Test
    public void testCreateStringArray() {
        String[] strs = {
                "a1",
                "b2"
        };
        parcel.writeStringArray(strs);
        parcel.setDataPosition(0);
        String[] newStrs = parcel.createStringArray();
        assertTrue(Arrays.equals(strs, newStrs));
    }
    
    @Test
    public void testReadWriteSimpleBundle() {
        Bundle b1 = new Bundle();
        b1.putString("hello", "world");
        parcel.writeBundle(b1);
        parcel.setDataPosition(0);
        Bundle b2 = parcel.readBundle();

        assertThat(b2).isEqualTo(b1);
        assertThat(b2.getString("hello")).isEqualTo("world");

        parcel.setDataPosition(0);
        parcel.writeBundle(b1);
        parcel.setDataPosition(0);
        b2 = parcel.readBundle(null /* ClassLoader */);
        assertThat(b2).isEqualTo(b1);
        assertThat(b2.getString("hello")).isEqualTo("world");
    }
    
    @Test
    public void testReadWriteNestedBundles() {
        Bundle innerBundle = new Bundle();
        innerBundle.putString("hello", "world");
        Bundle b1 = new Bundle();
        b1.putBundle("bundle", innerBundle);
        b1.putInt("int", 23);
        parcel.writeBundle(b1);
        parcel.setDataPosition(0);
        Bundle b2 = parcel.readBundle();

        assertThat(b2).isEqualTo(b1);
        assertThat(b2.getBundle("bundle")).isEqualTo(innerBundle);
        assertThat(b2.getInt("int")).isEqualTo(23);
        assertThat(b2.getBundle("bundle").getString("hello")).isEqualTo("world");

        parcel.setDataPosition(0);
        parcel.writeBundle(b1);
        parcel.setDataPosition(0);
        b2 = parcel.readBundle(null /* ClassLoader */);
        assertThat(b2).isEqualTo(b1);
        assertThat(b2.getBundle("bundle")).isEqualTo(innerBundle);
        assertThat(b2.getInt("int")).isEqualTo(23);
        assertThat(b2.getBundle("bundle").getString("hello")).isEqualTo("world");
    }
    
    @Test
    public void testReadWriteBundleWithDifferentValueTypes() {
        Bundle b1 = new Bundle();
        b1.putString("hello", "world");
        b1.putBoolean("boolean", true);
        b1.putByte("byte", (byte) 0xAA);
        b1.putShort("short", (short) 0xBABE);
        b1.putInt("int", 1);
        b1.putFloat("float", 0.5f);
        b1.putDouble("double", 1.25);
        parcel.writeBundle(b1);
        parcel.setDataPosition(0);
        
        Bundle b2 = parcel.readBundle();
        assertThat(b2).isEqualTo(b1);
        assertThat(b2.getString("hello")).isEqualTo("world");
        assertThat(b2.getBoolean("boolean")).isEqualTo(true);
        assertThat(b2.getByte("byte")).isEqualTo((byte) 0xAA);
        assertThat(b2.getShort("short")).isEqualTo((short) 0xBABE);
        assertThat(b2.getInt("int")).isEqualTo(1);
        assertThat(b2.getFloat("float")).isEqualTo(0.5F);
        assertThat(b2.getDouble("double")).isEqualTo(1.25);

        parcel.setDataPosition(0);
        parcel.writeBundle(b1);
        parcel.setDataPosition(0);
        b2 = parcel.readBundle(null /* ClassLoader */);
        assertThat(b2).isEqualTo(b1);
        assertThat(b2.getString("hello")).isEqualTo("world");
        assertThat(b2.getBoolean("boolean")).isEqualTo(true);
        assertThat(b2.getByte("byte")).isEqualTo((byte) 0xAA);
        assertThat(b2.getShort("short")).isEqualTo((short) 0xBABE);
        assertThat(b2.getInt("int")).isEqualTo(1);
        assertThat(b2.getFloat("float")).isEqualTo(0.5F);
        assertThat(b2.getDouble("double")).isEqualTo(1.25);    
    }

    @Test
    public void testWriteCreateStringArray() {
      final String[] strings = { "foo", "bar" };
      parcel.writeStringArray(strings);
      parcel.setDataPosition(0);
      final String[] strings2 = parcel.createStringArray();
      assertTrue(Arrays.equals(strings, strings2));
    }

    @Test
    public void testReadWriteStringList() {
        final List<String> strings = Arrays.asList( "foo", "bar" );
        parcel.writeStringList(strings);
        parcel.setDataPosition(0);
        List<String> extractedStrings = new ArrayList<String>();
        parcel.readStringList(extractedStrings);
        assertThat(extractedStrings).isEqualTo(strings);
    }

    @Test
    public void testWriteCreateStringArrayList() {
        final List<String> strings = Arrays.asList( "foo", "bar" );
        parcel.writeStringList(strings);
        parcel.setDataPosition(0);
        List<String> extractedStrings = parcel.createStringArrayList();
        assertThat(extractedStrings).isEqualTo(strings);
    }

    @Test
    public void testReadWriteByteArray() throws Exception {
        final byte[] bytes = {1, 2};
        parcel.writeByteArray(bytes);
        parcel.setDataPosition(0);
        final byte[] bytes2 = new byte[bytes.length];
        parcel.readByteArray(bytes2);
        assertTrue(Arrays.equals(bytes, bytes2));
    }

    @Test
    public void testReadWriteBooleanArray() {
        final boolean[] booleans = {false, true, true};
        parcel.writeBooleanArray(booleans);
        parcel.setDataPosition(0);
        final boolean[] booleans2 = new boolean[booleans.length];
        parcel.readBooleanArray(booleans2);
        assertTrue(Arrays.equals(booleans, booleans2));
    }

    @Test
    public void testReadWriteCharArray() {
        final char[] chars = {'a', 'b', 'c'};
        parcel.writeCharArray(chars);
        parcel.setDataPosition(0);
        final char[] chars2 = new char[chars.length];
        parcel.readCharArray(chars2);
        assertTrue(Arrays.equals(chars, chars2));
    }

    @Test
    public void testWriteCreateBooleanArray() {
        final boolean[] booleans = {false, true, true};
        parcel.writeBooleanArray(booleans);
        parcel.setDataPosition(0);
        final boolean[] booleans2 = parcel.createBooleanArray();
        assertTrue(Arrays.equals(booleans, booleans2));
    }

    @Test
    public void testWriteCreateByteArray() {
        final byte[] bytes = {1, 2};
        parcel.writeByteArray(bytes);
        parcel.setDataPosition(0);
        final byte[] bytes2 = parcel.createByteArray();
        assertTrue(Arrays.equals(bytes, bytes2));
    }

    @Test
    public void testWriteCreateCharArray() {
        final char[] chars = {'a', 'b', 'c'};
        parcel.writeCharArray(chars);
        parcel.setDataPosition(0);
        final char[] chars2 = parcel.createCharArray();
        assertTrue(Arrays.equals(chars, chars2));
    }

    @Test
    public void testWriteCreateIntArray() {
        final int[] ints = {1, 2};
        parcel.writeIntArray(ints);
        parcel.setDataPosition(0);
        final int[] ints2 = parcel.createIntArray();
        assertTrue(Arrays.equals(ints, ints2));
    }

    @Test
    public void testWriteCreateLongArray() {
        final long[] longs = {1, 2};
        parcel.writeLongArray(longs);
        parcel.setDataPosition(0);
        final long[] longs2 = parcel.createLongArray();
        assertTrue(Arrays.equals(longs, longs2));
    }

    @Test
    public void testWriteCreateFloatArray() {
        final float[] floats = { 1.5f, 2.25f };
        parcel.writeFloatArray(floats);
        parcel.setDataPosition(0);
        final float[] floats2 = parcel.createFloatArray();
        assertTrue(Arrays.equals(floats, floats2));
    }

    @Test
    public void testWriteCreateDoubleArray() {
        final double[] doubles = { 1.2, 2.2 };
        parcel.writeDoubleArray(doubles);
        parcel.setDataPosition(0);
        final double[] doubles2 = parcel.createDoubleArray();
        assertTrue(Arrays.equals(doubles, doubles2));
    }

    @Test
    public void testDataPositionAfterStringWrite() {
        parcel.writeString("string");
        assertThat(parcel.dataPosition()).isEqualTo(10);
    }

    @Test
    public void testDataPositionAfterByteWrite() {
        parcel.writeByte((byte) 0);
        assertThat(parcel.dataPosition()).isEqualTo(1);
    }

    @Test
    public void testDataPositionAfterIntWrite() {
        parcel.writeInt(1);
        assertThat(parcel.dataPosition()).isEqualTo(4);
    }

    @Test
    public void testDataPositionAfterLongWrite() {
        parcel.writeLong(23);
        assertThat(parcel.dataPosition()).isEqualTo(8);
    }

    @Test
    public void testDataPositionAfterFloatWrite() {
        parcel.writeFloat(0.5f);
        assertThat(parcel.dataPosition()).isEqualTo(4);
    }

    @Test
    public void testDataPositionAfterDoubleWrite() {
        parcel.writeDouble(8.8);
        assertThat(parcel.dataPosition()).isEqualTo(8);
    }

    @Test
    public void testResetDataPositionAfterWrite() {
        parcel.writeInt(4);
        parcel.setDataPosition(0);
        assertThat(parcel.dataPosition()).isEqualTo(0);
    }

    @Test
    public void testOverwritePreviousValue() {
        parcel.writeInt(4);
        parcel.setDataPosition(0);
        parcel.writeInt(34);
        parcel.setDataPosition(0);
        assertThat(parcel.readInt()).isEqualTo(34);
        assertThat(parcel.dataSize()).isEqualTo(4);
    }

    @Test
    public void testAppendFromUsingCompleteParcelCopy() {
        Parcel data = Parcel.obtain();
        data.writeInt(1);
        data.writeInt(5);
        data.setDataPosition(0);

        parcel.appendFrom(data, 0, data.dataSize());
        parcel.setDataPosition(0);

        assertThat(parcel.readInt()).isEqualTo(1);
        assertThat(parcel.readInt()).isEqualTo(5);
        assertThat(parcel.dataPosition()).isEqualTo(parcel.dataSize());
    }

    @Test
    public void testAppendFromMiddleOfParcel() {
        Parcel data = Parcel.obtain();
        data.writeInt(1);
        data.writeInt(2);
        data.writeInt(3);

        // Copy 2nd integer
        parcel.appendFrom(data, 4, 4);
        parcel.setDataPosition(0);

        assertThat(parcel.readInt()).isEqualTo(2);
        assertThat(parcel.dataSize()).isEqualTo(4);
    }

    @Test
    public void testAppendFromPastEndOfParcel() {
        try {
            Parcel data = Parcel.obtain();
            data.writeInt(1);

            parcel.appendFrom(data, 0, 20);
            Assert.fail();
        } catch (IllegalArgumentException expected) {
        }
    }
}
