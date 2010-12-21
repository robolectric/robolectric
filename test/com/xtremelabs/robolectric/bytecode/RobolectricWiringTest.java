package com.xtremelabs.robolectric.bytecode;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.util.Join;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class RobolectricWiringTest {
    private List<String> mismatches;

    @Before public void setUp() throws Exception {
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
        Member implementedMember;

        boolean isConstructor = shadowMethod.getName().equals("__constructor__");
        if (isAnnotatedImplementation(shadowMethod) || isConstructor) {
            if (isConstructor) {
                implementedMember = findConstructor(implementedClass, shadowMethod);
            } else {
                implementedMember = findMethod(implementedClass, shadowMethod);
            }
            if (implementedMember == null) {
                mismatches.add(shadowMethod.toGenericString() + " doesn't match a real method");
            } else if (staticMismatch(shadowMethod, implementedMember)) {
                mismatches.add(shadowMethod.toGenericString() + " doesn't match the staticness of the real method");
            }
            if (!Modifier.isPublic(shadowMethod.getModifiers())) {
                mismatches.add(shadowMethod.toGenericString() + " should be public");
            }
        } else {
            implementedMember = findMethod(implementedClass, shadowMethod);
            if (implementedMember != null) {
                mismatches.add(shadowMethod.toGenericString() + " should be annotated @Implementation");
            }
        }
    }

    private boolean isAnnotatedImplementation(Method shadowMethod) {
        // works around a weird bug causing overridden methods to show no annotations
        try {
            return shadowMethod.getDeclaringClass().getDeclaredMethod(shadowMethod.getName(), shadowMethod.getParameterTypes()).isAnnotationPresent(Implementation.class);
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
