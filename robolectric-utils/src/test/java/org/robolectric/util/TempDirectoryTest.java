package org.robolectric.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TempDirectoryTest {
  @Test
  public void nonEmptyDirectoryDeleted() throws IOException {
    Path path = TempDirectory.create();
    Path temp = Files.createTempDirectory(path, "hello");
    TempDirectory.destroy(path);
    assertThat(temp).doesNotExist();
  }

  @Test
  public void rougeDeletion() throws IOException {
    Path path = TempDirectory.create();
    Files.delete(path);
    TempDirectory.destroy(path);
    assertThat(path).doesNotExist();
    assertThat(TempDirectory.create()).isNotSameAs(path);
  }

  @Test
  public void createsUniqueDirs() throws IOException {
    Path path = TempDirectory.create();
    TempDirectory.destroy(path);
    Path newPath = TempDirectory.create();
    assertThat(path).isNotSameAs(newPath);
  }
}
