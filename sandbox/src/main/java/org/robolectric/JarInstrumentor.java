package org.robolectric;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import org.robolectric.internal.bytecode.ClassNodeProvider;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.internal.bytecode.InstrumentationConfiguration.Builder;
import org.robolectric.internal.bytecode.OldClassInstrumentor;
import org.robolectric.internal.bytecode.ShadowDecorator;
import org.robolectric.util.Util;

/**
 * Instruments an entire jar.
 */
public class JarInstrumentor {

  private final InstrumentationConfiguration instrumentationConfiguration;
  private final ShadowDecorator shadowDecorator;
  private final OldClassInstrumentor classInstrumentor;

  public JarInstrumentor() {
    instrumentationConfiguration = createInstrumentationConfiguration();
    shadowDecorator = new ShadowDecorator();
    classInstrumentor = new OldClassInstrumentor(shadowDecorator);
  }

  public static void main(String[] args) throws Exception {
    new JarInstrumentor().run(args);
  }

  private void run(String[] args) throws IOException {
    if (args.length != 2) {
      System.err.println("Usage: JarInstrumentor <source jar> <dest jar>");
      System.exit(1);
    }

    instrumentJar(new File(args[0]), new File(args[1]));
  }

  private void instrumentJar(File sourceFile, File destFile) throws IOException {
    long startNs = System.nanoTime();
    JarFile jarFile = new JarFile(sourceFile);
    ClassNodeProvider classNodeProvider =
        new ClassNodeProvider() {
          @Override
          protected byte[] getClassBytes(String className) throws ClassNotFoundException {
            return JarInstrumentor.getClassBytes(className, jarFile);
          }
        };

    int nonClassCount = 0;
    int classCount = 0;
    Set<String> failedClasses = new TreeSet<>();
    try (JarOutputStream jarOut =
        new JarOutputStream(
            new BufferedOutputStream(new FileOutputStream(destFile), 32 * 1024))) {
      System.out.println("Instrumenting from " + sourceFile + " to " + destFile);
      Enumeration<JarEntry> entries = jarFile.entries();
      while (entries.hasMoreElements()) {
        JarEntry jarEntry = entries.nextElement();

        String name = jarEntry.getName();
        if (name.endsWith("/")) {
          jarOut.putNextEntry(new JarEntry(name));
        } else if (name.endsWith(".class")) {
          String className = name.substring(0, name.length() - ".class".length()).replace('/', '.');

          boolean classIsRenamed = isClassRenamed(className);
          if (classIsRenamed) {
            System.out.println("className = " + className);
            continue;
          }

          try {
            byte[] classBytes = getClassBytes(className, jarFile);
            byte[] outBytes =
                classInstrumentor.instrument(
                    classBytes, instrumentationConfiguration, classNodeProvider);
            jarOut.putNextEntry(new JarEntry(name));
            jarOut.write(outBytes);
            classCount++;
          } catch (Exception e) {
            failedClasses.add(className);
            System.err.print("Failed to instrument " + className + ": ");
            e.printStackTrace();
          }
        } else {
          // resources & stuff
          jarOut.putNextEntry(new JarEntry(name));
          Util.copy(jarFile.getInputStream(jarEntry), jarOut);
          nonClassCount++;
        }
      }
    }
    long elapsedNs = System.nanoTime() - startNs;
    System.out.println(
        String.format(
            Locale.getDefault(),
            "Wrote %d classes and %d resources in %1.2f seconds",
            classCount,
            nonClassCount,
            elapsedNs / 1000000000.0));
    if (!failedClasses.isEmpty()) {
      System.out.println("Failed to instrument:");
    }
    for (String failedClass : failedClasses) {
      System.out.println("- " + failedClass);
    }
  }

  private boolean isClassRenamed(String className) {
    String internalName = className.replace('.', '/');
    String remappedName = instrumentationConfiguration.mappedTypeName(internalName);
    return !remappedName.equals(internalName);
  }

  private static byte[] getClassBytes(String className, JarFile jarFile)
      throws ClassNotFoundException {
    String classFilename = className.replace('.', '/') + ".class";
    ZipEntry entry = jarFile.getEntry(classFilename);
    try {
      InputStream inputStream;
      if (entry == null) {
        inputStream = JarInstrumentor.class.getClassLoader().getResourceAsStream(classFilename);
      } else {
        inputStream = jarFile.getInputStream(entry);
      }
      if (inputStream == null) {
        throw new ClassNotFoundException("Couldn't find " + className.replace('/', '.'));
      }
      return Util.readBytes(inputStream);
    } catch (IOException e) {
      throw new ClassNotFoundException("Couldn't load " + className.replace('/', '.'), e);
    }
  }

  private static InstrumentationConfiguration createInstrumentationConfiguration() {
    Builder builder =
        InstrumentationConfiguration.newBuilder()
            .doNotAcquirePackage("java.")
            .doNotAcquirePackage("sun.")
            .doNotAcquirePackage("org.robolectric.annotation.")
            .doNotAcquirePackage("org.robolectric.internal.")
            .doNotAcquirePackage("org.robolectric.util.")
            .doNotAcquirePackage("org.junit.");

    builder
        .doNotAcquireClass("org.robolectric.TestLifecycle")
        .doNotAcquireClass("org.robolectric.AndroidManifest")
        .doNotAcquireClass("org.robolectric.RobolectricTestRunner")
        .doNotAcquireClass("org.robolectric.RobolectricTestRunner%HelperTestRunner")
        .doNotAcquireClass("org.robolectric.res.ResourcePath")
        .doNotAcquireClass("org.robolectric.res.ResourceTable")
        .doNotAcquireClass("org.robolectric.res.builder.XmlBlock");

    builder
        .doNotAcquirePackage("javax.")
        .doNotAcquirePackage("org.junit")
        .doNotAcquirePackage("org.hamcrest")
        .doNotAcquirePackage("org.robolectric.annotation.")
        .doNotAcquirePackage("org.robolectric.internal.")
        .doNotAcquirePackage("org.robolectric.manifest.")
        .doNotAcquirePackage("org.robolectric.res.")
        .doNotAcquirePackage("org.robolectric.util.")
        .doNotAcquirePackage("org.robolectric.RobolectricTestRunner$")
        .doNotAcquirePackage("sun.")
        .doNotAcquirePackage("com.sun.")
        .doNotAcquirePackage("org.w3c.")
        .doNotAcquirePackage("org.xml.")
        .doNotAcquirePackage("org.specs2")  // allows for android projects with mixed scala\java tests to be
        .doNotAcquirePackage("scala.")      //  run with Maven Surefire (see the RoboSpecs project on github)
        .doNotAcquirePackage("kotlin.")
        // Fix #958: SQLite native library must be loaded once.
        .doNotAcquirePackage("com.almworks.sqlite4java")
        .doNotAcquirePackage("org.jacoco.");

    // Instrumenting these classes causes a weird failure.
    builder.doNotInstrumentClass("android.R")
        .doNotInstrumentClass("android.R$styleable");

    builder.addInstrumentedPackage("dalvik.")
        .addInstrumentedPackage("libcore.")
        .addInstrumentedPackage("android.")
        .addInstrumentedPackage("com.android.internal.")
        .addInstrumentedPackage("org.apache.http.")
        .addInstrumentedPackage("org.ccil.cowan.tagsoup")
        .addInstrumentedPackage("org.kxml2.");

    builder.doNotInstrumentPackage("androidx.test");
    return builder.build();
  }
}
