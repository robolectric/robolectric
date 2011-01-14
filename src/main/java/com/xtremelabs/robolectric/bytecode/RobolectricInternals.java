package com.xtremelabs.robolectric.bytecode;

import com.xtremelabs.robolectric.internal.Implements;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings({"UnusedDeclaration"})
public class RobolectricInternals {
    // initialized via magic by AndroidTranslator
    private static ClassHandler classHandler;
    private static Set<String> unloadableClassNames = new HashSet<String>();

    private static final ThreadLocal<Vars> ALL_VARS = new ThreadLocal<Vars>() {
        @Override protected Vars initialValue() {
            return new Vars();
        }
    };

    private static class Vars {
        Object callDirectly;
    }

    public static <T> T newInstanceOf(Class<T> clazz) {
        try {
            Constructor<T> defaultConstructor = clazz.getDeclaredConstructor();
            defaultConstructor.setAccessible(true);
            return defaultConstructor.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static void bindShadowClass(Class<?> shadowClass) {
        Implements realClass = shadowClass.getAnnotation(Implements.class);
        if (realClass == null) {
            throw new IllegalArgumentException(shadowClass + " is not annotated with @Implements");
        }

        try {
            ShadowWrangler.getInstance().bindShadowClass(realClass.value(), shadowClass);
        } catch (TypeNotPresentException typeLoadingException) {
            String unloadableClassName = shadowClass.getSimpleName();
            if (isIgnorable(typeLoadingException)) {
                //this allows users of the robolectric.jar file to use the non-Google APIs version of the api
                if (unloadableClassNames.add(unloadableClassName)) {
                    System.out.println("Warning: an error occurred while binding shadow class: " + unloadableClassName);
                }
            } else {
                throw typeLoadingException;
            }
        }
    }

    private static boolean isIgnorable(TypeNotPresentException typeLoadingException) {
        Throwable cause = typeLoadingException.getCause();
        if (cause instanceof NoClassDefFoundError) {
            cause = cause.getCause();
            if (cause instanceof ClassNotFoundException) {
                cause = cause.getCause();
                // instanceof doesn't work here. Are we in different classloaders?
                if (cause.getClass().getName().equals(RobolectricClassNotFoundException.class.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static <T> T directlyOn(T shadowedObject) {
        Vars vars = ALL_VARS.get();

        if (vars.callDirectly != null) {
            Object expectedInstance = vars.callDirectly;
            vars.callDirectly = null;
            throw new RuntimeException("already expecting a direct call on <" + expectedInstance + "> but here's a new request for <" + shadowedObject + ">");
        }

        vars.callDirectly = shadowedObject;
        return shadowedObject;
    }

    public static boolean shouldCallDirectly(Object directInstance) {
        Vars vars = ALL_VARS.get();
        if (vars.callDirectly != null) {
            if (vars.callDirectly != directInstance) {
                Object expectedInstance = vars.callDirectly;
                vars.callDirectly = null;
                throw new RuntimeException("expected to perform direct call on <" + expectedInstance + "> but got <" + directInstance + ">");
            } else {
                vars.callDirectly = null;
            }
            return true;
        } else {
            return false;
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public static Object methodInvoked(Class clazz, String methodName, Object instance, String[] paramTypes, Object[] params) throws Exception {
        return classHandler.methodInvoked(clazz, methodName, instance, paramTypes, params);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public static Object autobox(Object o) {
        return o;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public static Object autobox(boolean o) {
        return o;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public static Object autobox(byte o) {
        return o;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public static Object autobox(char o) {
        return o;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public static Object autobox(short o) {
        return o;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public static Object autobox(int o) {
        return o;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public static Object autobox(long o) {
        return o;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public static Object autobox(float o) {
        return o;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public static Object autobox(double o) {
        return o;
    }
}
