package org.robolectric.util;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class SQLiteLibraryLoaderTest {

  /** Saved system properties. */
  private String savedOs, savedArch;

  /** Saved library name mapper. */
  private SQLiteLibraryLoader.LibraryNameMapper savedMapper;

  @Before
  public void deleteExtractedLibrary() {
    SQLiteLibraryLoader.getNativeLibraryPath().delete();
    SQLiteLibraryLoader.mustReload();
  }

  @Test
  public void shouldExtractNativeLibrary() {
    File extractedPath = SQLiteLibraryLoader.getNativeLibraryPath();
    assertThat(extractedPath).doesNotExist();
    SQLiteLibraryLoader.load();
    assertThat(extractedPath).exists();
  }

  @Test
  public void shouldNotRewriteExistingLibraryIfThereAreNoChanges() throws Exception{
    SQLiteLibraryLoader.load();
    File extractedPath = SQLiteLibraryLoader.getNativeLibraryPath();
    assertThat(extractedPath).exists();

    final long resetTime = 1234L;
    assertThat(extractedPath.setLastModified(resetTime)).describedAs("Cannot reset modification date").isTrue();
    // actual time may be truncated to seconds
    long time = extractedPath.lastModified();
    assertThat(time).isLessThanOrEqualTo(resetTime);

    SQLiteLibraryLoader.mustReload();
    SQLiteLibraryLoader.load();
    extractedPath = SQLiteLibraryLoader.getNativeLibraryPath();
    assertThat(extractedPath.lastModified()).isEqualTo(time);
  }

  @Test
  public void shouldRewriteExistingLibraryIfThereAreChanges() throws Exception {
    IOUtils.copy(IOUtils.toInputStream("changed"), new FileOutputStream(SQLiteLibraryLoader.getNativeLibraryPath()));
    long firstSize = SQLiteLibraryLoader.getNativeLibraryPath().length();

    SQLiteLibraryLoader.load();
    File extractedPath = SQLiteLibraryLoader.getNativeLibraryPath();
    assertThat(extractedPath).exists();
    assertThat(extractedPath.length()).isGreaterThan(firstSize);
  }


  @Before
  public void saveSystemProperties() {
    savedOs = System.getProperty("os.name");
    savedArch = System.getProperty("os.arch");
    savedMapper = SQLiteLibraryLoader.libraryNameMapper;
  }

  @After
  public void restoreSystemProperties() {
    System.setProperty("os.name", savedOs);
    System.setProperty("os.arch", savedArch);
    SQLiteLibraryLoader.libraryNameMapper = savedMapper;
  }

  @Test
  public void shouldFindLibraryForWindowsX86() throws IOException {
    SQLiteLibraryLoader.libraryNameMapper = new LibraryMapperTest("", "dll");
    System.setProperty("os.name", "Windows XP");
    System.setProperty("os.arch", "x86");
    SQLiteLibraryLoader.getLibraryStream().close();
    System.setProperty("os.name", "Windows 7");
    System.setProperty("os.arch", "x86");
    SQLiteLibraryLoader.getLibraryStream().close();
  }

  @Test
  public void shouldFindLibraryForWindowsAmd64() throws IOException {
    SQLiteLibraryLoader.libraryNameMapper = new LibraryMapperTest("", "dll");
    System.setProperty("os.name", "Windows XP");
    System.setProperty("os.arch", "amd64");
    SQLiteLibraryLoader.getLibraryStream().close();
    System.setProperty("os.name", "Windows 7");
    System.setProperty("os.arch", "amd64");
    SQLiteLibraryLoader.getLibraryStream().close();
  }

  @Test
  public void shouldFindLibraryForLinuxI386() throws IOException {
    SQLiteLibraryLoader.libraryNameMapper = new LibraryMapperTest("lib", "so");
    System.setProperty("os.name", "Some linux version");
    System.setProperty("os.arch", "i386");
    SQLiteLibraryLoader.getLibraryStream().close();
  }

  @Test
  public void shouldFindLibraryForLinuxAmd64() throws IOException {
    SQLiteLibraryLoader.libraryNameMapper = new LibraryMapperTest("lib", "so");
    System.setProperty("os.name", "Some linux version");
    System.setProperty("os.arch", "amd64");
    SQLiteLibraryLoader.getLibraryStream().close();
  }

  @Test
  public void shouldFindLibraryForMacWithAnyArch() throws IOException {
    SQLiteLibraryLoader.libraryNameMapper = new LibraryMapperTest("lib", "jnilib");
    System.setProperty("os.name", "Mac OS X");
    System.setProperty("os.arch", "any architecture");
    SQLiteLibraryLoader.getLibraryStream().close();
  }

  private static class LibraryMapperTest implements SQLiteLibraryLoader.LibraryNameMapper {
    private final String prefix;
    private final String ext;

    private LibraryMapperTest(String prefix, String ext) {
      this.prefix = prefix;
      this.ext = ext;
    }

    @Override
    public String mapLibraryName(String name) {
      return prefix + name + "." + ext;
    }
  }

}
