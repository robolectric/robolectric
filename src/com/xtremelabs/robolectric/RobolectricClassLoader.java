package com.xtremelabs.robolectric;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.LoaderClassPath;
import javassist.NotFoundException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class RobolectricClassLoader extends javassist.Loader {
    private static final Attributes.Name VERSION_ATTRIBUTE = new Attributes.Name("version");
    private AndroidTranslator androidTranslator;
    private JarFile cacheFile;

    public RobolectricClassLoader(ClassHandler classHandler) {
        super(RobolectricClassLoader.class.getClassLoader(), null);

        delegateLoadingOf(AndroidTranslator.class.getName());

        try {
            ClassPool classPool = new ClassPool();
            classPool.appendClassPath(new LoaderClassPath(RobolectricClassLoader.class.getClassLoader()));

            androidTranslator = new AndroidTranslator(classHandler);
            addTranslator(classPool, androidTranslator);
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        } catch (CannotCompileException e) {
            throw new RuntimeException(e);
        }
        final File cacheJarFile = new File("tmp/cached-robolectric-classes.jar");
        try {
            cacheFile = new JarFile(cacheJarFile);
            int cacheVersion = 0;
            Manifest manifest = cacheFile.getManifest();
            if (manifest != null) {
                Attributes attributes = manifest.getEntries().get("robolectric");
                if (attributes != null) {
                    String cacheVersionStr = (String) attributes.get(VERSION_ATTRIBUTE);
                    if (cacheVersionStr != null) {
                        cacheVersion = Integer.parseInt(cacheVersionStr);
                    }
                }
            }
            if (cacheVersion != AndroidTranslator.CACHE_VERSION) {
                cacheJarFile.delete();
                cacheFile = new JarFile(cacheJarFile);
            }
        } catch (IOException e) {
            // no problem
        }
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override public void run() {
                Manifest manifest = new Manifest();
                Attributes attributes = new Attributes();
                attributes.put(VERSION_ATTRIBUTE, String.valueOf(AndroidTranslator.CACHE_VERSION));
                manifest.getEntries().put("robolectric", attributes);

                androidTranslator.saveAllClassesToCache(cacheJarFile, manifest);
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
