package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.util.Pair;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowPairTest {

  @Test
  public void testConstructor() throws Exception {
    Pair<String, Integer> pair = new Pair<>("a", 1);
    assertThat(pair.first).isEqualTo("a");
    assertThat(pair.second).isEqualTo(1);
  }

  @Test
  public void testStaticCreate() throws Exception {
    Pair<String, String> p = Pair.create("Foo", "Bar");
    assertThat(p.first).isEqualTo("Foo");
    assertThat(p.second).isEqualTo("Bar");
  }

  @Test
  public void testEquals() throws Exception {
    assertThat(Pair.create("1", 2)).isEqualTo(Pair.create("1", 2));
  }

  @Test
  public void testHash() throws Exception {
    assertThat(Pair.create("1", 2).hashCode()).isEqualTo(Pair.create("1", 2).hashCode());
  }
}
