package org.robolectric.bytecode;

import javassist.CtClass;

enum RoboType {
    VOID(null, null, "", "", Void.TYPE),
    BOOLEAN(false, "false", ".booleanValue()", "java.lang.Boolean", Boolean.TYPE),
    BYTE(0, "0", ".byteValue()", "java.lang.Byte", Byte.TYPE),
    CHAR(0, "0", ".charValue()", "java.lang.Character", Character.TYPE),
    SHORT(0, "0", ".shortValue()", "java.lang.Short", Short.TYPE),
    INT(0, "0", ".intValue()", "java.lang.Integer", Integer.TYPE),
    LONG(0, "0l", ".longValue()", "java.lang.Long", Long.TYPE),
    FLOAT(0, "0f", ".floatValue()", "java.lang.Float", Float.TYPE),
    DOUBLE(0, "0d", ".doubleValue()", "java.lang.Double", Double.TYPE),
    OBJECT(null, "null", "", null, null);

    RoboType(Object defaultReturnValue, String defaultReturnString, String unboxString, String nonPrimitiveClassName, Class type) {
        this.defaultReturnValue = defaultReturnValue;
        this.defaultReturnString = defaultReturnString;
        this.unboxString = unboxString;
        this.nonPrimitiveClassName = nonPrimitiveClassName;
        this.type = type;
    }

    private Object defaultReturnValue;
    private String defaultReturnString;
    private String unboxString;
    private String nonPrimitiveClassName;
    private Class type;

    Object defaultReturnValue() {
        return defaultReturnValue;
    }

    String defaultReturnString() {
        return defaultReturnString;
    }

    String unboxString() {
        return unboxString;
    }

    String nonPrimitiveClassName(CtClass returnCtClass) {
        return nonPrimitiveClassName == null ? returnCtClass.getName() : nonPrimitiveClassName;
    }

    boolean isVoid() {
        return this == VOID;
    }

    public static RoboType find(CtClass ctClass) {
        if (ctClass.equals(CtClass.voidType)) {
            return VOID;
        } else if (ctClass.equals(CtClass.booleanType)) {
            return BOOLEAN;
        } else if (ctClass.equals(CtClass.byteType)) {
            return BYTE;
        } else if (ctClass.equals(CtClass.charType)) {
            return CHAR;
        } else if (ctClass.equals(CtClass.shortType)) {
            return SHORT;
        } else if (ctClass.equals(CtClass.intType)) {
            return INT;
        } else if (ctClass.equals(CtClass.longType)) {
            return LONG;
        } else if (ctClass.equals(CtClass.floatType)) {
            return FLOAT;
        } else if (ctClass.equals(CtClass.doubleType)) {
            return DOUBLE;
        } else if (!ctClass.isPrimitive()) {
            return OBJECT;
        } else {
            throw new RuntimeException("unknown return type " + ctClass);
        }
    }

    public static Class findPrimitiveClass(String name) {
        for (RoboType type : RoboType.values()) {
            if (type.type != null && type.type.getName().equals(name)) {
                return type.type;
            }
        }
        return null;
    }
}
