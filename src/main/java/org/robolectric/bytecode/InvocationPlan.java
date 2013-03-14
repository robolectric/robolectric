package org.robolectric.bytecode;

import android.support.v4.content.LocalBroadcastManager;
import org.fest.reflect.core.Reflection;
import org.fest.reflect.exception.ReflectionError;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

class InvocationPlan {
    private final ShadowMap shadowMap;
    private final Class clazz;
    private final Class shadowClass;
    private final String methodName;
    private final boolean isStatic;
    private final String[] paramTypes;

    private final ShadowConfig shadowConfig;
    private final ClassLoader classLoader;
    private final boolean hasShadowImplementation;
    private Class<?>[] paramClasses;
    private Class<?> declaredShadowClass;
    private Method method;

    public InvocationPlan(ShadowMap shadowMap, InvocationProfile invocationProfile) {
        this(shadowMap, invocationProfile.clazz, invocationProfile.shadowClass,
                invocationProfile.methodName, invocationProfile.isStatic, invocationProfile.paramTypes);
    }

    public InvocationPlan(ShadowMap shadowMap, Class clazz, Class shadowClass, String methodName, boolean isStatic, String... paramTypes) {
        this.shadowMap = shadowMap;
        shadowConfig = shadowMap.get(clazz);
        this.clazz = clazz;
        this.shadowClass = shadowClass;
        this.methodName = methodName.equals("<init>")
                ? InstrumentingClassLoader.CONSTRUCTOR_METHOD_NAME
                : methodName;
        this.isStatic = isStatic;
        this.paramTypes = paramTypes;

        this.classLoader = clazz.getClassLoader();
        this.hasShadowImplementation = prepare();
    }

    public boolean hasShadowImplementation() {
        return hasShadowImplementation;
    }

    public Class<?> getDeclaredShadowClass() {
        return declaredShadowClass;
    }

    public Method getMethod() {
        return method;
    }

    public boolean isI18nSafe() {
        // method is loaded by another class loader. So do everything reflectively.
        Annotation[] annos = method.getAnnotations();
        for (Annotation anno : annos) {
            String name = anno.annotationType().getName();
            if (name.equals(Implementation.class.getName())) {
                try {
                    Method m = (anno).getClass().getMethod("i18nSafe");
                    return (Boolean) m.invoke(anno);
                } catch (Exception e) {
                    return true;    // should probably throw some other exception
                }
            }
        }

        return true;
    }

    public boolean prepare() {
        paramClasses = getParamClasses();

        Class<?> originalClass = ShadowWrangler.loadClass(clazz.getName(), classLoader);

        declaredShadowClass = findDeclaredShadowClassForMethod(originalClass, methodName, paramClasses);
        if (declaredShadowClass == null) {
            return false;
        }

        if (isStatic) {
            Class<?> staticShadowClass = findShadowClass(clazz);
            this.method = findShadowMethodOn(staticShadowClass);
        } else {
            String directShadowMethodName = RobolectricInternals.directMethodName(declaredShadowClass.getName(), methodName);

            method = findMethod(shadowClass, directShadowMethodName, paramClasses);
            if (method == null) {
                method = findShadowMethodOn(shadowClass);
            }
            if (method == null) {
                method = findShadowMethodOn(declaredShadowClass);
            }
        }

        if (method == null) {
//                if (debug) {
//                    System.out.println("No shadow method found for " + clazz + "." + methodName + "(" + asList(paramClasses) + ") on " + declaredShadowClass.getName());
//                }
            return false;
        }

        if (isStatic != Modifier.isStatic(method.getModifiers())) {
            throw new RuntimeException("method staticness of " + clazz.getName() + "." + methodName + " and " + declaredShadowClass.getName() + "." + method.getName() + " don't match");
        }

        // todo: not this
        if (clazz.getName().startsWith("android.support") && !clazz.getName().equals(LocalBroadcastManager.class.getName())) {
            return false;
        }

        method.setAccessible(true);

        return true;
    }

    private Method findShadowMethodOn(Class<?> shadowClass) {
        Method method = findMethod(shadowClass, methodName, paramClasses);
        if (method != null && method.getDeclaringClass().equals(Object.class)) return null;

        if (shadowConfig != null)
            if (shadowConfig.callThroughByDefault)
                if (method != null)
                    if (!clazz.equals(shadowedClass(method))) {
                        method = null;
                    }
        return method;
    }

    private Method findMethod(Class clazz, String methodName, Class<?>[] parameterTypes) {
        try {
            return Reflection.method(methodName).withParameterTypes(parameterTypes).in(clazz).info();
        } catch (ReflectionError e) {
            return null;
        }
    }

    private Class<?> findDeclaredShadowClassForMethod(Class<?> originalClass, String methodName, Class<?>[] paramClasses) {
        Class<?> declaringClass = findDeclaringClassForMethod(methodName, paramClasses,
            originalClass);
        return findShadowClass(declaringClass);
    }

    private Class<?> findShadowClass(Class<?> originalClass) {
        String declaredShadowClassName = shadowMap.getShadowClassName(originalClass);
        if (declaredShadowClassName == null) {
            return null;
        }
        return ShadowWrangler.loadClass(declaredShadowClassName, classLoader);
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
            paramClasses[i] = ShadowWrangler.loadClass(paramTypes[i], classLoader);
        }
        return paramClasses;
    }

    private Class<?> shadowedClass(Method method) {
        Class<?> declaringClass = method.getDeclaringClass();
        Implements annotation = declaringClass.getAnnotation(Implements.class);
        if (annotation == null) return null;
        Class<?> classBeingShadowed = annotation.value();
        return classBeingShadowed;
    }

    @Override
    public String toString() {
        return "delegating to " + declaredShadowClass.getName() + "." + method.getName()
                + "(" + Arrays.toString(method.getParameterTypes()) + ")";
    }

    public Object callOriginal(Object instance, Object[] params) throws InvocationTargetException, IllegalAccessException {
        try {
            Method method = clazz.getDeclaredMethod(RobolectricInternals.directMethodName(clazz.getName(), methodName), paramClasses);
            method.setAccessible(true);
            return method.invoke(instance, params);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean shouldDelegateToRealMethodWhenMethodShadowIsMissing() {
        String className = clazz.getName();
        ShadowConfig shadowConfig = shadowMap.get(className);
        int dollarIndex;
        if (shadowConfig == null && (dollarIndex = className.indexOf('$')) > -1) {
            className = className.substring(0, dollarIndex);
            shadowConfig = shadowMap.get(className);

            // todo: test
        }
        if (shadowConfig != null && shadowConfig.callThroughByDefault) {
            return true;
        }

        boolean delegateToReal = invokeApiMethodBodiesWhenShadowMethodIsMissing(clazz, methodName, paramClasses);
//            if (debug) {
//                System.out.println("DEBUG: Shall we invoke real method on " + clazz + "." + methodName + "("
//                        + Join.join(", ", paramClasses) + ")? " + (delegateToReal ? "yup!" : "nope!"));
//            }
        return delegateToReal;
    }

    private boolean invokeApiMethodBodiesWhenShadowMethodIsMissing(Class clazz, String methodName, Class<?>[] paramClasses) {
        if (clazz.getName().startsWith("android.support")) {
            return true;
        }

        if (clazz.getName().equals("android.app.PendingIntent")) return false; // todo: grot as we remove some more shadows


      // todo: prolly removable now?
        if (methodName.equals("equals") && paramClasses.length == 1 && paramClasses[0] == Object.class) return true;
        if (methodName.equals("hashCode") && paramClasses.length == 0) return true;
        if (methodName.equals("toString") && paramClasses.length == 0) return true;

        return false;
    }
}
