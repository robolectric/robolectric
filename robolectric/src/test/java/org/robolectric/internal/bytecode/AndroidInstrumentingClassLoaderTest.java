package org.robolectric.internal.bytecode;

import android.os.Build;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.internal.AndroidConfigurer;
import org.robolectric.internal.InvokeDynamic;
import org.robolectric.internal.Shadow;
import org.robolectric.internal.ShadowConstants;
import org.robolectric.internal.ShadowExtractor;
import org.robolectric.internal.ShadowImpl;
import org.robolectric.internal.ShadowedObject;
import org.robolectric.internal.bytecode.testing.AChild;
import org.robolectric.internal.bytecode.testing.AClassThatCallsAMethodReturningAForgettableClass;
import org.robolectric.internal.bytecode.testing.AClassThatExtendsAClassWithFinalEqualsHashCode;
import org.robolectric.internal.bytecode.testing.AClassThatRefersToAForgettableClass;
import org.robolectric.internal.bytecode.testing.AClassThatRefersToAForgettableClassInItsConstructor;
import org.robolectric.internal.bytecode.testing.AClassThatRefersToAForgettableClassInMethodCalls;
import org.robolectric.internal.bytecode.testing.AClassThatRefersToAForgettableClassInMethodCallsReturningPrimitive;
import org.robolectric.internal.bytecode.testing.AClassToForget;
import org.robolectric.internal.bytecode.testing.AClassToRemember;
import org.robolectric.internal.bytecode.testing.AClassWithEqualsHashCodeToString;
import org.robolectric.internal.bytecode.testing.AClassWithFunnyConstructors;
import org.robolectric.internal.bytecode.testing.AClassWithMethodReturningArray;
import org.robolectric.internal.bytecode.testing.AClassWithMethodReturningBoolean;
import org.robolectric.internal.bytecode.testing.AClassWithMethodReturningDouble;
import org.robolectric.internal.bytecode.testing.AClassWithMethodReturningInteger;
import org.robolectric.internal.bytecode.testing.AClassWithNativeMethod;
import org.robolectric.internal.bytecode.testing.AClassWithNativeMethodReturningPrimitive;
import org.robolectric.internal.bytecode.testing.AClassWithNoDefaultConstructor;
import org.robolectric.internal.bytecode.testing.AClassWithStaticMethod;
import org.robolectric.internal.bytecode.testing.AClassWithoutEqualsHashCodeToString;
import org.robolectric.internal.bytecode.testing.AFinalClass;
import org.robolectric.internal.bytecode.testing.AnEnum;
import org.robolectric.internal.bytecode.testing.AnExampleClass;
import org.robolectric.internal.bytecode.testing.AnInstrumentedChild;
import org.robolectric.internal.bytecode.testing.AnInstrumentedClassWithoutToStringWithSuperToString;
import org.robolectric.internal.bytecode.testing.AnUninstrumentedClass;
import org.robolectric.internal.bytecode.testing.AnUninstrumentedParent;
import org.robolectric.util.Util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.SwitchPoint;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.invoke.MethodHandles.*;
import static java.lang.invoke.MethodType.methodType;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.util.ReflectionHelpers.newInstance;
import static org.robolectric.util.ReflectionHelpers.setStaticField;

public class AndroidInstrumentingClassLoaderTest {

  private ClassLoader classLoader;

  @Before
  public void setUp() throws Exception {
    classLoader = new InstrumentingClassLoader(configureBuilder().build());
  }

  @Test
  public void shouldMakeBuildVersionIntsNonFinal() throws Exception {
    Class<?> versionClass = loadClass(Build.VERSION.class);
    int modifiers = versionClass.getDeclaredField("SDK_INT").getModifiers();
    assertThat(Modifier.isFinal(modifiers)).as("SDK_INT should be non-final").isFalse();
  }

  ////////////////////////

  @NotNull
  private InstrumentationConfiguration.Builder configureBuilder() {
    return AndroidConfigurer.configure(InstrumentationConfiguration.newBuilder(), new AndroidInterceptors().build());
  }

  private Class<?> loadClass(Class<?> clazz) throws ClassNotFoundException {
    return classLoader.loadClass(clazz.getName());
  }
}
