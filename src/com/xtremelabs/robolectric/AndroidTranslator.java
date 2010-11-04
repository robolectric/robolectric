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
    public static final int CACHE_VERSION = 5;

    private static final List<AndroidTranslator> INSTANCES = new ArrayList<AndroidTranslator>();
    public static final String BYPASS_SHADOW_FIELD_NAME = "___bypassShadow___";

    private int index;
    private ClassHandler classHandler;
    private Map<String, byte[]> modifiedClasses = new HashMap<String, byte[]>();
    boolean startedWriting = false;

    public AndroidTranslator(ClassHandler classHandler) {
        this.classHandler = classHandler;
        index = addInstance(this);
    }

    synchronized static private int addInstance(AndroidTranslator androidTranslator) {
        INSTANCES.add(androidTranslator);
        return INSTANCES.size() - 1;
    }

    synchronized static public AndroidTranslator get(int index) {
        return INSTANCES.get(index);
    }


    @Override
    public void start(ClassPool classPool) throws NotFoundException, CannotCompileException {
    }

    @Override
    public void onLoad(ClassPool classPool, String className) throws NotFoundException, CannotCompileException {
        if (startedWriting) {
            throw new IllegalStateException("shouldn't be modifying bytecode after we've started writing cache! class=" + className);
        }
        
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

            addBypassShadowField(ctClass, BYPASS_SHADOW_FIELD_NAME);

            fixConstructors(ctClass);
            fixMethods(ctClass);

            try {
                modifiedClasses.put(className, ctClass.toBytecode());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void addBypassShadowField(CtClass ctClass, String fieldName) {
        try {
            try {
                ctClass.getField(fieldName);
            } catch (NotFoundException e) {
                CtField field = new CtField(CtClass.booleanType, fieldName, ctClass);
                field.setModifiers(java.lang.reflect.Modifier.PUBLIC | java.lang.reflect.Modifier.STATIC);
                ctClass.addField(field);
            }
        } catch (CannotCompileException e) {
            throw new RuntimeException(e);
        }
    }

    private void fixConstructors(CtClass ctClass) throws CannotCompileException, NotFoundException {
        boolean needsDefault = true;

        for (CtConstructor ctConstructor : ctClass.getConstructors()) {
            try {
                needsDefault = fixConstructor(ctClass, needsDefault, ctConstructor);
            } catch (Exception e) {
                throw new RuntimeException("problem instrumenting " + ctConstructor, e);
            }
        }

        if (needsDefault) {
            String methodBody = generateConstructorBody(ctClass, new CtClass[0]);
            ctClass.addConstructor(CtNewConstructor.make(new CtClass[0], new CtClass[0], "{\n" + methodBody + "}\n", ctClass));
        }
    }

    private boolean fixConstructor(CtClass ctClass, boolean needsDefault, CtConstructor ctConstructor) throws NotFoundException, CannotCompileException {
        String methodBody = generateConstructorBody(ctClass, ctConstructor.getParameterTypes());

        ctConstructor.setBody("{\n" + methodBody + "}\n");
        if (ctConstructor.getParameterTypes().length == 0) {
            needsDefault = false;
        }
        return needsDefault;
    }

    private String generateConstructorBody(CtClass ctClass, CtClass[] parameterTypes) throws NotFoundException {
        return generateMethodBody(ctClass,
                new CtMethod(CtClass.voidType, "<init>", parameterTypes, ctClass),
                CtClass.voidType,
                Type.VOID,
                false);
    }

    private void fixMethods(CtClass ctClass) throws NotFoundException, CannotCompileException {
        for (CtMethod ctMethod : ctClass.getDeclaredMethods()) {
            String describeBefore = describe(ctMethod);
            try {
                fixMethod(ctClass, ctMethod);
            } catch (Exception e) {
                throw new RuntimeException("problem instrumenting " + describeBefore, e);
            }
        }
    }

    private String describe(CtMethod ctMethod) throws NotFoundException {
        return Modifier.toString(ctMethod.getModifiers()) + " " + ctMethod.getReturnType().getSimpleName() + " " + ctMethod.getLongName();
    }

    private void fixMethod(CtClass ctClass, CtMethod ctMethod) throws NotFoundException, CannotCompileException {
        int originalModifiers = ctMethod.getModifiers();
        int newModifiers = originalModifiers;

        boolean wasNative = Modifier.isNative(originalModifiers);
        boolean wasFinal = Modifier.isFinal(originalModifiers);
        boolean wasAbstract = Modifier.isAbstract(originalModifiers);

        if (wasNative) {
            newModifiers = Modifier.clear(newModifiers, Modifier.NATIVE);
        }
        if (wasFinal) {
            newModifiers = Modifier.clear(newModifiers, Modifier.FINAL);
        }
        ctMethod.setModifiers(newModifiers);

        CtClass returnCtClass = ctMethod.getReturnType();
        Type returnType = Type.find(returnCtClass);

        String methodName = ctMethod.getName();
        CtClass[] paramTypes = ctMethod.getParameterTypes();

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

        boolean isStatic = Modifier.isStatic(originalModifiers);

        String methodBody;
        if (wasAbstract || wasNative) {
            methodBody = returnType.isVoid() ? "" : "return " + returnType.defaultReturnString() + ";";

            CtMethod newMethod = CtNewMethod.make(
                    ctMethod.getModifiers(),
                    returnCtClass,
                    methodName,
                    paramTypes,
                    ctMethod.getExceptionTypes(),
                    "{\n" + methodBody + "\n}",
                    ctClass);

            ctMethod.setBody(newMethod, null);
        } else {
            methodBody = generateMethodBody(ctClass, ctMethod, returnCtClass, returnType, isStatic);

            ctMethod.insertBefore("{\n" + methodBody + "}\n");
        }
    }

    public String generateMethodBody(CtClass ctClass, CtMethod ctMethod, CtClass returnCtClass, Type returnType, boolean aStatic) throws NotFoundException {
        boolean returnsVoid = returnType.isVoid();
        String className = ctClass.getName();

        String methodBody;
        StringBuilder buf = new StringBuilder();
        buf.append("if (!");
        buf.append(className);
        buf.append("." + BYPASS_SHADOW_FIELD_NAME + ") {\n");

        if (!returnsVoid) {
            buf.append("Object x = ");
        }
        buf.append(AndroidTranslator.class.getName());
        buf.append(".get(");
        buf.append(getIndex());
        buf.append(").methodInvoked(\n  ");
        buf.append(className);
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
            buf.append(";\n");
        } else {
            buf.append("return;\n");
        }

        buf.append("}\n");
        buf.append(className);
        buf.append("." + BYPASS_SHADOW_FIELD_NAME + " = false;\n");

        methodBody = buf.toString();
        return methodBody;
    }

    protected int getIndex() {
        return index;
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
                buf.append("$").append(i + 1);
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
        startedWriting = true;

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
