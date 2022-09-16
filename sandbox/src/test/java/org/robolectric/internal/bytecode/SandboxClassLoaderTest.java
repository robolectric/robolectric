package org.robolectric.internal.bytecode;

import static com.google.common.truth.Truth.assertThat;
import static java.lang.invoke.MethodHandles.constant;
import static java.lang.invoke.MethodHandles.dropArguments;
import static java.lang.invoke.MethodHandles.insertArguments;
import static java.lang.invoke.MethodType.methodType;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.util.ReflectionHelpers.newInstance;
import static org.robolectric.util.ReflectionHelpers.setStaticField;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.SwitchPoint;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.testing.AChild;
import org.robolectric.testing.AClassThatCallsAMethodReturningAForgettableClass;
import org.robolectric.testing.AClassThatExtendsAClassWithFinalEqualsHashCode;
import org.robolectric.testing.AClassThatRefersToAForgettableClass;
import org.robolectric.testing.AClassThatRefersToAForgettableClassInItsConstructor;
import org.robolectric.testing.AClassThatRefersToAForgettableClassInMethodCalls;
import org.robolectric.testing.AClassToForget;
import org.robolectric.testing.AClassToRemember;
import org.robolectric.testing.AClassWithEqualsHashCodeToString;
import org.robolectric.testing.AClassWithFunnyConstructors;
import org.robolectric.testing.AClassWithMethodReturningArray;
import org.robolectric.testing.AClassWithMethodReturningBoolean;
import org.robolectric.testing.AClassWithMethodReturningDouble;
import org.robolectric.testing.AClassWithMethodReturningInteger;
import org.robolectric.testing.AClassWithNativeMethod;
import org.robolectric.testing.AClassWithNativeMethodReturningPrimitive;
import org.robolectric.testing.AClassWithNoDefaultConstructor;
import org.robolectric.testing.AClassWithStaticMethod;
import org.robolectric.testing.AFinalClass;
import org.robolectric.testing.AnEnum;
import org.robolectric.testing.AnExampleClass;
import org.robolectric.testing.AnInstrumentedChild;
import org.robolectric.testing.AnUninstrumentedClass;
import org.robolectric.testing.AnUninstrumentedParent;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.Util;

@RunWith(JUnit4.class)
public class SandboxClassLoaderTest {

  private ClassLoader classLoader;
  private List<String> transcript = new ArrayList<>();
  private MyClassHandler classHandler = new MyClassHandler(transcript);
  private ShadowImpl shadow;

  @Before
  public void setUp() throws Exception {
    shadow = new ShadowImpl();
  }

  @Test
  public void shouldMakeClassesNonFinal() throws Exception {
    Class<?> clazz = loadClass(AFinalClass.class);
    assertEquals(0, clazz.getModifiers() & Modifier.FINAL);
  }

  @Test
  public void forClassesWithNoDefaultConstructor_shouldCreateOneButItShouldNotCallShadow()
      throws Exception {
    Constructor<?> defaultCtor = loadClass(AClassWithNoDefaultConstructor.class).getConstructor();
    assertTrue(Modifier.isPublic(defaultCtor.getModifiers()));
    defaultCtor.setAccessible(true);
    Object instance = defaultCtor.newInstance();
    assertThat((Object) shadow.extract(instance)).isNotNull();
    assertThat(transcript).isEmpty();
  }

  @Test
  public void shouldDelegateToHandlerForConstructors() throws Exception {
    Class<?> clazz = loadClass(AClassWithNoDefaultConstructor.class);
    Constructor<?> ctor = clazz.getDeclaredConstructor(String.class);
    assertTrue(Modifier.isPublic(ctor.getModifiers()));
    ctor.setAccessible(true);
    Object instance = ctor.newInstance("new one");
    assertThat(transcript)
        .containsExactly(
            "methodInvoked: AClassWithNoDefaultConstructor.__constructor__(java.lang.String new"
                + " one)");

    Field nameField = clazz.getDeclaredField("name");
    nameField.setAccessible(true);
    assertNull(nameField.get(instance));
  }

  @Test
  public void shouldDelegateClassLoadForUnacquiredClasses() throws Exception {
    InstrumentationConfiguration config = mock(InstrumentationConfiguration.class);
    when(config.shouldAcquire(anyString())).thenReturn(false);
    when(config.shouldInstrument(any(ClassDetails.class))).thenReturn(false);
    ClassLoader classLoader = new SandboxClassLoader(config);
    Class<?> exampleClass = classLoader.loadClass(AnExampleClass.class.getName());
    assertSame(getClass().getClassLoader(), exampleClass.getClassLoader());
  }

  @Test
  public void shouldPerformClassLoadForAcquiredClasses() throws Exception {
    ClassLoader classLoader = new SandboxClassLoader(configureBuilder().build());
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
    ClassLoader classLoader = new SandboxClassLoader(configureBuilder().build());
    Class<?> exampleClass = classLoader.loadClass(AnExampleClass.class.getName());
    assertSame(classLoader, exampleClass.getClassLoader());
    Field roboDataField = exampleClass.getField(ShadowConstants.CLASS_HANDLER_DATA_FIELD_NAME);
    assertNotNull(roboDataField);
    assertThat(Modifier.isPublic(roboDataField.getModifiers())).isTrue();

    // Java 9 doesn't allow updates to final fields from outside <init> or <clinit>:
    // https://bugs.openjdk.java.net/browse/JDK-8157181
    // Therefore, these fields need to be nonfinal / be made nonfinal.
    assertThat(Modifier.isFinal(roboDataField.getModifiers())).isFalse();
    assertThat(Modifier.isFinal(exampleClass.getField("STATIC_FINAL_FIELD").getModifiers()))
        .isFalse();
    assertThat(Modifier.isFinal(exampleClass.getField("nonstaticFinalField").getModifiers()))
        .isFalse();
  }

  @Test
  public void callingNormalMethodShouldInvokeClassHandler() throws Exception {
    Class<?> exampleClass = loadClass(AnExampleClass.class);
    Method normalMethod = exampleClass.getMethod("normalMethod", String.class, int.class);

    Object exampleInstance = exampleClass.getDeclaredConstructor().newInstance();
    assertEquals(
        "response from methodInvoked: AnExampleClass.normalMethod(java.lang.String"
            + " value1, int 123)",
        normalMethod.invoke(exampleInstance, "value1", 123));
    assertThat(transcript)
        .containsExactly(
            "methodInvoked: AnExampleClass.__constructor__()",
            "methodInvoked: AnExampleClass.normalMethod(java.lang.String value1, int 123)");
  }

  @Test
  public void shouldGenerateClassSpecificDirectAccessMethod() throws Exception {
    Class<?> exampleClass = loadClass(AnExampleClass.class);
    String methodName = shadow.directMethodName(exampleClass.getName(), "normalMethod");
    Method directMethod = exampleClass.getDeclaredMethod(methodName, String.class, int.class);
    directMethod.setAccessible(true);
    Object exampleInstance = exampleClass.getDeclaredConstructor().newInstance();
    assertEquals("normalMethod(value1, 123)", directMethod.invoke(exampleInstance, "value1", 123));
    assertThat(transcript).containsExactly("methodInvoked: AnExampleClass.__constructor__()");
  }

  @Test
  public void
      soMockitoDoesntExplodeDueToTooManyMethods_shouldGenerateClassSpecificDirectAccessMethodWhichIsPrivateAndFinal()
          throws Exception {
    Class<?> exampleClass = loadClass(AnExampleClass.class);
    String methodName = shadow.directMethodName(exampleClass.getName(), "normalMethod");
    Method directMethod = exampleClass.getDeclaredMethod(methodName, String.class, int.class);
    assertTrue(Modifier.isPrivate(directMethod.getModifiers()));
    assertTrue(Modifier.isFinal(directMethod.getModifiers()));
  }

  @Test
  public void callingStaticMethodShouldInvokeClassHandler() throws Exception {
    Class<?> exampleClass = loadClass(AClassWithStaticMethod.class);
    Method normalMethod = exampleClass.getMethod("staticMethod", String.class);

    assertEquals(
        "response from methodInvoked: AClassWithStaticMethod.staticMethod(java.lang.String value1)",
        normalMethod.invoke(null, "value1"));
    assertThat(transcript)
        .containsExactly(
            "methodInvoked: AClassWithStaticMethod.staticMethod(java.lang.String value1)");
  }

  @Test
  public void callingStaticDirectAccessMethodShouldWork() throws Exception {
    Class<?> exampleClass = loadClass(AClassWithStaticMethod.class);
    String methodName = shadow.directMethodName(exampleClass.getName(), "staticMethod");
    Method directMethod = exampleClass.getDeclaredMethod(methodName, String.class);
    directMethod.setAccessible(true);
    assertEquals("staticMethod(value1)", directMethod.invoke(null, "value1"));
  }

  @Test
  public void callingNormalMethodReturningIntegerShouldInvokeClassHandler() throws Exception {
    Class<?> exampleClass = loadClass(AClassWithMethodReturningInteger.class);
    classHandler.valueToReturn = 456;

    Method normalMethod = exampleClass.getMethod("normalMethodReturningInteger", int.class);
    Object exampleInstance = exampleClass.getDeclaredConstructor().newInstance();
    assertEquals(456, normalMethod.invoke(exampleInstance, 123));
    assertThat(transcript)
        .containsExactly(
            "methodInvoked: AClassWithMethodReturningInteger.__constructor__()",
            "methodInvoked: AClassWithMethodReturningInteger.normalMethodReturningInteger(int"
                + " 123)");
  }

  @Test
  public void callingMethodReturningDoubleShouldInvokeClassHandler() throws Exception {
    Class<?> exampleClass = loadClass(AClassWithMethodReturningDouble.class);
    classHandler.valueToReturn = 456;

    Method normalMethod = exampleClass.getMethod("normalMethodReturningDouble", double.class);
    Object exampleInstance = exampleClass.getDeclaredConstructor().newInstance();
    assertEquals(456.0, normalMethod.invoke(exampleInstance, 123d));
    assertThat(transcript)
        .containsExactly(
            "methodInvoked: AClassWithMethodReturningDouble.__constructor__()",
            "methodInvoked: AClassWithMethodReturningDouble.normalMethodReturningDouble(double"
                + " 123.0)");
  }

  @Test
  public void callingNativeMethodShouldInvokeClassHandler() throws Exception {
    Class<?> exampleClass = loadClass(AClassWithNativeMethod.class);
    Method normalMethod = exampleClass.getDeclaredMethod("nativeMethod", String.class, int.class);
    Object exampleInstance = exampleClass.getDeclaredConstructor().newInstance();
    assertEquals(
        "response from methodInvoked:"
            + " AClassWithNativeMethod.nativeMethod(java.lang.String value1, int 123)",
        normalMethod.invoke(exampleInstance, "value1", 123));
    assertThat(transcript)
        .containsExactly(
            "methodInvoked: AClassWithNativeMethod.__constructor__()",
            "methodInvoked: AClassWithNativeMethod.nativeMethod(java.lang.String value1, int 123)");
  }

  @Test
  public void directlyCallingNativeMethodShouldBeNoOp() throws Exception {
    Class<?> exampleClass = loadClass(AClassWithNativeMethod.class);
    Object exampleInstance = exampleClass.getDeclaredConstructor().newInstance();
    Method directMethod = findDirectMethod(exampleClass, "nativeMethod", String.class, int.class);
    assertThat(Modifier.isNative(directMethod.getModifiers())).isFalse();

    assertThat(directMethod.invoke(exampleInstance, "", 1)).isNull();
  }

  @Test
  public void directlyCallingNativeMethodReturningPrimitiveShouldBeNoOp() throws Exception {
    Class<?> exampleClass = loadClass(AClassWithNativeMethodReturningPrimitive.class);
    Object exampleInstance = exampleClass.getDeclaredConstructor().newInstance();
    Method directMethod = findDirectMethod(exampleClass, "nativeMethod");
    assertThat(Modifier.isNative(directMethod.getModifiers())).isFalse();

    assertThat(directMethod.invoke(exampleInstance)).isEqualTo(0);
  }

  @Test
  public void shouldHandleMethodsReturningBoolean() throws Exception {
    Class<?> exampleClass = loadClass(AClassWithMethodReturningBoolean.class);
    classHandler.valueToReturn = true;

    Method directMethod =
        exampleClass.getMethod("normalMethodReturningBoolean", boolean.class, boolean[].class);
    directMethod.setAccessible(true);
    Object exampleInstance = exampleClass.getDeclaredConstructor().newInstance();
    assertEquals(true, directMethod.invoke(exampleInstance, true, new boolean[0]));
    assertThat(transcript)
        .containsExactly(
            "methodInvoked: AClassWithMethodReturningBoolean.__constructor__()",
            "methodInvoked: AClassWithMethodReturningBoolean.normalMethodReturningBoolean(boolean"
                + " true, boolean[] {})");
  }

  @Test
  public void shouldHandleMethodsReturningArray() throws Exception {
    Class<?> exampleClass = loadClass(AClassWithMethodReturningArray.class);
    classHandler.valueToReturn = new String[] {"miao, mieuw"};

    Method directMethod = exampleClass.getMethod("normalMethodReturningArray");
    directMethod.setAccessible(true);
    Object exampleInstance = exampleClass.getDeclaredConstructor().newInstance();
    assertThat(transcript)
        .containsExactly("methodInvoked: AClassWithMethodReturningArray.__constructor__()");
    transcript.clear();
    assertArrayEquals(
        new String[] {"miao, mieuw"}, (String[]) directMethod.invoke(exampleInstance));
    assertThat(transcript)
        .containsExactly(
            "methodInvoked: AClassWithMethodReturningArray.normalMethodReturningArray()");
  }

  @Test
  public void shouldInvokeShadowForEachConstructorInInheritanceTree() throws Exception {
    loadClass(AChild.class).getDeclaredConstructor().newInstance();
    assertThat(transcript)
        .containsExactly(
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
    assertThat(transcript)
        .containsExactly(
            "methodInvoked:"
                + " AClassWithFunnyConstructors.__constructor__("
                + AnUninstrumentedParent.class.getName()
                + " UninstrumentedParent{parentName='hortense'},"
                + " java.lang.String"
                + " foo)",
            "methodInvoked: AClassWithFunnyConstructors.__constructor__(java.lang.String"
                + " hortense)");

    // should not run constructor bodies...
    assertEquals(null, getDeclaredFieldValue(aClass, o, "name"));
    assertEquals(null, getDeclaredFieldValue(aClass, o, "uninstrumentedParent"));
  }

  @Test
  public void shouldGenerateClassSpecificDirectAccessMethodForConstructorWhichDoesNotCallSuper()
      throws Exception {
    Class<?> aClass = loadClass(AClassWithFunnyConstructors.class);
    Object instance = aClass.getConstructor(String.class).newInstance("horace");
    assertThat(transcript)
        .containsExactly(
            "methodInvoked:"
                + " AClassWithFunnyConstructors.__constructor__("
                + AnUninstrumentedParent.class.getName()
                + " UninstrumentedParent{parentName='horace'},"
                + " java.lang.String"
                + " foo)",
            "methodInvoked: AClassWithFunnyConstructors.__constructor__(java.lang.String horace)");
    transcript.clear();

    // each directly-accessible constructor body will need to be called explicitly, with the correct
    // args...

    Class<?> uninstrumentedParentClass = loadClass(AnUninstrumentedParent.class);
    Method directMethod =
        findDirectMethod(aClass, "__constructor__", uninstrumentedParentClass, String.class);
    Object uninstrumentedParentIn =
        uninstrumentedParentClass.getDeclaredConstructor(String.class).newInstance("hortense");
    assertEquals(null, directMethod.invoke(instance, uninstrumentedParentIn, "foo"));
    assertThat(transcript).isEmpty();

    assertEquals(null, getDeclaredFieldValue(aClass, instance, "name"));
    Object uninstrumentedParentOut =
        getDeclaredFieldValue(aClass, instance, "uninstrumentedParent");
    assertEquals(
        "hortense",
        getDeclaredFieldValue(uninstrumentedParentClass, uninstrumentedParentOut, "parentName"));

    Method directMethod2 = findDirectMethod(aClass, "__constructor__", String.class);
    assertEquals(null, directMethod2.invoke(instance, "hortense"));
    assertThat(transcript).isEmpty();

    assertEquals("hortense", getDeclaredFieldValue(aClass, instance, "name"));
  }

  private Method findDirectMethod(
      Class<?> declaringClass, String methodName, Class<?>... argClasses)
      throws NoSuchMethodException {
    String directMethodName = shadow.directMethodName(declaringClass.getName(), methodName);
    Method directMethod = declaringClass.getDeclaredMethod(directMethodName, argClasses);
    directMethod.setAccessible(true);
    return directMethod;
  }

  @Test
  public void shouldNotInstrumentFinalEqualsHashcode() throws ClassNotFoundException {
    loadClass(AClassThatExtendsAClassWithFinalEqualsHashCode.class);
  }

  @Test
  @SuppressWarnings("CheckReturnValue")
  public void shouldAlsoInstrumentEqualsAndHashCodeAndToStringWhenDeclared() throws Exception {
    Class<?> theClass = loadClass(AClassWithEqualsHashCodeToString.class);
    Object instance = theClass.getDeclaredConstructor().newInstance();
    assertThat(transcript)
        .containsExactly("methodInvoked:" + " AClassWithEqualsHashCodeToString.__constructor__()");
    transcript.clear();

    instance.toString();
    assertThat(transcript)
        .containsExactly("methodInvoked:" + " AClassWithEqualsHashCodeToString.toString()");
    transcript.clear();

    classHandler.valueToReturn = true;
    //noinspection ResultOfMethodCallIgnored,ObjectEqualsNull
    instance.equals(null);
    assertThat(transcript)
        .containsExactly(
            "methodInvoked:"
                + " AClassWithEqualsHashCodeToString.equals(java.lang.Object"
                + " null)");
    transcript.clear();

    classHandler.valueToReturn = 42;
    //noinspection ResultOfMethodCallIgnored
    instance.hashCode();
    assertThat(transcript)
        .containsExactly("methodInvoked:" + " AClassWithEqualsHashCodeToString.hashCode()");
  }

  @Test
  public void shouldRemapClasses() throws Exception {
    setClassLoader(new SandboxClassLoader(createRemappingConfig()));
    Class<?> theClass = loadClass(AClassThatRefersToAForgettableClass.class);
    assertEquals(loadClass(AClassToRemember.class), theClass.getField("someField").getType());
    assertEquals(
        Array.newInstance(loadClass(AClassToRemember.class), 0).getClass(),
        theClass.getField("someFields").getType());
  }

  private InstrumentationConfiguration createRemappingConfig() {
    return configureBuilder()
        .addClassNameTranslation(AClassToForget.class.getName(), AClassToRemember.class.getName())
        .build();
  }

  @Test
  public void shouldFixTypesInFieldAccess() throws Exception {
    setClassLoader(new SandboxClassLoader(createRemappingConfig()));
    Class<?> theClass = loadClass(AClassThatRefersToAForgettableClassInItsConstructor.class);
    Object instance = theClass.getDeclaredConstructor().newInstance();
    Method method =
        theClass.getDeclaredMethod(
            shadow.directMethodName(theClass.getName(), ShadowConstants.CONSTRUCTOR_METHOD_NAME));
    method.setAccessible(true);
    method.invoke(instance);
  }

  @Test
  public void shouldFixTypesInMethodArgsAndReturn() throws Exception {
    setClassLoader(new SandboxClassLoader(createRemappingConfig()));
    Class<?> theClass = loadClass(AClassThatRefersToAForgettableClassInMethodCalls.class);
    assertNotNull(
        theClass.getDeclaredMethod(
            "aMethod", int.class, loadClass(AClassToRemember.class), String.class));
  }

  @Test
  public void shouldInterceptFilteredMethodInvocations() throws Exception {
    setClassLoader(
        new SandboxClassLoader(
            configureBuilder()
                .addInterceptedMethod(new MethodRef(AClassToForget.class, "forgettableMethod"))
                .build()));

    Class<?> theClass = loadClass(AClassThatRefersToAForgettableClass.class);
    Object instance = theClass.getDeclaredConstructor().newInstance();
    Object output =
        theClass
            .getMethod("interactWithForgettableClass")
            .invoke(shadow.directlyOn(instance, (Class<Object>) theClass));
    assertEquals("null, get this!", output);
  }

  @Test
  public void shouldInterceptFilteredStaticMethodInvocations() throws Exception {
    setClassLoader(
        new SandboxClassLoader(
            configureBuilder()
                .addInterceptedMethod(
                    new MethodRef(AClassToForget.class, "forgettableStaticMethod"))
                .build()));

    Class<?> theClass = loadClass(AClassThatRefersToAForgettableClass.class);
    Object instance = theClass.getDeclaredConstructor().newInstance();
    Object output =
        theClass
            .getMethod("interactWithForgettableStaticMethod")
            .invoke(shadow.directlyOn(instance, (Class<Object>) theClass));
    assertEquals("yess? forget this: null", output);
  }

  @Nonnull
  private InstrumentationConfiguration.Builder configureBuilder() {
    InstrumentationConfiguration.Builder builder = InstrumentationConfiguration.newBuilder();
    builder
        .doNotAcquirePackage("java.")
        .doNotAcquirePackage("jdk.internal.")
        .doNotAcquirePackage("sun.")
        .doNotAcquirePackage("com.sun.")
        .doNotAcquirePackage("org.robolectric.internal.")
        .doNotAcquirePackage("org.robolectric.pluginapi.");
    return builder;
  }

  @Test
  public void shouldRemapClassesWhileInterceptingMethods() throws Exception {
    InstrumentationConfiguration config =
        configureBuilder()
            .addClassNameTranslation(
                AClassToForget.class.getName(), AClassToRemember.class.getName())
            .addInterceptedMethod(
                new MethodRef(
                    AClassThatCallsAMethodReturningAForgettableClass.class, "getAForgettableClass"))
            .build();

    setClassLoader(new SandboxClassLoader(config));
    Class<?> theClass = loadClass(AClassThatCallsAMethodReturningAForgettableClass.class);
    theClass
        .getMethod("callSomeMethod")
        .invoke(
            shadow.directlyOn(
                theClass.getDeclaredConstructor().newInstance(), (Class<Object>) theClass));
  }

  @Test
  public void shouldWorkWithEnums() throws Exception {
    loadClass(AnEnum.class);
  }

  @Test
  public void shouldReverseAnArray() throws Exception {
    assertArrayEquals(new Integer[] {5, 4, 3, 2, 1}, Util.reverse(new Integer[] {1, 2, 3, 4, 5}));
    assertArrayEquals(new Integer[] {4, 3, 2, 1}, Util.reverse(new Integer[] {1, 2, 3, 4}));
    assertArrayEquals(new Integer[] {1}, Util.reverse(new Integer[] {1}));
    assertArrayEquals(new Integer[] {}, Util.reverse(new Integer[] {}));
  }

  /////////////////////////////

  private Object getDeclaredFieldValue(Class<?> aClass, Object o, String fieldName)
      throws Exception {
    Field field = aClass.getDeclaredField(fieldName);
    field.setAccessible(true);
    return field.get(o);
  }

  public static class MyClassHandler implements ClassHandler {
    private static final Object GENERATE_YOUR_OWN_VALUE = new Object();
    private List<String> transcript;
    private Object valueToReturn = GENERATE_YOUR_OWN_VALUE;
    private Object valueToReturnFromIntercept = null;

    public MyClassHandler(List<String> transcript) {
      this.transcript = transcript;
    }

    @Override
    public void classInitializing(Class clazz) {}

    @Override
    public Object initializing(Object instance) {
      return "a shadow!";
    }

    public Object methodInvoked(
        Class clazz, String methodName, Object instance, String[] paramTypes, Object[] params) {
      StringBuilder buf = new StringBuilder();
      buf.append("methodInvoked:" + " ")
          .append(clazz.getSimpleName())
          .append(".")
          .append(methodName)
          .append("(");
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
    public MethodHandle getShadowCreator(Class<?> theClass) {
      return dropArguments(constant(String.class, "a shadow!"), 0, theClass);
    }

    @SuppressWarnings(value = {"UnusedDeclaration", "unused"})
    private Object invoke(InvocationProfile invocationProfile, Object instance, Object[] params) {
      return methodInvoked(
          invocationProfile.clazz,
          invocationProfile.methodName,
          instance,
          invocationProfile.paramTypes,
          params);
    }

    @Override
    public MethodHandle findShadowMethodHandle(
        Class<?> theClass, String name, MethodType type, boolean isStatic)
        throws IllegalAccessException {
      String signature = getSignature(theClass, name, type, isStatic);
      InvocationProfile invocationProfile =
          new InvocationProfile(signature, isStatic, getClass().getClassLoader());

      try {
        MethodHandle mh =
            MethodHandles.lookup()
                .findVirtual(
                    getClass(),
                    "invoke",
                    methodType(
                        Object.class, InvocationProfile.class, Object.class, Object[].class));
        mh = insertArguments(mh, 0, this, invocationProfile);

        if (isStatic) {
          return mh.bindTo(null).asCollector(Object[].class, type.parameterCount());
        } else {
          return mh.asCollector(Object[].class, type.parameterCount() - 1);
        }
      } catch (NoSuchMethodException e) {
        throw new AssertionError(e);
      }
    }

    public String getSignature(Class<?> caller, String name, MethodType type, boolean isStatic) {
      String className = caller.getName().replace('.', '/');
      // Remove implicit first argument
      if (!isStatic) type = type.dropParameterTypes(0, 1);
      return className + "/" + name + type.toMethodDescriptorString();
    }

    @Override
    public Object intercept(String signature, Object instance, Object[] params, Class theClass)
        throws Throwable {
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

  private void setClassLoader(ClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  private Class<?> loadClass(Class<?> clazz) throws ClassNotFoundException {
    if (classLoader == null) {
      classLoader = new SandboxClassLoader(configureBuilder().build());
    }

    setStaticField(
        classLoader.loadClass(InvokeDynamicSupport.class.getName()),
        "INTERCEPTORS",
        new Interceptors(Collections.<Interceptor>emptyList()));
    setStaticField(
        classLoader.loadClass(Shadow.class.getName()),
        "SHADOW_IMPL",
        newInstance(classLoader.loadClass(ShadowImpl.class.getName())));

    ShadowInvalidator invalidator = Mockito.mock(ShadowInvalidator.class);
    when(invalidator.getSwitchPoint(any(Class.class))).thenReturn(new SwitchPoint());

    String className = RobolectricInternals.class.getName();
    Class<?> robolectricInternalsClass = ReflectionHelpers.loadClass(classLoader, className);
    ReflectionHelpers.setStaticField(robolectricInternalsClass, "classHandler", classHandler);
    ReflectionHelpers.setStaticField(robolectricInternalsClass, "shadowInvalidator", invalidator);

    return classLoader.loadClass(clazz.getName());
  }

  @Test
  public void shouldDumpClassesWhenConfigured() throws Exception {
    Path tempDir = Files.createTempDirectory("SandboxClassLoaderTest");
    System.setProperty("robolectric.dumpClassesDirectory", tempDir.toAbsolutePath().toString());
    ClassLoader classLoader = new SandboxClassLoader(configureBuilder().build());
    classLoader.loadClass(AnExampleClass.class.getName());
    try (Stream<Path> stream = Files.list(tempDir)) {
      List<Path> files = stream.collect(Collectors.toList());
      assertThat(files).hasSize(1);
      assertThat(files.get(0).toAbsolutePath().toString())
          .containsMatch("org.robolectric.testing.AnExampleClass-robo-instrumented-\\d+.class");
      Files.delete(files.get(0));
    } finally {
      Files.delete(tempDir);
      System.clearProperty("robolectric.dumpClassesDirectory");
    }
  }
}
