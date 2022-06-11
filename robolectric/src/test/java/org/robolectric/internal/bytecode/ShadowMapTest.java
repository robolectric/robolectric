package org.robolectric.internal.bytecode;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.android.AndroidSdkShadowMatcher;
import org.robolectric.annotation.Implements;
import org.robolectric.internal.ShadowProvider;
import org.robolectric.sandbox.ShadowMatcher;
import org.robolectric.shadows.ShadowActivity;

@RunWith(JUnit4.class)
public class ShadowMapTest {

  private static final String A = A.class.getName();
  private static final String A1 = A1.class.getName();
  private static final String A2 = A2.class.getName();
  private static final String B = B.class.getName();
  private static final String B1 = B1.class.getName();
  private static final String B2 = B2.class.getName();
  private static final String C1 = C1.class.getName();
  private static final String C2 = C2.class.getName();
  private static final String C3 = C3.class.getName();
  private static final String X = X.class.getName();

  private ShadowMap baseShadowMap;

  @Before
  public void setUp() throws Exception {
    List<ShadowProvider> shadowProviders =
        Collections.singletonList(
            new ShadowProvider() {
              @Override
              public void reset() {}

              @Override
              public String[] getProvidedPackageNames() {
                return new String[0];
              }

              @Override
              public List<Map.Entry<String, String>> getShadows() {
                return Collections.emptyList();
              }
            });
    baseShadowMap = ShadowMap.createFromShadowProviders(shadowProviders);
  }

  @Test public void shouldLookUpShadowClassesByNamingConvention() throws Exception {
    ShadowMap map = baseShadowMap.newBuilder().build();
    assertThat(map.getShadowInfo(Activity.class, ShadowMatcher.MATCH_ALL)).isNull();
  }

  @Test public void shouldNotReturnMismatchedClassesJustBecauseTheSimpleNameMatches() throws Exception {
    ShadowMap map = baseShadowMap.newBuilder()
        .addShadowClasses(ShadowActivity.class)
        .build();
    assertThat(map.getShadowInfo(android.app.Activity.class, ShadowMatcher.MATCH_ALL).shadowClassName)
        .isEqualTo(ShadowActivity.class.getName());
  }

  @Test public void getInvalidatedClasses_disjoin() {
    ShadowMap current = baseShadowMap.newBuilder().addShadowClass(A1, A2, true, false).build();
    ShadowMap previous = baseShadowMap.newBuilder().addShadowClass(B1, B2, true, false).build();

    assertThat(current.getInvalidatedClasses(previous)).containsExactly(A1, B1);
  }

  @Test public void getInvalidatedClasses_overlap() {
    ShadowMap current =
        baseShadowMap
            .newBuilder()
            .addShadowClass(A1, A2, true, false)
            .addShadowClass(C1, C2, true, false)
            .build();
    ShadowMap previous =
        baseShadowMap
            .newBuilder()
            .addShadowClass(A1, A2, true, false)
            .addShadowClass(C1, C3, true, false)
            .build();

    assertThat(current.getInvalidatedClasses(previous)).containsExactly(C1);
  }

  @Test public void equalsHashCode() throws Exception {
    ShadowMap a = baseShadowMap.newBuilder().addShadowClass(A, B, true, false).build();
    ShadowMap b = baseShadowMap.newBuilder().addShadowClass(A, B, true, false).build();
    assertThat(a).isEqualTo(b);
    assertThat(a.hashCode()).isEqualTo(b.hashCode());

    ShadowMap c = b.newBuilder().build();
    assertThat(c).isEqualTo(b);
    assertThat(c.hashCode()).isEqualTo(b.hashCode());

    ShadowMap d = baseShadowMap.newBuilder().addShadowClass(A, X, true, false).build();
    assertThat(d).isNotEqualTo(a);
    assertThat(d.hashCode()).isNotEqualTo(b.hashCode());
  }

  @Test
  public void builtinShadowsForSameClass_differentSdkVersions() {
    ImmutableMultimap<String, String> builtinShadows =
        ImmutableMultimap.of(
            Activity.class.getCanonicalName(),
            ShadowActivity29.class.getName(),
            Activity.class.getCanonicalName(),
            ShadowActivity30.class.getName());
    ImmutableList<ShadowProvider> shadowProviders =
        ImmutableList.of(
            new ShadowProvider() {
              @Override
              public void reset() {}

              @Override
              public String[] getProvidedPackageNames() {
                return new String[0];
              }

              @Override
              public List<Map.Entry<String, String>> getShadows() {
                return new ArrayList<>(builtinShadows.entries());
              }
            });
    ShadowMatcher sdk29 = new AndroidSdkShadowMatcher(29);
    ShadowMatcher sdk30 = new AndroidSdkShadowMatcher(30);
    ShadowMap map = ShadowMap.createFromShadowProviders(shadowProviders);
    assertThat(map.getShadowInfo(Activity.class, sdk29).shadowClassName)
        .isEqualTo(ShadowActivity29.class.getName());
    assertThat(map.getShadowInfo(Activity.class, sdk30).shadowClassName)
        .isEqualTo(ShadowActivity30.class.getName());
  }

  static class Activity {}

  static class A {}
  static class A1 {}
  static class A2 {}
  static class B {}
  static class B1 {}
  static class B2 {}
  static class C1 {}
  static class C2 {}
  static class C3 {}
  static class X {}

  @Implements(value = Activity.class, maxSdk = 29)
  static class ShadowActivity29 {}

  @Implements(value = Activity.class, minSdk = 30)
  static class ShadowActivity30 {}
}
