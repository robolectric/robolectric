package com.xtremelabs.robolectric.bytecode;

import com.xtremelabs.robolectric.RobolectricConfig;
import com.xtremelabs.robolectric.internal.RealObject;
import com.xtremelabs.robolectric.util.I18nException;
import com.xtremelabs.robolectric.util.Join;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtField;
import javassist.NotFoundException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShadowWrangler implements ClassHandler {
    public static final String SHADOW_FIELD_NAME = "__shadow__";

    private static ShadowWrangler singleton;

    public boolean debug = false;
    private boolean strictI18n = false;
    
    private final Map<Class, MetaShadow> metaShadowMap = new HashMap<Class, MetaShadow>();
    private Map<String, String> shadowClassMap = new HashMap<String, String>();
    private Map<Class, Field> shadowFieldMap = new HashMap<Class, Field>();
    private boolean logMissingShadowMethods = false;

    // sorry! it really only makes sense to have one per ClassLoader anyway though [xw/hu]
    public static ShadowWrangler getInstance() {
        if (singleton == null) {
            singleton = new ShadowWrangler();
        }
        return singleton;
    }

    private ShadowWrangler() {
    }
    
    @Override
    public void configure(RobolectricConfig robolectricConfig) {
    	strictI18n = robolectricConfig.getStrictI18n();
    }

    @Override
    public void instrument(CtClass ctClass) {
        try {
            CtClass objectClass = ctClass.getClassPool().get(Object.class.getName());
            try {
                ctClass.getField(SHADOW_FIELD_NAME);
            } catch (NotFoundException e) {
                CtField field = new CtField(objectClass, SHADOW_FIELD_NAME, ctClass);
                field.setModifiers(Modifier.PUBLIC);
                ctClass.addField(field);
            }
        } catch (CannotCompileException e) {
            throw new RuntimeException(e);
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void beforeTest() {
        shadowClassMap.clear();
    }

    @Override
    public void afterTest() {
    }

    public void bindShadowClass(Class<?> realClass, Class<?> shadowClass) {
        shadowClassMap.put(realClass.getName(), shadowClass.getName());
        if (debug) System.out.println("shadow " + realClass + " with " + shadowClass);
    }

    @Override
    public Object methodInvoked(Class clazz, String methodName, Object instance, String[] paramTypes, Object[] params) throws Throwable {
        InvocationPlan invocationPlan = new InvocationPlan(clazz, methodName, instance, paramTypes);
        if (!invocationPlan.prepare()) {
            reportNoShadowMethodFound(clazz, methodName, paramTypes);
            return null;
        }
        
        if (strictI18n && !invocationPlan.isI18nSafe()) {
        	throw new I18nException("Method " + methodName + " on class " + clazz.getName() + " is not i18n-safe.");
        }

        try {
            return invocationPlan.getMethod().invoke(invocationPlan.getShadow(), params);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(invocationPlan.getShadow().getClass().getName() + " is not assignable from " +
                    invocationPlan.getDeclaredShadowClass().getName(), e);
        } catch (InvocationTargetException e) {
            throw stripStackTrace(e.getCause());
        }
    }

    private <T extends Throwable> T stripStackTrace(T throwable) {
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
        return throwable;
    }

    private void reportNoShadowMethodFound(Class clazz, String methodName, String[] paramTypes) {
        if (logMissingShadowMethods) {
            System.out.println("No Shadow method found for " + clazz.getSimpleName() + "." + methodName + "(" +
                    Join.join(", ", (Object[]) paramTypes) + ")");
        }
    }

    public static Class<?> loadClass(String paramType, ClassLoader classLoader) {
        Class primitiveClass = Type.findPrimitiveClass(paramType);
        if (primitiveClass != null) return primitiveClass;

        int arrayLevel = 0;
        while (paramType.endsWith("[]")) {
            arrayLevel++;
            paramType = paramType.substring(0, paramType.length() - 2);
        }

        Class<?> clazz = Type.findPrimitiveClass(paramType);
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
        Field field = getShadowField(instance);
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

    private Field getShadowField(Object instance) {
        Class clazz = instance.getClass();
        Field field = shadowFieldMap.get(clazz);
        if (field == null) {
            try {
                field = clazz.getField(SHADOW_FIELD_NAME);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(instance.getClass().getName() + " has no shadow field", e);
            }
            shadowFieldMap.put(clazz, field);
        }
        return field;
    }

    public Object shadowOf(Object instance) {
        if (instance == null) {
            throw new NullPointerException("can't get a shadow for null");
        }
        Field field = getShadowField(instance);
        return readField(instance, field);
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
        		if (name.equals("com.xtremelabs.robolectric.internal.Implementation")) {
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
            Class<?>[] paramClasses = getParamClasses();

            Class<?> originalClass = loadClass(clazz.getName(), classLoader);

            declaredShadowClass = findDeclaredShadowClassForMethod(originalClass, methodName, paramClasses);
            if (declaredShadowClass == null) {
                return false;
            }

            if (methodName.equals("<init>")) {
                methodName = "__constructor__";
            }

            if (instance != null) {
                shadow = shadowFor(instance);
                method = getMethod(shadow.getClass(), methodName, paramClasses);
            } else {
                shadow = null;
                method = getMethod(findShadowClass(clazz), methodName, paramClasses);
            }

            if (method == null) {
                if (debug) {
                    System.out.println("No method found for " + clazz + "." + methodName + "(" + Arrays.asList(paramClasses) + ") on " + declaredShadowClass.getName());
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
            if (this.methodName.equals("<init>")) {
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
            Method method = null;
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
            // why doesn't getAnnotation(com.xtremelabs.robolectric.internal.Implements) work here? It always returns null. pg 20101115
            // It doesn't work because the method and declaringClass were loaded by the delegate class loader. Different classloaders so types don't match. mp 20110823
            for (Annotation annotation : declaringClass.getAnnotations()) {
                if (annotation.annotationType().toString().equals("interface com.xtremelabs.robolectric.internal.Implements")) {
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
