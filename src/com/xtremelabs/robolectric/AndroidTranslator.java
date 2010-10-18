package com.xtremelabs.robolectric;

import javassist.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public class AndroidTranslator implements Translator {
    /**
     * IMPORTANT -- increment this number when the bytecode generated for modified classes changes
     * so the cache file can be invalidated.
     */
    public static final int CACHE_VERSION = 1;

    private static final List<AndroidTranslator> INSTANCES = new ArrayList<AndroidTranslator>();

    private int index;
    private ClassHandler classHandler;
    private Map<String, byte[]> modifiedClasses = new HashMap<String, byte[]>();

    public AndroidTranslator(ClassHandler classHandler) {
        this.classHandler = classHandler;
        index = addInstance(this);
    }

    synchronized static private int addInstance(AndroidTranslator androidTranslator) {
        INSTANCES.add(androidTranslator);
        return INSTANCES.size() - CACHE_VERSION;
    }

    synchronized static public AndroidTranslator get(int index) {
        return INSTANCES.get(index);
    }


    @Override
    public void start(ClassPool classPool) throws NotFoundException, CannotCompileException {
    }

    @Override
    public void onLoad(ClassPool classPool, String className) throws NotFoundException, CannotCompileException {
        boolean needsStripping =
                className.startsWith("android.")
                        || className.startsWith("org.apache.http")
                        || className.startsWith("com.google.android.");

        CtClass ctClass = classPool.get(className);
        if (needsStripping) {
            int modifiers = ctClass.getModifiers();
            if (Modifier.isFinal(modifiers)) {
                ctClass.setModifiers(modifiers & ~Modifier.FINAL);
            }

            if (ctClass.isInterface()) return;

            classHandler.instrument(ctClass);
            fixConstructors(ctClass);
            fixMethods(ctClass);

            try {
                modifiedClasses.put(className, ctClass.toBytecode());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void fixConstructors(CtClass ctClass) throws CannotCompileException, NotFoundException {
        boolean needsDefault = true;

        for (CtConstructor ctConstructor : ctClass.getConstructors()) {
            String methodBody = generateConstructorBody(ctClass, ctConstructor.getParameterTypes());

            ctConstructor.setBody("{\n" + methodBody + "\n}");
            if (ctConstructor.getParameterTypes().length == 0) {
                needsDefault = false;
            }
        }

        if (needsDefault) {
            String methodBody = generateConstructorBody(ctClass, new CtClass[0]);
            ctClass.addConstructor(CtNewConstructor.make(new CtClass[0], new CtClass[0], methodBody, ctClass));
        }
    }

    private String generateConstructorBody(CtClass ctClass, CtClass[] parameterTypes) throws NotFoundException {
        return generateMethodBody(ctClass,
                new CtMethod(CtClass.voidType, "<init>", parameterTypes, ctClass),
                CtClass.voidType,
                Type.VOID,
                true,
                false);
    }

    private void fixMethods(CtClass ctClass) throws NotFoundException, CannotCompileException {
        for (CtMethod ctMethod : ctClass.getDeclaredMethods()) {
            int modifiers = ctMethod.getModifiers();
            if (Modifier.isNative(modifiers)) {
                modifiers = modifiers & ~Modifier.NATIVE;
            }
            if (Modifier.isFinal(modifiers)) {
                modifiers = modifiers & ~Modifier.FINAL;
            }
            ctMethod.setModifiers(modifiers);

            CtClass returnCtClass = ctMethod.getReturnType();
            Type returnType = Type.find(returnCtClass);

            String methodName = ctMethod.getName();
            CtClass[] paramTypes = ctMethod.getParameterTypes();

            boolean isAbstract = (ctMethod.getModifiers() & Modifier.ABSTRACT) != 0;
//            if (!isAbstract) {
//                if (methodName.startsWith("set") && paramTypes.length == 1) {
//                    String fieldName = "__" + methodName.substring(3);
//                    if (declareField(ctClass, fieldName, paramTypes[0])) {
//                        methodBody = fieldName + " = $1;\n" + methodBody;
//                    }
//                } else if (methodName.startsWith("get") && paramTypes.length == 0) {
//                    String fieldName = "__" + methodName.substring(3);
//                    if (declareField(ctClass, fieldName, returnType)) {
//                        methodBody = "return " + fieldName + ";\n";
//                    }
//                }
//            }

            boolean returnsVoid = returnType.isVoid();
            boolean isStatic = Modifier.isStatic(modifiers);

            String methodBody;
            if (!isAbstract) {
                methodBody = generateMethodBody(ctClass, ctMethod, returnCtClass, returnType, returnsVoid, isStatic);
            } else {
                methodBody = returnsVoid ? "" : "return " + returnType.defaultReturnString() + ";";
            }

            CtMethod newMethod = CtNewMethod.make(
                    ctMethod.getModifiers(),
                    returnCtClass,
                    methodName,
                    paramTypes,
                    ctMethod.getExceptionTypes(),
                    "{\n" + methodBody + "\n}",
                    ctClass);
            ctMethod.setBody(newMethod, null);
        }
    }

    private String generateMethodBody(CtClass ctClass, CtMethod ctMethod, CtClass returnCtClass, Type returnType, boolean returnsVoid, boolean aStatic) throws NotFoundException {
        String methodBody;
        StringBuilder buf = new StringBuilder();
        if (!returnsVoid) {
            buf.append("Object x = ");
        }
        buf.append(AndroidTranslator.class.getName());
        buf.append(".get(");
        buf.append(index);
        buf.append(").methodInvoked(");
        buf.append(ctClass.getName());
        buf.append(".class, \"");
        buf.append(ctMethod.getName());
        buf.append("\", ");
        if (!aStatic) {
            buf.append("this");
        } else {
            buf.append("null");
        }
        buf.append(", ");

        appendParamTypeArray(buf, ctMethod);
        buf.append(", ");
        appendParamArray(buf, ctMethod);

        buf.append(")");
        buf.append(";\n");

        if (!returnsVoid) {
            buf.append("if (x != null) return ((");
            buf.append(returnType.nonPrimitiveClassName(returnCtClass));
            buf.append(") x)");
            buf.append(returnType.unboxString());
            buf.append(";\n");
            buf.append("return ");
            buf.append(returnType.defaultReturnString());
            buf.append(";");
        }

        methodBody = buf.toString();
        return methodBody;
    }

    private void appendParamTypeArray(StringBuilder buf, CtMethod ctMethod) throws NotFoundException {
        CtClass[] parameterTypes = ctMethod.getParameterTypes();
        if (parameterTypes.length == 0) {
            buf.append("new String[0]");
        } else {
            buf.append("new String[] {");
            for (int i = 0; i < parameterTypes.length; i++) {
                if (i > 0) buf.append(", ");
                buf.append("\"");
                CtClass parameterType = parameterTypes[i];
                buf.append(parameterType.getName());
                buf.append("\"");
            }
            buf.append("}");
        }
    }

    private void appendParamArray(StringBuilder buf, CtMethod ctMethod) throws NotFoundException {
        int parameterCount = ctMethod.getParameterTypes().length;
        if (parameterCount == 0) {
            buf.append("new Object[0]");
        } else {
            buf.append("new Object[] {");
            for (int i = 0; i < parameterCount; i++) {
                if (i > 0) buf.append(", ");
                buf.append(AndroidTranslator.class.getName());
                buf.append(".autobox(");
                buf.append("$").append(i + CACHE_VERSION);
                buf.append(")");
            }
            buf.append("}");
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public Object methodInvoked(Class clazz, String methodName, Object instance, String[] paramTypes, Object[] params) {
        return classHandler.methodInvoked(clazz, methodName, instance, paramTypes, params);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public static Object autobox(Object o) {
        return o;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public static Object autobox(boolean o) {
        return o;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public static Object autobox(byte o) {
        return o;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public static Object autobox(char o) {
        return o;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public static Object autobox(short o) {
        return o;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public static Object autobox(int o) {
        return o;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public static Object autobox(long o) {
        return o;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public static Object autobox(float o) {
        return o;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public static Object autobox(double o) {
        return o;
    }

    private boolean declareField(CtClass ctClass, String fieldName, CtClass fieldType) throws CannotCompileException, NotFoundException {
        CtMethod ctMethod = getMethod(ctClass, "get" + fieldName, "");
        if (ctMethod == null) {
            return false;
        }
        CtClass getterFieldType = ctMethod.getReturnType();

        if (!getterFieldType.equals(fieldType)) {
            return false;
        }

        if (getField(ctClass, fieldName) == null) {
            CtField field = new CtField(fieldType, fieldName, ctClass);
            field.setModifiers(Modifier.PRIVATE);
            ctClass.addField(field);
        }

        return true;
    }

    private CtField getField(CtClass ctClass, String fieldName) {
        try {
            return ctClass.getField(fieldName);
        } catch (NotFoundException e) {
            return null;
        }
    }

    private CtMethod getMethod(CtClass ctClass, String methodName, String desc) {
        try {
            return ctClass.getMethod(methodName, desc);
        } catch (NotFoundException e) {
            return null;
        }
    }

    public void saveAllClassesToCache(File file, Manifest manifest) {
        if (modifiedClasses.size() > 0) {
            JarOutputStream jarOutputStream = null;
            try {
                File cacheJarDir = file.getParentFile();
                if (!cacheJarDir.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    cacheJarDir.mkdirs();
                }

                if (file.exists()) {
                    jarOutputStream = new JarOutputStream(new FileOutputStream(file, true));
                } else {
                    jarOutputStream = new JarOutputStream(new FileOutputStream(file), manifest);
                }
                for (Map.Entry<String, byte[]> entry : modifiedClasses.entrySet()) {
                    String key = entry.getKey();
                    jarOutputStream.putNextEntry(new JarEntry(key.replace('.', '/') + ".class"));
                    jarOutputStream.write(entry.getValue());
                    jarOutputStream.closeEntry();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                if (jarOutputStream != null) {
                    try {
                        jarOutputStream.close();
                    } catch (IOException ignore) {
                    }
                }
            }
        }
    }
}
