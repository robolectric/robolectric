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
      while (entries.hasMoreElements()) {
        JarEntry jarEntry = entries.nextElement();

        byte[] bytes;
        if (jarEntry.getName().endsWith(".class")) {
          System.out.println("Instrumenting " + jarEntry.getName());
          bytes = sandboxClassLoader.instrument(jarEntry.getName().replaceAll("\\.class$", "").replaceAll("/", "."));
        } else {
          System.out.println("Copying " + jarEntry.getName());
          bytes = ByteStreams.toByteArray(jarFile.getInputStream(jarEntry));
        }

        jarOutputStream.putNextEntry(new JarEntry(jarEntry.getName()));
        jarOutputStream.write(bytes);
      }
      jarOutputStream.close();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }
}
