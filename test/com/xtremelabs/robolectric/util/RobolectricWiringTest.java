package com.xtremelabs.robolectric.util;

import com.xtremelabs.robolectric.Robolectric;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class RobolectricWiringTest {
    private List<String> mismatches;

    @Before public void setUp() throws Exception {
        mismatches = new ArrayList<String>();
    }

    @Test
    public void testAllImplementationMethodsHaveCorrentSignature() throws Exception {

        for (Class<?> shadowClass : Robolectric.getDefaultShadowClasses()) {
            verifyClass(shadowClass);
        }

        assertEquals("@Implementation method mismatch: " + mismatches, 0, mismatches.size());
    }

    private void verifyClass(final Class<?> shadowClass) {
        Implements annotation = shadowClass.getAnnotation(Implements.class);
        Class implementedClass = annotation.value();

        try {
            shadowClass.getConstructor(implementedClass);
        } catch (NoSuchMethodException e) {
            try {
                shadowClass.getConstructor();
            } catch (NoSuchMethodException e1) {
                mismatches.add("Missing constructor for " + shadowClass.getSimpleName());
            }
        }

        for (Method shadowMethod : shadowClass.getDeclaredMethods()) {
            verifyMethod(implementedClass, shadowMethod);
        }
    }

    private void verifyMethod(Class implementedClass, Method shadowMethod) {
        boolean isConstructor = shadowMethod.getName().equals("__constructor__");
        if (shadowMethod.isAnnotationPresent(Implementation.class) || isConstructor) {
            Member implementedMember;
            if (isConstructor) {
                implementedMember = findConstructor(implementedClass, shadowMethod);
            } else {
                implementedMember = findMethod(implementedClass, shadowMethod);
            }
            if (implementedMember == null || staticMismatch(shadowMethod, implementedMember)) {
                mismatches.add(shadowMethod.toGenericString());
            }
            if (!Modifier.isPublic(shadowMethod.getModifiers())) {
                mismatches.add(shadowMethod.toGenericString() + " should be public");
            }
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
