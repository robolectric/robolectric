package com.xtremelabs.robolectric;

import com.xtremelabs.robolectric.util.RealObject;
import com.xtremelabs.robolectric.util.ShadowWrangler;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtField;
import javassist.NotFoundException;

import java.lang.reflect.*;
import java.util.*;

public class ProxyDelegatingHandler implements ClassHandler {
    public static final String SHADOW_FIELD_NAME = "__shadow__";
    private static ProxyDelegatingHandler singleton;

    private Map<String, String> shadowClassMap = new HashMap<String, String>();
    private Map<Class, Field> shadowFieldMap = new HashMap<Class, Field>();
    private final Map<Class, MetaShadow> metaShadowMap = new HashMap<Class, MetaShadow>();

    public boolean debug = false;

    // sorry! it really only makes sense to have one per ClassLoader anyway though [xw/hu]
    public static ProxyDelegatingHandler getInstance() {
        if (singleton == null) {
            singleton = new ProxyDelegatingHandler();
        }
        return singleton;
    }

    private ProxyDelegatingHandler() {
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

    public void addProxyClass(Class<?> realClass, Class<?> handlerClass) {
        shadowClassMap.put(realClass.getName(), handlerClass.getName());
        if (debug) System.out.println("handle " + realClass + " with " + handlerClass);
    }

    @Override
    public Object methodInvoked(Class clazz, String methodName, Object instance, String[] paramTypes, Object[] params) {
        InvocationPlan invocationPlan = new InvocationPlan(clazz, methodName, instance, paramTypes);
        if (!invocationPlan.prepare()) return null;

        try {
            return invocationPlan.getMethod().invoke(invocationPlan.getShadow(), params);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(invocationPlan.getShadow().getClass().getName() + " is not assignable from " + invocationPlan.getHandlingClass().getName(), e);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            } else {
                throw new RuntimeException("Did your shadow implementation of a method throw an exception? Refer to the bottom of this stack trace.", e);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
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

        String shadowClassName = getHandlingClassName(instance.getClass());

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
        for (Field shadowWranglerField : metaShadow.shadowWranglerFields) {
            writeField(shadow, this, shadowWranglerField);
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

    private String getHandlingClassName(Class clazz) {
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
            throw new RuntimeException("no instance for which to get a proxy");
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

    private class InvocationPlan {
        private Class clazz;
        private String methodName;
        private Object instance;
        private String[] paramTypes;
        private Class<?> handlingClass;
        private Method method;
        private Object shadow;

        public InvocationPlan(Class clazz, String methodName, Object instance, String... paramTypes) {
            this.clazz = clazz;
            this.methodName = methodName;
            this.instance = instance;
            this.paramTypes = paramTypes;
        }

        public Class<?> getHandlingClass() {
            return handlingClass;
        }

        public Method getMethod() {
            return method;
        }

        public Object getShadow() {
            return shadow;
        }

        public boolean prepare() {
            Class<?>[] paramClasses = new Class<?>[paramTypes.length];

            ClassLoader classLoader = clazz.getClassLoader();
            for (int i = 0; i < paramTypes.length; i++) {
                paramClasses[i] = loadClass(paramTypes[i], classLoader);
            }

            Class<?> originalClass = loadClass(clazz.getName(), classLoader);

            Class<?> declaringClass;
            if (methodName.equals("<init>")) {
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
            String handlingClassName = getHandlingClassName(declaringClass);
            if (handlingClassName == null) {
                return false;
            }
            handlingClass = loadClass(handlingClassName, classLoader);

            if (methodName.equals("<init>")) {
                methodName = "__constructor__";
            }

            if (instance != null) {
                shadow = shadowFor(instance);
                method = getMethod(shadow.getClass(), methodName, paramClasses);
            } else {
                shadow = null;
                String shadowClassName = getHandlingClassName(clazz);
                Class<?> shadowClass = loadClass(shadowClassName, classLoader);
                method = getMethod(shadowClass, methodName, paramClasses);
            }

            if (method == null) {
                if (debug) {
                    System.out.println("No method found for " + clazz + "." + methodName + "(" + Arrays.asList(paramClasses) + ") on " + handlingClass.getName());
                }
                return false;
            }

            if ((instance == null) != Modifier.isStatic(method.getModifiers())) {
                throw new RuntimeException("method staticness of " + clazz.getName() + "." + methodName + " and " + handlingClassName + "." + method.getName() + " don't match");
            }

            method.setAccessible(true);

            return true;
        }

        private Method getMethod(Class<?> clazz, String methodName, Class<?>[] paramClasses) {
            try {
                return clazz.getMethod(methodName, paramClasses);
            } catch (NoSuchMethodException e) {
                try {
                    return clazz.getDeclaredMethod(methodName, paramClasses);
                } catch (NoSuchMethodException e1) {
                    return null;
                }
            }
        }

        @Override public String toString() {
            return "delegating to " + handlingClass.getName() + "." + method.getName()
                    + "(" + Arrays.toString(method.getParameterTypes()) + ")";
        }
    }

    private class MetaShadow {
        private Class<?> shadowClass;
        List<Field> realObjectFields = new ArrayList<Field>();
        List<Field> shadowWranglerFields = new ArrayList<Field>();

        public MetaShadow(Class<?> shadowClass) {
            this.shadowClass = shadowClass;

            while (shadowClass != null) {
                for (Field field : shadowClass.getDeclaredFields()) {
                    if (field.isAnnotationPresent(RealObject.class)) {
                        field.setAccessible(true);
                        realObjectFields.add(field);
                    }

                    if (field.isAnnotationPresent(ShadowWrangler.class)) {
                        field.setAccessible(true);
                        shadowWranglerFields.add(field);
                    }
                }
                shadowClass = shadowClass.getSuperclass();
            }

        }
    }
}
