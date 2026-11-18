package org.robolectric.preinstrumented;

import static com.google.common.truth.Truth.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.annotation.internal.Instrument;

/** Test for {@link JarInstrumentor}. */
@RunWith(JUnit4.class)
public class JarInstrumentorTest {

  @Rule public final TemporaryFolder tempFolder = new TemporaryFolder();

  private JarInstrumentor spyDummyInstrumentor;

  @Before
  public void setUp() {
    JarInstrumentor dummyInstrumentor =
        new JarInstrumentor() {
          @Override
          protected void instrumentJar(File sourceJarFile, File destJarFile) {
            // No-op. We only want to test the command line processing. Stub the actual
            // instrumentation.
          }

          @Override
          protected void exit(int status) {
            // No-op. Tests should never call system.exit().
          }
        };
    spyDummyInstrumentor = spy(dummyInstrumentor);
  }

  @Test
  public void processCommandLine_legacyUsage() throws Exception {
    spyDummyInstrumentor.processCommandLine(new String[] {"source.jar", "dest.jar"});
    verify(spyDummyInstrumentor).instrumentJar(new File("source.jar"), new File("dest.jar"));
  }

  @Test
  public void processCommandLine_unknownArguments() throws Exception {
    spyDummyInstrumentor.processCommandLine(new String[] {"source.jar", "dest.jar", "--some-flag"});
    verify(spyDummyInstrumentor, never()).instrumentJar(any(File.class), any(File.class));
    verify(spyDummyInstrumentor).exit(1);
  }

  @Test
  public void instrumentJar_preservesResourcesAndInstrumentsClasses() throws Exception {
    File sourceJar = createSampleJar();
    File destJar = new File(tempFolder.getRoot(), "dest.jar");

    new JarInstrumentor().instrumentJar(sourceJar, destJar);

    try (JarFile source = new JarFile(sourceJar);
        JarFile result = new JarFile(destJar)) {
      assertThat(result.getJarEntry("res/")).isNotNull();
      assertEntry(result, "res/raw.bin", ZipEntry.STORED, "stored-payload");
      assertEntry(result, "res/text.txt", ZipEntry.DEFLATED, "deflated-payload");
      assertEntry(result, "resources.arsc", ZipEntry.DEFLATED, "arsc-payload");

      String uninstrumented = classEntryName(SampleClass.class);
      assertThat(readEntryBytes(result, result.getJarEntry(uninstrumented)))
          .isEqualTo(readEntryBytes(source, source.getJarEntry(uninstrumented)));

      String instrumented = classEntryName(InstrumentedSampleClass.class);
      JarEntry instrumentedEntry = result.getJarEntry(instrumented);
      assertThat(instrumentedEntry.getMethod()).isEqualTo(ZipEntry.DEFLATED);
      byte[] instrumentedBytes = readEntryBytes(result, instrumentedEntry);
      assertThat(instrumentedBytes)
          .isNotEqualTo(readEntryBytes(source, source.getJarEntry(instrumented)));
      // Verify valid Java class file magic header (0xCAFEBABE).
      assertThat(
              new byte[] {
                instrumentedBytes[0],
                instrumentedBytes[1],
                instrumentedBytes[2],
                instrumentedBytes[3]
              })
          .isEqualTo(new byte[] {(byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE});
    }
  }

  @Test
  public void instrumentJar_overwritesAndIsDeterministic() throws Exception {
    File sourceJar = createSampleJar();
    File destJar1 = tempFolder.newFile("dest1.jar");
    Files.writeString(destJar1.toPath(), "not-a-valid-zip");
    File destJar2 = new File(tempFolder.getRoot(), "dest2.jar");

    JarInstrumentor instrumentor = new JarInstrumentor();
    instrumentor.instrumentJar(sourceJar, destJar1);
    instrumentor.instrumentJar(sourceJar, destJar2);

    assertThat(Files.readAllBytes(destJar1.toPath()))
        .isEqualTo(Files.readAllBytes(destJar2.toPath()));
  }

  @Test
  public void instrumentJar_rejectsPathTraversal() throws Exception {
    File sourceJar = tempFolder.newFile("bad.jar");
    try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(sourceJar))) {
      writeEntry(
          jos,
          "build.prop",
          "ro.build.version.codename=REL\nro.build.version.sdk=36\n".getBytes(UTF_8),
          ZipEntry.DEFLATED);
      writeEntry(jos, "../evil.txt", "bad".getBytes(UTF_8), ZipEntry.DEFLATED);
    }

    File destJar = tempFolder.newFile("dest.jar");
    assertThrows(IOException.class, () -> new JarInstrumentor().instrumentJar(sourceJar, destJar));
  }

  @Test
  public void instrumentJar_filtersResourcesAndPackages() throws Exception {
    File sourceJar = createSampleJar();
    File destJar = new File(tempFolder.getRoot(), "filtered.jar");
    File packagesFile = tempFolder.newFile("packages.txt");
    Files.writeString(packagesFile.toPath(), "com.example.other\n");
    File resourcesFile = tempFolder.newFile("resources.txt");
    Files.writeString(resourcesFile.toPath(), "build.prop\nres/\n");

    new JarInstrumentor()
        .processCommandLine(
            new String[] {
              "--packages_to_keep=" + packagesFile.getAbsolutePath(),
              "--resources_to_keep=" + resourcesFile.getAbsolutePath(),
              sourceJar.getAbsolutePath(),
              destJar.getAbsolutePath(),
            });

    try (JarFile result = new JarFile(destJar)) {
      assertThat(result.getJarEntry("build.prop")).isNotNull();
      assertThat(result.getJarEntry("res/raw.bin")).isNotNull();
      assertThat(result.getJarEntry("res/text.txt")).isNotNull();
      assertThat(result.getJarEntry("other.txt")).isNull();
      assertThat(result.getJarEntry(classEntryName(SampleClass.class))).isNull();
    }
  }

  private File createSampleJar() throws IOException {
    File file = tempFolder.newFile("source.jar");
    try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(file))) {
      writeEntry(
          jos,
          "build.prop",
          "ro.build.version.codename=REL\nro.build.version.sdk=36\n".getBytes(UTF_8),
          ZipEntry.DEFLATED);
      jos.putNextEntry(new JarEntry("res/"));
      jos.closeEntry();
      writeEntry(jos, "res/raw.bin", "stored-payload".getBytes(UTF_8), ZipEntry.STORED);
      writeEntry(jos, "resources.arsc", "arsc-payload".getBytes(UTF_8), ZipEntry.STORED);
      writeEntry(jos, "res/text.txt", "deflated-payload".getBytes(UTF_8), ZipEntry.DEFLATED);
      writeEntry(jos, "other.txt", "excluded-by-filter".getBytes(UTF_8), ZipEntry.DEFLATED);
      writeClassEntry(jos, SampleClass.class);
      writeClassEntry(jos, InstrumentedSampleClass.class);
    }
    return file;
  }

  private static String classEntryName(Class<?> clazz) {
    return clazz.getName().replace('.', '/') + ".class";
  }

  private static void writeClassEntry(JarOutputStream jos, Class<?> clazz) throws IOException {
    String name = classEntryName(clazz);
    try (InputStream is = JarInstrumentorTest.class.getClassLoader().getResourceAsStream(name)) {
      assertThat(is).isNotNull();
      writeEntry(jos, name, is.readAllBytes(), ZipEntry.DEFLATED);
    }
  }

  private static void writeEntry(JarOutputStream jos, String name, byte[] data, int method)
      throws IOException {
    JarEntry entry = new JarEntry(name);
    entry.setMethod(method);
    if (method == ZipEntry.STORED) {
      entry.setSize(data.length);
      entry.setCompressedSize(data.length);
      CRC32 crc = new CRC32();
      crc.update(data);
      entry.setCrc(crc.getValue());
    }
    jos.putNextEntry(entry);
    jos.write(data);
    jos.closeEntry();
  }

  private static void assertEntry(JarFile jar, String name, int method, String expectedContent)
      throws IOException {
    JarEntry entry = jar.getJarEntry(name);
    assertThat(entry).isNotNull();
    assertThat(entry.getMethod()).isEqualTo(method);
    assertThat(readEntryBytes(jar, entry)).isEqualTo(expectedContent.getBytes(UTF_8));
  }

  private static byte[] readEntryBytes(JarFile jarFile, JarEntry entry) throws IOException {
    try (InputStream is = jarFile.getInputStream(entry)) {
      return is.readAllBytes();
    }
  }

  static class SampleClass {}

  @Instrument
  static class InstrumentedSampleClass {}
}
