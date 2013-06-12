package org.robolectric.shadows;

import android.os.Bundle;
import android.os.Parcelable;
import junit.framework.AssertionFailedError;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import java.util.ArrayList;

import static org.junit.Assert.*;


@RunWith(TestRunners.WithDefaults.class)
public class BundleTest {

  private Bundle bundle;

  @Before public void setUp() throws Exception {
    bundle = new Bundle();
  }

  @Test
  public void testContainsKey() throws Exception {
    assertFalse(bundle.containsKey("foo"));
    bundle.putString("foo", "bar");
    assertTrue(bundle.containsKey("foo"));
  }

  @Test
  public void testInt() {
    bundle.putInt("foo", 5);
    assertEquals(5,bundle.getInt("foo"));
    assertEquals(0,bundle.getInt("bar"));
    assertEquals(7, bundle.getInt("bar", 7));
  }

  @Test
  public void testSize() {
    assertEquals(0, bundle.size());
    bundle.putInt("foo", 5);
    assertEquals(1, bundle.size());
    bundle.putInt("bar", 5);
    assertEquals(2, bundle.size());
  }

  @Test
  public void testLong() {
    bundle.putLong("foo", 5);
    assertEquals(5, bundle.getLong("foo"));
    assertEquals(0,bundle.getLong("bar"));
    assertEquals(7, bundle.getLong("bar", 7));
  }

  @Test
  public void testDouble() {
    bundle.putDouble("foo", 5);
    assertEquals(Double.valueOf(5), Double.valueOf(bundle.getDouble("foo")));
    assertEquals(Double.valueOf(0),Double.valueOf(bundle.getDouble("bar")));
    assertEquals(Double.valueOf(7), Double.valueOf(bundle.getDouble("bar", 7)));
  }

  @Test
  public void testBoolean() {
    bundle.putBoolean("foo", true);
    assertEquals(true, bundle.getBoolean("foo"));
    assertEquals(false, bundle.getBoolean("bar"));
    assertEquals(true, bundle.getBoolean("bar", true));
  }

  @Test
  public void testFloat() {
    bundle.putFloat("foo", 5f);
    assertEquals(Float.valueOf(5), Float.valueOf(bundle.getFloat("foo")));
    assertEquals(Float.valueOf(0),Float.valueOf(bundle.getFloat("bar")));
    assertEquals(Float.valueOf(7), Float.valueOf(bundle.getFloat("bar", 7)));
  }

  @Test
  public void testGetOfWrongType() {
    bundle.putFloat("foo", 5f);
    assertEquals(0, bundle.getChar("foo"));
    assertEquals(null, bundle.getCharArray("foo"));
    assertEquals(0, bundle.getInt("foo"));
    assertEquals(null, bundle.getIntArray("foo"));
    assertEquals(null, bundle.getIntegerArrayList("foo"));
    assertEquals(0, bundle.getShort("foo"));
    assertEquals(null, bundle.getShortArray("foo"));
    assertEquals(false, bundle.getBoolean("foo"));
    assertEquals(null, bundle.getBooleanArray("foo"));
    assertEquals(0, bundle.getLong("foo"));
    assertEquals(null, bundle.getLongArray("foo"));
    assertEquals(null, bundle.getFloatArray("foo"));
    assertEquals(0, bundle.getDouble("foo"), 0.005);
    assertEquals(null, bundle.getDoubleArray("foo"));
    assertEquals(null, bundle.getString("foo"));
    assertEquals(null, bundle.getStringArray("foo"));
    assertEquals(null, bundle.getStringArrayList("foo"));
    assertEquals(null, bundle.getBundle("foo"));
    assertEquals(null, bundle.getParcelable("foo"));
    assertEquals(null, bundle.getParcelableArray("foo"));
    assertEquals(null, bundle.getParcelableArrayList("foo"));

    bundle.putInt("foo", 1);
    assertEquals(0, bundle.getFloat("foo"), 0.005f);
  }

  @Test
  public void testRemove() {
    bundle.putFloat("foo", 5f);
    bundle.putFloat("foo2", 5f);

    bundle.remove("foo");

    assertFalse(bundle.containsKey("foo"));
    assertTrue(bundle.containsKey("foo2"));
  }

  @Test
  public void testClear() {
    bundle.putFloat("foo", 5f);

    bundle.clear();

    assertEquals(0, bundle.size());
  }

  @Test
  public void testIsEmpty() {
    assertTrue(bundle.isEmpty());
    bundle.putBoolean("foo", true);
    assertFalse(bundle.isEmpty());
  }

  @Test
  public void testStringArray() {
    bundle.putStringArray("foo", new String[] { "a" });
    Assert.assertArrayEquals(new String[] { "a" }, bundle.getStringArray("foo"));
    assertNull(bundle.getStringArray("bar"));
  }

  @Test
  public void testStringArrayList() {
    ArrayList<String> list = new ArrayList<String>();
    list.add("a");

    bundle.putStringArrayList("foo", new ArrayList<String>(list));
    Assert.assertEquals(list, bundle.getStringArrayList("foo"));
    assertNull(bundle.getStringArrayList("bar"));
  }

  @Test
  public void testIntegerArrayList() {
    ArrayList<Integer> list = new ArrayList<Integer>();
    list.add(100);

    bundle.putIntegerArrayList("foo", new ArrayList<Integer>(list));
    Assert.assertEquals(list, bundle.getIntegerArrayList("foo"));
    assertNull(bundle.getIntegerArrayList("bar"));
  }

  @Test
  public void testBundle() {
    Bundle innerBundle = new Bundle();
    innerBundle.putInt("int", 7);
    bundle.putBundle("bundle", innerBundle);

    assertEquals(innerBundle, bundle.getBundle("bundle"));
    assertNull(bundle.getBundle("bar"));
  }

  @Test
  public void testBooleanArray() {
    boolean [] arr = new boolean[] { false, true };
    bundle.putBooleanArray("foo", arr);

    assertArrayEquals(arr, bundle.getBooleanArray("foo"));
    assertNull(bundle.getBooleanArray("bar"));
  }

  @Test
  public void testByteArray() {
    byte [] arr = new byte[] { 12, 24 };
    bundle.putByteArray("foo", arr);

    Assert.assertArrayEquals(arr, bundle.getByteArray("foo"));
    assertNull(bundle.getByteArray("bar"));
  }

  @Test
  public void testCharArray() {
    char [] arr = new char[] { 'c', 'j' };
    bundle.putCharArray("foo", arr);

    Assert.assertArrayEquals(arr, bundle.getCharArray("foo"));
    assertNull(bundle.getCharArray("bar"));
  }

  @Test
  public void testDoubleArray() {
    double [] arr = new double[] { 1.2, 3.4 };
    bundle.putDoubleArray("foo", arr);

    assertArrayEquals(arr, bundle.getDoubleArray("foo"));
    assertNull(bundle.getDoubleArray("bar"));
  }

  @Test
  public void testIntArray() {
    int [] arr = new int[] { 87, 65 };
    bundle.putIntArray("foo", arr);

    Assert.assertArrayEquals(arr, bundle.getIntArray("foo"));
    assertNull(bundle.getIntArray("bar"));
  }

  @Test
  public void testLongArray() {
    long [] arr = new long[] { 23, 11 };
    bundle.putLongArray("foo", arr);

    Assert.assertArrayEquals(arr, bundle.getLongArray("foo"));
    assertNull(bundle.getLongArray("bar"));
  }

  @Test
  public void testShortArray() {
    short [] arr = new short[] { 89, 37 };
    bundle.putShortArray("foo", arr);

    Assert.assertArrayEquals(arr, bundle.getShortArray("foo"));
    assertNull(bundle.getShortArray("bar"));
  }

  @Test
  public void testParcelableArray() {
    Bundle innerBundle = new Bundle();
    innerBundle.putInt("value", 1);
    Parcelable[] arr = new Parcelable[] { innerBundle };
    bundle.putParcelableArray("foo", arr);

    Assert.assertArrayEquals(arr, bundle.getParcelableArray("foo"));
    assertNull(bundle.getParcelableArray("bar"));
  }

  @Test
  public void testCopyConstructor() {
    bundle.putInt("value", 1);
    Bundle copiedBundle = new Bundle(bundle);

    Assert.assertEquals(copiedBundle, bundle);
  }

  private void assertArrayEquals(double[] expected, double[] actual) {
    if (expected != null && actual == null) {
      throw new AssertionFailedError();
    } else if (expected == null && actual != null) {
      throw new AssertionFailedError();
    } else {
      for (int i = 0; i < expected.length; i++) {
        if (expected[i] != actual[i])
          throw new AssertionFailedError();
      }

      if (expected.length != actual.length)
        throw new AssertionFailedError();
    }
  }

  private void assertArrayEquals(boolean[] expected, boolean[] actual) {
    if (expected != null && actual == null) {
      throw new AssertionFailedError();
    } else if (expected == null && actual != null) {
      throw new AssertionFailedError();
    } else {
      for (int i = 0; i < expected.length; i++) {
        if (expected[i] != actual[i])
          throw new AssertionFailedError();
      }

      if (expected.length != actual.length)
        throw new AssertionFailedError();
    }
  }
}
