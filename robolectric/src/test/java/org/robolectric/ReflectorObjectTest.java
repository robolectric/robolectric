package org.robolectric;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.util.reflector.Reflector.reflector;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ReflectorObjectTest.ShadowClass;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.ReflectorObject;
import org.robolectric.annotation.internal.Instrument;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Reflector;

/** Tests for @{@link Direct} annotation incorporated inside {@link Reflector}. */
@RunWith(AndroidJUnit4.class)
@Config(shadows = ShadowClass.class)
public class ReflectorObjectTest {

  static final String TEST_STRING = "A test string.";

  private SomeClass someClass;
  private ShadowClass shadowClass;

  @Before
  public void setUp() throws Exception {
    someClass = new SomeClass();
    shadowClass = Shadow.extract(someClass);
  }

  @Test
  public void reflectorObject_shouldCallSameImplementationAsReflector() {
    SomeClassReflector classReflector = shadowClass.objectReflector;
    assertThat(classReflector).isNotNull();

    assertThat(classReflector.someMethod()).isEqualTo(TEST_STRING);
    assertThat(classReflector.someMethod())
        .isEqualTo(reflector(SomeClassReflector.class, shadowClass.realObject).someMethod());
  }

  /** Basic class to be instrumented for testing. */
  @Instrument
  public static class SomeClass {

    String someMethod() {
      return TEST_STRING;
    }
  }

  /** Shadow of {@link SomeClass} that changes all method implementations. */
  @Implements(SomeClass.class)
  public static class ShadowClass {

    @RealObject SomeClass realObject;

    @ReflectorObject SomeClassReflector objectReflector;
  }

  /**
   * Accessor interface for {@link SomeClass}'s internals with the @{@link Direct} annotation at the
   * class level.
   */
  @ForType(value = SomeClass.class)
  interface SomeClassReflector {

    String someMethod();
  }
}
