package org.robolectric.bytecode;

import android.widget.CursorAdapter;
import org.junit.Test;
import org.robolectric.shadows.ShadowCursorAdapter;

import static org.fest.assertions.api.Assertions.assertThat;

public class ShadowMapTest {
  @Test public void shouldLookUpShadowClassesByNamingConvention() throws Exception {
    ShadowMap map = new ShadowMap.Builder().build();
    assertThat(map.get(android.support.v4.widget.CursorAdapter.class)).isNull();
  }

  @Test public void shouldNotReturnMismatchedClassesJustBecauseTheSimpleNameMatches() throws Exception {
    ShadowMap map = new ShadowMap.Builder().build();
    assertThat(map.get(CursorAdapter.class).shadowClassName).isEqualTo(ShadowCursorAdapter.class.getName());
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
}
