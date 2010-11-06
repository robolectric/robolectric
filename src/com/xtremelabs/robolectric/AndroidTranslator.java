package com.xtremelabs.robolectric;

import javassist.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"UnusedDeclaration"})
public class AndroidTranslator implements Translator {
    /**
     * IMPORTANT -- increment this number when the bytecode generated for modified classes changes
     * so the cache file can be invalidated.
     */
    public static final int CACHE_VERSION = 9;

    private static final List<ClassHandler> CLASS_HANDLERS = new ArrayList<ClassHandler>();

    private ClassHandler classHandler;
    private ClassCache classCache;

    public AndroidTranslator(ClassHandler classHandler, ClassCache classCache) {
        this.classHandler = classHandler;
        this.classCache = classCache;
    }

    public static ClassHandler getClassHandler(int index) {
        return CLASS_HANDLERS.get(index);
    }

    @Override
    public void start(ClassPool classPool) throws NotFoundException, CannotCompileException {
        injectClassHandlerToInstrumentedClasses(classPool);
    }

    private void injectClassHandlerToInstrumentedClasses(ClassPool classPool) throws NotFoundException, CannotCompileException {
        int index;
        synchronized (CLASS_HANDLERS) {
            CLASS_HANDLERS.add(classHandler);
            index = CLASS_HANDLERS.size() - 1;
        }

        CtClass robolectricInternalsCtClass = classPool.get(RobolectricInternals.class.getName());
        robolectricInternalsCtClass.setModifiers(Modifier.PUBLIC);

        robolectricInternalsCtClass.getClassInitializer().insertBefore("{\n" +
                "classHandler = " + AndroidTranslator.class.getName() + ".getClassHandler(" + index + ");\n" +
                "}");
    }

    @Override
    public void onLoad(ClassPool classPool, String className) throws NotFoundException, CannotCompileException {
        if (classCache.isWriting()) {
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

            fixConstructors(ctClass);
            fixMethods(ctClass);

            try {
                classCache.addClass(className, ctClass.toBytecode());
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
        boolean hasDefault = false;

        for (CtConstructor ctConstructor : ctClass.getConstructors()) {
            try {
                fixConstructor(ctClass, hasDefault, ctConstructor);

                if (ctConstructor.getParameterTypes().length == 0) {
                    hasDefault = true;
                }
            } catch (Exception e) {
                throw new RuntimeException("problem instrumenting " + ctConstructor, e);
            }
        }

        if (!hasDefault) {
            String methodBody = generateConstructorBody(ctClass, new CtClass[0]);
            ctClass.addConstructor(CtNewConstructor.make(new CtClass[0], new CtClass[0], "{\n" + methodBody + "}\n", ctClass));
        }
    }

    private boolean fixConstructor(CtClass ctClass, boolean needsDefault, CtConstructor ctConstructor) throws NotFoundException, CannotCompileException {
        String methodBody = generateConstructorBody(ctClass, ctConstructor.getParameterTypes());
        ctConstructor.setBody("{\n" + methodBody + "}\n");
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
            fixMethod(ctClass, ctMethod);
        }
        CtMethod equalsMethod = ctClass.getMethod("equals", "(Ljava/lang/Object;)Z");
        CtMethod hashCodeMethod = ctClass.getMethod("hashCode", "()I");
        CtMethod toStringMethod = ctClass.getMethod("toString", "()Ljava/lang/String;");

        fixMethod(ctClass, equalsMethod);
        fixMethod(ctClass, hashCodeMethod);
        fixMethod(ctClass, toStringMethod);
    }

    private String describe(CtMethod ctMethod) throws NotFoundException {
        return Modifier.toString(ctMethod.getModifiers()) + " " + ctMethod.getReturnType().getSimpleName() + " " + ctMethod.getLongName();
    }

    private void fixMethod(CtClass ctClass, CtMethod ctMethod) throws NotFoundException {
        String describeBefore = describe(ctMethod);
        try {
            CtClass declaringClass = ctMethod.getDeclaringClass();
            int originalModifiers = ctMethod.getModifiers();

            boolean wasNative = Modifier.isNative(originalModifiers);
            boolean wasFinal = Modifier.isFinal(originalModifiers);
            boolean wasAbstract = Modifier.isAbstract(originalModifiers);
            boolean wasDeclaredInClass = declaringClass.equals(ctClass);

            if (wasFinal && ctClass.isEnum()) {
                return;
            }

            int newModifiers = originalModifiers;
            if (wasNative) {
                newModifiers = Modifier.clear(newModifiers, Modifier.NATIVE);
            }
            if (wasFinal) {
                newModifiers = Modifier.clear(newModifiers, Modifier.FINAL);
            }
            if (wasDeclaredInClass) {
                ctMethod.setModifiers(newModifiers);
            }

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
            String methodBody = generateMethodBody(ctClass, ctMethod, wasNative, wasAbstract, returnCtClass, returnType, isStatic);

            if (!wasDeclaredInClass) {
                CtMethod newMethod = makeNewMethod(ctClass, ctMethod, returnCtClass, methodName, paramTypes, "{\n" + methodBody + generateCallToSuper(methodName, paramTypes) + "\n}");
                newMethod.setModifiers(newModifiers);
                ctClass.addMethod(newMethod);
            } else if (wasAbstract || wasNative) {
                CtMethod newMethod = makeNewMethod(ctClass, ctMethod, returnCtClass, methodName, paramTypes, "{\n" + methodBody + "\n}");
                ctMethod.setBody(newMethod, null);
            } else {
                ctMethod.insertBefore("{\n" + methodBody + "}\n");
            }
        } catch (Exception e) {
            throw new RuntimeException("problem instrumenting " + describeBefore, e);
        }
    }

    private CtMethod makeNewMethod(CtClass ctClass, CtMethod ctMethod, CtClass returnCtClass, String methodName, CtClass[] paramTypes, String methodBody) throws CannotCompileException, NotFoundException {
        return CtNewMethod.make(
                ctMethod.getModifiers(),
                returnCtClass,
                methodName,
                paramTypes,
                ctMethod.getExceptionTypes(),
                methodBody,
                ctClass);
    }

    public String generateCallToSuper(String methodName, CtClass[] paramTypes) {
        return "return super." + methodName + "(" + makeParameterReplacementList(paramTypes.length) + ");";
    }

    public String makeParameterReplacementList(int length) {
        if (length == 0) {
            return "";
        }

        String parameterReplacementList = "$1";
        for (int i = 2; i <= length; ++i) {
            parameterReplacementList += ", $" + i;
        }
        return parameterReplacementList;
    }

    private String generateMethodBody(CtClass ctClass, CtMethod ctMethod, boolean wasNative, boolean wasAbstract, CtClass returnCtClass, Type returnType, boolean aStatic) throws NotFoundException {
        String methodBody;
        if (wasAbstract) {
            methodBody = returnType.isVoid() ? "" : "return " + returnType.defaultReturnString() + ";";
        } else {
            methodBody = generateMethodBody(ctClass, ctMethod, returnCtClass, returnType, aStatic);
        }

        if (wasNative) {
            methodBody += returnType.isVoid() ? "" : "return " + returnType.defaultReturnString() + ";";
        }
        return methodBody;
    }

    public String generateMethodBody(CtClass ctClass, CtMethod ctMethod, CtClass returnCtClass, Type returnType, boolean isStatic) throws NotFoundException {
        boolean returnsVoid = returnType.isVoid();
        String className = ctClass.getName();

        String methodBody;
        StringBuilder buf = new StringBuilder();
        buf.append("if (!");
        buf.append(RobolectricInternals.class.getName());
        buf.append(".shouldCallDirectly(");
        buf.append(isStatic ? className + ".class" : "this");
        buf.append(")) {\n");

        if (!returnsVoid) {
            buf.append("Object x = ");
        }
        buf.append(RobolectricInternals.class.getName());
        buf.append(".methodInvoked(\n  ");
        buf.append(className);
        buf.append(".class, \"");
        buf.append(ctMethod.getName());
        buf.append("\", ");
        if (!isStatic) {
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
            if (ctMethod.getDeclaringClass().getName().equals("java.lang.Object")) {
                buf.append(generateCallToSuper(ctMethod.getName(), ctMethod.getParameterTypes()));
            } else {
                buf.append("return ");
                buf.append(returnType.defaultReturnString());
            }
            buf.append(";\n");
        } else {
            buf.append("return;\n");
        }

        buf.append("}\n");

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
                buf.append(RobolectricInternals.class.getName());
                buf.append(".autobox(");
                buf.append("$").append(i + 1);
                buf.append(")");
            }
            buf.append("}");
        }
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
}
