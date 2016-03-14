package org.robolectric.res;

import org.junit.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class FileFsFileTest {

  @Test
  public void from_shouldConstructPath() {
    final String path = FileFsFile.from("foo", "bar", "baz").getPath();
    assertThat(path).isEqualTo("foo" + File.separator + "bar" + File.separator + "baz");
  }

  @Test
  public void from_shouldIgnoreNullOrMissingComponents() {
    final String path = FileFsFile.from(null, "", "foo", "bar").getPath();
    assertThat(path).isEqualTo("foo" + File.separator + "bar");
  }
}