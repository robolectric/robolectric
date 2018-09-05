package org.robolectric;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.internal.Instrument;
import org.robolectric.internal.SandboxTestRunner;
import org.robolectric.internal.bytecode.SandboxConfig;
import org.robolectric.internal.bytecode.ShadowWrangler;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.testing.Foo;
import org.robolectric.testing.ShadowFoo;

@RunWith(SandboxTestRunner.class)
public class ShadowWranglerIntegrationTest {

  private static final boolean YES = true;

  private String name;

  @Before
  public void setUp() throws Exception {
    name = "context";
  }

  @Test
  @SandboxConfig(shadows = {ShadowForAClassWithDefaultConstructor_HavingNoConstructorDelegate.class})
  public void testConstructorInvocation_WithDefaultConstructorAndNoConstructorDelegateOnShadowClass() throws Exception {
    AClassWithDefaultConstructor instance = new AClassWithDefaultConstructor();
    assertThat(Shadow.<Object>extract(instance)).isInstanceOf(ShadowForAClassWithDefaultConstructor_HavingNoConstructorDelegate.class);
    assertThat(instance.initialized).isTrue();
  }

  @Test
  @SandboxConfig(shadows = { ShadowFoo.class })
  public void testConstructorInvocation() throws Exception {
    Foo foo = new Foo(name);
    assertSame(name, shadowOf(foo).name);
  }

  @Test
  @SandboxConfig(shadows = {ShadowFoo.class})
  public void testRealObjectAnnotatedFieldsAreSetBeforeConstructorIsCalled() throws Exception {
    Foo foo = new Foo(name);
    assertSame(name, shadowOf(foo).name);
    assertSame(foo, shadowOf(foo).realFooField);

    assertSame(foo, shadowOf(foo).realFooInConstructor);
    assertSame(foo, shadowOf(foo).realFooInParentConstructor);
  }

  @Test
  @SandboxConfig(shadows = {ShadowFoo.class})
  public void testMethodDelegation() throws Exception {
    Foo foo = new Foo(name);
    assertSame(name, foo.getName());
  }

  @Test
  @SandboxConfig(shadows = {WithEquals.class})
  public void testEqualsMethodDelegation() throws Exception {
    Foo foo1 = new Foo(name);
    Foo foo2 = new Foo(name);
    assertEquals(foo1, foo2);
  }

  @Test
  @SandboxConfig(shadows = {WithEquals.class})
  public void testHashCodeMethodDelegation() throws Exception {
    Foo foo = new Foo(name);
    assertEquals(42, foo.hashCode());
  }

  @Test
  @SandboxConfig(shadows = {WithToString.class})
  public void testToStringMethodDelegation() throws Exception {
    Foo foo = new Foo(name);
    assertEquals("the expected string", foo.toString());
  }

  @Test
  @SandboxConfig(shadows = {ShadowFoo.class})
  public void testShadowSelectionSearchesSuperclasses() throws Exception {
    TextFoo textFoo = new TextFoo(name);
    assertEquals(ShadowFoo.class, Shadow.extract(textFoo).getClass());
  }

  @Test
  @SandboxConfig(shadows = {ShadowFoo.class, ShadowTextFoo.class})
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
  @SandboxConfig(shadows = ShadowThrowInShadowMethod.class)
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

    assertThat(stackTrace[2].getClassName()).isEqualTo(ShadowWranglerIntegrationTest.class.getName());
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
  @SandboxConfig(shadows = ShadowThrowInRealMethod.class)
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

    assertThat(stackTrace[1].getClassName()).isEqualTo(ShadowWranglerIntegrationTest.class.getName());
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

  @Test @SandboxConfig(shadows = {Shadow2OfChild.class, ShadowOfParent.class})
  public void whenShadowMethodIsOverriddenInShadowWithSameShadowedClass_shouldUseOverriddenMethod() throws Exception {
    assertThat(new Child().get()).isEqualTo("get from Shadow2OfChild");
  }

  @Test @SandboxConfig(shadows = {Shadow22OfChild.class, ShadowOfParent.class})
  public void whenShadowMethodIsNotOverriddenInShadowWithSameShadowedClass_shouldUseOverriddenMethod() throws Exception {
    assertThat(new Child().get()).isEqualTo("get from Shadow2OfChild");
  }

  @Test @SandboxConfig(shadows = {Shadow3OfChild.class, ShadowOfParent.class})
  public void whenShadowMethodIsOverriddenInShadowOfAnotherClass_shouldNotUseShadowSuperclassMethods() throws Exception {
    assertThat(new Child().get()).isEqualTo("from child (from shadow of parent)");
  }

  @Test @SandboxConfig(shadows = {ShadowOfParentWithPackageImpl.class})
  public void whenShadowMethodIsntCorrectlyVisible_shouldNotUseShadowMethods() throws Exception {
    assertThat(new Parent().get()).isEqualTo("from parent");
  }

  @Instrument
  public static class Parent {
    public String get() {
      return "from parent";
    }
  }

  @Instrument
  public static class Child extends Parent {
    @Override
    public String get() {
      return "from child (" + super.get() + ")";
    }
  }

  @Implements(Parent.class)
  public static class ShadowOfParent {
    @Implementation
    protected String get() {
      return "from shadow of parent";
    }
  }

  @Implements(Parent.class)
  public static class ShadowOfParentWithPackageImpl {
    @Implementation
    String get() {
      return "from ShadowOfParentWithPackageImpl";
    }
  }

  @Implements(value = Child.class)
  public static class ShadowOfChild extends ShadowOfParent {
    @Implementation
    @Override
    protected String get() {
      return "get from ShadowOfChild";
    }
  }

  @Implements(value = Child.class)
  public static class Shadow2OfChild extends ShadowOfChild {
    @Implementation
    @Override
    protected String get() {
      return "get from Shadow2OfChild";
    }
  }

  @Implements(value = Child.class)
  public static class Shadow22OfChild extends Shadow2OfChild {
  }

  public static class SomethingOtherThanChild extends Child {
  }

  @Implements(value = SomethingOtherThanChild.class)
  public static class Shadow3OfChild extends ShadowOfChild {
    @Implementation
    @Override
    protected String get() {
      return "get from Shadow3OfChild";
    }
  }

  private ShadowFoo shadowOf(Foo foo) {
    return (ShadowFoo) Shadow.extract(foo);
  }

  private ShadowTextFoo shadowOf(TextFoo foo) {
    return (ShadowTextFoo) Shadow.extract(foo);
  }

  @Implements(Foo.class)
  public static class WithEquals {
    @Implementation
    protected void __constructor__(String s) {
    }

    @Override
    @Implementation
    public boolean equals(Object o) {
      return true;
    }

    @Override
    @Implementation
    public int hashCode() {
      return 42;
    }

  }

  @Implements(Foo.class)
  public static class WithToString {
    @Implementation
    protected void __constructor__(String s) {
    }

    @Override
    @Implementation
    public String toString() {
      return "the expected string";
    }
  }

  @Implements(TextFoo.class)
  public static class ShadowTextFoo extends ShadowFoo {
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

    @Implementation
    protected void __constructor__(String name) {
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

  @SandboxConfig(shadows = ShadowAClassWithDifficultArgs.class)
  @Test public void shouldAllowLooseSignatureMatches() throws Exception {
    assertThat(new AClassWithDifficultArgs().aMethod("bc")).isEqualTo("abc");
  }

  @Implements(value = AClassWithDifficultArgs.class, looseSignatures = true)
  public static class ShadowAClassWithDifficultArgs {
    @Implementation
    protected Object aMethod(Object s) {
      return "a" + s;
    }
  }

  @Instrument
  public static class AClassWithDifficultArgs {
    public CharSequence aMethod(CharSequence s) {
      return s;
    }
  }

  @Test @SandboxConfig(shadows = ShadowOfAClassWithStaticInitializer.class)
  public void classesWithInstrumentedShadowsDontDoubleInitialize() throws Exception {
    // if we didn't reject private shadow methods, __staticInitializer__ on the shadow
    // would be executed twice.
    new AClassWithStaticInitializer();
    assertThat(ShadowOfAClassWithStaticInitializer.initCount).isEqualTo(1);
    assertThat(AClassWithStaticInitializer.initCount).isEqualTo(1);
  }

  @Instrument
  public static class AClassWithStaticInitializer {
    static int initCount;
    static {
      initCount++;
    }
  }

  @Instrument // because it's fairly common that people accidentally instrument their own shadows
  @Implements(AClassWithStaticInitializer.class)
  public static class ShadowOfAClassWithStaticInitializer {
    static int initCount;
    static {
      initCount++;
    }
  }

  @Test @SandboxConfig(shadows = Shadow22OfAClassWithBrokenStaticInitializer.class)
  public void staticInitializerShadowMethodsObeySameRules() throws Exception {
    new AClassWithBrokenStaticInitializer();
  }

  @Instrument
  public static class AClassWithBrokenStaticInitializer {
    static {
      if (YES) throw new RuntimeException("broken!");
    }
  }

  @Implements(AClassWithBrokenStaticInitializer.class)
  public static class Shadow2OfAClassWithBrokenStaticInitializer {
    @Implementation
    protected static void __staticInitializer__() {
      // don't call real static initializer
    }
  }

  @Implements(AClassWithBrokenStaticInitializer.class)
  public static class Shadow22OfAClassWithBrokenStaticInitializer
      extends Shadow2OfAClassWithBrokenStaticInitializer {
  }
}
