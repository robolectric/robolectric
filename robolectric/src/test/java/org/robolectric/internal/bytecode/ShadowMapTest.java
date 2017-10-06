package org.robolectric.internal.bytecode;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.shadows.ShadowCursorAdapter;

@RunWith(JUnit4.class)
public class ShadowMapTest {
  @Test public void shouldLookUpShadowClassesByNamingConvention() throws Exception {
    ShadowMap map = new ShadowMap.Builder().build();
    assertThat(map.get(CursorAdapter.class)).isNull();
  }

  @Test public void shouldNotReturnMismatchedClassesJustBecauseTheSimpleNameMatches() throws Exception {
    ShadowMap map = new ShadowMap.Builder().build();
    assertThat(map.get(android.widget.CursorAdapter.class).shadowClassName).isEqualTo(ShadowCursorAdapter.class.getName());
  }

  @Test public void getInvalidatedClasses_disjoin() {
    ShadowMap current = new ShadowMap.Builder().addShadowClass("a1", "a2", true, false, false).build();
    ShadowMap previous = new ShadowMap.Builder().addShadowClass("b1", "b2", true, false, false).build();

    assertThat(current.getInvalidatedClasses(previous)).containsOnly("a1", "b1");
  }

  @Test public void getInvalidatedClasses_overlap() {
    ShadowMap current = new ShadowMap.Builder()
        .addShadowClass("a1", "a2", true, false, false)
        .addShadowClass("c1", "c2", true, false, false)
        .build();
    ShadowMap previous = new ShadowMap.Builder()
        .addShadowClass("a1", "a2", true, false, false)
        .addShadowClass("c1", "c3", true, false, false)
        .build();

    assertThat(current.getInvalidatedClasses(previous)).containsExactly("c1");
  }

  @Test public void equalsHashCode() throws Exception {
    ShadowMap a = new ShadowMap.Builder().addShadowClass("a", "b", true, false, false).build();
    ShadowMap b = new ShadowMap.Builder().addShadowClass("a", "b", true, false, false).build();
    assertThat(a).isEqualTo(b);
    assertThat(a.hashCode()).isEqualTo(b.hashCode());

    ShadowMap c = b.newBuilder().build();
    assertThat(c).isEqualTo(b);
    assertThat(c.hashCode()).isEqualTo(b.hashCode());

    ShadowMap d = new ShadowMap.Builder().addShadowClass("a", "x", true, false, false).build();
    assertThat(d).isNotEqualTo(a);
    assertThat(d.hashCode()).isNotEqualTo(b.hashCode());
  }

  static class CursorAdapter {
  }

}
