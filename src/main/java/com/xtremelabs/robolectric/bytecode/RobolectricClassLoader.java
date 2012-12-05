package com.xtremelabs.robolectric.bytecode;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.LoaderClassPath;
import javassist.NotFoundException;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

public class RobolectricClassLoader extends javassist.Loader {
    private final Map<String, Class> classes = new HashMap<String, Class>();
    private final ClassCache classCache;
    private final Setup setup;

    public RobolectricClassLoader(ClassLoader classLoader, ClassCache classCache, AndroidTranslator androidTranslator, Setup setup) {
        super(classLoader, null);
        this.setup = setup;

        List<Class<?>> classesToDelegate = setup.getClassesToDelegateFromRcl();
        for (Class<?> aClass : classesToDelegate) {
            delegateLoadingOf(aClass.getName());
        }


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
        boolean shouldComeFromThisClassLoader = setup.shouldAcquire(name);

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
    synchronized protected Class findClass(String name) throws ClassNotFoundException {
        Class<?> clazz = classes.get(name);

        if (clazz == null) {
            if (classes.containsKey(name)) throw new ClassNotFoundException(name);

            byte[] classBytes = classCache.getClassBytesFor(name);

            try {
                if (classBytes != null) {
                    clazz = defineClass(name, classBytes, 0, classBytes.length);
                } else {
                    clazz = super.findClass(name);
                }

                classes.put(name, clazz);
            } catch (ClassNotFoundException e) {
                classes.put(name, null);
                throw e;
            }
        }

        return clazz;
    }

    @Nullable
    @Override
    public URL getResource(String s) {
        URL resource = super.getResource(s);
        if (resource != null) return resource;
        return RobolectricClassLoader.class.getClassLoader().getResource(s);
    }

    @Override
    public InputStream getResourceAsStream(String s) {
        InputStream resourceAsStream = super.getResourceAsStream(s);
        if (resourceAsStream != null) return resourceAsStream;
        return RobolectricClassLoader.class.getClassLoader().getResourceAsStream(s);
    }

    @Override
    public Enumeration<URL> getResources(String s) throws IOException {
        List<URL> resources = Collections.list(super.getResources(s));
        if (!resources.isEmpty()) return Collections.enumeration(resources);
        return RobolectricClassLoader.class.getClassLoader().getResources(s);
    }
}
