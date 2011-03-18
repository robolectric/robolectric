package com.xtremelabs.robolectric.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionUtil {
    public static Object invoke(Object object, String methodName, Class<?>[] argTypes, Object... args) {
        try {
            Method onLayout = object.getClass().getDeclaredMethod(methodName, argTypes);
            onLayout.setAccessible(true);
            return onLayout.invoke(object, args);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
