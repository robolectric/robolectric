package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.os.Build;
import android.os.PersistableBundle;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
public class ShadowPersistableBundleTest {
  private final PersistableBundle bundle = new PersistableBundle();

  @Test
  public void containsKey() {
    assertThat(bundle.containsKey("foo")).isFalse();
    bundle.putString("foo", "bar");
    assertThat(bundle.containsKey("foo")).isTrue();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.M)
  public void putBoolean() {
    bundle.putBoolean("foo", true);
    assertThat(bundle.getBoolean("foo")).isTrue();
    assertThat(bundle.getBoolean("bar")).isFalse();
    assertThat(bundle.getBoolean("bar", true)).isTrue();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.M)
  public void putBooleanArray() {
    boolean[] array = {false, true};
    bundle.putBooleanArray("foo", array);
    assertThat(bundle.getBooleanArray("foo")).isEqualTo(array);
    assertThat(bundle.getBooleanArray("bar")).isNull();
  }

  @Test
  public void putDouble() {
    bundle.putDouble("foo", 1.23);
    assertThat(bundle.getDouble("foo")).isEqualTo(1.23);
    assertThat(bundle.getDouble("bar")).isEqualTo(0.0);
    assertThat(bundle.getDouble("bar", 4.56)).isEqualTo(4.56);
  }

  @Test
  public void putDoubleArray() {
    double[] array = new double[] {1.23, 4.56};
    bundle.putDoubleArray("foo", array);
    assertThat(bundle.getDoubleArray("foo")).isEqualTo(array);
    assertThat(bundle.getDoubleArray("bar")).isNull();
  }

  @Test
  public void putInt() {
    bundle.putInt("foo", 1);
    assertThat(bundle.getInt("foo")).isEqualTo(1);
    assertThat(bundle.getInt("bar")).isEqualTo(0);
    assertThat(bundle.getInt("bar", 5)).isEqualTo(5);
  }

  @Test
  public void putIntArray() {
    int[] array = new int[] {1, 2};
    bundle.putIntArray("foo", array);
    assertThat(bundle.getIntArray("foo")).isEqualTo(array);
    assertThat(bundle.getIntArray("bar")).isNull();
  }

  @Test
  public void putLong() {
    bundle.putLong("foo", 1L);
    assertThat(bundle.getLong("foo")).isEqualTo(1L);
    assertThat(bundle.getLong("bar")).isEqualTo(0L);
    assertThat(bundle.getLong("bar", 5L)).isEqualTo(5L);
  }

  @Test
  public void putLongArray() {
    long[] array = new long[] {1L, 2L};
    bundle.putLongArray("foo", array);
    assertThat(bundle.getLongArray("foo")).isEqualTo(array);
    assertThat(bundle.getLongArray("bar")).isNull();
  }

  @Test
  public void putString() {
    bundle.putString("foo", "abc");
    assertThat(bundle.getString("foo")).isEqualTo("abc");
    assertThat(bundle.getString("bar")).isNull();
    assertThat(bundle.getString("bar", "def")).isEqualTo("def");
  }

  @Test
  public void putStringArray() {
    String[] array = new String[] {"abc", "def"};
    bundle.putStringArray("foo", array);
    assertThat(bundle.getStringArray("foo")).isEqualTo(array);
    assertThat(bundle.getStringArray("bar")).isNull();
  }

  @Test
  public void putPersistableBundle() {
    PersistableBundle nested = new PersistableBundle();
    nested.putInt("foo", 1);
    bundle.putPersistableBundle("foo", nested);
    assertThat(bundle.getPersistableBundle("foo")).isEqualTo(nested);
    assertThat(bundle.getPersistableBundle("bar")).isNull();
  }

  @Test
  public void remove() {
    bundle.putInt("foo", 1);
    bundle.putInt("bar", 2);
    bundle.remove("foo");

    assertThat(bundle.containsKey("foo")).isFalse();
    assertThat(bundle.containsKey("bar")).isTrue();
  }

  @Test
  public void clear() {
    bundle.putInt("foo", 1);
    bundle.clear();

    assertThat(bundle.containsKey("foo")).isFalse();
    assertThat(bundle.size()).isEqualTo(0);
  }

  @Test
  public void isEmpty() {
    assertThat(bundle.isEmpty()).isTrue();
    bundle.putInt("foo", 1);
    assertThat(bundle.isEmpty()).isFalse();
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
  public void getWrongType() {
    bundle.putInt("foo", 1);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
      assertThat(bundle.getBoolean("foo")).isFalse();
      assertThat(bundle.getBooleanArray("foo")).isNull();
    }

    assertThat(bundle.getDouble("foo")).isEqualTo(0.0);
    assertThat(bundle.getDoubleArray("foo")).isNull();
    assertThat(bundle.getIntArray("foo")).isNull();
    assertThat(bundle.getLong("foo")).isEqualTo(0);
    assertThat(bundle.getLongArray("foo")).isNull();
    assertThat(bundle.getString("foo")).isNull();
    assertThat(bundle.getStringArray("foo")).isNull();
    assertThat(bundle.getPersistableBundle("foo")).isNull();

    bundle.putDouble("foo", 1.23);
    assertThat(bundle.getInt("foo")).isEqualTo(0);
  }
}
