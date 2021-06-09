package org.robolectric;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.util.reflector.Reflector.reflector;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.DirectReflectorTest.ShadowClass;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.internal.Instrument;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Reflector;
import org.robolectric.util.reflector.Static;

/** Tests for @{@link Direct} annotation incorporated inside {@link Reflector}. */
@RunWith(AndroidJUnit4.class)
@Config(shadows = ShadowClass.class)
public class DirectReflectorTest {

  private SomeClass someClass;

  @Before
  public void setUp() throws Exception {
    someClass = new SomeClass();
  }

  @Test
  public void shouldCallShadowImplementation_public() {
    assertThat(someClass.somePublicMethod()).isFalse();
  }

  @Test
  public void shouldCallShadowImplementation_private() {
    assertThat(someClass.somePrivateMethod()).isFalse();
  }

  @Test
  public void shouldCallShadowImplementation_static() {
    assertThat(SomeClass.someStaticMethod()).isFalse();
  }

  @Test
  public void classLevelReflector_shouldCallOriginalImplementation_public() {
    assertThat(reflector(ClassLevelReflector.class, someClass).somePublicMethod()).isTrue();
  }

  @Test
  public void classLevelReflector_shouldCallOriginalImplementation_private() {
    assertThat(reflector(ClassLevelReflector.class, someClass).somePrivateMethod()).isTrue();
  }

  @Test
  public void classLevelReflector_shouldCallOriginalImplementation_static() {
    assertThat(reflector(ClassLevelReflector.class, someClass).someStaticMethod()).isTrue();
  }

  @Test
  public void methodLevelReflector_shouldCallOriginalImplementation_public() {
    assertThat(reflector(MethodLevelReflector.class, someClass).somePublicMethod()).isTrue();
  }

  @Test
  public void methodLevelReflector_shouldCallOriginalImplementation_private() {
    assertThat(reflector(MethodLevelReflector.class, someClass).somePrivateMethod()).isTrue();
  }

  @Test
  public void methodLevelReflector_shouldCallOriginalImplementation_static() {
    assertThat(reflector(MethodLevelReflector.class, someClass).someStaticMethod()).isTrue();
  }

  /** Basic class to be instrumented for testing. */
  @Instrument
  public static class SomeClass {

    public boolean somePublicMethod() {
      return true;
    }

    private boolean somePrivateMethod() {
      return true;
    }

    public static boolean someStaticMethod() {
      return true;
    }
  }

  /** Shadow of {@link SomeClass} that changes all method implementations. */
  @Implements(SomeClass.class)
  public static class ShadowClass {

    @Implementation
    public boolean somePublicMethod() {
      return false;
    }

    @Implementation
    protected boolean somePrivateMethod() {
      return false;
    }

    @Implementation
    public static boolean someStaticMethod() {
      return false;
    }
  }

  /**
   * Accessor interface for {@link SomeClass}'s internals with the @{@link Direct} annotation at the
   * class level.
   */
  @ForType(value = SomeClass.class, direct = true)
  interface ClassLevelReflector {

    boolean somePublicMethod();

    boolean somePrivateMethod();

    @Static
    boolean someStaticMethod();
  }

  /**
   * Accessor interface for {@link SomeClass}'s internals with @{@link Direct} annotations at the
   * method level.
   */
  @ForType(SomeClass.class)
  interface MethodLevelReflector {

    @Direct
    boolean somePublicMethod();

    @Direct
    boolean somePrivateMethod();

    @Static
    @Direct
    boolean someStaticMethod();
  }
}
