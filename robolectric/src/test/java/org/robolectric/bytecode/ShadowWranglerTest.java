package org.robolectric.bytecode;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;
import org.robolectric.bytecode.testing.Foo;
import org.robolectric.bytecode.testing.ShadowFoo;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.internal.Instrument;
import org.robolectric.annotation.RealObject;

import java.io.IOException;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.robolectric.Robolectric.shadowOf_;

@RunWith(TestRunners.WithoutDefaults.class)
public class ShadowWranglerTest {
  private String name;

  @Before
  public void setUp() throws Exception {
    name = "context";
  }

  @Test
  @Config(shadows = {ShadowForAClassWithDefaultConstructor_HavingNoConstructorDelegate.class})
  public void testConstructorInvocation_WithDefaultConstructorAndNoConstructorDelegateOnShadowClass() throws Exception {
    AClassWithDefaultConstructor instance = new AClassWithDefaultConstructor();
    assertThat(shadowOf_(instance)).isExactlyInstanceOf(ShadowForAClassWithDefaultConstructor_HavingNoConstructorDelegate.class);
    assertThat(instance.initialized).isTrue();
  }

  @Test
  @Config(shadows = {ShadowFoo.class})
  public void testConstructorInvocation() throws Exception {
    Foo foo = new Foo(name);
    assertSame(name, shadowOf(foo).name);
    assertSame(foo, shadowOf(foo).realFooCtor);
  }

  @Test
  @Config(shadows = {ShadowFoo.class})
  public void testRealObjectAnnotatedFieldsAreSetBeforeConstructorIsCalled() throws Exception {
    Foo foo = new Foo(name);
    assertSame(name, shadowOf(foo).name);
    assertSame(foo, shadowOf(foo).realFooField);

    assertSame(foo, shadowOf(foo).realFooInConstructor);
    assertSame(foo, shadowOf(foo).realFooInParentConstructor);
  }

  @Test
  @Config(shadows = {ShadowFoo.class})
  public void testMethodDelegation() throws Exception {
    Foo foo = new Foo(name);
    assertSame(name, foo.getName());
  }

  @Test
  @Config(shadows = {WithEquals.class})
  public void testEqualsMethodDelegation() throws Exception {
    Foo foo1 = new Foo(name);
    Foo foo2 = new Foo(name);
    assertEquals(foo1, foo2);
  }

  @Test
  @Config(shadows = {WithEquals.class})
  public void testHashCodeMethodDelegation() throws Exception {
    Foo foo = new Foo(name);
    assertEquals(42, foo.hashCode());
  }

  @Test
  @Config(shadows = {WithToString.class})
  public void testToStringMethodDelegation() throws Exception {
    Foo foo = new Foo(name);
    assertEquals("the expected string", foo.toString());
  }

  @Test
  @Config(shadows = {ShadowFoo.class})
  public void testShadowSelectionSearchesSuperclasses() throws Exception {
    TextFoo textFoo = new TextFoo(name);
    assertEquals(ShadowFoo.class, shadowOf_(textFoo).getClass());
  }

  @Test
  @Config(shadows = {ShadowFoo.class, ShadowTextFoo.class})
  public void shouldUseMostSpecificShadow() throws Exception {
    TextFoo textFoo = new TextFoo(name);
    assertThat(shadowOf(textFoo)).isInstanceOf(ShadowTextFoo.class);
  }

  @Test
  public void testPrimitiveArrays() throws Exception {
    Class<?> objArrayClass = ShadowWrangler.loadClass("java.lang.Object[]", getClass().getClassLoader());
    assertTrue(objArrayClass.isArray());
    assertEquals(Object.class, objArrayClass.getComponentType());

    Class<?> intArrayClass = ShadowWrangler.loadClass("int[]", getClass().getClassLoader());
    assertTrue(intArrayClass.isArray());
    assertEquals(Integer.TYPE, intArrayClass.getComponentType());
  }

  @Test
  @Config(shadows = ShadowThrowInShadowMethod.class)
  public void shouldRemoveNoiseFromShadowedStackTraces() throws Exception {
    ThrowInShadowMethod instance = new ThrowInShadowMethod();

    Exception e = null;
    try {
      instance.method();
    } catch (Exception e1) {
      e = e1;
    }

    assertNotNull(e);
    assertEquals(IOException.class, e.getClass());
    assertEquals("fake exception", e.getMessage());
    StackTraceElement[] stackTrace = e.getStackTrace();

    assertThat(stackTrace[0].getClassName()).isEqualTo(ShadowThrowInShadowMethod.class.getName());
    assertThat(stackTrace[0].getMethodName()).isEqualTo("method");
    assertThat(stackTrace[0].getLineNumber()).isGreaterThan(0);

    assertThat(stackTrace[1].getClassName()).isEqualTo(ThrowInShadowMethod.class.getName());
    assertThat(stackTrace[1].getMethodName()).isEqualTo("method");
    assertThat(stackTrace[1].getLineNumber()).isLessThan(0);

    assertThat(stackTrace[2].getClassName()).isEqualTo(ShadowWranglerTest.class.getName());
    assertThat(stackTrace[2].getMethodName()).isEqualTo("shouldRemoveNoiseFromShadowedStackTraces");
    assertThat(stackTrace[2].getLineNumber()).isGreaterThan(0);
  }

  @Instrument
  public static class ThrowInShadowMethod {
    public void method() throws IOException {
    }
  }

  @Implements(ThrowInShadowMethod.class)
  public static class ShadowThrowInShadowMethod {
    public void method() throws IOException {
      throw new IOException("fake exception");
    }
  }


  @Test
  @Config(shadows = ShadowThrowInRealMethod.class)
  public void shouldRemoveNoiseFromUnshadowedStackTraces() throws Exception {
    ThrowInRealMethod instance = new ThrowInRealMethod();

    Exception e = null;
    try {
      instance.method();
    } catch (Exception e1) {
      e = e1;
    }

    assertNotNull(e);
    assertEquals(IOException.class, e.getClass());
    assertEquals("fake exception", e.getMessage());
    StackTraceElement[] stackTrace = e.getStackTrace();

    assertThat(stackTrace[0].getClassName()).isEqualTo(ThrowInRealMethod.class.getName());
    assertThat(stackTrace[0].getMethodName()).isEqualTo("method");
    assertThat(stackTrace[0].getLineNumber()).isGreaterThan(0);

    assertThat(stackTrace[1].getClassName()).isEqualTo(ShadowWranglerTest.class.getName());
    assertThat(stackTrace[1].getMethodName()).isEqualTo("shouldRemoveNoiseFromUnshadowedStackTraces");
    assertThat(stackTrace[1].getLineNumber()).isGreaterThan(0);
  }

  @Instrument
  public static class ThrowInRealMethod {
    public void method() throws IOException {
      throw new IOException("fake exception");
    }
  }

  @Implements(ThrowInRealMethod.class)
  public static class ShadowThrowInRealMethod {
  }

  @Test @Config(shadows = {ShadowOfChildWithInheritance.class, ShadowOfParent.class})
  public void whenInheritanceIsEnabled_shouldUseShadowSuperclassMethods() throws Exception {
    assertThat(new Child().get()).isEqualTo("from shadow of parent");
  }

  @Test @Config(shadows = {ShadowOfChildWithoutInheritance.class, ShadowOfParent.class})
  public void whenInheritanceIsDisabled_shouldUseShadowSuperclassMethods() throws Exception {
    assertThat(new Child().get()).isEqualTo("from child (from shadow of parent)");
  }

  @Instrument
  public static class Parent {
    public String get() {
      return "from parent";
    }
  }

  @Instrument
  public static class Child extends Parent {
    public String get() {
      return "from child (" + super.get() + ")";
    }
  }

  @Implements(Parent.class)
  public static class ShadowOfParent {
    @Implementation
    public String get() {
      return "from shadow of parent";
    }
  }

  @Implements(value = Child.class, inheritImplementationMethods = true)
  public static class ShadowOfChildWithInheritance extends ShadowOfParent {
  }

  @Implements(value = Child.class, inheritImplementationMethods = false)
  public static class ShadowOfChildWithoutInheritance extends ShadowOfParent {
  }

  private ShadowFoo shadowOf(Foo foo) {
    return shadowOf_(foo);
  }

  private ShadowTextFoo shadowOf(TextFoo foo) {
    return shadowOf_(foo);
  }

  @Implements(Foo.class)
  public static class WithEquals {
    @SuppressWarnings("UnusedDeclaration")
    public void __constructor__(String s) {
    }

    @Override
    public boolean equals(Object o) {
      return true;
    }


    @Override
    public int hashCode() {
      return 42;
    }

  }

  @Implements(Foo.class)
  public static class WithToString {
    @SuppressWarnings("UnusedDeclaration")
    public void __constructor__(String s) {
    }

    @Override
    public String toString() {
      return "the expected string";
    }
  }

  @Implements(TextFoo.class)
  public static class ShadowTextFoo extends ShadowFoo {
    public ShadowTextFoo(Foo foo) {
      super(foo);
    }
  }

  @Instrument
  public static class TextFoo extends Foo {
    public TextFoo(String s) {
      super(s);
    }
  }

  @Implements(Foo.class)
  public static class ShadowFooParent {
    @RealObject
    private Foo realFoo;
    public Foo realFooInParentConstructor;

    public void __constructor__(String name) {
      realFooInParentConstructor = realFoo;
    }
  }

  @Instrument
  public static class AClassWithDefaultConstructor {
    public boolean initialized;

    public AClassWithDefaultConstructor() {
      initialized = true;
    }
  }

  @Implements(AClassWithDefaultConstructor.class)
  public static class ShadowForAClassWithDefaultConstructor_HavingNoConstructorDelegate {
  }

  @Config(shadows = ShadowAClassWithDifficultArgs.class)
  @Test public void shouldAllowLooseSignatureMatches() throws Exception {
    assertThat(new AClassWithDifficultArgs().aMethod("bc")).isEqualTo("abc");
  }

  @Implements(value = AClassWithDifficultArgs.class, looseSignatures = true)
  public static class ShadowAClassWithDifficultArgs {
    @Implementation
    public Object aMethod(Object s) {
      return "a" + s;
    }
  }

  @Instrument
  public static class AClassWithDifficultArgs {
    public CharSequence aMethod(CharSequence s) {
      return s;
    }
  }
}
