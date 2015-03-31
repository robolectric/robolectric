package org.robolectric.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class TempDirectoryTest {
  @Test
  public void verifyReuse() {
    TempDirectory dir = new TempDirectory();
    Path path = dir.createImpl(false);
    dir.destroyImpl(path);
    assertThat(path).exists();
    assertThat(dir.createImpl(false)).isSameAs(path);
  }

  @Test
  public void directoryIsEmpty() throws IOException {
    TempDirectory dir = new TempDirectory();
    Path path = dir.createImpl(false);
    Path temp = Files.createTempDirectory(path, "hello");
    dir.destroyImpl(path);
    assertThat(temp).doesNotExist();
  }

  @Test
  public void rougeDeletion() throws IOException {
    TempDirectory dir = new TempDirectory();
    Path path = dir.createImpl(false);
    Files.delete(path);
    dir.destroyImpl(path);
    assertThat(path).doesNotExist();
    assertThat(dir.createImpl(false)).isNotSameAs(path);
  }

  @Test
  public void rougeDeletionAfterDestroy() throws IOException {
    TempDirectory dir = new TempDirectory();
    Path path = dir.createImpl(false);
    dir.destroyImpl(path);
    Files.delete(path);
    Path newPath = dir.createImpl(false);
    assertThat(path).doesNotExist();
    assertThat(path).isNotSameAs(newPath);
  }
}
