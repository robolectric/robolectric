package org.robolectric.bytecode;

import org.objectweb.asm.Type;

import java.util.Arrays;

class InvocationProfile {
    final Class clazz;
    final String methodName;
    final boolean isStatic;
    final String[] paramTypes;
    private final boolean isSpecial;

    public InvocationProfile(String methodSignature, boolean isStatic, ClassLoader classLoader) {
        int parenStart = methodSignature.indexOf('(');
        int methodStart = methodSignature.lastIndexOf('/', parenStart);
        String className = methodSignature.substring(0, methodStart).replace('/', '.');
        this.clazz = loadClass(classLoader, className);
        this.methodName = methodSignature.substring(methodStart + 1, parenStart);

        Type[] argumentTypes = Type.getArgumentTypes(methodSignature.substring(parenStart));
        this.paramTypes = new String[argumentTypes.length];
        for (int i = 0; i < argumentTypes.length; i++) {
            paramTypes[i] = argumentTypes[i].getClassName();
        }
        this.isStatic = isStatic;

        this.isSpecial = methodSignature.endsWith("/equals(Ljava/lang/Object;)Z")
                || methodSignature.endsWith("/hashCode()I")
                || methodSignature.endsWith("/toString()Ljava/lang/String;");
    }

    public Class<?>[] getParamClasses(ClassLoader classLoader) throws ClassNotFoundException {
        Class[] classes = new Class[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            String paramType = paramTypes[i];
            classes[i] = ShadowWrangler.loadClass(paramType, classLoader);
        }
        return classes;
    }

    private Class<?> loadClass(ClassLoader classLoader, String className) {
        try {
            return classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InvocationProfile that = (InvocationProfile) o;

        if (isSpecial != that.isSpecial) return false;
        if (isStatic != that.isStatic) return false;
        if (clazz != null ? !clazz.equals(that.clazz) : that.clazz != null) return false;
        if (methodName != null ? !methodName.equals(that.methodName) : that.methodName != null) return false;
        if (!Arrays.equals(paramTypes, that.paramTypes)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = clazz != null ? clazz.hashCode() : 0;
        result = 31 * result + (methodName != null ? methodName.hashCode() : 0);
        result = 31 * result + (isStatic ? 1 : 0);
        result = 31 * result + (paramTypes != null ? Arrays.hashCode(paramTypes) : 0);
        result = 31 * result + (isSpecial ? 1 : 0);
        return result;
    }

    public boolean isSpecial() {
        return isSpecial;
    }
}
