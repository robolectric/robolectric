package org.robolectric.internal.bytecode;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.Shadows;
import org.robolectric.internal.ShadowProvider;
import org.robolectric.shadows.ShadowCursorAdapter;

@RunWith(JUnit4.class)
public class ShadowMapTest {

  private ShadowMap baseShadowMap;

  @Before
  public void setUp() throws Exception {
    Iterable<ShadowProvider> shadowProviders = Collections.singletonList(new Shadows());
    baseShadowMap = ShadowMap.createFromShadowProviders(shadowProviders);
  }

  @Test public void shouldLookUpShadowClassesByNamingConvention() throws Exception {
    ShadowMap map = baseShadowMap.newBuilder().build();
    assertThat(map.getShadowInfo(CursorAdapter.class, -1)).isNull();
  }

  @Test public void shouldNotReturnMismatchedClassesJustBecauseTheSimpleNameMatches() throws Exception {
    ShadowMap map = baseShadowMap.newBuilder().build();
    assertThat(map.getShadowInfo(android.widget.CursorAdapter.class, -1).shadowClassName)
        .isEqualTo(ShadowCursorAdapter.class.getName());
  }

  @Test public void getInvalidatedClasses_disjoin() {
    ShadowMap current = baseShadowMap.newBuilder().addShadowClass("a1", "a2", true, false, false).build();
    ShadowMap previous = baseShadowMap.newBuilder().addShadowClass("b1", "b2", true, false, false).build();

    assertThat(current.getInvalidatedClasses(previous)).containsOnly("a1", "b1");
  }

  @Test public void getInvalidatedClasses_overlap() {
    ShadowMap current = baseShadowMap.newBuilder()
        .addShadowClass("a1", "a2", true, false, false)
        .addShadowClass("c1", "c2", true, false, false)
        .build();
    ShadowMap previous = baseShadowMap.newBuilder()
        .addShadowClass("a1", "a2", true, false, false)
        .addShadowClass("c1", "c3", true, false, false)
        .build();

    assertThat(current.getInvalidatedClasses(previous)).containsExactly("c1");
  }

  @Test public void equalsHashCode() throws Exception {
    ShadowMap a = baseShadowMap.newBuilder().addShadowClass("a", "b", true, false, false).build();
    ShadowMap b = baseShadowMap.newBuilder().addShadowClass("a", "b", true, false, false).build();
    assertThat(a).isEqualTo(b);
    assertThat(a.hashCode()).isEqualTo(b.hashCode());

    ShadowMap c = b.newBuilder().build();
    assertThat(c).isEqualTo(b);
    assertThat(c.hashCode()).isEqualTo(b.hashCode());

    ShadowMap d = baseShadowMap.newBuilder().addShadowClass("a", "x", true, false, false).build();
    assertThat(d).isNotEqualTo(a);
    assertThat(d.hashCode()).isNotEqualTo(b.hashCode());
  }

  static class CursorAdapter {
  }

}
