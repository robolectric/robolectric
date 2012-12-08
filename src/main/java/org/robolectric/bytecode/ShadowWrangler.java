package org.robolectric.bytecode;

import org.robolectric.internal.RealObject;
import org.robolectric.util.I18nException;
import org.robolectric.util.Join;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

import static java.util.Arrays.asList;

public class ShadowWrangler implements ClassHandler {
    private static final int MAX_CALL_DEPTH = 200;
    private static final boolean STRIP_SHADOW_STACK_TRACES = true;

    private final Setup setup;

    public boolean debug = false;
    private boolean strictI18n = false;

    private final Map<Class, MetaShadow> metaShadowMap = new HashMap<Class, MetaShadow>();
    private Map<String, String> shadowClassMap = new HashMap<String, String>();
    private boolean logMissingShadowMethods = false;
    private static int callDepth = 0;

    public ShadowWrangler(Setup setup) {
        this.setup = setup;
    }

    @Override
    public void setStrictI18n(boolean strictI18n) {
        this.strictI18n = strictI18n;
    }

    @Override
    public void reset() {
        shadowClassMap.clear();
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
                    AndroidTranslator.performStaticInitialization(clazz);
                }
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        } else {
            AndroidTranslator.performStaticInitialization(clazz);
        }
    }

    public void bindShadowClass(String realClassName, Class<?> shadowClass) {
        bindShadowClass(realClassName, shadowClass.getName());
    }

    public void bindShadowClass(Class<?> realClass, Class<?> shadowClass) {
        bindShadowClass(realClass.getName(), shadowClass.getName());
    }

    public void bindShadowClass(String realClassName, String shadowClassName) {
        shadowClassMap.put(realClassName, shadowClassName);
        if (debug) System.out.println("shadow " + realClassName + " with " + shadowClassName);
    }

    private String indent(int count) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < count; i++) buf.append("  ");
        return buf.toString();
    }

    @Override
    public Object methodInvoked(Class clazz, String methodName, Object instance, String[] paramTypes, Object[] params) throws Exception {
        if (callDepth > MAX_CALL_DEPTH) throw stripStackTrace(new StackOverflowError("too deep!"));
        try {
            callDepth++;
            InvocationPlan invocationPlan = new InvocationPlan(clazz, methodName, instance, paramTypes);
            try {
                boolean hasShadowImplementation = invocationPlan.prepare();
                if (debug) {
                    System.out.println(indent(callDepth) + " -> " +
                            clazz.getName() + "." + methodName + "(" + Join.join(", ", paramTypes) + "): "
                            + (hasShadowImplementation ? "shadowed by " + (invocationPlan.shadow == null ? "?" : invocationPlan.shadow.getClass().getName()) : "direct"));
                }

                if (!hasShadowImplementation) {
                    reportNoShadowMethodFound(clazz, methodName, paramTypes);
                    if (invocationPlan.shouldDelegateToRealMethodWhenMethodShadowIsMissing()) {
                        return invocationPlan.callOriginal(params);
                    } else {
                        return null;
                    }
                }

                // todo: a little strange that this lives here...
                if (strictI18n && !invocationPlan.isI18nSafe()) {
                    throw new I18nException("Method " + methodName + " on class " + clazz.getName() + " is not i18n-safe.");
                }

                return invocationPlan.getMethod().invoke(invocationPlan.getShadow(), params);
            } catch (IllegalArgumentException e) {
                Object shadow = invocationPlan.getShadow();
                Class<? extends Object> aClass = shadow == null ? null:shadow.getClass();
                String aClassName = aClass == null ? "<unknown class>":aClass.getName();
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
            callDepth--;
        }
    }

    @Override
    public Object intercept(Class clazz, String methodName, Object instance, Object[] paramTypes, Object[] params) throws Throwable {
        if (debug) System.out.println("DEBUG: intercepted call to " + clazz.getName() + "." + methodName + "(" + Join.join(", ", params) + ")");
        return null;
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

        String shadowClassName = getShadowClassName(instance.getClass());

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
        String shadowClassName = shadowClassMap.get(originalClass.getName());
        if (shadowClassName == null) {
            return null;
        }
        return loadClass(shadowClassName, originalClass.getClassLoader());
    }

    private Class<?> findShadowClass(Class<?> originalClass) {
        String declaredShadowClassName = getShadowClassName(originalClass);
        if (declaredShadowClassName == null) {
            return null;
        }
        return loadClass(declaredShadowClassName, originalClass.getClassLoader());
    }

    private String getShadowClassName(Class clazz) {
        String shadowClassName = null;
        while (shadowClassName == null && clazz != null) {
            shadowClassName = shadowClassMap.get(clazz.getName());
            clazz = clazz.getSuperclass();
        }
        return shadowClassName;
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

    private class InvocationPlan {
        private Class clazz;
        private ClassLoader classLoader;
        private String methodName;
        private Object instance;
        private String[] paramTypes;
        private Class<?>[] paramClasses;
        private Class<?> declaredShadowClass;
        private Method method;
        private Object shadow;

        public InvocationPlan(Class clazz, String methodName, Object instance, String... paramTypes) {
            this.clazz = clazz;
            this.classLoader = clazz.getClassLoader();
            this.methodName = methodName;
            this.instance = instance;
            this.paramTypes = paramTypes;
        }

        public Class<?> getDeclaredShadowClass() {
            return declaredShadowClass;
        }

        public Method getMethod() {
            return method;
        }

        public Object getShadow() {
            return shadow;
        }

        public boolean isI18nSafe() {
        	// method is loaded by another class loader. So do everything reflectively.
        	Annotation[] annos = method.getAnnotations();
        	for (int i = 0; i < annos.length; i++) {
        		String name = annos[i].annotationType().getName();
        		if (name.equals("org.robolectric.internal.Implementation")) {
					try {
						Method m = (annos[i]).getClass().getMethod("i18nSafe");
	        			return (Boolean) m.invoke(annos[i]);
					} catch (Exception e) {
						return true;	// should probably throw some other exception
					}
        		}
        	}

        	return true;
        }

        public boolean prepare() {
            paramClasses = getParamClasses();

            Class<?> originalClass = loadClass(clazz.getName(), classLoader);

            declaredShadowClass = findDeclaredShadowClassForMethod(originalClass, methodName, paramClasses);
            if (declaredShadowClass == null) {
                return false;
            }

            if (methodName.equals("<init>")) {
                methodName = InstrumentingClassLoader.CONSTRUCTOR_METHOD_NAME;
            }

            if (instance != null) {
                shadow = shadowFor(instance);
                String directShadowMethodName = RobolectricInternals.directMethodName(declaredShadowClass.getName(), methodName);

                method = getMethod(shadow.getClass(), directShadowMethodName, paramClasses);
                if (method == null) {
                    method = getMethod(shadow.getClass(), methodName, paramClasses);
                }
            } else {
                shadow = null;
                method = getMethod(findShadowClass(clazz), methodName, paramClasses);
            }

            if (method == null) {
                if (debug) {
                    System.out.println("No method found for " + clazz + "." + methodName + "(" + asList(paramClasses) + ") on " + declaredShadowClass.getName());
                }
                return false;
            }

            if ((instance == null) != Modifier.isStatic(method.getModifiers())) {
                throw new RuntimeException("method staticness of " + clazz.getName() + "." + methodName + " and " + declaredShadowClass.getName() + "." + method.getName() + " don't match");
            }

            method.setAccessible(true);

            return true;
        }

        private Class<?> findDeclaredShadowClassForMethod(Class<?> originalClass, String methodName, Class<?>[] paramClasses) {
            Class<?> declaringClass = findDeclaringClassForMethod(methodName, paramClasses, originalClass);
            return findShadowClass(declaringClass);
        }

        private Class<?> findShadowClass(Class<?> originalClass) {
            String declaredShadowClassName = getShadowClassName(originalClass);
            if (declaredShadowClassName == null) {
                return null;
            }
            return loadClass(declaredShadowClassName, classLoader);
        }

        private Class<?> findDeclaringClassForMethod(String methodName, Class<?>[] paramClasses, Class<?> originalClass) {
            Class<?> declaringClass;
            if (this.methodName.equals(InstrumentingClassLoader.CONSTRUCTOR_METHOD_NAME)) {
                declaringClass = originalClass;
            } else {
                Method originalMethod;
                try {
                    originalMethod = originalClass.getDeclaredMethod(methodName, paramClasses);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
                declaringClass = originalMethod.getDeclaringClass();
            }
            return declaringClass;
        }

        private Class<?>[] getParamClasses() {
            Class<?>[] paramClasses = new Class<?>[paramTypes.length];

            for (int i = 0; i < paramTypes.length; i++) {
                paramClasses[i] = loadClass(paramTypes[i], classLoader);
            }
            return paramClasses;
        }

        private Method getMethod(Class<?> clazz, String methodName, Class<?>[] paramClasses) {
            Method method;
            try {
                method = clazz.getMethod(methodName, paramClasses);
            } catch (NoSuchMethodException e) {
                try {
                    method = clazz.getDeclaredMethod(methodName, paramClasses);
                } catch (NoSuchMethodException e1) {
                    method = null;
                }
            }

            if (method != null && !isOnShadowClass(method)) {
                method = null;
            }

            return method;
        }

        private boolean isOnShadowClass(Method method) {
            Class<?> declaringClass = method.getDeclaringClass();
            // why doesn't getAnnotation(org.robolectric.internal.Implements) work here? It always returns null. pg 20101115
            // It doesn't work because the method and declaringClass were loaded by the delegate class loader. Different classloaders so types don't match. mp 20110823
            for (Annotation annotation : declaringClass.getAnnotations()) { // todo fix
                if (annotation.annotationType().toString().equals("interface org.robolectric.internal.Implements")) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public String toString() {
            return "delegating to " + declaredShadowClass.getName() + "." + method.getName()
                    + "(" + Arrays.toString(method.getParameterTypes()) + ")";
        }

        public Object callOriginal(Object[] params) throws InvocationTargetException, IllegalAccessException {
            try {
                Method method = clazz.getDeclaredMethod(RobolectricInternals.directMethodName(clazz.getName(), methodName), paramClasses);
                method.setAccessible(true);
                return method.invoke(instance, params);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }

        public boolean shouldDelegateToRealMethodWhenMethodShadowIsMissing() {
            boolean delegateToReal = setup.invokeApiMethodBodiesWhenShadowMethodIsMissing(clazz, methodName, paramClasses);
            if (debug) {
                System.out.println("DEBUG: Shall we invoke real method on " + clazz + "." + methodName + "("
                          + Join.join(", ", paramClasses) + ")? " + (delegateToReal ? "yup!" : "nope!"));
            }
            return delegateToReal;
        }
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
