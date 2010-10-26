package com.xtremelabs.robolectric;

import com.xtremelabs.robolectric.util.RealObject;
import com.xtremelabs.robolectric.util.SheepWrangler;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtField;
import javassist.NotFoundException;

import java.lang.reflect.*;
import java.util.*;

public class ProxyDelegatingHandler implements ClassHandler {
    public static final String SHEEP_FIELD_NAME = "__sheep__";
    private static ProxyDelegatingHandler singleton;

    private Map<String, String> sheepClassMap = new HashMap<String, String>();
    private Map<Class, Field> sheepFieldMap = new HashMap<Class, Field>();
    private final Map<Class, MetaSheep> metaSheepMap = new HashMap<Class, MetaSheep>();

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
                ctClass.getField(SHEEP_FIELD_NAME);
            } catch (NotFoundException e) {
                CtField field = new CtField(objectClass, SHEEP_FIELD_NAME, ctClass);
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
        sheepClassMap.clear();
    }

    @Override
    public void afterTest() {
    }

    public void addProxyClass(Class<?> realClass, Class<?> handlerClass) {
        sheepClassMap.put(realClass.getName(), handlerClass.getName());
        if (debug) System.out.println("handle " + realClass + " with " + handlerClass);
    }

    @Override
    public Object methodInvoked(Class clazz, String methodName, Object instance, String[] paramTypes, Object[] params) {
        InvocationPlan invocationPlan = new InvocationPlan(clazz, methodName, instance, paramTypes);
        if (!invocationPlan.prepare()) return null;

        try {
            return invocationPlan.getMethod().invoke(invocationPlan.getSheep(), params);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(invocationPlan.getSheep().getClass().getName() + " is not assignable from " + invocationPlan.getHandlingClass().getName(), e);
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

    public Object sheepFor(Object instance) {
        Field field = getSheepField(instance);
        Object sheep = readField(instance, field);

        if (sheep != null) {
            return sheep;
        }

        String sheepClassName = getHandlingClassName(instance.getClass());

        if (debug)
            System.out.println("creating new " + sheepClassName + " as shadow for " + instance.getClass().getName());
        try {
            Class<?> sheepClass = loadClass(sheepClassName, instance.getClass().getClassLoader());
            Constructor<?> constructor = findConstructor(instance, sheepClass);
            if (constructor != null) {
                sheep = constructor.newInstance(instance);
            } else {
                sheep = sheepClass.newInstance();
            }
            field.set(instance, sheep);

            injectRealObjectOn(sheep, sheepClass, instance);

            return sheep;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private void injectRealObjectOn(Object sheep, Class<?> sheepClass, Object instance) {
        MetaSheep metaSheep = getMetaSheep(sheepClass);
        for (Field realObjectField : metaSheep.realObjectFields) {
            writeField(sheep, instance, realObjectField);
        }
        for (Field sheepWranglerField : metaSheep.sheepWranglerFields) {
            writeField(sheep, this, sheepWranglerField);
        }
    }

    private MetaSheep getMetaSheep(Class<?> sheepClass) {
        synchronized (metaSheepMap) {
            MetaSheep metaSheep = metaSheepMap.get(sheepClass);
            if (metaSheep == null) {
                metaSheep = new MetaSheep(sheepClass);
                metaSheepMap.put(sheepClass, metaSheep);
            }
            return metaSheep;
        }
    }

    private String getHandlingClassName(Class clazz) {
        String sheepClassName = null;
        while (sheepClassName == null && clazz != null) {
            sheepClassName = sheepClassMap.get(clazz.getName());
            clazz = clazz.getSuperclass();
        }
        return sheepClassName;
    }

    private Constructor<?> findConstructor(Object instance, Class<?> sheepClass) {
        Class clazz = instance.getClass();

        Constructor constructor;
        for (constructor = null; constructor == null && clazz != null; clazz = clazz.getSuperclass()) {
            try {
                constructor = sheepClass.getConstructor(clazz);
            } catch (NoSuchMethodException e) {
                // expected
            }
        }
        return constructor;
    }

    private Field getSheepField(Object instance) {
        Class clazz = instance.getClass();
        Field field = sheepFieldMap.get(clazz);
        if (field == null) {
            try {
                field = clazz.getField(SHEEP_FIELD_NAME);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(instance.getClass().getName() + " has no shadow field", e);
            }
            sheepFieldMap.put(clazz, field);
        }
        return field;
    }

    public Object shadowFor(Object instance) {
        if (instance == null) {
            throw new RuntimeException("no instance for which to get a proxy");
        }
        Field field = getSheepField(instance);
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
        private Object sheep;

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

        public Object getSheep() {
            return sheep;
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
                sheep = sheepFor(instance);
                method = getMethod(sheep.getClass(), methodName, paramClasses);
            } else {
                sheep = null;
                String sheepClassName = getHandlingClassName(clazz);
                Class<?> sheepClass = loadClass(sheepClassName, classLoader);
                method = getMethod(sheepClass, methodName, paramClasses);
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

    private class MetaSheep {
        private Class<?> sheepClass;
        List<Field> realObjectFields = new ArrayList<Field>();
        List<Field> sheepWranglerFields = new ArrayList<Field>();

        public MetaSheep(Class<?> sheepClass) {
            this.sheepClass = sheepClass;

            while (sheepClass != null) {
                for (Field field : sheepClass.getDeclaredFields()) {
                    if (field.isAnnotationPresent(RealObject.class)) {
                        field.setAccessible(true);
                        realObjectFields.add(field);
                    }

                    if (field.isAnnotationPresent(SheepWrangler.class)) {
                        field.setAccessible(true);
                        sheepWranglerFields.add(field);
                    }
                }
                sheepClass = sheepClass.getSuperclass();
            }

        }
    }
}
