package org.robolectric.bytecode;

import java.util.Arrays;

class InvocationProfile {
    final Class clazz;
    final Class shadowClass;
    final String methodName;
    final boolean isStatic;
    final String[] paramTypes;
    private final int hashCode;

    InvocationProfile(Class clazz, Class shadowClass, String methodName, boolean aStatic, String[] paramTypes) {
        this.clazz = clazz;
        this.shadowClass = shadowClass;
        this.methodName = methodName;
        isStatic = aStatic;
        this.paramTypes = paramTypes;

        // calculate hashCode early
        int result = clazz.hashCode();
        result = 31 * result + (shadowClass != null ? shadowClass.hashCode() : 0);
        result = 31 * result + methodName.hashCode();
        result = 31 * result + (isStatic ? 1 : 0);
        result = 31 * result + Arrays.hashCode(paramTypes);
        hashCode = result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InvocationProfile that = (InvocationProfile) o;

        if (isStatic != that.isStatic) return false;
        if (!clazz.equals(that.clazz)) return false;
        if (!methodName.equals(that.methodName)) return false;
        if (!Arrays.equals(paramTypes, that.paramTypes)) return false;
        if (shadowClass != null ? !shadowClass.equals(that.shadowClass) : that.shadowClass != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
