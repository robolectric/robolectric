package org.robolectric.preinstrumented;

import com.google.common.io.ByteStreams;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import org.robolectric.config.AndroidConfigurer;
import org.robolectric.interceptors.AndroidInterceptors;
import org.robolectric.internal.bytecode.ClassDetails;
import org.robolectric.internal.bytecode.ClassInstrumentor;
import org.robolectric.internal.bytecode.ClassNodeProvider;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.internal.bytecode.Interceptors;
import org.robolectric.util.inject.Injector;

/** Runs Robolectric invokedynamic instrumentation on an android-all jar. */
public class JarInstrumentor {

  private static final int ONE_MB = 1024 * 1024;

  private static final Injector INJECTOR = new Injector.Builder().build();

  private final ClassInstrumentor classInstrumentor;
  private final InstrumentationConfiguration instrumentationConfiguration;

  public JarInstrumentor() {
    AndroidConfigurer androidConfigurer = INJECTOR.getInstance(AndroidConfigurer.class);
    classInstrumentor = INJECTOR.getInstance(ClassInstrumentor.class);

    InstrumentationConfiguration.Builder builder = new InstrumentationConfiguration.Builder();
    Interceptors interceptors = new Interceptors(AndroidInterceptors.all());
    androidConfigurer.configure(builder, interceptors);
    instrumentationConfiguration = builder.build();
  }

  public static void main(String[] args) throws IOException, ClassNotFoundException {
    if (args.length != 2) {
      System.err.println("Usage: JarInstrumentor <source jar> <dest jar>");
      System.exit(1);
    }
    new JarInstrumentor().instrumentJar(new File(args[0]), new File(args[1]));
  }

  private void instrumentJar(File sourceFile, File destFile)
      throws IOException, ClassNotFoundException {
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

    // get the jar's SDK version
    try {
      classInstrumentor.setAndroidJarSDKVersion(getJarAndroidSDKVersion(jarFile));
    } catch (Exception e) {
      throw new AssertionError("Unable to get Android SDK version from Jar File", e);
    }

    try (JarOutputStream jarOut =
        new JarOutputStream(new BufferedOutputStream(new FileOutputStream(destFile), ONE_MB))) {
      Enumeration<JarEntry> entries = jarFile.entries();
      while (entries.hasMoreElements()) {
        JarEntry jarEntry = entries.nextElement();

        String name = jarEntry.getName();
        if (name.endsWith("/")) {
          jarOut.putNextEntry(createJarEntry(jarEntry));
        } else if (name.endsWith(".class")) {
          String className = name.substring(0, name.length() - ".class".length()).replace('/', '.');

          try {
            byte[] classBytes = getClassBytes(className, jarFile);
            ClassDetails classDetails = new ClassDetails(classBytes);
            byte[] outBytes = classBytes;
            if (instrumentationConfiguration.shouldInstrument(classDetails)) {
              outBytes =
                  classInstrumentor.instrument(
                      classDetails, instrumentationConfiguration, classNodeProvider);
            }
            jarOut.putNextEntry(createJarEntry(jarEntry));
            jarOut.write(outBytes);
            classCount++;
          } catch (NegativeArraySizeException e) {
            System.err.println(
                "Skipping instrumenting due to NegativeArraySizeException for class: " + className);
          }
        } else {
          // resources & stuff
          jarOut.putNextEntry(createJarEntry(jarEntry));
          ByteStreams.copy(jarFile.getInputStream(jarEntry), jarOut);
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
      return ByteStreams.toByteArray(inputStream);
    } catch (IOException e) {
      throw new ClassNotFoundException("Couldn't load " + className.replace('/', '.'), e);
    }
  }

  private static JarEntry createJarEntry(JarEntry original) {
    JarEntry entry = new JarEntry(original.getName());
    // Setting the timestamp to the original is necessary for deterministic output.
    entry.setTime(original.getTime());
    return entry;
  }

  private int getJarAndroidSDKVersion(JarFile jarFile) throws IOException {
    ZipEntry buildProp = jarFile.getEntry("build.prop");
    Properties buildProps = new Properties();
    buildProps.load(jarFile.getInputStream(buildProp));
    return Integer.parseInt(buildProps.getProperty("ro.build.version.sdk"));
  }
}
