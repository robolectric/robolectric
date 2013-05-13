package org.robolectric.shadows;

import android.util.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class PairTest {

  @Test
  public void testConstructor() throws Exception {
    Pair<String, Integer> pair = new Pair<String, Integer>("a", 1);
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
