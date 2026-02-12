package org.robolectric;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.ClassName;
import org.robolectric.annotation.Filter;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.internal.Instrument;
import org.robolectric.internal.SandboxTestRunner;
import org.robolectric.internal.bytecode.SandboxConfig;
import org.robolectric.internal.bytecode.ShadowConstants;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.testing.AnUninstrumentedClass;
import org.robolectric.testing.Pony;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

@RunWith(SandboxTestRunner.class)
public class ShadowingTest {

  @Test
  @SandboxConfig(shadows = {ShadowAccountManagerForTests.class})
  public void testStaticMethodsAreDelegated() {
    Object arg = mock(Object.class);
    AccountManager.get(arg);
    assertThat(ShadowAccountManagerForTests.wasCalled).isTrue();
    assertThat(ShadowAccountManagerForTests.arg).isSameInstanceAs(arg);
  }

  @Implements(AccountManager.class)
  public static class ShadowAccountManagerForTests {
    public static boolean wasCalled = false;
    public static Object arg;

    public static AccountManager get(Object arg) {
      wasCalled = true;
      ShadowAccountManagerForTests.arg = arg;
      return mock(AccountManager.class);
    }
  }

  static class Context {}

  static class AccountManager {
    public static AccountManager get(Object arg) {
      return null;
    }
  }

  @Test
  @SandboxConfig(shadows = {ShadowClassWithProtectedMethod.class})
  public void testProtectedMethodsAreDelegated() {
    ClassWithProtectedMethod overlay = new ClassWithProtectedMethod();
    assertEquals("shadow name", overlay.getName());
  }

  @Implements(ClassWithProtectedMethod.class)
  public static class ShadowClassWithProtectedMethod {
    @Implementation
    protected String getName() {
      return "shadow name";
    }
  }

  @Instrument
  public static class ClassWithProtectedMethod {
    protected String getName() {
      return "protected name";
    }
  }

  @Test
  @SandboxConfig(shadows = {ShadowPaintForTests.class})
  public void testNativeMethodsAreDelegated() {
    Paint paint = new Paint();
    paint.setColor(1234);

    assertThat(paint.getColor()).isEqualTo(1234);
  }

  @Instrument
  static class Paint {
    public native void setColor(int color);

    public native int getColor();
  }

  @Implements(Paint.class)
  public static class ShadowPaintForTests {
    private int color;

    @Implementation
    protected void setColor(int color) {
      this.color = color;
    }

    @Implementation
    protected int getColor() {
      return color;
    }
  }

  @Implements(ClassWithNoDefaultConstructor.class)
  public static class ShadowForClassWithNoDefaultConstructor {
    public static boolean shadowDefaultConstructorCalled = false;
    public static boolean shadowDefaultConstructorImplementorCalled = false;

    public ShadowForClassWithNoDefaultConstructor() {
      shadowDefaultConstructorCalled = true;
    }

    @Implementation
    protected void __constructor__() {
      shadowDefaultConstructorImplementorCalled = true;
    }
  }

  @Instrument
  @SuppressWarnings({"UnusedDeclaration"})
  public static class ClassWithNoDefaultConstructor {
    ClassWithNoDefaultConstructor(String string) {}
  }

  @Test
  @SandboxConfig(shadows = {Pony.ShadowPony.class})
  public void directlyOn_shouldCallThroughToOriginalMethodBody() {
    Pony pony = new Pony();

    assertEquals("Fake whinny! You're on my neck!", pony.ride("neck"));
    assertEquals(
        "Whinny! You're on my neck!",
        Shadow.directlyOn(pony, Pony.class, "ride", ClassParameter.from(String.class, "neck")));

    assertEquals("Fake whinny! You're on my haunches!", pony.ride("haunches"));
  }

  @Test
  @SandboxConfig(shadows = {Pony.ShadowPony.class})
  public void shouldCallRealForUnshadowedMethod() {
    assertEquals("Off I saunter to the salon!", new Pony().saunter("the salon"));
  }

  static class TextView {}

  static class ColorStateList {
    public ColorStateList(int[][] ints, int[] ints1) {}
  }

  static class TypedArray {}

  @Implements(TextView.class)
  public static class TextViewWithDummyGetTextColorsMethod {
    public static ColorStateList getTextColors(Context context, TypedArray attrs) {
      return new ColorStateList(new int[0][0], new int[0]);
    }
  }

  @Test
  @SandboxConfig(shadows = ShadowOfClassWithSomeConstructors.class)
  public void shouldGenerateSeparatedConstructorBodies() throws Exception {
    ClassWithSomeConstructors o = new ClassWithSomeConstructors("my name");
    assertNull(o.name);

    Method realConstructor =
        o.getClass().getDeclaredMethod(ShadowConstants.CONSTRUCTOR_METHOD_NAME, String.class);
    realConstructor.setAccessible(true);
    realConstructor.invoke(o, "my name");
    assertEquals("my name", o.name);
  }

  @Instrument
  public static class ClassWithSomeConstructors {
    public String name;

    public ClassWithSomeConstructors(String name) {
      this.name = name;
    }
  }

  @Implements(ClassWithSomeConstructors.class)
  public static class ShadowOfClassWithSomeConstructors {
    @Implementation
    protected void __constructor__(String s) {}
  }

  @Test
  @SandboxConfig(shadows = {ShadowApiImplementedClass.class})
  public void withNonApiSubclassesWhichExtendApi_shouldStillBeInvoked() {
    assertEquals("did foo", new NonApiSubclass().doSomething("foo"));
  }

  public static class NonApiSubclass extends ApiImplementedClass {
    public String doSomething(String value) {
      return "did " + value;
    }
  }

  @Instrument
  public static class ApiImplementedClass {}

  @Implements(ApiImplementedClass.class)
  public static class ShadowApiImplementedClass {}

  @Test
  public void shouldNotInstrumentClassIfNotAddedToConfig() {
    assertEquals(1, new NonInstrumentedClass().plus(0));
  }

  @Test
  @SandboxConfig(shadows = {ShadowNonInstrumentedClass.class})
  public void shouldInstrumentClassIfAddedToConfig() {
    assertEquals(2, new NonInstrumentedClass().plus(0));
  }

  public static class NonInstrumentedClass {
    public int plus(int x) {
      return x + 1;
    }
  }

  @Implements(NonInstrumentedClass.class)
  public static class ShadowNonInstrumentedClass {
    @Implementation
    protected int plus(int x) {
      return x + 2;
    }
  }

  @Test
  public void shouldNotInstrumentPackageIfNotAddedToConfig() throws Exception {
    Class<?> clazz = Class.forName(AnUninstrumentedClass.class.getName());
    assertTrue(Modifier.isFinal(clazz.getModifiers()));
  }

  @Test
  @SandboxConfig(instrumentedPackages = {"org.robolectric.testing"})
  public void shouldInstrumentPackageIfAddedToConfig() throws Exception {
    Class<?> clazz = Class.forName(AnUninstrumentedClass.class.getName());
    assertFalse(Modifier.isFinal(clazz.getModifiers()));
  }

  @Test
  @SandboxConfig(shadows = {ShadowClassWithInstanceFilterIgnoringReturn.class})
  public void testInstanceFilterIgnoringReturn() {
    ClassWithFilter instance = new ClassWithFilter();
    String result = instance.doSomething("foo");
    assertThat(result).isEqualTo("foo"); // Original value preserved
    assertThat(ShadowClassWithInstanceFilterIgnoringReturn.filterCalled).isTrue();
  }

  @Implements(ClassWithFilter.class)
  public static class ShadowClassWithInstanceFilterIgnoringReturn {
    public static boolean filterCalled = false;

    @Filter
    protected void doSomething(String input) {
      filterCalled = true;
    }
  }

  @Test
  @SandboxConfig(shadows = {ShadowClassWithStaticFilterIgnoringReturn.class})
  public void testStaticFilterIgnoringReturn() {
    ClassWithFilter instance = new ClassWithFilter();
    String result = instance.doSomething("foo");
    assertThat(result).isEqualTo("foo"); // Original value preserved
    assertThat(ShadowClassWithStaticFilterIgnoringReturn.filterCalled).isTrue();
  }

  @Implements(ClassWithFilter.class)
  public static class ShadowClassWithStaticFilterIgnoringReturn {
    public static boolean filterCalled = false;

    @Filter
    protected static void doSomething(String input) {
      filterCalled = true;
    }
  }

  @Test
  @SandboxConfig(shadows = {ShadowClassWithStaticFilterOnStaticMethodIgnoringReturn.class})
  public void testStaticFilterOnStaticMethodIgnoringReturn() {
    String result = ClassWithStaticFilter.doSomethingStatic("foo");
    assertThat(result).isEqualTo("foo"); // Original value preserved
    assertThat(ShadowClassWithStaticFilterOnStaticMethodIgnoringReturn.filterCalled).isTrue();
  }

  @Implements(ClassWithStaticFilter.class)
  public static class ShadowClassWithStaticFilterOnStaticMethodIgnoringReturn {
    public static boolean filterCalled = false;

    @Filter
    protected static void doSomethingStatic(String input) {
      filterCalled = true;
    }
  }

  @Test
  @SandboxConfig(shadows = {ShadowClassWithFilter.class})
  public void testFilterMethodIsCalled() {
    ShadowClassWithFilter.reset();
    ClassWithFilter instance = new ClassWithFilter();
    String result = instance.doSomething("foo");
    assertThat(result).isEqualTo("foo"); // Original code executed
    assertThat(ShadowClassWithFilter.filterCalled).isTrue();
    assertThat(ShadowClassWithFilter.capturedReturnValue).isEqualTo("foo");
    assertThat(ShadowClassWithFilter.capturedInput).isEqualTo("foo");
  }

  @Test
  @SandboxConfig(shadows = {ShadowClassWithFilterChangingReturn.class})
  public void testFilterMethodCanChangeReturn() {
    ClassWithFilter instance = new ClassWithFilter();
    String result = instance.doSomething("foo");
    assertThat(result).isEqualTo("hooked: foo");
  }

  @Test
  @SandboxConfig(shadows = {ShadowClassWithVoidFilter.class})
  public void testVoidFilterPreservesReturnValue() {
    ClassWithFilter instance = new ClassWithFilter();
    String result = instance.doSomething("foo");
    assertThat(result).isEqualTo("foo"); // Original value preserved
    assertThat(ShadowClassWithVoidFilter.filterCalled).isTrue();
  }

  @Implements(ClassWithFilter.class)
  public static class ShadowClassWithVoidFilter {
    public static boolean filterCalled = false;

    @Filter
    protected static void doSomething(String returnValue, String input) {
      filterCalled = true;
    }
  }

  @Test
  @SandboxConfig(shadows = {ShadowClassWithFilterUsingClassName.class})
  public void testFilterWithClassName() {
    ClassWithFilter instance = new ClassWithFilter();
    String result = instance.doSomething("foo");
    assertThat(result).isEqualTo("hooked: foo");
    assertThat(ShadowClassWithFilterUsingClassName.filterCalled).isTrue();
  }

  @Implements(ClassWithFilter.class)
  public static class ShadowClassWithFilterUsingClassName {
    public static boolean filterCalled = false;

    @Filter
    protected static String doSomething(
        String returnValue, @ClassName("java.lang.String") Object input) {
      filterCalled = true;
      return "hooked: " + returnValue;
    }
  }

  @Test
  @SandboxConfig(shadows = {ShadowClassWithFilterUsingMethodName.class})
  public void testFilterWithMethodName() {
    ClassWithFilter instance = new ClassWithFilter();
    String result = instance.doSomething("foo");
    assertThat(result).isEqualTo("hooked: foo");
    assertThat(ShadowClassWithFilterUsingMethodName.filterCalled).isTrue();
  }

  @Test
  @SandboxConfig(shadows = {ShadowClassWithInstanceFilterNonVoid.class})
  public void testInstanceFilterOnNonVoidMethod() {
    ClassWithFilter instance = new ClassWithFilter();
    String result = instance.doSomething("foo");
    assertThat(result).isEqualTo("hooked: foo");
    assertThat(ShadowClassWithInstanceFilterNonVoid.filterCalled).isTrue();
  }

  @Implements(ClassWithFilter.class)
  public static class ShadowClassWithInstanceFilterNonVoid {
    public static boolean filterCalled = false;

    @Filter
    protected String doSomething(String returnValue, String input) {
      filterCalled = true;
      return "hooked: " + returnValue;
    }
  }

  @Implements(ClassWithFilter.class)
  public static class ShadowClassWithFilterUsingMethodName {
    public static boolean filterCalled = false;

    @Filter(methodName = "doSomething")
    protected static String myFilterMethod(String returnValue, String input) {
      filterCalled = true;
      return "hooked: " + returnValue;
    }
  }

  @Instrument
  public static class ClassWithFilter {
    public String doSomething(String input) {
      return input;
    }
  }

  @Implements(ClassWithFilter.class)
  public static class ShadowClassWithFilter {
    public static boolean filterCalled = false;
    public static String capturedReturnValue;
    public static ClassWithFilter capturedReal;
    public static String capturedInput;

    public static void reset() {
      filterCalled = false;
      capturedReturnValue = null;
      capturedReal = null;
      capturedInput = null;
    }

    @Filter
    protected static void doSomething(String returnValue, String input) {
      filterCalled = true;
      capturedReturnValue = returnValue;
      capturedInput = input;
    }
  }

  @Implements(ClassWithFilter.class)
  public static class ShadowClassWithFilterChangingReturn {
    @Filter
    protected static String doSomething(String returnValue, String input) {
      return "hooked: " + returnValue;
    }
  }

  @Test
  @SandboxConfig(shadows = {ShadowClassWithConstructorFilter.class})
  public void testConstructorFilter() {
    ShadowClassWithConstructorFilter.reset();
    ClassWithConstructorFilter instance = new ClassWithConstructorFilter("bar");
    assertThat(instance.name).isEqualTo("hooked: bar"); // Modified by hook
    assertThat(ShadowClassWithConstructorFilter.filterCalled).isTrue();
    assertThat(ShadowClassWithConstructorFilter.capturedReal).isEqualTo(instance);
    assertThat(ShadowClassWithConstructorFilter.capturedInput).isEqualTo("bar");
  }

  @Instrument
  public static class ClassWithConstructorFilter {
    public String name;

    public ClassWithConstructorFilter(String name) {
      this.name = name;
    }
  }

  @Implements(ClassWithConstructorFilter.class)
  public static class ShadowClassWithConstructorFilter {
    @RealObject ClassWithConstructorFilter real;
    public static boolean filterCalled = false;
    public static ClassWithConstructorFilter capturedReal;
    public static String capturedInput;

    public static void reset() {
      filterCalled = false;
      capturedReal = null;
      capturedInput = null;
    }

    @Filter
    protected void __constructor__(String name) {
      filterCalled = true;
      capturedReal = real;
      capturedInput = name;
      real.name = "hooked: " + name;
    }
  }

  @Test
  @SandboxConfig(shadows = {ShadowClassWithStaticFilter.class})
  public void testStaticMethodFilter() {
    ShadowClassWithStaticFilter.reset();
    String result = ClassWithStaticFilter.doSomethingStatic("foo");
    assertThat(result).isEqualTo("hooked: foo");
    assertThat(ShadowClassWithStaticFilter.filterCalled).isTrue();
    assertThat(ShadowClassWithStaticFilter.capturedReturnValue).isEqualTo("foo");
    assertThat(ShadowClassWithStaticFilter.capturedInput).isEqualTo("foo");
  }

  @Instrument
  public static class ClassWithStaticFilter {
    public static String doSomethingStatic(String input) {
      return input;
    }
  }

  @Implements(ClassWithStaticFilter.class)
  public static class ShadowClassWithStaticFilter {
    public static boolean filterCalled = false;
    public static String capturedReturnValue;
    public static String capturedInput;

    public static void reset() {
      filterCalled = false;
      capturedReturnValue = null;
      capturedInput = null;
    }

    @Filter
    protected static String doSomethingStatic(String returnValue, String input) {
      filterCalled = true;
      capturedReturnValue = returnValue;
      capturedInput = input;
      return "hooked: " + returnValue;
    }
  }
}
