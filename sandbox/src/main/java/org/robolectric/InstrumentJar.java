package org.robolectric;

import com.google.common.io.ByteStreams;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.internal.bytecode.SandboxClassLoader;

public class InstrumentJar {
  public static void main(String[] args) {
    try {
      File inFile = new File(args[0]);
      JarFile jarFile = new JarFile(inFile);

      SandboxClassLoader sandboxClassLoader = new SandboxClassLoader(
          InstrumentJar.class.getClassLoader(),
          InstrumentationConfiguration.newBuilder().build(), inFile.toURL());


      File outFile = new File(args[1]);

      System.out.println("Instrumenting from " + inFile + " to " + outFile + "...");
      JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(outFile));
      Enumeration<JarEntry> entries = jarFile.entries();
      long totalMs = 0;
      long totalClassesInstrumented = 0;
      while (entries.hasMoreElements()) {
        JarEntry jarEntry = entries.nextElement();

        byte[] bytes;
        if (jarEntry.getName().endsWith(".class")) {
          long startTime = System.currentTimeMillis();
          try {
            bytes = sandboxClassLoader.instrument(jarEntry.getName().replaceAll("\\.class$", "").replaceAll("/", "."));
          } catch (Exception e) {
            System.out.println("Could not instrument: " + jarEntry.getName() + " Exception: " + e.toString());
            bytes = ByteStreams.toByteArray(jarFile.getInputStream(jarEntry));
          }
          long endTime = System.currentTimeMillis();
          long timeTaken = endTime - startTime;
//          System.out.println("Instrumented " + jarEntry.getName() + " in " + timeTaken);
          totalMs += timeTaken;
          totalClassesInstrumented++;
        } else {
//          System.out.println("Copying " + jarEntry.getName());
          bytes = ByteStreams.toByteArray(jarFile.getInputStream(jarEntry));
        }

        jarOutputStream.putNextEntry(new JarEntry(jarEntry.getName()));
        jarOutputStream.write(bytes);
      }
      System.out.println("Instrumented " + totalClassesInstrumented + " in " + totalMs + "ms");
      jarOutputStream.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
