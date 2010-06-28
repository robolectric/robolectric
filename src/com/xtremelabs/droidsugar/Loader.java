package com.xtremelabs.droidsugar;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.LoaderClassPath;
import javassist.NotFoundException;

public class Loader extends javassist.Loader {
    public Loader(ClassHandler classHandler) {
        super(Loader.class.getClassLoader(), null);

        delegateLoadingOf(AndroidTranslator.class.getName());

        try {
            ClassPool classPool = new ClassPool();
            classPool.appendClassPath(new LoaderClassPath(Loader.class.getClassLoader()));

            addTranslator(classPool, new AndroidTranslator(classHandler));
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        } catch (CannotCompileException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Class loadClass(String name) throws ClassNotFoundException {
        boolean shouldComeFromThisClassLoader = !(name.startsWith("org.junit") || name.startsWith("org.hamcrest"));
        
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
}
