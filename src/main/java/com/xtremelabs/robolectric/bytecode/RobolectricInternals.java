package com.xtremelabs.robolectric.bytecode;

import com.xtremelabs.robolectric.bytecode.DirectCallPolicy.DirectCallException;
import com.xtremelabs.robolectric.bytecode.DirectCallPolicy.FullStackDirectCallPolicy;
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
        DirectCallPolicy directCallPolicy = DirectCallPolicy.NOP;
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

    public static <T> T newInstance(Class<T> clazz, Class[] parameterTypes, Object[] params) {
        try {
            Constructor<T> declaredConstructor = clazz.getDeclaredConstructor(parameterTypes);
            declaredConstructor.setAccessible(true);
            return declaredConstructor.newInstance(params);
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
            if (isIgnorableClassLoadingException(typeLoadingException)) {
                //this allows users of the robolectric.jar file to use the non-Google APIs version of the api
                if (unloadableClassNames.add(unloadableClassName)) {
                    System.out.println("Warning: an error occurred while binding shadow class: " + unloadableClassName);
                }
            } else {
                throw typeLoadingException;
            }
        }
    }

    private static boolean isIgnorableClassLoadingException(Throwable typeLoadingException) {
        if (typeLoadingException != null) {
            // instanceof doesn't work here. Are we in different classloaders?
            if (typeLoadingException.getClass().getName().equals(IgnorableClassNotFoundException.class.getName())) {
                return true;
            }

            if (typeLoadingException instanceof NoClassDefFoundError
                    || typeLoadingException instanceof ClassNotFoundException
                    || typeLoadingException instanceof TypeNotPresentException) {
                return isIgnorableClassLoadingException(typeLoadingException.getCause());
            }
        }
        return false;
    }

    private static void setupDirectCallPolicy(DirectCallPolicy newPolicy) {
        Vars vars = ALL_VARS.get();
        try {
            if (newPolicy.checkForChange(vars.directCallPolicy)) {
                vars.directCallPolicy = newPolicy;
            }
        } catch (DirectCallException e) {
            vars.directCallPolicy = DirectCallPolicy.NOP;
            throw e;
        }
    }
    
    public static <T> T directlyOn(T shadowedObject) {
        setupDirectCallPolicy(new DirectCallPolicy.OneShotDirectCallPolicy(shadowedObject));
        return shadowedObject;
    }

    public static <T> T directlyOnFullStack(T shadowedObject) {
        setupDirectCallPolicy(FullStackDirectCallPolicy.withTarget(shadowedObject));
        return shadowedObject;
    }

    public static <T> T directlyOnFullStack(FullStackDirectCallPolicy.Builder<T> builder) {
        FullStackDirectCallPolicy policy = builder.create();
        setupDirectCallPolicy(policy);
        return builder.getTarget();
    }

    public static boolean shouldCallDirectly(Object directInstance) {
        Vars vars = ALL_VARS.get();
        try {
            return vars.directCallPolicy.shouldCallDirectly(directInstance);
        } catch (DirectCallException e) {
            vars.directCallPolicy = DirectCallPolicy.NOP;
            throw e;
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public static void onMethodInvocationFinish(final Object instance) {
        Vars vars = ALL_VARS.get();
        vars.directCallPolicy = vars.directCallPolicy.onMethodInvocationFinished(instance);
    }
    
    @SuppressWarnings({"UnusedDeclaration"})
    public static Object methodInvoked(Class clazz, String methodName, Object instance, String[] paramTypes, Object[] params) throws Throwable {
        try {
          return classHandler.methodInvoked(clazz, methodName, instance, paramTypes, params);
        } catch(java.lang.LinkageError e) {
          throw new Exception(e);
        }
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
