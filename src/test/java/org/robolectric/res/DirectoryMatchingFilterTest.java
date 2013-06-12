package org.robolectric.res;

import org.junit.After;
import org.junit.Test;
import org.robolectric.Robolectric;

import java.io.File;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DirectoryMatchingFilterTest {
  private static final String JAR_SEPARATOR = "/";
  private static final String UNIX_SEPARATOR = "/";
  private static final String WINDOWS_SEPARATOR = "\\";
  private static final String FOLDER_BASE_NAME = "folderBase";
  private static final String NOT_FOUND_FOLDER_BASE_NAME = "notFoundFolderBase";
  private static final String FILE_PATH_HAS_FOLDER_BASE_NAME_ON_UNIX = UNIX_SEPARATOR + "foo"
      + UNIX_SEPARATOR + "bar"
      + UNIX_SEPARATOR + FOLDER_BASE_NAME;
  private static final String FILE_PATH_HAS_FOLDER_BASE_NAME_ON_WINDOWS = "C:"
      + WINDOWS_SEPARATOR + "foo"
      + WINDOWS_SEPARATOR + "bar"
      + WINDOWS_SEPARATOR + FOLDER_BASE_NAME;
  private static final String JAR_PATH_HAS_FOLDER_BASE_NAME_ON_UNIX = UNIX_SEPARATOR + "foo"
      + UNIX_SEPARATOR + "bar.jar!"
      + JAR_SEPARATOR + "baz"
      + JAR_SEPARATOR + FOLDER_BASE_NAME;
  private static final String JAR_PATH_HAS_FOLDER_BASE_NAME_ON_WINDOWS = "C:"
      + WINDOWS_SEPARATOR + "foo"
      + WINDOWS_SEPARATOR + "bar.jar!"
      + JAR_SEPARATOR + "baz"
      + JAR_SEPARATOR + FOLDER_BASE_NAME;
  private String originalSeparator;

  @After
  public void tearDown() throws Exception {
    if (originalSeparator != null) {
      Robolectric.Reflection.setFinalStaticField(File.class.getDeclaredField("separator"), originalSeparator);
      originalSeparator = null;
    }
  }

  @Test
  public void shouldAcceptFsFileHasFolderBaseNameOnUnix() throws Exception {
    setFileSeparator(UNIX_SEPARATOR);
    DirectoryMatchingFilter filter = new DirectoryMatchingFilter(FOLDER_BASE_NAME);
    FsFile file = mock(FsFile.class);
    when(file.getPath()).thenReturn(FILE_PATH_HAS_FOLDER_BASE_NAME_ON_UNIX);

    assertThat(filter.accept(file)).isTrue();
  }

  @Test
  public void shouldAcceptJarFsFileHasFolderBaseNameOnUnix() throws Exception {
    setFileSeparator(UNIX_SEPARATOR);
    DirectoryMatchingFilter filter = new DirectoryMatchingFilter(FOLDER_BASE_NAME);
    FsFile file = mock(Fs.JarFs.JarFsFile.class);
    when(file.getPath()).thenReturn(JAR_PATH_HAS_FOLDER_BASE_NAME_ON_UNIX);

    assertThat(filter.accept(file)).isTrue();
  }

  @Test
  public void shouldAcceptFsFileHasFolderBaseNameOnWindows() throws Exception {
    setFileSeparator(WINDOWS_SEPARATOR);
    DirectoryMatchingFilter filter = new DirectoryMatchingFilter(FOLDER_BASE_NAME);
    FsFile file = mock(FsFile.class);
    when(file.getPath()).thenReturn(FILE_PATH_HAS_FOLDER_BASE_NAME_ON_WINDOWS);

    assertThat(filter.accept(file)).isTrue();
  }

  @Test
  public void shouldAcceptJarFsFileHasFolderBaseNameOnWindows() throws Exception {
    setFileSeparator(WINDOWS_SEPARATOR);
    DirectoryMatchingFilter filter = new DirectoryMatchingFilter(FOLDER_BASE_NAME);
    FsFile file = mock(Fs.JarFs.JarFsFile.class);
    when(file.getPath()).thenReturn(JAR_PATH_HAS_FOLDER_BASE_NAME_ON_WINDOWS);

    assertThat(filter.accept(file)).isTrue();
  }

  @Test
  public void shouldNotAcceptFsFileDoesNotHaveFolderBaseNameOnUnix() throws Exception {
    setFileSeparator(UNIX_SEPARATOR);
    DirectoryMatchingFilter filter = new DirectoryMatchingFilter(NOT_FOUND_FOLDER_BASE_NAME);
    FsFile file = mock(FsFile.class);
    when(file.getPath()).thenReturn(FILE_PATH_HAS_FOLDER_BASE_NAME_ON_UNIX);

    assertThat(filter.accept(file)).isFalse();
  }

  @Test
  public void shouldNotAcceptJarFsFileDoesNotHaveFolderBaseNameOnUnix() throws Exception {
    setFileSeparator(UNIX_SEPARATOR);
    DirectoryMatchingFilter filter = new DirectoryMatchingFilter(NOT_FOUND_FOLDER_BASE_NAME);
    FsFile file = mock(Fs.JarFs.JarFsFile.class);
    when(file.getPath()).thenReturn(JAR_PATH_HAS_FOLDER_BASE_NAME_ON_UNIX);

    assertThat(filter.accept(file)).isFalse();
  }

  @Test
  public void shouldNotAcceptFsFileDoesNotHaveFolderBaseNameOnWindows() throws Exception {
    setFileSeparator(WINDOWS_SEPARATOR);
    DirectoryMatchingFilter filter = new DirectoryMatchingFilter(NOT_FOUND_FOLDER_BASE_NAME);
    FsFile file = mock(FsFile.class);
    when(file.getPath()).thenReturn(FILE_PATH_HAS_FOLDER_BASE_NAME_ON_WINDOWS);

    assertThat(filter.accept(file)).isFalse();
  }

  @Test
  public void shouldNotAcceptJarFsFileDoseNotHaveFolderBaseNameOnWindows() throws Exception {
    setFileSeparator(WINDOWS_SEPARATOR);
    DirectoryMatchingFilter filter = new DirectoryMatchingFilter(NOT_FOUND_FOLDER_BASE_NAME);
    FsFile file = mock(Fs.JarFs.JarFsFile.class);
    when(file.getPath()).thenReturn(JAR_PATH_HAS_FOLDER_BASE_NAME_ON_WINDOWS);

    assertThat(filter.accept(file)).isFalse();
  }

  private void setFileSeparator(String separator) throws Exception {
    originalSeparator = (String) Robolectric.Reflection.setFinalStaticField(File.class.getDeclaredField("separator"), separator);
  }
}
