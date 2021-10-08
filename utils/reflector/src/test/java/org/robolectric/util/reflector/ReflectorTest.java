package org.robolectric.util.reflector;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;
import static org.robolectric.util.reflector.Reflector.reflector;

import java.util.Collections;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

@RunWith(JUnit4.class)
public class ReflectorTest {

  private SomeClass someClass;
  private _SomeClass_ reflector;
  private _SomeClass_ staticReflector;

  @Before
  public void setUp() throws Exception {
    someClass = new SomeClass("c");
    reflector = reflector(_SomeClass_.class, someClass);

    staticReflector = reflector(_SomeClass_.class, null);
  }

  @Test
  public void reflector_shouldCallPrivateMethod() {
    assertThat(reflector.someMethod("a", "b")).isEqualTo("a-b-c (someMethod)");
  }

  @Test
  public void reflector_shouldHonorWithTypeAnnotationForParams() {
    assertThat(reflector.anotherMethod("a", "b")).isEqualTo("a-b-c (anotherMethod)");
  }

  @Test
  public void reflector_defaultMethodsShouldWork() {
    assertThat(reflector.defaultMethod("someMethod", "a", "b")).isEqualTo("a-b-c (someMethod)");
    assertThat(reflector.defaultMethod("anotherMethod", "a", "b"))
        .isEqualTo("a-b-c (anotherMethod)");
  }

  @Test
  public void reflector_shouldUnboxReturnValues() {
    assertThat(reflector.returnLong()).isEqualTo(1234L);
  }

  @Test
  public void reflector_shouldCallStaticMethod() {
    assertThat(reflector.someStaticMethod("a", "b")).isEqualTo("a-b (someStaticMethod)");

    assertThat(staticReflector.someStaticMethod("a", "b")).isEqualTo("a-b (someStaticMethod)");
  }

  @Test
  public void reflector_fieldAccessors() {
    assertThat(reflector.getC()).isEqualTo("c");

    reflector.setC("c++");
    assertThat(reflector.getC()).isEqualTo("c++");
  }

  @Test
  public void reflector_primitiveFieldAccessors() {
    assertThat(reflector.getD()).isEqualTo(0);

    reflector.setD(1234);
    assertThat(reflector.getD()).isEqualTo(1234);
  }

  @Test
  public void reflector_staticFieldAccessors() {
    assertThat(reflector.getEStatic()).isEqualTo(null);

    reflector.setEStatic("eee!");
    assertThat(reflector.getEStatic()).isEqualTo("eee!");
  }

  @Test
  public void reflector_throwsCorrectExceptions() {
    Throwable expected = new ArrayIndexOutOfBoundsException();
    Throwable actual = null;
    try {
      reflector.throwException(expected);
      fail("should have failed");
    } catch (Exception thrown) {
      actual = thrown;
    }
    assertThat(actual).isSameInstanceAs(expected);
  }

  @Ignore
  @Test
  public void methodPerf() {
    SomeClass i = new SomeClass("c");

    System.out.println("reflection = " + Collections.singletonList(methodByReflectionHelpers(i)));
    System.out.println("accessor = " + Collections.singletonList(methodByReflector(i)));

    _SomeClass_ accessor = reflector(_SomeClass_.class, i);

    time("ReflectionHelpers", 10_000_000, () -> methodByReflectionHelpers(i));
    time("accessor", 10_000_000, () -> methodByReflector(i));
    time("saved accessor", 10_000_000, () -> methodBySavedReflector(accessor));

    time("ReflectionHelpers", 10_000_000, () -> methodByReflectionHelpers(i));
    time("accessor", 10_000_000, () -> methodByReflector(i));
    time("saved accessor", 10_000_000, () -> methodBySavedReflector(accessor));
  }

  @Ignore
  @Test
  public void fieldPerf() {
    SomeClass i = new SomeClass("c");

    System.out.println("reflection = " + Collections.singletonList(fieldByReflectionHelpers(i)));
    System.out.println("accessor = " + Collections.singletonList(fieldByReflector(i)));

    _SomeClass_ accessor = reflector(_SomeClass_.class, i);

    time("ReflectionHelpers", 10_000_000, () -> fieldByReflectionHelpers(i));
    time("accessor", 10_000_000, () -> fieldByReflector(i));
    time("saved accessor", 10_000_000, () -> fieldBySavedReflector(accessor));

    time("ReflectionHelpers", 10_000_000, () -> fieldByReflectionHelpers(i));
    time("accessor", 10_000_000, () -> fieldByReflector(i));
    time("saved accessor", 10_000_000, () -> fieldBySavedReflector(accessor));
  }

  @Test
  public void nonExistentMethod_throwsAssertionError() {
    SomeClass i = new SomeClass("c");
    _SomeClass_ accessor = reflector(_SomeClass_.class, i);
    AssertionError ex =
        assertThrows(AssertionError.class, () -> accessor.nonExistentMethod("a", "b", "c"));
    assertThat(ex).hasMessageThat().startsWith("Error invoking reflector method in ClassLoader ");
    assertThat(ex).hasCauseThat().isInstanceOf(NoSuchMethodException.class);
  }

  //////////////////////

  /** Accessor interface for {@link SomeClass}'s internals. */
  @ForType(SomeClass.class)
  interface _SomeClass_ {

    @Static
    String someStaticMethod(String a, String b);

    @Static @Accessor("eStatic")
    void setEStatic(String value);

    @Static @Accessor("eStatic")
    String getEStatic();

    @Accessor("c")
    void setC(String value);

    @Accessor("c")
    String getC();

    @Accessor("mD")
    void setD(int value);

    @Accessor("mD")
    int getD();

    String someMethod(String a, String b);

    String nonExistentMethod(String a, String b, String c);

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

    void throwException(Throwable t);
  }

  @SuppressWarnings("unused")
  static class SomeClass {

    private static String eStatic;
    private String c;
    private int mD;

    SomeClass(String c) {
      this.c = c;
    }

    private static String someStaticMethod(String a, String b) {
      return a + "-" + b + " (someStaticMethod)";
    }

    private String someMethod(String a, String b) {
      return a + "-" + b + "-" + c + " (someMethod)";
    }

    private String anotherMethod(String a, String b) {
      return a + "-" + b + "-" + c + " (anotherMethod)";
    }

    private long returnLong() {
      return 1234L;
    }

    @SuppressWarnings("unused")
    private void throwException(Throwable t) throws Throwable {
      throw t;
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

  private String methodByReflectionHelpers(SomeClass o) {
    return ReflectionHelpers.callInstanceMethod(
        o,
        "someMethod",
        ClassParameter.from(String.class, "a"),
        ClassParameter.from(String.class, "b"));
  }

  private String methodByReflector(SomeClass o) {
    _SomeClass_ accessor = reflector(_SomeClass_.class, o);
    return accessor.someMethod("a", "b");
  }

  private String methodBySavedReflector(_SomeClass_ reflector) {
    return reflector.someMethod("a", "b");
  }

  private String fieldByReflectionHelpers(SomeClass o) {
    ReflectionHelpers.setField(o, "c", "abc");
    return ReflectionHelpers.getField(o, "c");
  }

  private String fieldByReflector(SomeClass o) {
    reflector(_SomeClass_.class, o).setC("abc");
    return reflector(_SomeClass_.class, o).getC();
  }

  private String fieldBySavedReflector(_SomeClass_ reflector) {
    reflector.setC("abc");
    return reflector.getC();
  }
}
