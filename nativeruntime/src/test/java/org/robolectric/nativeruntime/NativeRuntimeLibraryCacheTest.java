package org.robolectric.nativeruntime;

import static com.google.common.truth.Truth.assertThat;
import static java.nio.file.Files.isSameFile;
import static java.nio.file.Files.newOutputStream;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Files.write;
import static org.junit.Assert.assertThrows;

import java.io.IOException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.robolectric.junit.rules.SetSystemPropertyRule;
import org.robolectric.util.TempDirectory;

public final class NativeRuntimeLibraryCacheTest {
  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();
  @Rule public SetSystemPropertyRule properties = new SetSystemPropertyRule();

  private Path cacheRoot;

  @Before
  public void setUp() throws Exception {
    cacheRoot = temporaryFolder.newFolder("cache").toPath();
  }

  @Test
  public void firstLoaderUsesCache_laterLoadersReceiveIndependentCopies() throws Exception {
    URL resource = archive("native.jar", new byte[] {1, 2, 3});
    DefaultNativeRuntimeLoader loader = new DefaultNativeRuntimeLoader();
    TempDirectory first = new TempDirectory("first");
    TempDirectory second = new TempDirectory("second");
    TempDirectory third = new TempDirectory("third");

    Path cached = loader.prepareLibrary(resource, "library", first, cacheRoot);
    Path copy =
        new DefaultNativeRuntimeLoader().prepareLibrary(resource, "library", second, cacheRoot);
    Path otherCopy = loader.prepareLibrary(resource, "library", third, cacheRoot);

    assertThat(cached.startsWith(cacheRoot)).isTrue();
    assertThat(cached.startsWith(first.getBasePath())).isFalse();
    assertThat(cached.getParent().resolve(".complete").toFile().exists()).isTrue();
    assertThat(copy).isEqualTo(second.getBasePath().resolve("library"));
    assertThat(otherCopy).isEqualTo(third.getBasePath().resolve("library"));
    assertThat(isSameFile(cached, copy)).isFalse();
    assertThat(isSameFile(copy, otherCopy)).isFalse();
    assertThat(readAllBytes(cached)).isEqualTo(new byte[] {1, 2, 3});
    assertThat(readAllBytes(copy)).isEqualTo(readAllBytes(cached));
    write(copy, new byte[] {4});
    assertThat(readAllBytes(cached)).isEqualTo(new byte[] {1, 2, 3});
    assertThat(readAllBytes(otherCopy)).isEqualTo(readAllBytes(cached));
  }

  @Test
  public void claimedAndLockedLibraryCanBeCopiedIndependently() throws Exception {
    URL resource = archive("native.jar", new byte[] {1, 2, 3});
    DefaultNativeRuntimeLoader loader = new DefaultNativeRuntimeLoader();
    Path cached = loader.prepareLibrary(resource, "library", new TempDirectory("first"), cacheRoot);
    try (FileChannel channel =
            FileChannel.open(cached, StandardOpenOption.READ, StandardOpenOption.WRITE);
        FileLock lock = channel.lock()) {
      Path copy =
          loader.prepareLibrary(resource, "library", new TempDirectory("second"), cacheRoot);
      assertThat(lock.isValid()).isTrue();
      assertThat(isSameFile(cached, copy)).isFalse();
      assertThat(readAllBytes(copy)).isEqualTo(readAllBytes(cached));
      write(copy, new byte[] {4});
      assertThat(readAllBytes(cached)).isEqualTo(new byte[] {1, 2, 3});
    }
  }

  @Test
  public void differentArchivesHaveSeparateCachedLibraries() throws Exception {
    DefaultNativeRuntimeLoader loader = new DefaultNativeRuntimeLoader();
    Path first =
        loader.prepareLibrary(
            archive("one.jar", new byte[] {1}), "library", new TempDirectory("first"), cacheRoot);
    Path second =
        loader.prepareLibrary(
            archive("two.jar", new byte[] {2}), "library", new TempDirectory("second"), cacheRoot);

    assertThat(first.getParent()).isNotEqualTo(second.getParent());
    assertThat(readAllBytes(first)).isEqualTo(new byte[] {1});
    assertThat(readAllBytes(second)).isEqualTo(new byte[] {2});
  }

  @Test
  public void rebuiltArchiveDoesNotReusePreviousLibrary() throws Exception {
    URL resource = archive("native.jar", new byte[] {1});
    DefaultNativeRuntimeLoader loader = new DefaultNativeRuntimeLoader();
    Path first = loader.prepareLibrary(resource, "library", new TempDirectory("first"), cacheRoot);
    Path jar = temporaryFolder.getRoot().toPath().resolve("native.jar");
    try (JarOutputStream output = new JarOutputStream(newOutputStream(jar))) {
      output.putNextEntry(new JarEntry("library"));
      output.write(new byte[] {2, 3, 4, 5});
      output.closeEntry();
    }

    Path second =
        loader.prepareLibrary(resource, "library", new TempDirectory("second"), cacheRoot);

    assertThat(second.getParent()).isNotEqualTo(first.getParent());
    assertThat(readAllBytes(second)).isEqualTo(new byte[] {2, 3, 4, 5});
    assertThat(readAllBytes(first)).isEqualTo(new byte[] {1});
  }

  @Test
  public void unavailableCacheFallsBackToSandboxCopy() throws Exception {
    TempDirectory sandbox = new TempDirectory("sandbox");
    Path unavailableCache = temporaryFolder.newFile("not-a-directory").toPath();
    Path library =
        new DefaultNativeRuntimeLoader()
            .prepareLibrary(
                archive("native.jar", new byte[] {1}), "library", sandbox, unavailableCache);

    assertThat(library).isEqualTo(sandbox.getBasePath().resolve("library"));
    assertThat(readAllBytes(library)).isEqualTo(new byte[] {1});
  }

  @Test
  public void disabledCacheUsesSandboxCopy() throws Exception {
    properties.set("robolectric.nativeruntime.cacheNativeLibrary", "false");
    TempDirectory sandbox = new TempDirectory("sandbox");
    Path library =
        new DefaultNativeRuntimeLoader()
            .prepareLibrary(archive("native.jar", new byte[] {1}), "library", sandbox, cacheRoot);

    assertThat(library).isEqualTo(sandbox.getBasePath().resolve("library"));
    assertThat(readAllBytes(library)).isEqualTo(new byte[] {1});
  }

  @Test
  public void unpackedResourceUsesIndependentCopy() throws Exception {
    Path source = temporaryFolder.newFile("library").toPath();
    write(source, new byte[] {1, 2, 3});
    TempDirectory sandbox = new TempDirectory("sandbox");
    Path library =
        new DefaultNativeRuntimeLoader()
            .prepareLibrary(source.toUri().toURL(), "library", sandbox, cacheRoot);

    assertThat(library).isEqualTo(sandbox.getBasePath().resolve("library"));
    assertThat(isSameFile(library, source)).isFalse();
    assertThat(readAllBytes(library)).isEqualTo(readAllBytes(source));
  }

  @Test
  public void missingResourceReportsFailure() throws Exception {
    URL missing = temporaryFolder.getRoot().toPath().resolve("missing").toUri().toURL();
    TempDirectory sandbox = new TempDirectory("sandbox");
    assertThrows(
        IOException.class,
        () ->
            new DefaultNativeRuntimeLoader()
                .prepareLibrary(missing, "library", sandbox, cacheRoot));
  }

  private URL archive(String name, byte[] bytes) throws Exception {
    Path jar = temporaryFolder.newFile(name).toPath();
    try (JarOutputStream output = new JarOutputStream(newOutputStream(jar))) {
      output.putNextEntry(new JarEntry("library"));
      output.write(bytes);
      output.closeEntry();
    }
    return new URL("jar:" + jar.toUri() + "!/library");
  }
}
