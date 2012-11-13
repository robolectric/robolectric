package com.xtremelabs.robolectric.bytecode;

import com.xtremelabs.robolectric.RobolectricContext;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.LoaderClassPath;
import javassist.NotFoundException;

import java.io.File;
import java.util.List;

public class RobolectricClassLoader extends javassist.Loader {
    private ClassCache classCache;
    private AndroidTranslator androidTranslator;

    public RobolectricClassLoader(ClassHandler classHandler) {
        this(classHandler, null);
    }

    public RobolectricClassLoader(ClassHandler classHandler, List<String> customClassNames) {
        this(RobolectricClassLoader.class.getClassLoader(), classHandler, customClassNames);
    }

    public RobolectricClassLoader(ClassLoader classLoader, ClassHandler classHandler, List<String> customClassNames) {
        super(classLoader, null);

        delegateLoadingOf(RobolectricClassLoader.class.getName());
        delegateLoadingOf(RobolectricContext.class.getName());
        delegateLoadingOf(RobolectricContext.Factory.class.getName());
        delegateLoadingOf(AndroidTranslator.class.getName());
        delegateLoadingOf(ClassHandler.class.getName());

        createClassCache();
        try {
            ClassPool classPool = new ClassPool();
            classPool.appendClassPath(new LoaderClassPath(classLoader));

            if (classLoader != RobolectricClassLoader.class.getClassLoader()) {
                classPool.appendClassPath(new LoaderClassPath(RobolectricClassLoader.class.getClassLoader()));
            }

            androidTranslator = createAndroidTranslator(classHandler, customClassNames);
            addTranslator(classPool, androidTranslator);
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        } catch (CannotCompileException e) {
            throw new RuntimeException(e);
        }
    }

    protected AndroidTranslator createAndroidTranslator(ClassHandler classHandler, List<String> customClassNames) {
        return new AndroidTranslator(classHandler, getClassCache(), customClassNames);
    }

    public ClassCache getClassCache() {
        if (classCache == null) {
            classCache = createClassCache();
        }

        return classCache;
    }

    protected ClassCache createClassCache() {
        final String classCachePath = System.getProperty("cached.robolectric.classes.path");
        final File classCacheDirectory;
        if (null == classCachePath || "".equals(classCachePath.trim())) {
            classCacheDirectory = new File("./tmp");
        } else {
            classCacheDirectory = new File(classCachePath);
        }

        return new ClassCache(new File(classCacheDirectory, "cached-robolectric-classes.jar").getAbsolutePath(), AndroidTranslator.CACHE_VERSION);
    }

    public void addCustomShadowClass(String classOrPackageToBeInstrumented) {
        androidTranslator.addCustomShadowClass(classOrPackageToBeInstrumented);
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

    @Override
    protected Class findClass(String name) throws ClassNotFoundException {
        byte[] classBytes = getClassCache().getClassBytesFor(name);
        if (classBytes != null) {
            return defineClass(name, classBytes, 0, classBytes.length);
        }
        return super.findClass(name);
    }
}
