package com.xtremelabs.droidsugar;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.LoaderClassPath;
import javassist.NotFoundException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Loader extends javassist.Loader {
    private AndroidTranslator androidTranslator;
    private JarFile cacheFile;

    public Loader(ClassHandler classHandler) {
        super(Loader.class.getClassLoader(), null);

        delegateLoadingOf(AndroidTranslator.class.getName());

        try {
            ClassPool classPool = new ClassPool();
            classPool.appendClassPath(new LoaderClassPath(Loader.class.getClassLoader()));

            androidTranslator = new AndroidTranslator(classHandler);
            addTranslator(classPool, androidTranslator);
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        } catch (CannotCompileException e) {
            throw new RuntimeException(e);
        }
        final File cacheJarFile = new File("tmp/cached-droid-sugar-classes.jar");
        try {
            cacheFile = new JarFile(cacheJarFile);
        } catch (IOException e) {
            // no problem
        }
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override public void run() {
                androidTranslator.saveAllClassesToCache(cacheJarFile);
            }
        });
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

    protected Class findClass(String name) throws ClassNotFoundException {
        String jarClassName = name.replace('.', '/');
        if (cacheFile != null) {
            JarEntry jarEntry = cacheFile.getJarEntry(jarClassName + ".class");
            if (jarEntry != null) {
                try {
                    int classSize = (int) jarEntry.getSize();
                    InputStream inputStream = cacheFile.getInputStream(jarEntry);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream(classSize);
                    int c;
                    while ((c = inputStream.read()) != -1) {
                        baos.write(c);
                    }
                    byte[] bytes = baos.toByteArray();
                    return defineClass(name, bytes, 0, classSize);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return super.findClass(name);
    }
}
