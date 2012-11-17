package com.xtremelabs.robolectric.bytecode;

import com.xtremelabs.robolectric.RobolectricContext;
import com.xtremelabs.robolectric.annotation.DisableStrictI18n;
import com.xtremelabs.robolectric.annotation.EnableStrictI18n;
import com.xtremelabs.robolectric.annotation.Values;
import com.xtremelabs.robolectric.internal.DoNotInstrument;
import com.xtremelabs.robolectric.internal.Instrument;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.LoaderClassPath;
import javassist.NotFoundException;

public class RobolectricClassLoader extends javassist.Loader {
    private final ClassCache classCache;

    public RobolectricClassLoader(ClassLoader classLoader, ClassCache classCache, AndroidTranslator androidTranslator) {
        super(classLoader, null);

        delegateLoadingOf(RobolectricClassLoader.class.getName());
        delegateLoadingOf(RobolectricContext.class.getName());
        delegateLoadingOf(RobolectricContext.Factory.class.getName());
        delegateLoadingOf(AndroidTranslator.class.getName());
        delegateLoadingOf(ClassHandler.class.getName());
        delegateLoadingOf(Instrument.class.getName());
        delegateLoadingOf(DoNotInstrument.class.getName());
        delegateLoadingOf(Values.class.getName());
        delegateLoadingOf(EnableStrictI18n.class.getName());
        delegateLoadingOf(DisableStrictI18n.class.getName());

        this.classCache = classCache;
        try {
            ClassPool classPool = new ClassPool();
            classPool.appendClassPath(new LoaderClassPath(classLoader));

            if (classLoader != RobolectricClassLoader.class.getClassLoader()) {
                classPool.appendClassPath(new LoaderClassPath(RobolectricClassLoader.class.getClassLoader()));
            }

            addTranslator(classPool, androidTranslator);
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        } catch (CannotCompileException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Class loadClass(String name) throws ClassNotFoundException {
        boolean shouldComeFromThisClassLoader = !(
                name.startsWith("org.junit")
                        || name.startsWith("org.hamcrest")
                        || name.startsWith("org.specs2") // allows for android projects with mixed scala\java tests to be
                        || name.startsWith("scala.")     //  run with Maven Surefire (see the RoboSpecs project on github)
                        || name.startsWith("org.sqlite.") // ugh, javassist is barfing while loading org.sqlite now for some reason?!?
        );

        Class<?> theClass;
        if (shouldComeFromThisClassLoader) {
            theClass = super.loadClass(name);
        } else {
            theClass = getParent().loadClass(name);
        }

        return theClass;
    }

    public Class<?> bootstrap(Class testClass) {
        String testClassName = testClass.getName();

        try {
            return loadClass(testClassName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Class findClass(String name) throws ClassNotFoundException {
        byte[] classBytes = classCache.getClassBytesFor(name);
        if (classBytes != null) {
            return defineClass(name, classBytes, 0, classBytes.length);
        }
        return super.findClass(name);
    }
}
