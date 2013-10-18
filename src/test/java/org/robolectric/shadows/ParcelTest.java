package org.robolectric.shadows;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import android.accounts.Account;
import android.os.Bundle;
import android.os.Parcel;

@RunWith(TestRunners.WithDefaults.class)
public class ParcelTest {

  private Parcel parcel;

  @Before
  public void setup() {
    parcel = Parcel.obtain();
  }

  @After
  public void tearDown() {
    parcel.recycle();
  }

  @Test
  public void testObtain() {
    assertThat(parcel).isNotNull();
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
    final int[] ints = { 1, 2 };
    parcel.writeIntArray(ints);
    parcel.setDataPosition(0);
    final int[] ints2 = new int[ints.length];
    parcel.readIntArray(ints2);
    assertTrue(Arrays.equals(ints, ints2));
  }

  @Test
  public void testReadWriteLongArray() throws Exception {
    final long[] longs = { 1, 2 };
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
    final float[] floats = { 1.1f, 2.0f };
    parcel.writeFloatArray(floats);
    parcel.setDataPosition(0);
    final float[] floats2 = new float[floats.length];
    parcel.readFloatArray(floats2);
    assertTrue(Arrays.equals(floats, floats2));
  }

  @Test
  public void testReadWriteDoubleArray() throws Exception {
    final double[] doubles = { 1.1f, 2.0f };
    parcel.writeDoubleArray(doubles);
    parcel.setDataPosition(0);
    final double[] doubles2 = new double[doubles.length];
    parcel.readDoubleArray(doubles2);
    assertTrue(Arrays.equals(doubles, doubles2));
  }

  @Test
  public void testReadWriteStringArray() throws Exception {
    final String[] strings = { "foo", "bar" };
    parcel.writeStringArray(strings);
    parcel.setDataPosition(0);
    final String[] strings2 = new String[strings.length];
    parcel.readStringArray(strings2);
    assertTrue(Arrays.equals(strings, strings2));
  }

  @Test
  public void testWriteAndCreateByteArray() {
    byte[] bytes = new byte[] { -1, 2, 3, 127 };
    parcel.writeByteArray(bytes);
    parcel.setDataPosition(0);
    byte[] actualBytes = parcel.createByteArray();
    assertTrue(Arrays.equals(bytes, actualBytes));
  }

  @Test
  public void testWriteAndCreateByteArray_lengthZero() {
    byte[] bytes = new byte[] {};
    parcel.writeByteArray(bytes);
    parcel.setDataPosition(0);
    byte[] actualBytes = parcel.createByteArray();
    assertTrue(Arrays.equals(bytes, actualBytes));
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

  @Test(expected = ClassCastException.class)
  public void testWriteStringReadInt() {
    String val = "test";
    parcel.writeString(val);
    parcel.setDataPosition(0);
    parcel.readInt();
  }

  @Test(expected = ClassCastException.class)
  public void testWriteIntReadString() {
    int val = 9;
    parcel.writeInt(val);
    parcel.setDataPosition(0);
    parcel.readString();
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

  @Test(expected = ClassCastException.class)
  public void testWriteStringReadLong() {
    String val = "test";
    parcel.writeString(val);
    parcel.setDataPosition(0);
    parcel.readLong();
  }

  @Test(expected = ClassCastException.class)
  public void testWriteLongReadString() {
    long val = 9;
    parcel.writeLong(val);
    parcel.setDataPosition(0);
    parcel.readString();
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

    parcel.setDataPosition(0);
    parcel.writeBundle(b1);
    parcel.setDataPosition(0);
    b2 = parcel.readBundle(null /* ClassLoader */);
    assertEquals("world", b2.getString("hello"));
  }

  @Test
  public void testCreateStringArrayList() throws Exception {
    parcel.writeStringList(Arrays.asList("str1", "str2"));
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
    ArrayList<TestParcelable> rehydrated = parcel
        .createTypedArrayList(TestParcelable.CREATOR);

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
    String[] strs = { "a1", "b2" };
    parcel.writeStringArray(strs);
    parcel.setDataPosition(0);
    String[] newStrs = parcel.createStringArray();
    assertTrue(Arrays.equals(strs, newStrs));
  }

  @Test
  public void testDataPositionAfterSomeWrites() {
    parcel.writeInt(1);
    assertThat(parcel.dataPosition()).isEqualTo(4);

    parcel.writeFloat(5);
    assertThat(parcel.dataPosition()).isEqualTo(8);

    parcel.writeDouble(37);
    assertThat(parcel.dataPosition()).isEqualTo(16);
  }

  @Test
  public void testDataPositionAfterSomeReads() {
    parcel.writeInt(1);
    parcel.writeFloat(5);
    parcel.writeDouble(37);
    parcel.setDataPosition(0);

    parcel.readInt();
    assertThat(parcel.dataPosition()).isEqualTo(4);

    parcel.readFloat();
    assertThat(parcel.dataPosition()).isEqualTo(8);

    parcel.readDouble();
    assertThat(parcel.dataPosition()).isEqualTo(16);
  }

  @Test
  public void testDataSizeAfterSomeWrites() {
    parcel.writeInt(1);
    assertThat(parcel.dataSize()).isEqualTo(4);

    parcel.writeFloat(5);
    assertThat(parcel.dataSize()).isEqualTo(8);

    parcel.writeDouble(37);
    assertThat(parcel.dataSize()).isEqualTo(16);
  }

  @Test
  public void testDataAvail() {
    parcel.writeInt(1);
    parcel.writeFloat(5);
    parcel.writeDouble(6);
    parcel.setDataPosition(4);

    assertThat(parcel.dataAvail()).isEqualTo(12);
  }

  @Test
  public void testSetDataPositionIntoMiddleOfParcel() {
    parcel.writeInt(1);
    parcel.writeFloat(5);
    parcel.writeDouble(6);
    parcel.setDataPosition(4);

    assertThat(parcel.readFloat()).isEqualTo(5);
  }

  @Test
  public void testAppendFrom() {
    parcel.writeInt(1);
    parcel.writeInt(2);
    parcel.writeInt(3);
    parcel.writeInt(4);

    Parcel parcel2 = Parcel.obtain();
    parcel2.appendFrom(parcel, 4, 8);
    parcel2.setDataPosition(0);

    assertThat(parcel2.readInt()).isEqualTo(2);
    assertThat(parcel2.readInt()).isEqualTo(3);
    assertThat(parcel2.dataSize()).isEqualTo(8);
  }

  @Test
  public void testMarshallAndUnmarshall() {
    parcel.writeInt(1);
    parcel.writeString("hello");
    parcel.writeDouble(25);
    parcel.writeFloat(1.25f);
    parcel.writeByte((byte) 0xAF);

    byte[] rawBytes = parcel.marshall();
    Parcel parcel2 = Parcel.obtain();
    parcel2.unmarshall(rawBytes, 0, rawBytes.length);

    assertThat(parcel2.readInt()).isEqualTo(1);
    assertThat(parcel2.readString()).isEqualTo("hello");
    assertThat(parcel2.readDouble()).isEqualTo(25.0);
    assertThat(parcel2.readFloat()).isEqualTo(1.25f);
    assertThat(parcel2.readByte()).isEqualTo((byte) 0xAF);
  }
}
