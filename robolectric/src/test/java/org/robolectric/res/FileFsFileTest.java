package org.robolectric.res;

import java.io.File;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;
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
    assertThat(path).isEqualTo(safe("foo/bar"));
  }

  @Test public void from_shouldAllowSingleDotPart() throws Exception {
    final String path = FileFsFile.from(".").getPath();
    assertThat(path).isEqualTo(".");
  }

  @Test public void from_shouldAllowLeadingSlash() throws Exception {
    final String path = FileFsFile.from(safe("/some/path")).getPath();
    assertThat(path).isEqualTo(safe("/some/path"));

  }

  @Test public void from_shouldIgnoreDotParts() throws Exception {
    final String path = FileFsFile.from(safe("/some/path/./to/here")).getPath();
    assertThat(path).isEqualTo(safe("/some/path/to/here"));

  }

  @Test public void join_shouldIgnoreDotParts() throws Exception {
    final String path = FileFsFile.from(".").join(safe("some/./path"), ".", safe("to/here")).getPath();
    assertThat(path).isEqualTo(safe("./some/path/to/here"));
  }

  @NotNull
  private String safe(String path) {
    return path.replace("/", File.separator);
  }
}