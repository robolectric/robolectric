package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.accounts.Account;
import android.content.Intent;
import android.os.BadParcelableException;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowParcel.UnreliableBehaviorError;
import org.robolectric.util.ReflectionHelpers;

@RunWith(AndroidJUnit4.class)
public class ShadowParcelTest {

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
    assertThat(parcel.dataPosition()).isEqualTo(0);
    assertInvariants();
  }

  @Test
  public void testReadLongWhenEmpty() {
    assertThat(parcel.readLong()).isEqualTo(0);
    assertThat(parcel.dataPosition()).isEqualTo(0);
    assertInvariants();
  }

  @Test
  public void testReadStringWhenEmpty() {
    assertThat(parcel.readString()).isNull();
    assertInvariants();
  }

  @Test
  public void testReadStrongBinderWhenEmpty() {
    assertThat(parcel.readStrongBinder()).isNull();
  }

  @Test
  public void testReadWriteNumbers() {
    parcel.writeInt(Integer.MIN_VALUE);
    assertThat(parcel.dataSize()).isEqualTo(4);
    parcel.writeLong(Long.MAX_VALUE);
    assertThat(parcel.dataSize()).isEqualTo(12);
    double d = 3.14159;
    parcel.writeDouble(d);
    assertThat(parcel.dataSize()).isEqualTo(20);
    float f = -6.022e23f;
    parcel.writeFloat(f);
    assertThat(parcel.dataSize()).isEqualTo(24);
    assertInvariants();

    parcel.setDataPosition(0);
    assertThat(parcel.readInt()).isEqualTo(Integer.MIN_VALUE);
    assertThat(parcel.dataPosition()).isEqualTo(4);
    assertThat(parcel.readLong()).isEqualTo(Long.MAX_VALUE);
    assertThat(parcel.dataPosition()).isEqualTo(12);
    assertThat(parcel.readDouble()).isEqualTo(d);
    assertThat(parcel.dataPosition()).isEqualTo(20);
    assertThat(parcel.readFloat()).isEqualTo(f);
    assertThat(parcel.dataPosition()).isEqualTo(24);
    assertWithMessage("read past end is valid").that(parcel.readInt()).isEqualTo(0);
    assertThat(parcel.dataPosition()).isEqualTo(24);
    assertInvariants();
  }

  @Test
  public void testReadWriteSingleStringEvenLength() {
    String val = "test";
    parcel.writeString(val);
    parcel.setDataPosition(0);
    assertThat(parcel.readString()).isEqualTo(val);
    assertWithMessage("4B length + 4*2B data + 2B null char + 2B padding")
        .that(parcel.dataSize())
        .isEqualTo(16);
  }

  @Test
  public void testReadWriteLongerStringOddLength() {
    String val = "0123456789abcde";
    parcel.writeString(val);
    parcel.setDataPosition(0);
    assertThat(parcel.readString()).isEqualTo(val);
    assertWithMessage("4B length + 15*2B data + 2B null char")
        .that(parcel.dataSize())
        .isEqualTo(36);
  }

  @Test
  public void testWriteNullString() {
    parcel.writeString(null);
    parcel.setDataPosition(0);
    assertThat(parcel.readString()).isNull();
    assertThat(parcel.dataPosition()).isEqualTo(4);
  }

  @Test
  public void testWriteEmptyString() {
    parcel.writeString("");
    parcel.setDataPosition(0);
    assertThat(parcel.readString()).isEmpty();
    assertWithMessage("4B length + 2B null char + 2B padding").that(parcel.dataSize()).isEqualTo(8);
  }

  @Test
  public void testReadWriteMultipleStrings() {
    for (int i = 0; i < 10; ++i) {
      parcel.writeString(Integer.toString(i));
      assertInvariants();
    }
    parcel.setDataPosition(0);
    for (int i = 0; i < 10; ++i) {
      assertThat(parcel.readString()).isEqualTo(Integer.toString(i));
    }
    // now try to read past the number of items written and see what happens
    assertThat(parcel.readString()).isNull();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void testReadWriteSingleStrongBinder() {
    IBinder binder = new Binder();
    parcel.writeStrongBinder(binder);
    parcel.setDataPosition(0);
    assertThat(parcel.readStrongBinder()).isEqualTo(binder);
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void testWriteNullStrongBinder() {
    parcel.writeStrongBinder(null);
    parcel.setDataPosition(0);
    assertThat(parcel.readStrongBinder()).isNull();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void testReadWriteMultipleStrongBinders() {
    List<IBinder> binders = new ArrayList<>();
    for (int i = 0; i < 10; ++i) {
      IBinder binder = new Binder();
      binders.add(binder);
      parcel.writeStrongBinder(binder);
    }
    parcel.setDataPosition(0);
    for (int i = 0; i < 10; ++i) {
      assertThat(parcel.readStrongBinder()).isEqualTo(binders.get(i));
    }
    // now try to read past the number of items written and see what happens
    assertThat(parcel.readStrongBinder()).isNull();
  }

  @Test
  public void testReadWriteSingleInt() {
    int val = 5;
    parcel.writeInt(val);
    parcel.setDataPosition(0);
    assertThat(parcel.readInt()).isEqualTo(val);
  }

  @Test
  public void testFullyOverwritten() {
    parcel.writeInt(1);
    // NOTE: Later, this 8-byte long gets chopped up by two 4-byte writes, but it's OK because this
    // byte range is not read until it has been fully overwritten.
    parcel.writeLong(5);
    parcel.writeInt(4);
    assertInvariants();

    parcel.setDataPosition(4);
    parcel.writeByte((byte) 55); // Byte and int have the parceled size.
    parcel.writeString(null); // And so does a null string.
    assertInvariants();

    parcel.setDataPosition(0);
    assertWithMessage("readInt@0").that(parcel.readInt()).isEqualTo(1);
    assertWithMessage("position post-readInt@0").that(parcel.dataPosition()).isEqualTo(4);
    assertWithMessage("readByte@4").that(parcel.readByte()).isEqualTo(55);
    assertWithMessage("position post-readByte@4").that(parcel.dataPosition()).isEqualTo(8);
    assertWithMessage("readString@8").that(parcel.readString()).isNull();
    assertWithMessage("position post-readString@8").that(parcel.dataPosition()).isEqualTo(12);
    assertWithMessage("readInt@12").that(parcel.readInt()).isEqualTo(4);
  }

  @Test
  public void testReadWithoutRewinding() {
    parcel.writeInt(123);
    try {
      parcel.readInt();
      fail("should have thrown");
    } catch (UnreliableBehaviorError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo("Did you forget to setDataPosition(0) before reading the parcel?");
    }
  }

  @Test
  public void testWriteThenReadIsOkIfNotAtEnd() {
    parcel.writeInt(1);
    parcel.writeInt(2);
    parcel.writeInt(3);
    parcel.writeInt(4);
    parcel.setDataPosition(0);
    parcel.writeInt(5);
    assertThat(parcel.readInt()).isEqualTo(2);
    assertThat(parcel.readInt()).isEqualTo(3);
    assertThat(parcel.readInt()).isEqualTo(4);
    // This should succeed: while this is weird, the caller didn't clearly forget to reset the data
    // position, and is reading past the end of the parcel in a normal way.
    assertThat(parcel.readInt()).isEqualTo(0);
  }

  @Test
  public void testInvalidReadFromMiddleOfObject() {
    parcel.writeLong(111L);
    parcel.writeLong(222L);
    parcel.setDataPosition(0);

    parcel.setDataPosition(4);
    try {
      parcel.readInt();
      fail("should have thrown UnreliableBehaviorError");
    } catch (UnreliableBehaviorError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Looking for Integer at position 4, found Long [111] taking 8 bytes, but "
                  + "[222] interrupts it at position 8");
    }
  }

  @Test
  public void testInvalidReadFromOverwrittenObject() {
    parcel.writeString("hello all");
    parcel.setDataPosition(4);
    parcel.writeInt(5);
    parcel.setDataPosition(0);

    try {
      parcel.readString();
      fail("should have thrown UnreliableBehaviorError");
    } catch (UnreliableBehaviorError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Looking for String at position 0, found String [hello all] taking 24 bytes, but "
                  + "[5] interrupts it at position 4");
    }
  }

  @Test
  public void testZeroCanBeCasted_4ByteTypesCanBeReadAs8Bytes() {
    parcel.writeByte((byte) 0);
    parcel.writeByte((byte) 0);
    parcel.writeInt(0);
    parcel.writeInt(0);
    parcel.writeFloat(0.0f);
    parcel.writeByteArray(new byte[0]);
    assertWithMessage("total size").that(parcel.dataSize()).isEqualTo(24);

    parcel.setDataPosition(0);
    assertThat(parcel.readLong()).isEqualTo(0L);
    assertWithMessage("long consumes 8B").that(parcel.dataPosition()).isEqualTo(8);
    assertThat(parcel.readDouble()).isEqualTo(0.0);
    assertWithMessage("double consumes 8B").that(parcel.dataPosition()).isEqualTo(16);
    assertThat(parcel.readString()).isEqualTo("");
    assertWithMessage("empty string 8B").that(parcel.dataPosition()).isEqualTo(24);
  }

  @Test
  public void testZeroCanBeCasted_8ByteTypesCanBeReadAs4Bytes() {
    parcel.writeLong(0);
    parcel.writeDouble(0.0);
    parcel.writeLong(0);
    assertWithMessage("total size").that(parcel.dataSize()).isEqualTo(24);

    parcel.setDataPosition(0);
    assertThat(parcel.readInt()).isEqualTo(0);
    assertThat(parcel.readFloat()).isEqualTo(0.0f);
    assertThat(parcel.createByteArray()).isEqualTo(new byte[0]);
    assertThat(parcel.dataPosition()).isEqualTo(12);
    assertThat(parcel.readInt()).isEqualTo(0);
    assertThat(parcel.readFloat()).isEqualTo(0.0f);
    assertThat(parcel.createByteArray()).isEqualTo(new byte[0]);
    assertThat(parcel.dataPosition()).isEqualTo(24);
  }

  @Test
  public void testZeroCanBeCasted_overwrittenValuesAreOk() {
    parcel.writeByteArray(new byte[8]);
    assertThat(parcel.dataPosition()).isEqualTo(12);
    parcel.writeDouble(0.0);
    assertThat(parcel.dataPosition()).isEqualTo(20);
    parcel.writeLong(0);
    parcel.setDataPosition(8);
    parcel.writeInt(0); // Overwrite the second half of the byte array.
    parcel.setDataPosition(16);
    parcel.writeLong(0); // Overwrite the second half of the double and first half of the long.
    parcel.setDataPosition(20);
    parcel.writeInt(0); // And overwrite the second half of *that* with an int.
    assertWithMessage("total size").that(parcel.dataSize()).isEqualTo(28);

    parcel.setDataPosition(0);
    assertWithMessage("initial array length").that(parcel.readInt()).isEqualTo(8);
    // After this, we are reading all zeroes.  If we just read them as regular old types, it would
    // yield errors, but the special-casing for zeroes addresses this.  Make sure each data type
    // consumes the correct number of bytes.
    assertThat(parcel.readLong()).isEqualTo(0L);
    assertThat(parcel.dataPosition()).isEqualTo(12);
    assertThat(parcel.readString()).isEqualTo("");
    assertThat(parcel.dataPosition()).isEqualTo(20);
    assertThat(parcel.createByteArray()).isEqualTo(new byte[0]);
    assertThat(parcel.dataPosition()).isEqualTo(24);
    assertThat(parcel.readInt()).isEqualTo(0);
    assertThat(parcel.dataPosition()).isEqualTo(28);
  }

  @Test
  public void testInvalidReadFromTruncatedObjectEvenAfterBufferRegrows() {
    parcel.writeString("hello all");
    parcel.setDataSize(12);
    // Restore the original size, but the data should be lost.
    parcel.setDataSize(100);
    parcel.setDataPosition(0);

    try {
      parcel.readString();
      fail("should have thrown UnreliableBehaviorError");
    } catch (UnreliableBehaviorError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Looking for String at position 0, found String [hello all] taking 24 bytes, but "
                  + "[uninitialized data or the end of the buffer] interrupts it at position 12");
    }
  }

  @Test
  public void testInvalidReadFromUninitializedData() {
    // Write two longs with an 8-byte gap in the middle:
    parcel.writeLong(333L);
    parcel.setDataSize(parcel.dataSize() + 8);
    parcel.setDataPosition(parcel.dataSize());
    parcel.writeLong(444L);

    parcel.setDataPosition(0);
    assertThat(parcel.readLong()).isEqualTo(333L);
    try {
      parcel.readLong();
      fail("should have thrown UnreliableBehaviorError");
    } catch (UnreliableBehaviorError e) {
      assertThat(e).hasMessageThat().isEqualTo("Reading uninitialized data at position 8");
    }
  }

  @Test
  public void testReadWriteIntArray() throws Exception {
    final int[] ints = { 1, 2 };
    parcel.writeIntArray(ints);
    // Make sure a copy was stored.
    ints[0] = 99;
    ints[1] = 99;
    parcel.setDataPosition(0);
    final int[] ints2 = new int[ints.length];
    parcel.readIntArray(ints2);
    assertThat(ints2).isEqualTo(new int[] {1, 2});
  }

  @Test
  public void testWriteAndCreateNullIntArray() throws Exception {
    parcel.writeIntArray(null);
    parcel.setDataPosition(0);
    assertThat(parcel.createIntArray()).isNull();
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
  public void testWriteAndCreateNullLongArray() throws Exception {
    parcel.writeLongArray(null);
    parcel.setDataPosition(0);
    assertThat(parcel.createLongArray()).isNull();
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
  public void testWriteAndCreateNullFloatArray() throws Exception {
    parcel.writeFloatArray(null);
    parcel.setDataPosition(0);
    assertThat(parcel.createFloatArray()).isNull();
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
  public void testWriteAndCreateNullDoubleArray() throws Exception {
    parcel.writeDoubleArray(null);
    parcel.setDataPosition(0);
    assertThat(parcel.createDoubleArray()).isNull();
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
  public void testWriteAndCreateNullStringArray() throws Exception {
    parcel.writeStringArray(null);
    parcel.setDataPosition(0);
    assertThat(parcel.createStringArray()).isNull();
  }

  @Test
  public void testWriteAndCreateByteArray_multipleOf4() {
    byte[] bytes = new byte[] {-1, 2, 3, 127};
    parcel.writeByteArray(bytes);
    // Make sure that the parcel is not storing the original array.
    bytes[0] = 55;
    bytes[1] = 55;
    bytes[2] = 55;
    bytes[3] = 55;
    assertWithMessage("4B length + 4B data").that(parcel.dataSize()).isEqualTo(8);
    parcel.setDataPosition(0);
    byte[] actualBytes = parcel.createByteArray();
    assertThat(actualBytes).isEqualTo(new byte[] {-1, 2, 3, 127});
  }

  @Test
  public void testWriteAndCreateByteArray_oddLength() {
    byte[] bytes = new byte[] {-1, 2, 3, 127, -128};
    parcel.writeByteArray(bytes);
    assertWithMessage("4B length + 5B data + 3B padding").that(parcel.dataSize()).isEqualTo(12);
    parcel.setDataPosition(0);
    assertThat(parcel.createByteArray()).isEqualTo(bytes);
  }

  @Test
  public void testByteArrayToleratesZeroes() {
    parcel.writeInt(19); // Length
    parcel.writeInt(0); // These are zero
    parcel.writeLong(0); // This is zero
    parcel.writeFloat(0.0f); // This is zero
    parcel.writeByteArray(new byte[0]); // This is also zero
    assertThat(parcel.dataSize()).isEqualTo(24);

    parcel.setDataPosition(0);
    assertThat(parcel.createByteArray()).isEqualTo(new byte[19]);
  }

  @Test
  public void testByteArrayOfZeroesCastedToZeroes() {
    parcel.writeByteArray(new byte[17]);
    assertWithMessage("total size").that(parcel.dataSize()).isEqualTo(24);

    parcel.setDataPosition(0);
    assertThat(parcel.readInt()).isEqualTo(17);
    assertThat(parcel.readInt()).isEqualTo(0);
    assertThat(parcel.readFloat()).isEqualTo(0.0f);
    assertThat(parcel.createByteArray()).isEqualTo(new byte[0]);
    assertThat(parcel.readString()).isEqualTo("");
  }

  @Test
  public void testByteArrayOfNonZeroCannotBeCastedToZeroes() {
    parcel.writeByteArray(new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 1});
    assertWithMessage("total size").that(parcel.dataSize()).isEqualTo(16);

    parcel.setDataPosition(0);
    assertThat(parcel.readInt()).isEqualTo(9);
    try {
      assertThat(parcel.readInt()).isEqualTo(0);
      fail("expected to fail");
    } catch (RuntimeException e) {
      assertThat(e)
          .hasCauseThat()
          .hasMessageThat()
          .startsWith("Looking for Integer at position 4, found byte[]");
      assertThat(e)
          .hasCauseThat()
          .hasMessageThat()
          .endsWith("taking 12 bytes, and it is non-portable to reinterpret it");
    }
  }

  @Test
  public void testByteArrayOfZeroesReadAsZeroes() {
    parcel.writeByteArray(new byte[15]);
    assertThat(parcel.dataSize()).isEqualTo(20);

    parcel.setDataPosition(0);
    assertThat(parcel.readInt()).isEqualTo(15);
    assertThat(parcel.readLong()).isEqualTo(0);
    assertThat(parcel.readLong()).isEqualTo(0);
    assertThat(parcel.dataPosition()).isEqualTo(20);
  }

  @Test
  public void testWriteAndCreateNullByteArray() throws Exception {
    parcel.writeByteArray(null);
    assertThat(parcel.dataSize()).isEqualTo(4);
    parcel.setDataPosition(0);
    assertThat(parcel.createByteArray()).isNull();
  }

  @Test
  public void testWriteAndCreateByteArray_lengthZero() {
    byte[] bytes = new byte[] {};
    parcel.writeByteArray(bytes);
    assertThat(parcel.dataSize()).isEqualTo(4);
    parcel.setDataPosition(0);
    byte[] actualBytes = parcel.createByteArray();
    assertTrue(Arrays.equals(bytes, actualBytes));
  }

  @Test
  public void testWriteAndReadByteArray_overwrittenLength() {
    byte[] bytes = new byte[] {-1, 2, 3, 127};
    parcel.writeByteArray(bytes);
    assertThat(parcel.dataSize()).isEqualTo(8);
    parcel.setDataPosition(0);
    parcel.writeInt(3);
    parcel.setDataPosition(0);
    try {
      parcel.createByteArray();
      fail("expected exception");
    } catch (UnreliableBehaviorError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo("Byte array's length prefix is 3 but real length is 4");
    }
  }

  @Test
  public void testWriteAndReadByteArray_justLengthButNoContents() {
    parcel.writeInt(3);
    parcel.setDataPosition(0);
    try {
      parcel.createByteArray();
      fail("expected exception");
    } catch (UnreliableBehaviorError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo("Byte array's length prefix is 3 but real length is 0");
    }
  }

  @Test
  public void testWriteAndReadByteArray_empty() {
    parcel.writeInt(0);
    parcel.setDataPosition(0);
    assertThat(parcel.createByteArray()).isEqualTo(new byte[0]);
  }

  @Test
  public void testWriteAndReadByteArray() {
    byte[] bytes = new byte[] { -1, 2, 3, 127 };
    parcel.writeByteArray(bytes);
    assertThat(parcel.dataSize()).isEqualTo(8);
    parcel.setDataPosition(0);
    byte[] actualBytes = new byte[bytes.length];
    parcel.readByteArray(actualBytes);
    assertTrue(Arrays.equals(bytes, actualBytes));
  }

  @Test(expected = RuntimeException.class)
  public void testWriteAndReadByteArray_badLength() {
    byte[] bytes = new byte[] { -1, 2, 3, 127 };
    parcel.writeByteArray(bytes);
    assertThat(parcel.dataSize()).isEqualTo(8);
    parcel.setDataPosition(0);
    byte[] actualBytes = new byte[1];
    parcel.readByteArray(actualBytes);
  }

  @Test(expected = RuntimeException.class)
  public void testWriteAndReadByteArray_nullNotAllowed() {
    parcel.writeByteArray(null);
    assertThat(parcel.dataSize()).isEqualTo(4);
    parcel.setDataPosition(0);
    byte[] actualBytes = new byte[1];
    parcel.readByteArray(actualBytes);
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
  public void testWriteStringReadInt() {
    String val = "test";
    parcel.writeString(val);
    parcel.setDataPosition(0);
    try {
      parcel.readInt();
      fail("should have thrown");
    } catch (RuntimeException e) {
      assertThat(e)
          .hasCauseThat()
          .hasMessageThat()
          .isEqualTo(
              "Looking for Integer at position 0, found String [test] taking 16 bytes, "
                  + "and it is non-portable to reinterpret it");
    }
  }

  @Test
  public void testWriteIntReadString() {
    int val = 9;
    parcel.writeInt(val);
    parcel.setDataPosition(0);
    try {
      parcel.readString();
      fail("should have thrown");
    } catch (RuntimeException e) {
      assertThat(e)
          .hasCauseThat()
          .hasMessageThat()
          .isEqualTo(
              "Looking for String at position 0, found Integer [9] taking 4 bytes, "
                  + "and it is non-portable to reinterpret it");
    }
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
    assertThat(parcel.readLong()).isEqualTo(0L);
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
    assertThat(parcel.readLong()).isEqualTo(0L);
  }

  @Test(expected = RuntimeException.class)
  public void testWriteStringReadLong() {
    String val = "test";
    parcel.writeString(val);
    parcel.setDataPosition(0);
    parcel.readLong();
  }

  @Test(expected = RuntimeException.class)
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
    ArrayList<TestParcelable> normals = new ArrayList<>();
    normals.add(normal);

    parcel.writeTypedList(normals);
    parcel.setDataPosition(0);
    List<org.robolectric.shadows.TestParcelable> rehydrated = parcel
        .createTypedArrayList(TestParcelable.CREATOR);

    assertEquals(1, rehydrated.size());
    assertEquals(23, rehydrated.get(0).contents);
  }

  @Test
  public void testParcelableWithPackageProtected() throws Exception {
    TestParcelablePackage normal = new TestParcelablePackage(23);

    parcel.writeParcelable(normal, 0);
    parcel.setDataPosition(0);

    TestParcelablePackage rehydrated = parcel.readParcelable(TestParcelablePackage.class.getClassLoader());

    assertEquals(normal.contents, rehydrated.contents);
  }

  @Test
  public void testParcelableWithBase() throws Exception {
    TestParcelableImpl normal = new TestParcelableImpl(23);

    parcel.writeParcelable(normal, 0);
    parcel.setDataPosition(0);

    TestParcelableImpl rehydrated = parcel.readParcelable(TestParcelableImpl.class.getClassLoader());

    assertEquals(normal.contents, rehydrated.contents);
  }

  @Test
  public void testParcelableWithPublicClass() throws Exception {
    TestParcelable normal = new TestParcelable(23);

    parcel.writeParcelable(normal, 0);
    parcel.setDataPosition(0);

    TestParcelable rehydrated = parcel.readParcelable(TestParcelable.class.getClassLoader());

    assertEquals(normal.contents, rehydrated.contents);
  }

  @Test
  public void testReadAndWriteStringList() throws Exception {
    ArrayList<String> original = new ArrayList<>();
    List<String> rehydrated = new ArrayList<>();
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
    HashMap<String, String> original = new HashMap<>();
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

    parcel.writeStrongBinder(new Binder()); // 20 bytes
    assertThat(parcel.dataPosition()).isEqualTo(36);
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

    assertThat(parcel.readFloat()).isEqualTo(5.0f);
  }

  @Test
  public void testSetDataPositionToEmptyString() {
    parcel.writeString("");
    parcel.setDataPosition(parcel.dataPosition());
    parcel.writeString("something else");

    parcel.setDataPosition(0);
    assertThat(parcel.readString()).isEmpty();
  }

  @Test
  public void testAppendFrom() {
    // Write a mixture of things, and overwrite something.
    parcel.writeInt(1);
    parcel.writeInt(2);
    parcel.writeInt(3);
    parcel.writeInt(4);

    // Create a parcel2 that sandwiches parcel1 with happy birthday.
    Parcel parcel2 = Parcel.obtain();
    parcel2.writeString("happy");

    parcel2.appendFrom(parcel, 4, 8);
    assertInvariants(parcel);
    assertInvariants(parcel2);

    parcel2.writeString("birthday");
    assertInvariants(parcel);

    parcel2.setDataPosition(0);
    assertThat(parcel2.readString()).isEqualTo("happy");
    assertThat(parcel2.readInt()).isEqualTo(2);
    assertThat(parcel2.readInt()).isEqualTo(3);
    assertThat(parcel2.readString()).isEqualTo("birthday");
    assertThat(parcel2.dataAvail()).isEqualTo(0);
  }

  @Test
  public void testMarshallAndUnmarshall() {
    parcel.writeInt(1);
    parcel.writeString("hello");
    parcel.writeDouble(25.0);
    parcel.writeFloat(1.25f);
    parcel.writeByte((byte) 0xAF);
    int oldSize = parcel.dataSize();

    parcel.setDataPosition(7);
    byte[] rawBytes = parcel.marshall();
    assertWithMessage("data position preserved").that(parcel.dataPosition()).isEqualTo(7);
    Parcel parcel2 = Parcel.obtain();
    assertInvariants(parcel2);
    parcel2.unmarshall(rawBytes, 0, rawBytes.length);
    assertThat(parcel2.dataPosition()).isEqualTo(parcel2.dataSize());
    parcel2.setDataPosition(0);

    assertThat(parcel2.dataSize()).isEqualTo(oldSize);
    assertThat(parcel2.readInt()).isEqualTo(1);
    assertThat(parcel2.readString()).isEqualTo("hello");
    assertThat(parcel2.readDouble()).isEqualTo(25.0);
    assertThat(parcel2.readFloat()).isEqualTo(1.25f);
    assertThat(parcel2.readByte()).isEqualTo((byte) 0xAF);
  }

  @Test
  public void testMarshallFailsFastReadingInterruptedObject() {
    parcel.writeString("hello all");
    parcel.setDataPosition(4);
    parcel.writeInt(1);
    try {
      parcel.marshall();
      fail();
    } catch (UnreliableBehaviorError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Looking for Object at position 0, found String [hello all] taking 24 bytes, but "
                  + "[1] interrupts it at position 4");
    }
  }

  @Test
  public void testMarshallFailsFastReadingTruncatedObject() {
    parcel.writeString("hello all");
    parcel.setDataSize(8);
    try {
      parcel.marshall();
      fail();
    } catch (UnreliableBehaviorError e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Looking for Object at position 0, found String [hello all] taking 24 bytes, but "
                  + "[uninitialized data or the end of the buffer] interrupts it at position 8");
    }
  }

  @Test
  public void testMarshallFailsFastReadingUninitializedData() {
    parcel.writeString("hello everyone");
    parcel.setDataSize(parcel.dataSize() + 4);
    parcel.setDataPosition(parcel.dataSize());
    parcel.writeInt(1);
    try {
      parcel.marshall();
      fail();
    } catch (UnreliableBehaviorError e) {
      assertThat(e).hasMessageThat().isEqualTo("Reading uninitialized data at position 36");
    }
  }

  @Test
  public void testMarshallIntent() {
    // Some security fuzzer tests rely on marshalled Intents, and they internally exercise some
    // fairly weird behavior around cutting and splicing individual parcels.  This makes a good
    // stress test for fairly common app behavior.
    Intent intent = new Intent("action.foo");
    intent.putExtra("key1", "str1");
    intent.putExtra("key2", 2);
    intent.putExtra("key3", 3L);

    parcel.writeString("hello world");
    parcel.writeParcelable(intent, 0);
    parcel.writeString("bye world");

    // First make sure that it wasn't overtly corrupted pre-marshalling.
    parcel.setDataPosition(0);
    parcel.readString();
    assertThat(
            ((Intent) parcel.readParcelable(Intent.class.getClassLoader())).getStringExtra("key1"))
        .isEqualTo("str1");

    byte[] data = parcel.marshall();
    Parcel parcel2 = Parcel.obtain();
    parcel2.unmarshall(data, 0, data.length);
    assertThat(parcel2.dataPosition()).isEqualTo(parcel2.dataSize());
    parcel2.setDataPosition(0);
    try {
      assertThat(parcel2.readString()).isEqualTo("hello world");

      Intent unmarshalledIntent = (Intent) parcel2.readParcelable(Intent.class.getClassLoader());
      assertThat(unmarshalledIntent.getAction()).isEqualTo("action.foo");
      assertThat(unmarshalledIntent.getStringExtra("key1")).isEqualTo("str1");
      assertThat(unmarshalledIntent.getIntExtra("key2", -1)).isEqualTo(2);
      assertThat(unmarshalledIntent.getLongExtra("key3", -1)).isEqualTo(3L);

      assertThat(parcel2.readString()).isEqualTo("bye world");
    } finally {
      parcel2.recycle();
    }
  }

  @Test
  public void testUnmarshallLegacyBlob() throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(bos);
    // This ensures legacy marshalled values still parse, even if they are not aligned.
    oos.writeInt(9);
    oos.writeInt(5);
    oos.writeObject("abcde");
    // Old/legacy empty-strings are encoded as only 4 bytes.  Make sure that the all-zeroes logic
    // doesn't kick in and instead consume 8 bytes.
    oos.writeInt(4);
    oos.writeObject("");
    oos.writeInt(4);
    oos.writeObject(0);
    oos.writeInt(1); // This "int" only takes 1 byte.
    oos.writeObject(81);
    oos.writeInt(4);
    oos.writeObject(Integer.MAX_VALUE);
    // A byte array in the previous encoding: length plus individual bytes.
    oos.writeInt(4);
    oos.writeObject(3); // Array length
    oos.writeInt(1);
    oos.writeObject((byte) 85);
    oos.writeInt(1);
    oos.writeObject((byte) 86);
    oos.writeInt(1);
    oos.writeObject((byte) 87);
    oos.flush();

    byte[] data = bos.toByteArray();
    parcel.unmarshall(data, 0, data.length);
    assertThat(parcel.dataPosition()).isEqualTo(parcel.dataSize());
    parcel.setDataPosition(0);
    assertThat(parcel.readString()).isEqualTo("abcde");
    assertWithMessage("end offset of legacy string").that(parcel.dataPosition()).isEqualTo(5);
    assertThat(parcel.readString()).isEqualTo("");
    assertWithMessage("end offset of legacy empty string").that(parcel.dataPosition()).isEqualTo(9);
    assertThat(parcel.readInt()).isEqualTo(0);
    assertWithMessage("end offset of zero int").that(parcel.dataPosition()).isEqualTo(13);
    assertThat(parcel.readByte()).isEqualTo(81);
    assertWithMessage("end offset of legacy byte").that(parcel.dataPosition()).isEqualTo(14);
    assertThat(parcel.readInt()).isEqualTo(Integer.MAX_VALUE);
    assertWithMessage("end offset of legacy int").that(parcel.dataPosition()).isEqualTo(18);
    assertThat(parcel.createByteArray()).isEqualTo(new byte[] {85, 86, 87});
    assertWithMessage("end offset of legacy int").that(parcel.dataPosition()).isEqualTo(25);
    assertWithMessage("total size of legacy parcel").that(parcel.dataSize()).isEqualTo(25);
  }

  @Test
  public void testUnmarshallZeroes() throws IOException {
    // This tests special-case handling of zeroes in marshalling.  A few tests rely on the rather
    // well-defined behavior that Parcel will interpret a byte array of all zeroes as zero
    // primitives and empty arrays.  When unmarshalling, this can be easily disambiguated from an
    // ObjectInputStream, which requires at least a non-zero magic, and is therefore not all
    // zeroes.
    byte[] mostlyZeroes = new byte[333];
    // Make a couple of non-zero values outside of the range being copied, just to ensure the range
    // is considered in the all-zeroes detection.
    mostlyZeroes[1] = (byte) 55;
    mostlyZeroes[302] = (byte) -32;
    // Parse the array of all zeroes.
    parcel.unmarshall(new byte[300], 2, 300);
    assertThat(parcel.dataSize()).isEqualTo(300);
    assertThat(parcel.dataPosition()).isEqualTo(300);
    assertWithMessage("unmarshall does not grow size incrementally but allocates the exact amount")
        .that(parcel.dataCapacity())
        .isEqualTo(300);
    parcel.setDataPosition(0);
    assertThat(parcel.readString()).isEqualTo("");
    assertWithMessage("end offset of empty string").that(parcel.dataPosition()).isEqualTo(8);
    assertThat(parcel.createByteArray()).isEqualTo(new byte[0]);
    assertWithMessage("end offset of empty byte array").that(parcel.dataPosition()).isEqualTo(12);
    assertThat(parcel.readInt()).isEqualTo(0);
    assertWithMessage("end offset of readInt zeroes").that(parcel.dataPosition()).isEqualTo(16);
    assertThat(parcel.readFloat()).isEqualTo(0.0f);
    assertWithMessage("end offset of readFloat zeroes").that(parcel.dataPosition()).isEqualTo(20);
    assertThat(parcel.readDouble()).isEqualTo(0.0d);
    assertWithMessage("end offset of readDouble zeroes").that(parcel.dataPosition()).isEqualTo(28);
    assertThat(parcel.readLong()).isEqualTo(0L);
    assertWithMessage("end offset of readLong zeroes").that(parcel.dataPosition()).isEqualTo(36);
    try {
      parcel.readParcelable(Account.class.getClassLoader());
      fail("Should not be able to unparcel something without the required header");
    } catch (BadParcelableException e) {
      // Good -- a stream of all zeroes should end up throwing BadParcelableException instead of
      // one of the Robolectric-specific exceptions.  One of the primary reasons for handling
      // zeroes to begin with is to allow tests to simulate BadParcelableException with a stream of
      // zeroes.
    }
  }

  @Test
  public void testUnmarshallEmpty() throws IOException {
    // Unmarshall an zero-length byte string, although, pass a non-empty array to make sure the
    // length/offset are respected.
    parcel.unmarshall(new byte[] {1, 2, 3}, 1, 0);
    assertThat(parcel.dataSize()).isEqualTo(0);
    assertThat(parcel.dataPosition()).isEqualTo(0);
    // Should not throw "Did you forget to setDataPosition(0)?" because it's still empty.
    assertThat(parcel.readInt()).isEqualTo(0);
  }

  @Test
  public void testSetDataSize() {
    parcel.writeInt(1);
    parcel.writeInt(2);
    parcel.writeInt(3);
    parcel.writeInt(4);
    parcel.writeInt(5);
    assertThat(parcel.dataSize()).isEqualTo(20);
    assertInvariants();
    int oldCapacity = parcel.dataCapacity();

    parcel.setDataSize(12);
    assertWithMessage("should equal requested size").that(parcel.dataSize()).isEqualTo(12);
    assertWithMessage("position gets truncated").that(parcel.dataPosition()).isEqualTo(12);
    assertWithMessage("capacity doesn't shrink").that(parcel.dataCapacity()).isEqualTo(oldCapacity);

    parcel.setDataSize(100);
    assertWithMessage("should equal requested size").that(parcel.dataSize()).isEqualTo(100);
    assertWithMessage("position untouched").that(parcel.dataPosition()).isEqualTo(12);
    assertInvariants();
  }

  @Test
  public void testDataSizeShrinkingAndGrowing() {
    assertWithMessage("still empty").that(parcel.dataSize()).isEqualTo(0);
    assertWithMessage("did not advance").that(parcel.dataPosition()).isEqualTo(0);
    for (int i = 0; i < 100; i++) {
      parcel.writeInt(1000 + i);
    }
    assertInvariants();
    assertWithMessage("now has 100 ints").that(parcel.dataSize()).isEqualTo(400);
    assertWithMessage("advanced 100 ints").that(parcel.dataPosition()).isEqualTo(400);

    parcel.setDataPosition(88);
    assertInvariants();
    parcel.setDataSize(100);
    assertInvariants();

    assertWithMessage("requested size honored").that(parcel.dataSize()).isEqualTo(100);
    assertWithMessage("requested position honored").that(parcel.dataPosition()).isEqualTo(88);
    assertWithMessage("data preserved (index 22, byte 88)").that(parcel.readInt()).isEqualTo(1022);

    parcel.setDataSize(8);
    assertInvariants();
    parcel.setDataCapacity(500); // Make sure it doesn't affect size.
    assertInvariants();
    assertWithMessage("truncated size").that(parcel.dataSize()).isEqualTo(8);
    assertWithMessage("truncated position").that(parcel.dataPosition()).isEqualTo(8);

    parcel.setDataSize(400);
    assertInvariants();
    parcel.setDataPosition(88);
    assertInvariants();
    try {
      parcel.readInt();
      fail();
    } catch (UnreliableBehaviorError e) {
      assertThat(e).hasMessageThat().isEqualTo("Reading uninitialized data at position 88");
    }
    parcel.setDataPosition(4);
    assertWithMessage("early data should be preserved").that(parcel.readInt()).isEqualTo(1001);
  }

  @Test
  public void testSetDataCapacity() {
    parcel.writeInt(-1);
    assertWithMessage("size is 1 int").that(parcel.dataSize()).isEqualTo(4);
    assertInvariants();
    parcel.setDataPosition(parcel.dataPosition());
    parcel.readInt();
    assertWithMessage("reading within capacity but over size does not increase size")
        .that(parcel.dataSize())
        .isEqualTo(4);

    parcel.setDataCapacity(100);
    assertInvariants();
    assertWithMessage("capacity equals requested").that(parcel.dataCapacity()).isEqualTo(100);
    assertWithMessage("size does not increase with capacity").that(parcel.dataSize()).isEqualTo(4);

    parcel.setDataCapacity(404);
    for (int i = 0; i < 100; i++) {
      parcel.writeInt(i);
    }
    assertInvariants();
    assertWithMessage("capacity exactly holds 404 ints").that(parcel.dataCapacity()).isEqualTo(404);
    assertWithMessage("101 ints in size").that(parcel.dataSize()).isEqualTo(404);
    assertWithMessage("advanced 101 ints").that(parcel.dataPosition()).isEqualTo(404);

    parcel.setDataCapacity(12);
    assertWithMessage("capacity never shrinks").that(parcel.dataCapacity()).isEqualTo(404);
    parcel.setDataSize(12);
    assertWithMessage("size does shrink").that(parcel.dataSize()).isEqualTo(12);
    parcel.setDataCapacity(12);
    assertWithMessage("capacity never shrinks").that(parcel.dataCapacity()).isEqualTo(404);
  }
  
  @Test
  public void testWriteAndEnforceCompatibleInterface() {
    parcel.writeInterfaceToken("com.example.IMyInterface");
    parcel.setDataPosition(0);
    parcel.enforceInterface("com.example.IMyInterface");
    // Nothing explodes
  }
  
  @Test
  public void testWriteAndEnforceIncompatibleInterface() {
    parcel.writeInterfaceToken("com.example.Derp");
    parcel.setDataPosition(0);
    try {
      parcel.enforceInterface("com.example.IMyInterface");
      fail("Expected SecurityException");
    } catch (SecurityException e) {
      // Expected
    }
  }

  @Test
  @Config(minSdk = M)
  public void testReadWriteFileDescriptor() throws Exception {
    File file = new File(ApplicationProvider.getApplicationContext().getFilesDir(), "test");
    RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
    FileDescriptor expectedFileDescriptor = randomAccessFile.getFD();

    parcel.writeFileDescriptor(expectedFileDescriptor);
    parcel.setDataPosition(0);

    FileDescriptor actualFileDescriptor = parcel.readRawFileDescriptor();

    // Since the test runs in a single process, for simplicity, we can assume the FD isn't changed
    int expectedFd = ReflectionHelpers.getField(expectedFileDescriptor, "fd");
    int actualFd = ReflectionHelpers.getField(actualFileDescriptor, "fd");
    assertThat(actualFd).isEqualTo(expectedFd);
  }

  private void assertInvariants() {
    assertInvariants(parcel);
  }

  private void assertInvariants(Parcel p) {
    assertWithMessage("capacity >= size").that(p.dataCapacity()).isAtLeast(p.dataSize());
    assertWithMessage("position <= size").that(p.dataPosition()).isAtMost(p.dataSize());
    assertWithMessage("available = size - position")
        .that(p.dataAvail())
        .isEqualTo(p.dataSize() - p.dataPosition());
    assertWithMessage("size % 4 == 0").that(p.dataSize() % 4).isEqualTo(0);
    assertWithMessage("capacity % 4 == 0").that(p.dataSize() % 4).isEqualTo(0);
  }
}
