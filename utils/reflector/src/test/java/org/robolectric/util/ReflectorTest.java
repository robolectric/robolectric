package org.robolectric.util;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.util.Reflector.reflector;

import java.util.Collections;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.Reflector.ForType;
import org.robolectric.util.Reflector.WithType;

@RunWith(JUnit4.class)
public class ReflectorTest {

  @Test
  public void reflector_shouldCallPrivateMethod() throws Exception {
    SomeClass someClass = new SomeClass("c");
    assertThat(reflector(_SomeClass_.class, someClass).someMethod("a", "b"))
        .isEqualTo("a-b-c (someMethod)");
  }

  @Test
  public void reflector_shouldHonorWithTypeAnnotationForParams() throws Exception {
    SomeClass someClass = new SomeClass("c");
    assertThat(reflector(_SomeClass_.class, someClass).anotherMethod("a", "b"))
        .isEqualTo("a-b-c (anotherMethod)");
  }

  @Test
  public void reflector_defaultMethodsShouldWork() throws Exception {
    SomeClass someClass = new SomeClass("c");
    _SomeClass_ reflector = reflector(_SomeClass_.class, someClass);

    assertThat(reflector.defaultMethod("someMethod", "a", "b"))
        .isEqualTo("a-b-c (someMethod)");
    assertThat(reflector.defaultMethod("anotherMethod", "a", "b"))
        .isEqualTo("a-b-c (anotherMethod)");
  }

  @Test
  public void reflector_shouldUnboxReturnValues() throws Exception {
    SomeClass someClass = new SomeClass("c");
    assertThat(reflector(_SomeClass_.class, someClass).returnLong())
        .isEqualTo(1234L);
  }

  @Ignore @Test
  public void perf() throws Exception {
    SomeClass i = new SomeClass("c");

    System.out.println("reflection = " + Collections.singletonList(byReflection(i)));
    System.out.println("accessor = " + Collections.singletonList(byAccessor(i)));

    _SomeClass_ accessor = reflector(_SomeClass_.class, i);

    time("reflection", 10_000_000, () -> byReflection(i));
    time("accessor", 10_000_000, () -> byAccessor(i));
    time("saved accessor", 10_000_000, () -> bySavedAccessor(accessor));

    time("reflection", 10_000_000, () -> byReflection(i));
    time("accessor", 10_000_000, () -> byAccessor(i));
    time("saved accessor", 10_000_000, () -> bySavedAccessor(accessor));
  }

  //////////////////////

  @ForType(SomeClass.class)
  interface _SomeClass_ {

    String someMethod(String a, String b);

    String anotherMethod(@WithType("java.lang.String") Object a, String b);

    default String defaultMethod(String which, String a, String b) {
      switch (which) {
        case "someMethod":
          return someMethod(a, b);
        case "anotherMethod":
          return anotherMethod(a, b);
        default:
          throw new IllegalStateException(which);
      }
    }

    long returnLong();
  }

  static class SomeClass {

    private String c;

    SomeClass(String c) {
      this.c = c;
    }

    @SuppressWarnings("unused")
    private String someMethod(String a, String b) {
      return a + "-" + b + "-" + c + " (someMethod)";
    }

    @SuppressWarnings("unused")
    private String anotherMethod(String a, String b) {
      return a + "-" + b + "-" + c + " (anotherMethod)";
    }

    @SuppressWarnings("unused")
    private long returnLong() {
      return 1234L;
    }
  }

  private void time(String name, int times, Runnable runnable) {
    long startTime = System.currentTimeMillis();
    for (int i = 0; i < times; i++) {
      runnable.run();
    }
    long elasedMs = System.currentTimeMillis() - startTime;
    System.out.println(name + " took " + elasedMs);
  }

  private String byReflection(SomeClass i) {
    return ReflectionHelpers.callInstanceMethod(i, "someMethod",
        ClassParameter.from(String.class, "a"),
        ClassParameter.from(String.class, "b"));
  }

  private String byAccessor(SomeClass i) {
    _SomeClass_ accessor = reflector(_SomeClass_.class, i);
    return accessor.someMethod("a", "b");
  }

  private String bySavedAccessor(_SomeClass_ accessor) {
    return accessor.someMethod("a", "b");
  }

}
