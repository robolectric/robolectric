package org.robolectric.integrationtests.mockito;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.spy;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.integrationtests.mockito.SpyTest.ShadowFoo;
import org.robolectric.util.ReflectionHelpers;

/** Tests for an Mockito spies with Robolectric. */
@RunWith(RobolectricTestRunner.class)
@Config(shadows = ShadowFoo.class)
public final class SpyTest {

  /**
   * This captures an esoteric issue where Robolectric's use {@link RealObject} may cause problems
   * with Mockito spies.
   *
   * @see <a href "https://github.com/mockito/mockito/issues/2552">The mockito issue</a>
   */
  @Test
  @Ignore("https://github.com/mockito/mockito/issues/2552")
  public void spy_shadowUpdatingFieldWithReflection() {
    Foo f = new Foo();
    Foo spyFoo = spy(f);
    spyFoo.setA(100);
    assertThat(spyFoo.getA()).isEqualTo(100);
  }

  /** A simple class with an int field. */
  public static class Foo {

    int a;

    public void setA(int value) {
      this.a = value;
    }

    public int getA() {
      return this.a;
    }
  }

  /** This class shadows {@link Foo#setA(int)} to set 'a' using reflection. */
  @Implements(Foo.class)
  public static class ShadowFoo {
    @RealObject Foo f;

    @Implementation
    protected void setA(int value) {
      ReflectionHelpers.setField(f, "a", value);
    }
  }
}
