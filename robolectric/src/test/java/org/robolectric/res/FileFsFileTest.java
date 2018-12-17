package org.robolectric.res;

import static com.google.common.truth.Truth.assertThat;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class FileFsFileTest {

  public String origFileSeparator;

  @Before
  public void setUp() throws Exception {
    origFileSeparator = FileFsFile.FILE_SEPARATOR;
  }

  @After
  public void tearDown() throws Exception {
    FileFsFile.FILE_SEPARATOR = origFileSeparator;
  }

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

  @Test
  public void from_shouldWorkOnWindows() throws Exception {
    FileFsFile.FILE_SEPARATOR = "\\";

    assertThat(partsOf(FileFsFile.from("\\", "a\\b\\c", "d")))
        .containsExactly("a", "b", "c", "d");
  }

  @Test
  public void join_shouldWorkOnWindows() throws Exception {
    FileFsFile.FILE_SEPARATOR = "\\";

    assertThat(partsOf(FileFsFile.from("a\\b\\c").join("d\\e\\f")))
        .containsExactly("a", "b", "c", "d", "e", "f");
  }

  List<String> partsOf(FsFile fsFile) {
    List<String> parts = new ArrayList<>();
    while (fsFile != null) {
      parts.add(fsFile.getName());
      fsFile = fsFile.getParent();
    }
    Collections.reverse(parts);
    return parts;
  }

  @Nonnull
  private String safe(String path) {
    return path.replace("/", File.separator);
  }
}