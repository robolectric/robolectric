package com.xtremelabs.robolectric;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtField;
import javassist.NotFoundException;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ProxyDelegatingHandler implements ClassHandler {
    public static final String FAKE_FIELD_NAME = "__fake__";
    private static ProxyDelegatingHandler singleton;

    private Map<String, String> classHandlers = new HashMap<String, String>();
    private Map<Class, Field> fakeFieldMap = new HashMap<Class, Field>();
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
                ctClass.getField(FAKE_FIELD_NAME);
            } catch (NotFoundException e) {
                CtField field = new CtField(objectClass, FAKE_FIELD_NAME, ctClass);
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
        classHandlers.clear();
    }

    @Override
    public void afterTest() {
    }

    public void addProxyClass(Class<?> realClass, Class<?> handlerClass) {
        classHandlers.put(realClass.getName(), handlerClass.getName());
        if (debug) System.out.println("handle " + realClass + " with " + handlerClass);
    }

    @Override
    public Object methodInvoked(Class clazz, String methodName, Object instance, String[] paramTypes, Object[] params) {
        InvocationPlan invocationPlan = new InvocationPlan(clazz, methodName, instance, paramTypes);
        if (!invocationPlan.prepare()) return null;

        try {
            return invocationPlan.getMethod().invoke(invocationPlan.getFakeObject(), params);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(invocationPlan.getFakeObject().getClass().getName() + " is not assignable from " + invocationPlan.getHandlingClass().getName(), e);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            } else {
                throw new RuntimeException("Did your fake implementation of a method throw an exception? Refer to the bottom of this stack trace.", e);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    static Class<?> loadClass(String paramType, ClassLoader classLoader) {
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

    public Object fakeObjectFor(Object instance) {
        Field field = getFakeField(instance);
        Object fake = getField(instance, field);

        if (fake != null) {
            return fake;
        }

        String fakeClassName = getHandlingClassName(instance.getClass());

        if (debug)
            System.out.println("creating new " + fakeClassName + " as fake for " + instance.getClass().getName());
        try {
            Class<?> fakeClass = loadClass(fakeClassName, instance.getClass().getClassLoader());
            Constructor<?> constructor = findConstructor(instance, fakeClass);
            if (constructor != null) {
                fake = constructor.newInstance(instance);
            } else {
                fake = fakeClass.newInstance();
            }
            field.set(instance, fake);

            return fake;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private String getHandlingClassName(Class clazz) {
        String fakeClassName = null;
        while (fakeClassName == null && clazz != null) {
            fakeClassName = classHandlers.get(clazz.getName());
            clazz = clazz.getSuperclass();
        }
        return fakeClassName;
    }

    private Constructor<?> findConstructor(Object instance, Class<?> fakeClass) {
        Class clazz = instance.getClass();

        Constructor constructor;
        for (constructor = null; constructor == null && clazz != null; clazz = clazz.getSuperclass()) {
            try {
                constructor = fakeClass.getConstructor(clazz);
            } catch (NoSuchMethodException e) {
                // expected
            }
        }
        return constructor;
    }

    private Object getField(Object instance, Field field) {
        try {
            return field.get(instance);
        } catch (IllegalAccessException e1) {
            throw new RuntimeException(e1);
        }
    }

    private Field getFakeField(Object instance) {
        Class clazz = instance.getClass();
        Field field = fakeFieldMap.get(clazz);
        if (field == null) {
            try {
                field = clazz.getField(FAKE_FIELD_NAME);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(instance.getClass().getName() + " has no fake field", e);
            }
            fakeFieldMap.put(clazz, field);
        }
        return field;
    }

    public Object proxyFor(Object instance) {
        if (instance == null) {
            throw new RuntimeException("no instance for which to get a proxy");
        }
        Field field = getFakeField(instance);
        return getField(instance, field);
    }

    private class InvocationPlan {
        private Class clazz;
        private String methodName;
        private Object instance;
        private String[] paramTypes;
        private Class<?> handlingClass;
        private Method method;
        private Object fakeObject;

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

        public Object getFakeObject() {
            return fakeObject;
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
                fakeObject = fakeObjectFor(instance);
                method = getMethod(fakeObject.getClass(), methodName, paramClasses);
            } else {
                fakeObject = null;
                String fakeClassName = getHandlingClassName(clazz);
                Class<?> fakeClass = loadClass(fakeClassName, classLoader);
                method = getMethod(fakeClass, methodName, paramClasses);
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
    }
}
