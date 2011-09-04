package com.xtremelabs.robolectric.bytecode;

import java.util.ArrayList;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.LoaderClassPath;
import javassist.NotFoundException;

public class RobolectricClassLoader extends javassist.Loader {
    private ClassCache classCache;

    public RobolectricClassLoader(ClassHandler classHandler) {
    	this(classHandler, null);
    }
    
    public RobolectricClassLoader(ClassHandler classHandler, ArrayList<String> customClassNames) {
        super(RobolectricClassLoader.class.getClassLoader(), null);

        delegateLoadingOf(AndroidTranslator.class.getName());
        delegateLoadingOf(ClassHandler.class.getName());

        classCache = new ClassCache("tmp/cached-robolectric-classes.jar", AndroidTranslator.CACHE_VERSION);
        try {
            ClassPool classPool = new ClassPool();
            classPool.appendClassPath(new LoaderClassPath(RobolectricClassLoader.class.getClassLoader()));

            AndroidTranslator androidTranslator = new AndroidTranslator(classHandler, classCache, customClassNames);
            addTranslator(classPool, androidTranslator);
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        } catch (CannotCompileException e) {
            throw new RuntimeException(e);
        }
    } 
    
    @Override
    public Class loadClass(String name) throws ClassNotFoundException {
        boolean shouldComeFromThisClassLoader = !(name.startsWith("org.junit") || name.startsWith("org.hamcrest")  
        		|| name.startsWith("org.specs2") || name.startsWith("scala.")); //org.specs2 and scala. allows for android projects with mixed scala\java tests to be run with Maven Surefire (see the RoboSpecs project on github)

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

    @Override protected Class findClass(String name) throws ClassNotFoundException {
        byte[] classBytes = classCache.getClassBytesFor(name);
        if (classBytes != null) {
            return defineClass(name, classBytes, 0, classBytes.length);
        }
        return super.findClass(name);
    }
}
