package org.robolectric.bytecode;

import android.net.Uri;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;
import javassist.*;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SuppressWarnings({"UnusedDeclaration"})
public class AndroidTranslator implements Translator {
    /**
     * IMPORTANT -- increment this number when the bytecode generated for modified classes changes
     * so the cache file can be invalidated.
     */
    public static final int CACHE_VERSION = 23;
//    public static final int CACHE_VERSION = -1;

    private final ClassCache classCache;
    private final Setup setup;

    private static boolean debug = false;

    public static void performStaticInitialization(Class<?> clazz) {
        if (debug) System.out.println("static initializing " + clazz);
        try {
            Method originalStaticInitializer = clazz.getDeclaredMethod(InstrumentingClassLoader.STATIC_INITIALIZER_METHOD_NAME);
            originalStaticInitializer.setAccessible(true);
            originalStaticInitializer.invoke(null);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public AndroidTranslator(ClassCache classCache, Setup setup) {
        this.classCache = classCache;
        this.setup = setup;
    }

    @Override
    public void start(ClassPool classPool) throws NotFoundException, CannotCompileException {
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
            String translatedClassName = setup.translateClassName(className);
            ctClass = classPool.get(translatedClassName);

            if (!translatedClassName.equals(className)) {
                ctClass.setName(className);
            }
        } catch (NotFoundException e) {
            throw new IgnorableClassNotFoundException(e);
        }

        if (ctClass.hasAnnotation(Implements.class)) {
            for (CtMethod ctMethod : ctClass.getDeclaredMethods()) {
                if (ctMethod.hasAnnotation(Implementation.class)) {
                    CtMethod copy = CtNewMethod.copy(ctMethod, MethodGenerator.directMethodName(ctClass, ctMethod.getName()), ctClass, MethodGenerator.IDENTITY_CLASS_MAP);
                    System.out.println("direct access for shadow " + copy.getLongName());
                    ctClass.addMethod(copy);
                }
            }
            return;
        }

        boolean shouldInstrument = setup.shouldInstrument(new JavassistClassInfo(ctClass));

        if (debug)
            System.out.println("Considering " + ctClass.getName() + ": " + (shouldInstrument ? "INSTRUMENTING" : "not instrumenting"));

        if (shouldInstrument) {
            int modifiers = ctClass.getModifiers();
            if (Modifier.isFinal(modifiers)) {
                ctClass.setModifiers(modifiers & ~Modifier.FINAL);
            }

            if (ctClass.isInterface() || ctClass.isEnum()) return;

            CtClass objectClass = classPool.get(Object.class.getName());
            try {
                ctClass.getField(InstrumentingClassLoader.CLASS_HANDLER_DATA_FIELD_NAME);
            } catch (NotFoundException e1) {
                CtField field = new CtField(objectClass, InstrumentingClassLoader.CLASS_HANDLER_DATA_FIELD_NAME, ctClass);
                field.setModifiers(java.lang.reflect.Modifier.PUBLIC);
                ctClass.addField(field);
            }

            CtClass superclass = ctClass.getSuperclass();
            if (!superclass.isFrozen()) {
                onLoad(classPool, superclass.getName());
            }

            MethodGenerator methodGenerator = new MethodGenerator(ctClass, setup);
//            methodGenerator.fixConstructors();
            methodGenerator.createSpecialConstructor();
            methodGenerator.fixMethods();
            methodGenerator.deferClassInitialization();

            try {
                classCache.addClass(className, ctClass.toBytecode());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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

    static class JavassistClassInfo implements ClassInfo {
        private final CtClass ctClass;

        public JavassistClassInfo(CtClass ctClass) {
            this.ctClass = ctClass;
        }

        @Override
        public String getName() {
            return ctClass.getName();
        }

        @Override
        public boolean isInterface() {
            return ctClass.isInterface();
        }

        @Override
        public boolean isAnnotation() {
            return ctClass.isAnnotation();
        }

        @Override
        public boolean hasAnnotation(Class<? extends Annotation> annotationClass) {
            return ctClass.hasAnnotation(annotationClass);
        }
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
}
