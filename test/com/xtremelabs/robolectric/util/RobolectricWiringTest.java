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

        for (Class<?> lectricClass : Robolectric.getGenericProxies()) {
            verifyClass(lectricClass);
        }

        assertEquals("@Implementation method mismatch: " + mismatches, 0, mismatches.size());
    }

    private void verifyClass(final Class<?> lectricClass) {
        Implements annotation = lectricClass.getAnnotation(Implements.class);
        Class implementedClass = annotation.value();

        try {
            lectricClass.getConstructor(implementedClass);
        } catch (NoSuchMethodException e) {
            try {
                lectricClass.getConstructor();
            } catch (NoSuchMethodException e1) {
                mismatches.add("Missing constructor for " + lectricClass.getSimpleName());
            }
        }

        for (Method lectricMethod : lectricClass.getDeclaredMethods()) {
            verifyMethod(implementedClass, lectricMethod);
        }
    }

    private void verifyMethod(Class implementedClass, Method lectricMethod) {
        boolean isConstructor = lectricMethod.getName().equals("__constructor__");
        if (lectricMethod.isAnnotationPresent(Implementation.class) || isConstructor) {
            Member implementedMember;
            if (isConstructor) {
                implementedMember = findConstructor(implementedClass, lectricMethod);
            } else {
                implementedMember = findMethod(implementedClass, lectricMethod);
            }
            if (implementedMember == null || staticMismatch(lectricMethod, implementedMember)) {
                mismatches.add(lectricMethod.toGenericString());
            }
        }
    }

    private Member findConstructor(Class implementedClass, Method lectricMethod) {
        Class<?>[] parameterTypes = lectricMethod.getParameterTypes();
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

    private Member findMethod(Class implementedClass, Method lectricMethod) {
        Class<?>[] parameterTypes = lectricMethod.getParameterTypes();
        String methodName = lectricMethod.getName();
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

    private boolean staticMismatch(Member lectricMethod, Member implementedMethod) {
        return Modifier.isStatic(implementedMethod.getModifiers()) != Modifier.isStatic(lectricMethod.getModifiers());
    }
}
