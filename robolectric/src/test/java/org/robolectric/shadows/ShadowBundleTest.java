package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.os.Bundle;
import android.os.Parcelable;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowBundleTest {
  private final Bundle bundle = new Bundle();

  @Test
  public void containsKey() {
    assertThat(bundle.containsKey("foo")).isFalse();
    bundle.putString("foo", "bar");
    assertThat(bundle.containsKey("foo")).isTrue();
  }

  @Test
  public void getInt() {
    bundle.putInt("foo", 5);
    assertThat(bundle.getInt("foo")).isEqualTo(5);
    assertThat(bundle.getInt("bar")).isEqualTo(0);
    assertThat(bundle.getInt("bar", 7)).isEqualTo(7);
  }

  @Test
  public void size() {
    assertThat(bundle.size()).isEqualTo(0);
    bundle.putInt("foo", 5);
    assertThat(bundle.size()).isEqualTo(1);
    bundle.putInt("bar", 5);
    assertThat(bundle.size()).isEqualTo(2);
  }

  @Test
  public void getLong() {
    bundle.putLong("foo", 5);
    assertThat(bundle.getLong("foo")).isEqualTo(5);
    assertThat(bundle.getLong("bar")).isEqualTo(0);
    assertThat(bundle.getLong("bar", 7)).isEqualTo(7);
  }

  @Test
  public void getDouble() {
    bundle.putDouble("foo", 5);
    assertThat(bundle.getDouble("foo")).isEqualTo(5.0);
    assertThat(bundle.getDouble("bar")).isEqualTo(0.0);
    assertThat(bundle.getDouble("bar", 7)).isEqualTo(7.0);
  }

  @Test
  public void getBoolean() {
    bundle.putBoolean("foo", true);
    assertThat(bundle.getBoolean("foo")).isTrue();
    assertThat(bundle.getBoolean("bar")).isFalse();
    assertThat(bundle.getBoolean("bar", true)).isTrue();
  }

  @Test
  public void getFloat() {
    bundle.putFloat("foo", 5f);
    assertThat(bundle.getFloat("foo")).isEqualTo(5.0f);
    assertThat(bundle.getFloat("bar")).isEqualTo(0.0f);
    assertThat(bundle.getFloat("bar", 7)).isEqualTo(7.0f);
  }

  @Test
  public void getWrongType() {
    bundle.putFloat("foo", 5f);
    assertThat(bundle.getCharArray("foo")).isNull();
    assertThat(bundle.getInt("foo")).isEqualTo(0);
    assertThat(bundle.getIntArray("foo")).isNull();
    assertThat(bundle.getIntegerArrayList("foo")).isNull();
    assertThat(bundle.getShort("foo")).isEqualTo((short) 0);
    assertThat(bundle.getShortArray("foo")).isNull();
    assertThat(bundle.getBoolean("foo")).isFalse();
    assertThat(bundle.getBooleanArray("foo")).isNull();
    assertThat(bundle.getLong("foo")).isEqualTo(0);
    assertThat(bundle.getLongArray("foo")).isNull();
    assertThat(bundle.getFloatArray("foo")).isNull();
    assertThat(bundle.getDouble("foo")).isEqualTo(0.0);
    assertThat(bundle.getDoubleArray("foo")).isNull();
    assertThat(bundle.getString("foo")).isNull();
    assertThat(bundle.getStringArray("foo")).isNull();
    assertThat(bundle.getStringArrayList("foo")).isNull();
    assertThat(bundle.getBundle("foo")).isNull();
    assertThat((Parcelable) bundle.getParcelable("foo")).isNull();
    assertThat(bundle.getParcelableArray("foo")).isNull();
    assertThat(bundle.getParcelableArrayList("foo")).isNull();

    bundle.putInt("foo", 1);
    assertThat(bundle.getFloat("foo")).isEqualTo(0.0f);
  }

  @Test
  public void remove() {
    bundle.putFloat("foo", 5f);
    bundle.putFloat("foo2", 5f);
    bundle.remove("foo");

    assertThat(bundle.containsKey("foo")).isFalse();
    assertThat(bundle.containsKey("foo2")).isTrue();
  }

  @Test
  public void clear() {
    bundle.putFloat("foo", 5f);
    bundle.clear();

    assertThat(bundle.size()).isEqualTo(0);
  }

  @Test
  public void isEmpty() {
    assertThat(bundle.isEmpty()).isTrue();
    bundle.putBoolean("foo", true);
    assertThat(bundle.isEmpty()).isFalse();
  }

  @Test
  public void stringArray() {
    bundle.putStringArray("foo", new String[] { "a" });
    assertThat(bundle.getStringArray("foo")).isEqualTo(new String[]{"a"});
    assertThat(bundle.getStringArray("bar")).isNull();
  }

  @Test
  public void stringArrayList() {
    ArrayList<String> list = new ArrayList<>();
    list.add("a");

    bundle.putStringArrayList("foo", new ArrayList<>(list));
    assertThat(bundle.getStringArrayList("foo")).isEqualTo(list);
    assertThat(bundle.getStringArrayList("bar")).isNull();
  }

  @Test
  public void intArrayList() {
    ArrayList<Integer> list = new ArrayList<>();
    list.add(100);

    bundle.putIntegerArrayList("foo", new ArrayList<>(list));
    assertThat(bundle.getIntegerArrayList("foo")).isEqualTo(list);
    assertThat(bundle.getIntegerArrayList("bar")).isNull();
  }

  @Test
  public void booleanArray() {
    boolean [] arr = new boolean[] { false, true };
    bundle.putBooleanArray("foo", arr);

    assertThat(bundle.getBooleanArray("foo")).isEqualTo(arr);
    assertThat(bundle.getBooleanArray("bar")).isNull();
  }

  @Test
  public void byteArray() {
    byte [] arr = new byte[] { 12, 24 };
    bundle.putByteArray("foo", arr);

    assertThat(bundle.getByteArray("foo")).isEqualTo(arr);
    assertThat(bundle.getByteArray("bar")).isNull();
  }

  @Test
  public void charArray() {
    char [] arr = new char[] { 'c', 'j' };
    bundle.putCharArray("foo", arr);

    assertThat(bundle.getCharArray("foo")).isEqualTo(arr);
    assertThat(bundle.getCharArray("bar")).isNull();
  }

  @Test
  public void doubleArray() {
    double [] arr = new double[] { 1.2, 3.4 };
    bundle.putDoubleArray("foo", arr);

    assertThat(bundle.getDoubleArray("foo")).isEqualTo(arr);
    assertThat(bundle.getDoubleArray("bar")).isNull();
  }

  @Test
  public void intArray() {
    int [] arr = new int[] { 87, 65 };
    bundle.putIntArray("foo", arr);

    assertThat(bundle.getIntArray("foo")).isEqualTo(arr);
    assertThat(bundle.getIntArray("bar")).isNull();
  }

  @Test
  public void longArray() {
    long [] arr = new long[] { 23, 11 };
    bundle.putLongArray("foo", arr);

    assertThat(bundle.getLongArray("foo")).isEqualTo(arr);
    assertThat(bundle.getLongArray("bar")).isNull();
  }

  @Test
  public void shortArray() {
    short [] arr = new short[] { 89, 37 };
    bundle.putShortArray("foo", arr);

    assertThat(bundle.getShortArray("foo")).isEqualTo(arr);
    assertThat(bundle.getShortArray("bar")).isNull();
  }

  @Test
  public void parcelableArray() {
    Bundle innerBundle = new Bundle();
    innerBundle.putInt("value", 1);
    Parcelable[] arr = new Parcelable[] { innerBundle };
    bundle.putParcelableArray("foo", arr);

    assertThat(bundle.getParcelableArray("foo")).isEqualTo(arr);
    assertThat(bundle.getParcelableArray("bar")).isNull();
  }
}
