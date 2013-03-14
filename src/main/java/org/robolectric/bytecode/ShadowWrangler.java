package org.robolectric.bytecode;

import org.robolectric.internal.RealObject;
import org.robolectric.util.Function;
import org.robolectric.util.I18nException;
import org.robolectric.util.Join;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ShadowWrangler implements ClassHandler {
    public static final Function<Object, Object> DO_NOTHING_HANDLER = new Function<Object, Object>() {
        @Override
        public Object call(Object value) {
            return null;
        }
    };
    private static final int MAX_CALL_DEPTH = 200;
    private static final boolean STRIP_SHADOW_STACK_TRACES = true;

    private final Setup setup;

    public boolean debug = false;
    private boolean strictI18n = false;

    private final Map<InvocationProfile, InvocationPlan> invocationPlans = new LinkedHashMap<InvocationProfile, InvocationPlan>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<InvocationProfile, InvocationPlan> eldest) {
            return size() > 500;
        }
    };
    private final Map<Class, MetaShadow> metaShadowMap = new HashMap<Class, MetaShadow>();
    private ShadowMap shadowClassMap = null;
    private boolean logMissingShadowMethods = false;
    private static ThreadLocal<Info> infos = new ThreadLocal<Info>() {
        @Override
        protected Info initialValue() {
            return new Info();
        }
    };

    public void setShadowMap(ShadowMap shadowMap) {
        this.shadowClassMap = shadowMap;
    }

    private static class Info {
        private int callDepth = 0;
    }

    public ShadowWrangler(Setup setup) {
        this.setup = setup;
    }

    @Override
    public void setStrictI18n(boolean strictI18n) {
        this.strictI18n = strictI18n;
    }

    @Override
    public void classInitializing(Class clazz) {
        Class<?> shadowClass = findDirectShadowClass(clazz);
        if (shadowClass != null) {
            try {
                Method method = shadowClass.getMethod(InstrumentingClassLoader.STATIC_INITIALIZER_METHOD_NAME);
                if (!Modifier.isStatic(method.getModifiers())) {
                    throw new RuntimeException(shadowClass.getName() + "." + method.getName() + " is not static");
                }
                method.setAccessible(true);
                method.invoke(null);
            } catch (NoSuchMethodException e) {
                if (setup.shouldPerformStaticInitializationIfShadowIsMissing()) {
                    RobolectricInternals.performStaticInitialization(clazz);
                }
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        } else {
            RobolectricInternals.performStaticInitialization(clazz);
        }
    }

    private String indent(int count) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < count; i++) buf.append("  ");
        return buf.toString();
    }

    @Override
    public Object methodInvoked(Class clazz, String methodName, Object instance, String[] paramTypes, Object[] params) throws Exception {
        Info info = infos.get();
        if (info.callDepth > MAX_CALL_DEPTH) throw stripStackTrace(new StackOverflowError("too deep!"));
        try {
            info.callDepth++;
            InvocationPlan invocationPlan = getInvocationPlan(clazz, methodName, instance, paramTypes);
            try {
                boolean hasShadowImplementation = invocationPlan.hasShadowImplementation();
                boolean callDirect = !hasShadowImplementation && invocationPlan.shouldDelegateToRealMethodWhenMethodShadowIsMissing();

                if (debug) {
                    String plan = hasShadowImplementation
                            ? "calling shadow " + (instance == null ? "?" : invocationPlan.getDeclaredShadowClass().getName())
                            : callDirect ? "calling direct" : "return null";
                    System.out.println(indent(info.callDepth) + " -> " +
                            clazz.getName() + "." + methodName + "(" + Join.join(", ", paramTypes) + "): " + plan);
                }

                if (!hasShadowImplementation) {
//                    reportNoShadowMethodFound(clazz, methodName, paramTypes);
                    return callDirect ? invocationPlan.callOriginal(instance, params) : null;
                } else {

                    // todo: a little strange that this lives here...
                    if (strictI18n && !invocationPlan.isI18nSafe()) {
                        throw new I18nException("Method " + methodName + " on class " + clazz.getName() + " is not i18n-safe.");
                    }

                    return invocationPlan.getMethod().invoke(instance == null ? null : shadowOf(instance), params);
                }
            } catch (IllegalArgumentException e) {
                Object shadow = instance == null ? null : shadowOf(instance);
                Class<? extends Object> aClass = shadow == null ? null : shadow.getClass();
                String aClassName = aClass == null ? "<unknown class>" : aClass.getName();
                throw new RuntimeException(aClassName + " is not assignable from " +
                        invocationPlan.getDeclaredShadowClass().getName(), e);
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                if (cause instanceof Exception) {
                    throw stripStackTrace((Exception) cause);
                }
                throw new RuntimeException(cause);
            }
        } finally {
            info.callDepth--;
        }
    }

    private InvocationPlan getInvocationPlan(Class clazz, String methodName, Object instance, String[] paramTypes) {
        boolean isStatic = instance == null;
        Class shadowClass = isStatic ? findDirectShadowClass(clazz) : shadowOf(instance).getClass();
        InvocationProfile invocationProfile = new InvocationProfile(clazz, shadowClass, methodName, isStatic, paramTypes);
        synchronized (invocationPlans) {
            InvocationPlan invocationPlan = invocationPlans.get(invocationProfile);
            if (invocationPlan == null) {
                invocationPlan = new InvocationPlan(shadowClassMap, invocationProfile);
                invocationPlans.put(invocationProfile, invocationPlan);
            }
            return invocationPlan;
        }
    }

    @Override
    public Object intercept(String className, String methodName, Object instance, Object[] paramTypes, Object[] params) throws Throwable {
        if (debug)
            System.out.println("DEBUG: intercepted call to " + className + "." + methodName + "(" + Join.join(", ", params) + ")");

        return getInterceptionHandler(className, methodName).call(instance);
    }

    public Function<Object, Object> getInterceptionHandler(String className, String methodName) {
        className = className.replace('/', '.');

        if (className.equals(LinkedHashMap.class.getName()) && methodName.equals("eldest")) {
            return new Function<Object, Object>() {
                @Override
                public Object call(Object value) {
                    LinkedHashMap map = (LinkedHashMap) value;
                    return map.entrySet().iterator().next();
                }
            };
        }

        return ShadowWrangler.DO_NOTHING_HANDLER;
    }

    private <T extends Throwable> T stripStackTrace(T throwable) {
        if (STRIP_SHADOW_STACK_TRACES) {
            List<StackTraceElement> stackTrace = new ArrayList<StackTraceElement>();
            for (StackTraceElement stackTraceElement : throwable.getStackTrace()) {
                String className = stackTraceElement.getClassName();
                boolean isInternalCall = className.startsWith("sun.reflect.")
                        || className.startsWith("java.lang.reflect.")
                        || className.equals(ShadowWrangler.class.getName())
                        || className.equals(RobolectricInternals.class.getName());
                if (!isInternalCall) {
                    stackTrace.add(stackTraceElement);
                }
            }
            throwable.setStackTrace(stackTrace.toArray(new StackTraceElement[stackTrace.size()]));
        }
        return throwable;
    }

    private void reportNoShadowMethodFound(Class clazz, String methodName, String[] paramTypes) {
        if (logMissingShadowMethods) {
            System.out.println("No Shadow method found for " + clazz.getSimpleName() + "." + methodName + "(" +
                    Join.join(", ", (Object[]) paramTypes) + ")");
        }
    }

    public static Class<?> loadClass(String paramType, ClassLoader classLoader) {
        Class primitiveClass = RoboType.findPrimitiveClass(paramType);
        if (primitiveClass != null) return primitiveClass;

        int arrayLevel = 0;
        while (paramType.endsWith("[]")) {
            arrayLevel++;
            paramType = paramType.substring(0, paramType.length() - 2);
        }

        Class<?> clazz = RoboType.findPrimitiveClass(paramType);
        if (clazz == null) {
            try {
                clazz = classLoader.loadClass(paramType);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        while (arrayLevel-- > 0) {
            clazz = Array.newInstance(clazz, 0).getClass();
        }

        return clazz;
    }

    public Object shadowFor(Object instance) {
        Field field = RobolectricInternals.getShadowField(instance);
        field.setAccessible(true);
        Object shadow = readField(instance, field);

        if (shadow != null) {
            return shadow;
        }

        String shadowClassName = shadowClassMap.getShadowClassName(instance.getClass());

        if (shadowClassName == null) return new Object();

        if (debug)
            System.out.println("creating new " + shadowClassName + " as shadow for " + instance.getClass().getName());
        try {
            Class<?> shadowClass = loadClass(shadowClassName, instance.getClass().getClassLoader());
            Constructor<?> constructor = findConstructor(instance, shadowClass);
            if (constructor != null) {
                shadow = constructor.newInstance(instance);
            } else {
                shadow = shadowClass.newInstance();
            }
            field.set(instance, shadow);

            injectRealObjectOn(shadow, shadowClass, instance);

            return shadow;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private void injectRealObjectOn(Object shadow, Class<?> shadowClass, Object instance) {
        MetaShadow metaShadow = getMetaShadow(shadowClass);
        for (Field realObjectField : metaShadow.realObjectFields) {
            writeField(shadow, instance, realObjectField);
        }
    }

    private MetaShadow getMetaShadow(Class<?> shadowClass) {
        synchronized (metaShadowMap) {
            MetaShadow metaShadow = metaShadowMap.get(shadowClass);
            if (metaShadow == null) {
                metaShadow = new MetaShadow(shadowClass);
                metaShadowMap.put(shadowClass, metaShadow);
            }
            return metaShadow;
        }
    }

    private Class<?> findDirectShadowClass(Class<?> originalClass) {
        ShadowConfig shadowConfig = shadowClassMap.get(originalClass.getName());
        if (shadowConfig == null) {
            return null;
        }
        return loadClass(shadowConfig.shadowClassName, originalClass.getClassLoader());
    }

    private Constructor<?> findConstructor(Object instance, Class<?> shadowClass) {
        Class clazz = instance.getClass();

        Constructor constructor;
        for (constructor = null; constructor == null && clazz != null; clazz = clazz.getSuperclass()) {
            try {
                constructor = shadowClass.getConstructor(clazz);
            } catch (NoSuchMethodException e) {
                // expected
            }
        }
        return constructor;
    }

    public Object shadowOf(Object instance) {
        if (instance == null) {
            throw new NullPointerException("can't get a shadow for null");
        }
        Field field = RobolectricInternals.getShadowField(instance);
        Object shadow = readField(instance, field);
        if (shadow == null) {
            shadow = shadowFor(instance);
        }
        return shadow;
    }

    private Object readField(Object target, Field field) {
        try {
            return field.get(target);
        } catch (IllegalAccessException e1) {
            throw new RuntimeException(e1);
        }
    }

    private void writeField(Object target, Object value, Field realObjectField) {
        try {
            realObjectField.set(target, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void logMissingInvokedShadowMethods() {
        logMissingShadowMethods = true;
    }

    public void silence() {
        logMissingShadowMethods = false;
    }

    private class MetaShadow {
        List<Field> realObjectFields = new ArrayList<Field>();

        public MetaShadow(Class<?> shadowClass) {
            while (shadowClass != null) {
                for (Field field : shadowClass.getDeclaredFields()) {
                    if (field.isAnnotationPresent(RealObject.class)) {
                        field.setAccessible(true);
                        realObjectFields.add(field);
                    }
                }
                shadowClass = shadowClass.getSuperclass();
            }

        }
    }
}
