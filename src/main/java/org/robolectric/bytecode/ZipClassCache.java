package org.robolectric.bytecode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public class ZipClassCache implements ClassCache {
    private static final Attributes.Name VERSION_ATTRIBUTE = new Attributes.Name("version");

    private Map<String, byte[]> cachedClasses = new HashMap<String, byte[]>();
    private boolean startedWriting = false;

    public ZipClassCache(String classCachePath, final int expectedCacheVersion) {
        final File cacheJarFile = new File(classCachePath);
        JarFile cacheFile;
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
            if (cacheVersion != expectedCacheVersion || expectedCacheVersion == -1) {
                cacheJarFile.delete();
            } else {
                readEntries(cacheFile);
            }
        } catch (IOException e) {
            // no problem
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override public void run() {
                Manifest manifest = new Manifest();
                Attributes attributes = new Attributes();
                attributes.put(VERSION_ATTRIBUTE, String.valueOf(expectedCacheVersion));
                manifest.getEntries().put("robolectric", attributes);

                saveAllClassesToCache(cacheJarFile, manifest);
            }
        });
    }

    @Override
    public byte[] getClassBytesFor(String name) {
        return cachedClasses.get(name);
    }

    @Override
    public boolean isWriting() {
        synchronized (this) {
            return startedWriting;
        }
    }

    @Override
    public void addClass(String className, byte[] classBytes) {
        cachedClasses.put(className, classBytes);
    }

    private void readEntries(JarFile cacheFile) {
        Enumeration<JarEntry> entries = cacheFile.entries();
        try {
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String className = entry.getName();
                if (className.endsWith(".class")) {
                    int classSize = (int) entry.getSize();
                    InputStream inputStream = cacheFile.getInputStream(entry);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream(classSize);
                    int c;
                    while ((c = inputStream.read()) != -1) {
                        baos.write(c);
                    }
                    className = className.substring(0, className.indexOf(".class")).replace('/', '.');
                    addClass(className, baos.toByteArray());
                }

            }
        } catch (IOException e) {
            // no problem, we didn't want those bytes that much anyway
        }
    }

    protected void saveAllClassesToCache(File file, Manifest manifest) {
        synchronized (this) {
            startedWriting = true;
    
            if (cachedClasses.size() > 0) {
                JarOutputStream jarOutputStream = null;
                try {
                    File cacheJarDir = file.getParentFile();
                    if (!cacheJarDir.exists()) {
                        cacheJarDir.mkdirs();
                    }
    
                    jarOutputStream = new JarOutputStream(new FileOutputStream(file), manifest);
                    for (Map.Entry<String, byte[]> entry : cachedClasses.entrySet()) {
                        String key = entry.getKey();
                        writeToFile(file, entry, key);
                        jarOutputStream.putNextEntry(new JarEntry(key.replace('.', '/') + ".class"));
                        jarOutputStream.write(entry.getValue());
                        jarOutputStream.closeEntry();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    if (jarOutputStream != null) {
                        try {
                            jarOutputStream.close();
                        } catch (IOException ignore) {
                        }
                    }
                }
            }
            startedWriting = false;
        }
    }

    private void writeToFile(File file, Map.Entry<String, byte[]> entry, String key) throws IOException {
        File classFile = new File(new File(file.getParentFile(), "classes"), key.replaceAll("\\.", "/") + ".class");
        classFile.getParentFile().mkdirs();
        FileOutputStream fileOutputStream = new FileOutputStream(classFile);
        fileOutputStream.write(entry.getValue());
        fileOutputStream.close();
    }
}
