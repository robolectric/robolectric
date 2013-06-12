package org.robolectric.bytecode;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.internal.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.Join;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class RobolectricWiringTest {
  private List<String> mismatches;

  @Before
  public void setUp() throws Exception {
    mismatches = new ArrayList<String>();
  }

  @Test
  public void testAllImplementationMethodsHaveCorrectSignature() throws Exception {
    for (Class<?> shadowClass : Robolectric.getDefaultShadowClasses()) {
      verifyClass(shadowClass);
    }

    Assert.assertEquals("@Implementation method mismatch: " + Join.join("\n", mismatches), 0, mismatches.size());
  }

  private void verifyClass(final Class<?> shadowClass) {
    Implements annotation = shadowClass.getAnnotation(Implements.class);
    Class implementedClass = annotation.value();
    if (implementedClass.getName().equals(Robolectric.Anything.class.getName())) return;

    try {
      shadowClass.getConstructor(implementedClass);
    } catch (NoSuchMethodException e) {
      try {
        shadowClass.getConstructor();
      } catch (NoSuchMethodException e1) {
        mismatches.add("Missing constructor for " + shadowClass.getSimpleName());
      }
    }


    String expectedName = ShadowMap.convertToShadowName(implementedClass.getName());
    if (!shadowClass.getName().equals(expectedName)) {
      mismatches.add("Shadow class " + shadowClass.getName() + " didn't have the expected name, should be " + expectedName);
    }

    for (Method shadowMethod : shadowClass.getDeclaredMethods()) {
      verifyMethod(implementedClass, shadowMethod);
    }
  }

  private void verifyMethod(Class implementedClass, Method shadowMethod) {
    Member implementedMember;

    boolean isConstructor = shadowMethod.getName().equals("__constructor__");
    if (isAnnotatedImplementation(shadowMethod) || isConstructor) {
      if (isConstructor) {
        implementedMember = findConstructor(implementedClass, shadowMethod);
      } else {
        implementedMember = findMethod(implementedClass, shadowMethod);
      }
      if (implementedMember == null) {
        if (!isAnnotatedHiddenApi(shadowMethod)) {
          mismatches.add(shadowMethod.toGenericString() + " doesn't match a real method (maybe it's a @HiddenApi?)");
        }
      } else if (staticMismatch(shadowMethod, implementedMember)) {
        mismatches.add(shadowMethod.toGenericString() + " doesn't match the staticness of the real method");
      }
      if (!Modifier.isPublic(shadowMethod.getModifiers())) {
        mismatches.add(shadowMethod.toGenericString() + " should be public");
      }
    } else {
      implementedMember = findMethod(implementedClass, shadowMethod);
      if (implementedMember != null) {
        if (shadowMethod.toGenericString().contains("ShadowDialogFragment.dismissAllowingStateLoss")) {
          System.err.println("!!!!!!!!!!  WARNING: ShadowDialogFragment.dismissAllowingStateLoss should be annotated @Implementation when Maven gets the new Android Support Library  !!!!!!!!!!");
          return;
        }
        mismatches.add(shadowMethod.toGenericString() + " should be annotated @Implementation");
      }
    }
  }

  private boolean isAnnotatedHiddenApi(Method shadowMethod) {
    return isAnnotated(shadowMethod, HiddenApi.class);
  }

  private boolean isAnnotatedImplementation(Method shadowMethod) {
    return isAnnotated(shadowMethod, Implementation.class);

  }

  private boolean isAnnotated(Method shadowMethod, Class<? extends Annotation> annotationClass) {
    // works around a weird bug causing overridden methods to show no annotations
    try {
      return shadowMethod.getDeclaringClass().getDeclaredMethod(shadowMethod.getName(), shadowMethod.getParameterTypes()).isAnnotationPresent(annotationClass);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  private Member findConstructor(Class implementedClass, Method shadowMethod) {
    Class<?>[] parameterTypes = shadowMethod.getParameterTypes();
    try {
      return implementedClass.getConstructor(parameterTypes);
    } catch (NoSuchMethodException e1) {
      try {
        return implementedClass.getDeclaredConstructor(parameterTypes);
      } catch (NoSuchMethodException e2) {
        return null;
      }
    }
  }

  private Member findMethod(Class implementedClass, Method shadowMethod) {
    Class<?>[] parameterTypes = shadowMethod.getParameterTypes();
    String methodName = shadowMethod.getName();
    try {
      return implementedClass.getMethod(methodName, parameterTypes);
    } catch (NoSuchMethodException e1) {
      try {
        return implementedClass.getDeclaredMethod(methodName, parameterTypes);
      } catch (NoSuchMethodException e2) {
        return null;
      }
    }
  }

  private boolean staticMismatch(Member shadowMethod, Member implementedMethod) {
    return Modifier.isStatic(implementedMethod.getModifiers()) != Modifier.isStatic(shadowMethod.getModifiers());
  }
}
