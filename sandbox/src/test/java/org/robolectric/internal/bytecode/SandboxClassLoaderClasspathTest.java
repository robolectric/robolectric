package org.robolectric.internal.bytecode;

import static com.google.common.truth.Truth.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for how {@link SandboxClassLoader} finds class path resources, and what it caches. */
@RunWith(JUnit4.class)
public class SandboxClassLoaderClasspathTest {
  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private Path root;

  @Before
  public void setUp() throws IOException {
    root = temporaryFolder.getRoot().toPath().toRealPath();
  }

  @Test
  public void parsedJavaClassPath_isCachedPerValue_andHandsOutCopies() {
    String original = System.getProperty("java.class.path");
    try {
      String a = root.resolve("a.jar") + File.pathSeparator + root.resolve("b.jar");
      System.setProperty("java.class.path", a);
      URL[] first = SandboxClassLoader.parsedJavaClassPath();
      first[0] = null;
      URL[] second = SandboxClassLoader.parsedJavaClassPath();
      assertThat(second).hasLength(2);
      assertThat(second[0].getPath()).endsWith("/a.jar");

      System.setProperty("java.class.path", root.resolve("c.jar").toString());
      URL[] changed = SandboxClassLoader.parsedJavaClassPath();
      assertThat(changed).hasLength(1);
      assertThat(changed[0].getPath()).endsWith("/c.jar");
    } finally {
      System.setProperty("java.class.path", original);
    }
  }
}
