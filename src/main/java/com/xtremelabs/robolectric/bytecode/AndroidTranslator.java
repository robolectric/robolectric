package com.xtremelabs.robolectric.bytecode;

import android.net.Uri;
import com.xtremelabs.robolectric.internal.DoNotInstrument;
import com.xtremelabs.robolectric.internal.Instrument;
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
    public static final int CACHE_VERSION = 21;

    private static final List<ClassHandler> CLASS_HANDLERS = new ArrayList<ClassHandler>();

    private ClassHandler classHandler;
    private ClassCache classCache;
    private final List<String> instrumentingList = new ArrayList<String>();
    private final List<String> instrumentingExcludeList = new ArrayList<String>();

    public AndroidTranslator(ClassHandler classHandler, ClassCache classCache) {
        this.classHandler = classHandler;
        this.classCache = classCache;

        // Initialize lists
        instrumentingList.add("android.");
        instrumentingList.add("com.google.android.maps");
        instrumentingList.add("org.apache.http.impl.client.DefaultRequestDirector");

        instrumentingExcludeList.add("android.support.v4.app.NotificationCompat");
        instrumentingExcludeList.add("android.support.v4.content.LocalBroadcastManager");
        instrumentingExcludeList.add("android.support.v4.util.LruCache");
    }

    public AndroidTranslator(ClassHandler classHandler, ClassCache classCache, List<String> customShadowClassNames) {
        this(classHandler, classCache);
        if (customShadowClassNames != null && !customShadowClassNames.isEmpty()) {
            instrumentingList.addAll(customShadowClassNames);
        }
    }

    public void addCustomShadowClass(String customShadowClassName) {
        if (!instrumentingList.contains(customShadowClassName)) {
            instrumentingList.add(customShadowClassName);
        }
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

        if (classHasFromAndroidEquivalent(className)) {
            replaceClassWithFromAndroidEquivalent(classPool, className);
            return;
        }

        CtClass ctClass;
        try {
            ctClass = classPool.get(className);
        } catch (NotFoundException e) {
            throw new IgnorableClassNotFoundException(e);
        }

        if (shouldInstrument(ctClass)) {
            int modifiers = ctClass.getModifiers();
            if (Modifier.isFinal(modifiers)) {
                ctClass.setModifiers(modifiers & ~Modifier.FINAL);
            }

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

    /* package */ boolean shouldInstrument(CtClass ctClass) {
        if (ctClass.hasAnnotation(Instrument.class)) {
            return true;
        } else if (ctClass.isInterface() || ctClass.hasAnnotation(DoNotInstrument.class)) {
            return false;
        } else {
            for (String klassName : instrumentingExcludeList) {
                if (ctClass.getName().startsWith(klassName)) {
                    return false;
                }
            }
            for (String klassName : instrumentingList) {
                if (ctClass.getName().startsWith(klassName)) {
                    return true;
                }
            }
            return false;
        }
    }

    private boolean classHasFromAndroidEquivalent(String className) {
        return className.startsWith(Uri.class.getName());
    }

    private void replaceClassWithFromAndroidEquivalent(ClassPool classPool, String className) throws NotFoundException {
        FromAndroidClassNameParts classNameParts = new FromAndroidClassNameParts(className);
        if (classNameParts.isFromAndroid()) return;

        String from = classNameParts.getNameWithFromAndroid();
        CtClass ctClass = classPool.getAndRename(from, className);

        ClassMap map = new ClassMap() {
            @Override
            public Object get(Object jvmClassName) {
                FromAndroidClassNameParts classNameParts = new FromAndroidClassNameParts(jvmClassName.toString());
                if (classNameParts.isFromAndroid()) {
                    return classNameParts.getNameWithoutFromAndroid();
                } else {
                    return jvmClassName;
                }
            }
        };
        ctClass.replaceClassName(map);
    }

    class FromAndroidClassNameParts {
        private static final String TOKEN = "__FromAndroid";

        private String prefix;
        private String suffix;

        FromAndroidClassNameParts(String name) {
            int dollarIndex = name.indexOf("$");
            prefix = name;
            suffix = "";
            if (dollarIndex > -1) {
                prefix = name.substring(0, dollarIndex);
                suffix = name.substring(dollarIndex);
            }
        }

        public boolean isFromAndroid() {
            return prefix.endsWith(TOKEN);
        }

        public String getNameWithFromAndroid() {
            return prefix + TOKEN + suffix;
        }

        public String getNameWithoutFromAndroid() {
            return prefix.replace(TOKEN, "") + suffix;
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

        if (ctClass.isEnum()) {
            // skip enum constructors because they are not stubs in android.jar
            return;
        }

        boolean hasDefault = false;

        for (CtConstructor ctConstructor : ctClass.getDeclaredConstructors()) {
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
                false,
                false);
    }

    private void fixMethods(CtClass ctClass) throws NotFoundException, CannotCompileException {
        for (CtMethod ctMethod : ctClass.getDeclaredMethods()) {
            fixMethod(ctClass, ctMethod, true);
        }
        CtMethod equalsMethod = ctClass.getMethod("equals", "(Ljava/lang/Object;)Z");
        CtMethod hashCodeMethod = ctClass.getMethod("hashCode", "()I");
        CtMethod toStringMethod = ctClass.getMethod("toString", "()Ljava/lang/String;");

        fixMethod(ctClass, equalsMethod, false);
        fixMethod(ctClass, hashCodeMethod, false);
        fixMethod(ctClass, toStringMethod, false);
    }

    private String describe(CtMethod ctMethod) throws NotFoundException {
        return Modifier.toString(ctMethod.getModifiers()) + " " + ctMethod.getReturnType().getSimpleName() + " " + ctMethod.getLongName();
    }

    private void fixMethod(CtClass ctClass, CtMethod ctMethod, boolean wasFoundInClass) throws NotFoundException {
        String describeBefore = describe(ctMethod);
        try {
            CtClass declaringClass = ctMethod.getDeclaringClass();
            int originalModifiers = ctMethod.getModifiers();

            boolean wasNative = Modifier.isNative(originalModifiers);
            boolean wasFinal = Modifier.isFinal(originalModifiers);
            boolean wasAbstract = Modifier.isAbstract(originalModifiers);
            boolean wasDeclaredInClass = ctClass == declaringClass;

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
            if (wasFoundInClass) {
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
            String methodBody = generateMethodBody(ctClass, ctMethod, wasNative, wasAbstract, returnCtClass, returnType, isStatic, !wasFoundInClass);

            if (!wasFoundInClass) {
                CtMethod newMethod = makeNewMethod(ctClass, ctMethod, returnCtClass, methodName, paramTypes, "{\n" + methodBody + generateCallToSuper(methodName, paramTypes) + "\n}");
                newMethod.setModifiers(newModifiers);
                if (wasDeclaredInClass) {
                    ctMethod.insertBefore("{\n" + methodBody + "}\n");
                } else {
                    ctClass.addMethod(newMethod);
                }
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

    private String generateMethodBody(CtClass ctClass, CtMethod ctMethod, boolean wasNative, boolean wasAbstract, CtClass returnCtClass, Type returnType, boolean aStatic, boolean shouldGenerateCallToSuper) throws NotFoundException {
        String methodBody;
        if (wasAbstract) {
            methodBody = returnType.isVoid() ? "" : "return " + returnType.defaultReturnString() + ";";
        } else {
            methodBody = generateMethodBody(ctClass, ctMethod, returnCtClass, returnType, aStatic, shouldGenerateCallToSuper);
        }

        if (wasNative) {
            methodBody += returnType.isVoid() ? "" : "return " + returnType.defaultReturnString() + ";";
        }
        return methodBody;
    }

    public String generateMethodBody(CtClass ctClass, CtMethod ctMethod, CtClass returnCtClass, Type returnType, boolean isStatic, boolean shouldGenerateCallToSuper) throws NotFoundException {
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
            if (shouldGenerateCallToSuper) {
                buf.append(generateCallToSuper(ctMethod.getName(), ctMethod.getParameterTypes()));
            } else {
                buf.append("return ");
                buf.append(returnType.defaultReturnString());
                buf.append(";\n");
            }
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
