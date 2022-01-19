package org.robolectric;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.robolectric.shadow.api.Shadow.invokeConstructor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ShadowConstructorTest.ShadowBar;
import org.robolectric.ShadowConstructorTest.ShadowFoo;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.internal.Instrument;
import org.robolectric.internal.SandboxTestRunner;
import org.robolectric.internal.bytecode.SandboxConfig;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/**
 * Tests for some nuances of constructor shadowing, particularly when shadowing constructor
 * hierarchies.
 */
@RunWith(SandboxTestRunner.class)
@SandboxConfig(shadows = {ShadowFoo.class, ShadowBar.class})
public class ShadowConstructorTest {
  @Test
  public void constructorShadows_invokesAllConstructors() {
    Bar b = new Bar(10);
    assertThat(b.a).isEqualTo(1);
    assertThat(b.b).isEqualTo(11);
  }

  @Test
  @SandboxConfig(shadows = {ShadowFooWithStaticConstructor.class})
  public void staticConstructor_isNotAllowed() {
    BootstrapMethodError e = assertThrows(BootstrapMethodError.class, () -> new Foo(1));
    assertThat(e)
        .hasCauseThat()
        .hasMessageThat()
        .isEqualTo("static __constructor__ shadow methods are not supported");
  }

  @Instrument
  static class Foo {
    int a;

    Foo() {}

    Foo(int a) {
      this.a = a;
    }
  }

  @Instrument
  static class Bar extends Foo {
    int b;

    Bar(int b) {
      super(1);
      this.b = b + this.a;
    }
  }

  /**
   * Shadow for {@link org.robolectric.ShadowConstructorTest.Foo}
   *
   * <p>This just shadows {@link Foo#Foo(int)} to invoke the original constructor.
   */
  @Implements(Foo.class)
  public static class ShadowFoo {
    @RealObject protected Foo realFoo;

    @Implementation
    protected void __constructor__(int a) {
      invokeConstructor(Foo.class, realFoo, ClassParameter.from(int.class, a));
    }
  }

  /**
   * Shadow for {@link org.robolectric.ShadowConstructorTest.Bar}
   *
   * <p>Similar to {@link ShadowFoo}, this just shadows {@link Bar#Bar(int)} and invokes the
   * original.
   */
  @Implements(Bar.class)
  public static class ShadowBar extends ShadowFoo {
    @RealObject protected Bar realBar;

    @Implementation
    @Override
    protected void __constructor__(int b) {
      invokeConstructor(Bar.class, realBar, ClassParameter.from(int.class, b));
    }
  }

  /** Shadow for {@link org.robolectric.ShadowConstructorTest.Foo} */
  @Implements(Foo.class)
  public static class ShadowFooWithStaticConstructor {
    @RealObject protected Foo realFoo;

    @Implementation
    protected static void __constructor__(int a) {}
  }
}
