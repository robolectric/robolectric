package com.xtremelabs.robolectric.bytecode;

import android.net.Uri;
import com.xtremelabs.robolectric.internal.DoNotInstrument;
import com.xtremelabs.robolectric.internal.Instrument;
import javassist.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"UnusedDeclaration"})
public class AndroidTranslator implements Translator {
    /**
     * IMPORTANT -- increment this number when the bytecode generated for modified classes changes
     * so the cache file can be invalidated.
     */
    public static final int CACHE_VERSION = 22;

    static final String STATIC_INITIALIZER_METHOD_NAME = "__staticInitializer__";

    private static final List<ClassHandler> CLASS_HANDLERS = new ArrayList<ClassHandler>();

    private ClassHandler classHandler;
    private ClassCache classCache;
    private final List<String> instrumentingList = new ArrayList<String>();
    private final List<String> instrumentingExcludeList = new ArrayList<String>();


    public static ClassHandler getClassHandler(int index) {
        return CLASS_HANDLERS.get(index);
    }

    public static void performStaticInitialization(Class<?> clazz) {
        System.out.println("static initializing " + clazz);
        try {
            Method originalStaticInitializer = clazz.getDeclaredMethod(STATIC_INITIALIZER_METHOD_NAME);
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

    public AndroidTranslator(ClassHandler classHandler, ClassCache classCache) {
        this.classHandler = classHandler;
        this.classCache = classCache;

        // Initialize lists
        instrumentingList.add("android.");
//        instrumentingList.add("libcore.");
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

        robolectricInternalsCtClass.makeClassInitializer().insertBefore("{\n" +
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

            if (ctClass.isInterface() || ctClass.isEnum()) return;

            classHandler.instrument(ctClass);

            CtClass superclass = ctClass.getSuperclass();
            if (!superclass.isFrozen()) {
                onLoad(classPool, superclass.getName());
            }

            MethodGenerator methodGenerator = new MethodGenerator(ctClass);
            methodGenerator.fixConstructors();
            methodGenerator.fixMethods();
            methodGenerator.deferClassInitialization();

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
}
