package org.robolectric.util;

import static com.google.common.truth.Truth.assertThat;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.shadows.util.SQLiteLibraryLoader;

@RunWith(AndroidJUnit4.class)
public class SQLiteLibraryLoaderTest {
  /** Saved system properties. */
  private String savedOs, savedArch;
  private SQLiteLibraryLoader loader;

  @Before
  public void setUp() {
    loader = new SQLiteLibraryLoader();
  }

  @Before
  public void saveSystemProperties() {
    savedOs = System.getProperty("os.name");
    savedArch = System.getProperty("os.arch");
  }

  @After
  public void restoreSystemProperties() {
    System.setProperty("os.name", savedOs);
    System.setProperty("os.arch", savedArch);
  }

  @Test
  public void shouldExtractNativeLibrary() {
    assertThat(loader.isLoaded()).isFalse();
    loader.doLoad();
    assertThat(loader.isLoaded()).isTrue();
  }

  @Test
  public void shouldFindLibraryForWindowsXPX86() throws IOException {
    assertThat(loadLibrary(new SQLiteLibraryLoader(WINDOWS), "Windows XP", "x86"))
        .isEqualTo("windows-x86/sqlite4java.dll");
  }

  @Test
  public void shouldFindLibraryForWindows7X86() throws IOException {
    assertThat(loadLibrary(new SQLiteLibraryLoader(WINDOWS), "Windows 7", "x86"))
        .isEqualTo("windows-x86/sqlite4java.dll");
  }

  @Test
  public void shouldFindLibraryForWindowsXPAmd64() throws IOException {
    assertThat(loadLibrary(new SQLiteLibraryLoader(WINDOWS), "Windows XP", "amd64"))
        .isEqualTo("windows-x86_64/sqlite4java.dll");
  }

  @Test
  public void shouldFindLibraryForWindows7Amd64() throws IOException {
    assertThat(loadLibrary(new SQLiteLibraryLoader(WINDOWS), "Windows 7", "amd64"))
        .isEqualTo("windows-x86_64/sqlite4java.dll");
  }

  @Test
  public void shouldFindLibraryForLinuxi386() throws IOException {
    assertThat(loadLibrary(new SQLiteLibraryLoader(LINUX), "Some linux version", "i386"))
        .isEqualTo("linux-x86/libsqlite4java.so");
  }

  @Test
  public void shouldFindLibraryForLinuxx86() throws IOException {
    assertThat(loadLibrary(new SQLiteLibraryLoader(LINUX), "Some linux version", "x86"))
        .isEqualTo("linux-x86/libsqlite4java.so");
  }

  @Test
  public void shouldFindLibraryForLinuxAmd64() throws IOException {
    assertThat(loadLibrary(new SQLiteLibraryLoader(LINUX), "Some linux version", "amd64"))
        .isEqualTo("linux-x86_64/libsqlite4java.so");
  }

  @Test
  public void shouldFindLibraryForMacWithAnyArch() throws IOException {
    assertThat(loadLibrary(new SQLiteLibraryLoader(MAC), "Mac OS X", "any architecture"))
        .isEqualTo("mac-x86_64/libsqlite4java.jnilib");
  }

  @Test
  public void shouldFindLibraryForMacWithAnyArchAndDyLibMapping() throws IOException {
    assertThat(loadLibrary(new SQLiteLibraryLoader(MAC_DYLIB), "Mac OS X", "any architecture"))
        .isEqualTo("mac-x86_64/libsqlite4java.jnilib");
  }

  @Test(expected = UnsupportedOperationException.class)
  public void shouldThrowExceptionIfUnknownNameAndArch() throws Exception {
    loadLibrary(new SQLiteLibraryLoader(LINUX), "ACME Electronic", "FooBar2000");
  }

  private String loadLibrary(SQLiteLibraryLoader loader, String name, String arch) throws IOException {
    setNameAndArch(name, arch);
    return loader.getLibClasspathResourceName();
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

  private static void setNameAndArch(String name, String arch) {
    System.setProperty("os.name", name);
    System.setProperty("os.arch", arch);
  }

  private static final SQLiteLibraryLoader.LibraryNameMapper LINUX = new LibraryMapperTest("lib", "so");
  private static final SQLiteLibraryLoader.LibraryNameMapper WINDOWS = new LibraryMapperTest("", "dll");
  private static final SQLiteLibraryLoader.LibraryNameMapper MAC = new LibraryMapperTest("lib", "jnilib");
  private static final SQLiteLibraryLoader.LibraryNameMapper MAC_DYLIB = new LibraryMapperTest("lib", "dylib");
}
