package org.robolectric.internal.bytecode;

import android.os.Build;
import org.junit.Test;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.internal.bytecode.testing.*;
import org.robolectric.internal.ShadowConstants;
import org.robolectric.internal.Shadow;
import org.robolectric.internal.ShadowExtractor;
import org.robolectric.util.Transcript;
import org.robolectric.util.Util;

import java.lang.reflect.*;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

// Don't end in "Test" or ant will try to run this as a test
abstract public class InstrumentingClassLoaderTestBase {
  private ClassLoader classLoader;
  private Transcript transcript = new Transcript();
  private MyClassHandler classHandler = new MyClassHandler(transcript);

  protected abstract ClassLoader createClassLoader(InstrumentingClassLoaderConfig config) throws ClassNotFoundException;

  @Test
  public void shouldMakeClassesNonFinal() throws Exception {
    Class<?> clazz = loadClass(AFinalClass.class);
    assertEquals(0, clazz.getModifiers() & Modifier.FINAL);
  }

  @Test
  public void forClassesWithNoDefaultConstructor_shouldCreateOneButItShouldNotCallShadow() throws Exception {
    Constructor<?> defaultCtor = loadClass(AClassWithNoDefaultConstructor.class).getConstructor();
    assertTrue(Modifier.isPublic(defaultCtor.getModifiers()));
    defaultCtor.setAccessible(true);
    Object instance = defaultCtor.newInstance();
    assertThat(ShadowExtractor.extract(instance)).isNotNull();
    transcript.assertNoEventsSoFar();
  }

  @Test
  public void shouldDelegateToHandlerForConstructors() throws Exception {
    Class<?> clazz = loadClass(AClassWithNoDefaultConstructor.class);
    Constructor<?> ctor = clazz.getDeclaredConstructor(String.class);
    assertTrue(Modifier.isPublic(ctor.getModifiers()));
    ctor.setAccessible(true);
    Object instance = ctor.newInstance("new one");
    transcript.assertEventsSoFar("methodInvoked: AClassWithNoDefaultConstructor.__constructor__(java.lang.String new one)");

    Field nameField = clazz.getDeclaredField("name");
    nameField.setAccessible(true);
    assertNull(nameField.get(instance));
  }

  @Test
  public void shouldDelegateClassLoadForUnacquiredClasses() throws Exception {
    ClassLoader classLoader = createClassLoader(new MyConfig(false, false));
    Class<?> exampleClass = classLoader.loadClass(AnExampleClass.class.getName());
    assertSame(getClass().getClassLoader(), exampleClass.getClassLoader());
  }

  @Test
  public void shouldPerformClassLoadForAcquiredClasses() throws Exception {
    ClassLoader classLoader = createClassLoader(new MyConfig(true, false));
    Class<?> exampleClass = classLoader.loadClass(AnUninstrumentedClass.class.getName());
    assertSame(classLoader, exampleClass.getClassLoader());
    try {
      exampleClass.getField(ShadowConstants.CLASS_HANDLER_DATA_FIELD_NAME);
      fail("class shouldn't be instrumented!");
    } catch (Exception e) {
      // expected
    }
  }

  @Test
  public void shouldPerformClassLoadAndInstrumentLoadForInstrumentedClasses() throws Exception {
    ClassLoader classLoader = createClassLoader(new MyConfig(true, true));
    Class<?> exampleClass = classLoader.loadClass(AnExampleClass.class.getName());
    assertSame(classLoader, exampleClass.getClassLoader());
    assertNotNull(exampleClass.getField(ShadowConstants.CLASS_HANDLER_DATA_FIELD_NAME));
  }

  @Test
  public void callingNormalMethodShouldInvokeClassHandler() throws Exception {
    Class<?> exampleClass = loadClass(AnExampleClass.class);
    Method normalMethod = exampleClass.getMethod("normalMethod", String.class, int.class);

    Object exampleInstance = exampleClass.newInstance();
    assertEquals("response from methodInvoked: AnExampleClass.normalMethod(java.lang.String value1, int 123)",
        normalMethod.invoke(exampleInstance, "value1", 123));
    transcript.assertEventsSoFar("methodInvoked: AnExampleClass.__constructor__()",
        "methodInvoked: AnExampleClass.normalMethod(java.lang.String value1, int 123)");
  }

  @Test
  public void shouldGenerateClassSpecificDirectAccessMethod() throws Exception {
    Class<?> exampleClass = loadClass(AnExampleClass.class);
    String methodName = Shadow.directMethodName(AnExampleClass.class.getName(), "normalMethod");
    Method directMethod = exampleClass.getDeclaredMethod(methodName, String.class, int.class);
    directMethod.setAccessible(true);
    Object exampleInstance = exampleClass.newInstance();
    assertEquals("normalMethod(value1, 123)", directMethod.invoke(exampleInstance, "value1", 123));
    transcript.assertEventsSoFar("methodInvoked: AnExampleClass.__constructor__()");
  }

  @Test
  public void soMockitoDoesntExplodeDueToTooManyMethods_shouldGenerateDirectAccessMethodWhichIsPrivate() throws Exception {
    Class<?> exampleClass = loadClass(AnExampleClass.class);
    String methodName = Shadow.directMethodName("normalMethod");
    Method directMethod = exampleClass.getDeclaredMethod(methodName, String.class, int.class);
    assertTrue(Modifier.isPrivate(directMethod.getModifiers()));
    assertFalse(Modifier.isFinal(directMethod.getModifiers()));
  }

  @Test
  public void soMockitoDoesntExplodeDueToTooManyMethods_shouldGenerateClassSpecificDirectAccessMethodWhichIsPrivateAndFinal() throws Exception {
    Class<?> exampleClass = loadClass(AnExampleClass.class);
    String methodName = Shadow.directMethodName(AnExampleClass.class.getName(), "normalMethod");
    Method directMethod = exampleClass.getDeclaredMethod(methodName, String.class, int.class);
    assertTrue(Modifier.isPrivate(directMethod.getModifiers()));
    assertTrue(Modifier.isFinal(directMethod.getModifiers()));
  }

  @Test
  public void callingStaticMethodShouldInvokeClassHandler() throws Exception {
    Class<?> exampleClass = loadClass(AClassWithStaticMethod.class);
    Method normalMethod = exampleClass.getMethod("staticMethod", String.class);

    assertEquals("response from methodInvoked: AClassWithStaticMethod.staticMethod(java.lang.String value1)",
        normalMethod.invoke(null, "value1"));
    transcript.assertEventsSoFar("methodInvoked: AClassWithStaticMethod.staticMethod(java.lang.String value1)");
  }

  @Test
  public void callingStaticDirectAccessMethodShouldWork() throws Exception {
    Class<?> exampleClass = loadClass(AClassWithStaticMethod.class);
    String methodName = Shadow.directMethodName(
        AClassWithStaticMethod.class.getName(), "staticMethod");
    Method directMethod = exampleClass.getDeclaredMethod(methodName, String.class);
    directMethod.setAccessible(true);
    assertEquals("staticMethod(value1)", directMethod.invoke(null, "value1"));
  }

  @Test
  public void callingNormalMethodReturningIntegerShouldInvokeClassHandler() throws Exception {
    Class<?> exampleClass = loadClass(AClassWithMethodReturningInteger.class);
    classHandler.valueToReturn = 456;

    Method normalMethod = exampleClass.getMethod("normalMethodReturningInteger", int.class);
    Object exampleInstance = exampleClass.newInstance();
    assertEquals(456, normalMethod.invoke(exampleInstance, 123));
    transcript.assertEventsSoFar("methodInvoked: AClassWithMethodReturningInteger.__constructor__()",
        "methodInvoked: AClassWithMethodReturningInteger.normalMethodReturningInteger(int 123)");
  }

  @Test
  public void whenClassHandlerReturnsNull_callingNormalMethodReturningIntegerShouldWork() throws Exception {
    Class<?> exampleClass = loadClass(AClassWithMethodReturningInteger.class);
    classHandler.valueToReturn = null;

    Method normalMethod = exampleClass.getMethod("normalMethodReturningInteger", int.class);
    Object exampleInstance = exampleClass.newInstance();
    assertEquals(0, normalMethod.invoke(exampleInstance, 123));
    transcript.assertEventsSoFar("methodInvoked: AClassWithMethodReturningInteger.__constructor__()",
        "methodInvoked: AClassWithMethodReturningInteger.normalMethodReturningInteger(int 123)");
  }

  @Test
  public void callingMethodReturningDoubleShouldInvokeClassHandler() throws Exception {
    Class<?> exampleClass = loadClass(AClassWithMethodReturningDouble.class);
    classHandler.valueToReturn = 456;

    Method normalMethod = exampleClass.getMethod("normalMethodReturningDouble", double.class);
    Object exampleInstance = exampleClass.newInstance();
    assertEquals(456.0, normalMethod.invoke(exampleInstance, 123d));
    transcript.assertEventsSoFar("methodInvoked: AClassWithMethodReturningDouble.__constructor__()",
        "methodInvoked: AClassWithMethodReturningDouble.normalMethodReturningDouble(double 123.0)");
  }

  @Test
  public void callingNativeMethodShouldInvokeClassHandler() throws Exception {
    Class<?> exampleClass = loadClass(AClassWithNativeMethod.class);
    Method normalMethod = exampleClass.getDeclaredMethod("nativeMethod", String.class, int.class);
    Object exampleInstance = exampleClass.newInstance();
    assertEquals("response from methodInvoked: AClassWithNativeMethod.nativeMethod(java.lang.String value1, int 123)",
        normalMethod.invoke(exampleInstance, "value1", 123));
    transcript.assertEventsSoFar("methodInvoked: AClassWithNativeMethod.__constructor__()",
        "methodInvoked: AClassWithNativeMethod.nativeMethod(java.lang.String value1, int 123)");
  }

  @Test
  public void directlyCallingNativeMethodShouldBeNoOp() throws Exception {
    Class<?> exampleClass = loadClass(AClassWithNativeMethod.class);
    Object exampleInstance = exampleClass.newInstance();
    Method directMethod = findDirectMethod(exampleClass, "nativeMethod", String.class, int.class);
    assertThat(Modifier.isNative(directMethod.getModifiers())).isFalse();

    assertThat(directMethod.invoke(exampleInstance, "", 1)).isNull();
  }

  @Test
  public void directlyCallingNativeMethodReturningPrimitiveShouldBeNoOp() throws Exception {
    Class<?> exampleClass = loadClass(AClassWithNativeMethodReturningPrimitive.class);
    Object exampleInstance = exampleClass.newInstance();
    Method directMethod = findDirectMethod(exampleClass, "nativeMethod");
    assertThat(Modifier.isNative(directMethod.getModifiers())).isFalse();

    assertThat(directMethod.invoke(exampleInstance)).isEqualTo(0);
  }

  @Test
  public void shouldHandleMethodsReturningBoolean() throws Exception {
    Class<?> exampleClass = loadClass(AClassWithMethodReturningBoolean.class);
    classHandler.valueToReturn = true;

    Method directMethod = exampleClass.getMethod("normalMethodReturningBoolean", boolean.class, boolean[].class);
    directMethod.setAccessible(true);
    Object exampleInstance = exampleClass.newInstance();
    assertEquals(true, directMethod.invoke(exampleInstance, true, new boolean[0]));
    transcript.assertEventsSoFar("methodInvoked: AClassWithMethodReturningBoolean.__constructor__()",
        "methodInvoked: AClassWithMethodReturningBoolean.normalMethodReturningBoolean(boolean true, boolean[] {})");
  }

  @Test
  public void shouldHandleMethodsReturningArray() throws Exception {
    Class<?> exampleClass = loadClass(AClassWithMethodReturningArray.class);
    classHandler.valueToReturn = new String[]{"miao, mieuw"};

    Method directMethod = exampleClass.getMethod("normalMethodReturningArray");
    directMethod.setAccessible(true);
    Object exampleInstance = exampleClass.newInstance();
    transcript.assertEventsSoFar("methodInvoked: AClassWithMethodReturningArray.__constructor__()");
    assertArrayEquals(new String[]{"miao, mieuw"}, (String[]) directMethod.invoke(exampleInstance));
    transcript.assertEventsSoFar("methodInvoked: AClassWithMethodReturningArray.normalMethodReturningArray()");
  }

  @Test
  public void shouldInvokeShadowForEachConstructorInInheritanceTree() throws Exception {
    loadClass(AChild.class).newInstance();
    transcript.assertEventsSoFar(
        "methodInvoked: AGrandparent.__constructor__()",
        "methodInvoked: AParent.__constructor__()",
        "methodInvoked: AChild.__constructor__()");
  }

  @Test
  public void shouldRetainSuperCallInConstructor() throws Exception {
    Class<?> aClass = loadClass(AnInstrumentedChild.class);
    Object o = aClass.getDeclaredConstructor(String.class).newInstance("hortense");
    assertEquals("HORTENSE's child", aClass.getSuperclass().getDeclaredField("parentName").get(o));
    assertNull(aClass.getDeclaredField("childName").get(o));
  }

  @Test
  public void shouldCorrectlySplitStaticPrepFromConstructorChaining() throws Exception {
    Class<?> aClass = loadClass(AClassWithFunnyConstructors.class);
    Object o = aClass.getDeclaredConstructor(String.class).newInstance("hortense");
    transcript.assertEventsSoFar(
        "methodInvoked: AClassWithFunnyConstructors.__constructor__(" + AnUninstrumentedParent.class.getName() + " UninstrumentedParent{parentName='hortense'}, java.lang.String foo)",
        "methodInvoked: AClassWithFunnyConstructors.__constructor__(java.lang.String hortense)");

    // should not run constructor bodies...
    assertEquals(null, getDeclaredFieldValue(aClass, o, "name"));
    assertEquals(null, getDeclaredFieldValue(aClass, o, "uninstrumentedParent"));
  }

  @Test
  public void shouldGenerateClassSpecificDirectAccessMethodForConstructorWhichDoesNotCallSuper() throws Exception {
    Class<?> aClass = loadClass(AClassWithFunnyConstructors.class);
    Object instance = aClass.getConstructor(String.class).newInstance("horace");
    transcript.assertEventsSoFar(
        "methodInvoked: AClassWithFunnyConstructors.__constructor__(" + AnUninstrumentedParent.class.getName() + " UninstrumentedParent{parentName='horace'}, java.lang.String foo)",
        "methodInvoked: AClassWithFunnyConstructors.__constructor__(java.lang.String horace)");

    // each directly-accessible constructor body will need to be called explicitly, with the correct args...

    Class<?> uninstrumentedParentClass = loadClass(AnUninstrumentedParent.class);
    Method directMethod = findDirectMethod(aClass, "__constructor__", uninstrumentedParentClass, String.class);
    Object uninstrumentedParentIn = uninstrumentedParentClass.getDeclaredConstructor(String.class).newInstance("hortense");
    assertEquals(null, directMethod.invoke(instance, uninstrumentedParentIn, "foo"));
    transcript.assertNoEventsSoFar();

    assertEquals(null, getDeclaredFieldValue(aClass, instance, "name"));
    Object uninstrumentedParentOut = getDeclaredFieldValue(aClass, instance, "uninstrumentedParent");
    assertEquals("hortense", getDeclaredFieldValue(uninstrumentedParentClass, uninstrumentedParentOut, "parentName"));

    Method directMethod2 = findDirectMethod(aClass, "__constructor__", String.class);
    assertEquals(null, directMethod2.invoke(instance, "hortense"));
    transcript.assertNoEventsSoFar();

    assertEquals("hortense", getDeclaredFieldValue(aClass, instance, "name"));
  }

  private Method findDirectMethod(Class<?> declaringClass, String methodName, Class<?>... argClasses) throws NoSuchMethodException {
    String directMethodName = Shadow.directMethodName(declaringClass.getName(), methodName);
    Method directMethod = declaringClass.getDeclaredMethod(directMethodName, argClasses);
    directMethod.setAccessible(true);
    return directMethod;
  }

  @Test
  public void shouldInstrumentEqualsAndHashCodeAndToStringEvenWhenUndeclared() throws Exception {
    Class<?> theClass = loadClass(AClassWithoutEqualsHashCodeToString.class);
    Object instance = theClass.newInstance();
    transcript.assertEventsSoFar("methodInvoked: AClassWithoutEqualsHashCodeToString.__constructor__()");

    instance.toString();
    transcript.assertEventsSoFar("methodInvoked: AClassWithoutEqualsHashCodeToString.toString()");

    classHandler.valueToReturn = true;
    //noinspection ResultOfMethodCallIgnored,ObjectEqualsNull
    instance.equals(null);
    transcript.assertEventsSoFar("methodInvoked: AClassWithoutEqualsHashCodeToString.equals(java.lang.Object null)");

    classHandler.valueToReturn = 42;
    //noinspection ResultOfMethodCallIgnored
    instance.hashCode();
    transcript.assertEventsSoFar("methodInvoked: AClassWithoutEqualsHashCodeToString.hashCode()");
  }

  @Test
  public void shouldAlsoInstrumentEqualsAndHashCodeAndToStringWhenDeclared() throws Exception {
    Class<?> theClass = loadClass(AClassWithEqualsHashCodeToString.class);
    Object instance = theClass.newInstance();
    transcript.assertEventsSoFar("methodInvoked: AClassWithEqualsHashCodeToString.__constructor__()");

    instance.toString();
    transcript.assertEventsSoFar("methodInvoked: AClassWithEqualsHashCodeToString.toString()");

    classHandler.valueToReturn = true;
    //noinspection ResultOfMethodCallIgnored,ObjectEqualsNull
    instance.equals(null);
    transcript.assertEventsSoFar("methodInvoked: AClassWithEqualsHashCodeToString.equals(java.lang.Object null)");

    classHandler.valueToReturn = 42;
    //noinspection ResultOfMethodCallIgnored
    instance.hashCode();
    transcript.assertEventsSoFar("methodInvoked: AClassWithEqualsHashCodeToString.hashCode()");
  }

  @Test
  public void shouldProperlyCallSuperWhenForcingDeclarationOfEqualsHashCodeToString() throws Exception {
    Class<?> theClass = loadClass(AnInstrumentedClassWithoutToStringWithSuperToString.class);
    Object instance = theClass.newInstance();
    transcript.assertEventsSoFar("methodInvoked: AnInstrumentedClassWithoutToStringWithSuperToString.__constructor__()");

    instance.toString();
    transcript.assertEventsSoFar("methodInvoked: AnInstrumentedClassWithoutToStringWithSuperToString.toString()");

    assertEquals("baaaaaah", findDirectMethod(theClass, "toString").invoke(instance));
  }

  @Test
  public void shouldRemapClasses() throws Exception {
    setClassLoader(createClassLoader(new ClassRemappingConfig()));
    Class<?> theClass = loadClass(AClassThatRefersToAForgettableClass.class);
    assertEquals(loadClass(AClassToRemember.class), theClass.getField("someField").getType());
    assertEquals(Array.newInstance(loadClass(AClassToRemember.class), 0).getClass(), theClass.getField("someFields").getType());
  }

  @Test
  public void shouldFixTypesInFieldAccess() throws Exception {
    setClassLoader(createClassLoader(new ClassRemappingConfig()));
    Class<?> theClass = loadClass(AClassThatRefersToAForgettableClassInItsConstructor.class);
    Object instance = theClass.newInstance();
    Method method = theClass.getDeclaredMethod(Shadow.directMethodName(theClass.getName(), ShadowConstants.CONSTRUCTOR_METHOD_NAME));
    method.setAccessible(true);
    method.invoke(instance);
  }

  @Test
  public void shouldFixTypesInMethodArgsAndReturn() throws Exception {
    setClassLoader(createClassLoader(new ClassRemappingConfig()));
    Class<?> theClass = loadClass(AClassThatRefersToAForgettableClassInMethodCalls.class);
    assertNotNull(theClass.getMethod("aMethod", int.class, loadClass(AClassToRemember.class), String.class));
  }

  @Test
  public void shouldInterceptFilteredMethodInvocations() throws Exception {
    setClassLoader(createClassLoader(new MethodInterceptingConfig(new InstrumentingClassLoaderConfig.MethodRef(AClassToForget.class, "forgettableMethod"))));
    Class<?> theClass = loadClass(AClassThatRefersToAForgettableClass.class);
    Object instance = theClass.newInstance();
    Object output = theClass.getMethod("interactWithForgettableClass").invoke(Shadow.directlyOn(instance, (Class<Object>) theClass));
    assertEquals("null, get this!", output);
  }

  @Test
  public void shouldInterceptFilteredStaticMethodInvocations() throws Exception {
    setClassLoader(createClassLoader(new MethodInterceptingConfig(new InstrumentingClassLoaderConfig.MethodRef(AClassToForget.class, "forgettableStaticMethod"))));
    Class<?> theClass = loadClass(AClassThatRefersToAForgettableClass.class);
    Object instance = theClass.newInstance();
    Object output = theClass.getMethod("interactWithForgettableStaticMethod").invoke(Shadow.directlyOn(instance, (Class<Object>) theClass));
    assertEquals("yess? forget this: null", output);
  }

  @Test
  public void byte_shouldBeHandledAsReturnValueFromInterceptHandler() throws Exception {
    classHandler.valueToReturnFromIntercept = (byte) 10;
    assertThat(invokeInterceptedMethodOnAClassToForget("byteMethod")).isEqualTo((byte) 10);
  }

  @Test
  public void byteArray_shouldBeHandledAsReturnValueFromInterceptHandler() throws Exception {
    classHandler.valueToReturnFromIntercept = new byte[]{10, 12, 14};
    assertThat(invokeInterceptedMethodOnAClassToForget("byteArrayMethod")).isEqualTo(new byte[]{10, 12, 14});
  }

  @Test
  public void int_shouldBeHandledAsReturnValueFromInterceptHandler() throws Exception {
    classHandler.valueToReturnFromIntercept = 20;
    assertThat(invokeInterceptedMethodOnAClassToForget("intMethod")).isEqualTo(20);
  }

  @Test
  public void intArray_shouldBeHandledAsReturnValueFromInterceptHandler() throws Exception {
    classHandler.valueToReturnFromIntercept = new int[]{20, 22, 24};
    assertThat(invokeInterceptedMethodOnAClassToForget("intArrayMethod")).isEqualTo(new int[]{20, 22, 24});
  }

  @Test
  public void long_shouldBeHandledAsReturnValueFromInterceptHandler() throws Exception {
    classHandler.valueToReturnFromIntercept = 30L;
    assertThat(invokeInterceptedMethodOnAClassToForget("longMethod")).isEqualTo(30L);
  }

  @Test
  public void longArray_shouldBeHandledAsReturnValueFromInterceptHandler() throws Exception {
    classHandler.valueToReturnFromIntercept = new long[] {30L, 32L, 34L};
    assertThat(invokeInterceptedMethodOnAClassToForget("longArrayMethod")).isEqualTo(new long[] {30L, 32L, 34L});
  }

  @Test
  public void float_shouldBeHandledAsReturnValueFromInterceptHandler() throws Exception {
    classHandler.valueToReturnFromIntercept = 40f;
    assertThat(invokeInterceptedMethodOnAClassToForget("floatMethod")).isEqualTo(40f);
  }

  @Test
  public void floatArray_shouldBeHandledAsReturnValueFromInterceptHandler() throws Exception {
    classHandler.valueToReturnFromIntercept = new float[] {50f, 52f, 54f};
    assertThat(invokeInterceptedMethodOnAClassToForget("floatArrayMethod")).isEqualTo(new float[] {50f, 52f, 54f});
  }

  @Test
  public void double_shouldBeHandledAsReturnValueFromInterceptHandler() throws Exception {
    classHandler.valueToReturnFromIntercept = 80.0;
    assertThat(invokeInterceptedMethodOnAClassToForget("doubleMethod")).isEqualTo(80.0);
  }

  @Test
  public void doubleArray_shouldBeHandledAsReturnValueFromInterceptHandler() throws Exception {
    classHandler.valueToReturnFromIntercept = new double[] {90.0, 92.0, 94.0};
    assertThat(invokeInterceptedMethodOnAClassToForget("doubleArrayMethod")).isEqualTo(new double[] {90.0, 92.0, 94.0});
  }

  @Test
  public void short_shouldBeHandledAsReturnValueFromInterceptHandler() throws Exception {
    classHandler.valueToReturnFromIntercept = (short) 60;
    assertThat(invokeInterceptedMethodOnAClassToForget("shortMethod")).isEqualTo((short) 60);
  }

  @Test
  public void shortArray_shouldBeHandledAsReturnValueFromInterceptHandler() throws Exception {
    classHandler.valueToReturnFromIntercept = new short[] {70, 72, 74};
    assertThat(invokeInterceptedMethodOnAClassToForget("shortArrayMethod")).isEqualTo(new short[] {70, 72, 74});
  }

  @Test
  public void void_shouldBeHandledAsReturnValueFromInterceptHandler() throws Exception {
    classHandler.valueToReturnFromIntercept = null;
    assertThat(invokeInterceptedMethodOnAClassToForget("voidReturningMethod")).isNull();
  }

  private Object invokeInterceptedMethodOnAClassToForget(String methodName) throws Exception {
    setClassLoader(createClassLoader(new MethodInterceptingConfig(new InstrumentingClassLoaderConfig.MethodRef(AClassToForget.class, "*"))));
    Class<?> theClass = loadClass(AClassThatRefersToAForgettableClassInMethodCallsReturningPrimitive.class);
    Object instance = theClass.newInstance();
    return theClass.getMethod(methodName).invoke(Shadow.directlyOn(instance, (Class<Object>) theClass));
  }

  @Test
  public void shouldPassArgumentsFromInterceptedMethods() throws Exception {
    classHandler.valueToReturnFromIntercept = 10L;
    setClassLoader(createClassLoader(new MethodInterceptingConfig(new InstrumentingClassLoaderConfig.MethodRef(AClassToForget.class, "*"))));
    Class<?> theClass = loadClass(AClassThatRefersToAForgettableClassInMethodCallsReturningPrimitive.class);
    Object instance = theClass.newInstance();
    Shadow.directlyOn(instance, (Class<Object>) theClass, "longMethod");
    transcript.assertEventsSoFar(
        "methodInvoked: AClassThatRefersToAForgettableClassInMethodCallsReturningPrimitive.__constructor__()",
        "intercept: org/robolectric/internal/bytecode/testing/AClassToForget/longReturningMethod(Ljava/lang/String;IJ)J with params (str str, 123 123, 456 456)");
  }

  @Test
  public void shouldRemapClassesWhileInterceptingMethods() throws Exception {
    setClassLoader(createClassLoader(new MethodInterceptingClassRemappingConfig(new InstrumentingClassLoaderConfig.MethodRef(AClassThatCallsAMethodReturningAForgettableClass.class, "getAForgettableClass"))));
    Class<?> theClass = loadClass(AClassThatCallsAMethodReturningAForgettableClass.class);
    theClass.getMethod("callSomeMethod").invoke(Shadow.directlyOn(theClass.newInstance(), (Class<Object>) theClass));
  }

  @Test
  public void directMethodName_shouldGetSimpleName() throws Exception {
    assertEquals("$$robo$$SomeName_5c63_method", Shadow.directMethodName("a.b.c.SomeName", "method"));
    assertEquals("$$robo$$SomeName_3b43_method", Shadow.directMethodName("a.b.c.SomeClass$SomeName", "method"));
  }

  @Test
  public void shouldWorkWithEnums() throws Exception {
    loadClass(AnEnum.class);
  }

  @Test
  public void shouldReverseAnArray() throws Exception {
    assertArrayEquals(new Integer[]{5, 4, 3, 2, 1}, Util.reverse(new Integer[]{1, 2, 3, 4, 5}));
    assertArrayEquals(new Integer[]{4, 3, 2, 1}, Util.reverse(new Integer[]{1, 2, 3, 4}));
    assertArrayEquals(new Integer[]{1}, Util.reverse(new Integer[]{1}));
    assertArrayEquals(new Integer[]{}, Util.reverse(new Integer[]{}));
  }

  @Test
  public void shouldMakeBuildVersionIntsNonFinal() throws Exception {
    Class<?> versionClass = loadClass(Build.VERSION.class);
    int modifiers = versionClass.getDeclaredField("SDK_INT").getModifiers();
    assertThat(Modifier.isFinal(modifiers)).as("SDK_INT should be non-final").isFalse();
  }

  /////////////////////////////

  private Object getDeclaredFieldValue(Class<?> aClass, Object o, String fieldName) throws Exception {
    Field field = aClass.getDeclaredField(fieldName);
    field.setAccessible(true);
    return field.get(o);
  }

  public static class MyClassHandler implements ClassHandler {
    private static Object GENERATE_YOUR_OWN_VALUE = new Object();
    private Transcript transcript;
    private Object valueToReturn = GENERATE_YOUR_OWN_VALUE;
    private Object valueToReturnFromIntercept = null;

    public MyClassHandler(Transcript transcript) {
      this.transcript = transcript;
    }

    @Override
    public void classInitializing(Class clazz) {
    }

    @Override
    public Object initializing(Object instance) {
      return "a shadow!";
    }

    public Object methodInvoked(Class clazz, String methodName, Object instance, String[] paramTypes, Object[] params) throws Throwable {
      StringBuilder buf = new StringBuilder();
      buf.append("methodInvoked: ").append(clazz.getSimpleName()).append(".").append(methodName).append("(");
      for (int i = 0; i < paramTypes.length; i++) {
        if (i > 0) buf.append(", ");
        Object param = params[i];
        Object display = param == null ? "null" : param.getClass().isArray() ? "{}" : param;
        buf.append(paramTypes[i]).append(" ").append(display);
      }
      buf.append(")");
      transcript.add(buf.toString());

      if (valueToReturn != GENERATE_YOUR_OWN_VALUE) return valueToReturn;
      return "response from " + buf.toString();
    }

    @Override
    public Plan methodInvoked(String signature, boolean isStatic, Class<?> theClass) {
      final InvocationProfile invocationProfile = new InvocationProfile(signature, isStatic, getClass().getClassLoader());
      return new Plan() {
        @Override
        public Object run(Object instance, Object roboData, Object[] params) throws Exception {
          try {
            return methodInvoked(invocationProfile.clazz, invocationProfile.methodName, instance, invocationProfile.paramTypes, params);
          } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
          }
        }
      };
    }

    @Override
    public Object intercept(String signature, Object instance, Object[] params, Class theClass) throws Throwable {
      StringBuilder buf = new StringBuilder();
      buf.append("intercept: ").append(signature).append(" with params (");
      for (int i = 0; i < params.length; i++) {
        if (i > 0) buf.append(", ");
        Object param = params[i];
        Object display = param == null ? "null" : param.getClass().isArray() ? "{}" : param;
        buf.append(params[i]).append(" ").append(display);
      }
      buf.append(")");
      transcript.add(buf.toString());
      return valueToReturnFromIntercept;
    }

    @Override
    public <T extends Throwable> T stripStackTrace(T throwable) {
      return throwable;
    }
  }

  private static class MyConfig extends InstrumentingClassLoaderConfig {
    private final boolean shouldAcquire;
    private final boolean shouldInstrument;

    private MyConfig(boolean shouldAcquire, boolean shouldInstrument) {
      this.shouldAcquire = shouldAcquire;
      this.shouldInstrument = shouldInstrument;
    }

    @Override
    public boolean shouldAcquire(String name) {
      return shouldAcquire && !name.startsWith("java.");
    }

    @Override
    public boolean shouldInstrument(ClassInfo classInfo) {
      return shouldInstrument;
    }
  }

  private void setClassLoader(ClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  private Class<?> loadClass(Class<?> clazz) throws ClassNotFoundException {
    if (classLoader == null) {
      classLoader = createClassLoader(new InstrumentingClassLoaderConfig());
    }
    RobolectricTestRunner.injectClassHandler(classLoader, classHandler);
    return classLoader.loadClass(clazz.getName());
  }

  private static class MethodInterceptingConfig extends InstrumentingClassLoaderConfig {
    private final HashSet<MethodRef> methodRefs = new HashSet<>();

    private MethodInterceptingConfig(MethodRef... methodRefsToIntercept) {
      Collections.addAll(methodRefs, methodRefsToIntercept);
    }

    @Override
    public Set<MethodRef> methodsToIntercept() {
      return methodRefs;
    }
  }

  private static class ClassRemappingConfig extends InstrumentingClassLoaderConfig {
    @Override
    public Map<String, String> classNameTranslations() {
      Map<String, String> map = new HashMap<>();
      map.put(AClassToForget.class.getName(), AClassToRemember.class.getName());
      return map;
    }

    @Override
    public boolean shouldAcquire(String name) {
      if (name.equals(AClassToForget.class.getName())) {
        throw new RuntimeException(name + " not found (for pretend)!");
      }
      return super.shouldAcquire(name);
    }
  }

  private static class MethodInterceptingClassRemappingConfig extends ClassRemappingConfig {
    private final HashSet<MethodRef> methodRefs = new HashSet<>();

    private MethodInterceptingClassRemappingConfig(MethodRef... methodRefsToIntercept) {
      Collections.addAll(methodRefs, methodRefsToIntercept);
    }

    @Override
    public Set<MethodRef> methodsToIntercept() {
      return methodRefs;
    }
  }
}
