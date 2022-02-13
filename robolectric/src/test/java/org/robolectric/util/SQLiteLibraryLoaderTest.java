package org.robolectric.util;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.Assume.assumeTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.shadows.util.SQLiteLibraryLoader;

@RunWith(AndroidJUnit4.class)
public class SQLiteLibraryLoaderTest {
  private static final SQLiteLibraryLoader.LibraryNameMapper LINUX =
      new LibraryMapperTest("lib", "so");
  private static final SQLiteLibraryLoader.LibraryNameMapper WINDOWS =
      new LibraryMapperTest("", "dll");
  private static final SQLiteLibraryLoader.LibraryNameMapper MAC =
      new LibraryMapperTest("lib", "dylib");
  private static final String OS_NAME_WINDOWS_XP = "Windows XP";
  private static final String OS_NAME_WINDOWS_7 = "Windows 7";
  private static final String OS_NAME_WINDOWS_10 = "Windows 10";
  private static final String OS_NAME_LINUX = "Some linux version";
  private static final String OS_NAME_MAC = "Mac OS X";
  private static final String OS_ARCH_ARM64 = "aarch64";
  private static final String OS_ARCH_X86 = "x86";
  private static final String OS_ARCH_X64 = "x86_64";
  private static final String OS_ARCH_AMD64 = "amd64";
  private static final String OS_ARCH_I386 = "i386";
  private static final String SYSTEM_PROPERTY_OS_NAME = "os.name";
  private static final String SYSTEM_PROPERTY_OS_ARCH = "os.arch";

  /** Saved system properties. */
  private String savedOs, savedArch;

  private SQLiteLibraryLoader loader;

  @Before
  public void setUp() {
    loader = new SQLiteLibraryLoader();
  }

  @Before
  public void saveSystemProperties() {
    savedOs = System.getProperty(SYSTEM_PROPERTY_OS_NAME);
    savedArch = System.getProperty(SYSTEM_PROPERTY_OS_ARCH);
  }

  @After
  public void restoreSystemProperties() {
    System.setProperty(SYSTEM_PROPERTY_OS_NAME, savedOs);
    System.setProperty(SYSTEM_PROPERTY_OS_ARCH, savedArch);
  }

  @Test
  public void shouldExtractNativeLibrary() {
    assumeTrue(SQLiteLibraryLoader.isOsSupported());
    assertThat(loader.isLoaded()).isFalse();
    loader.doLoad();
    assertThat(loader.isLoaded()).isTrue();
  }

  @Test
  public void shouldFindLibraryForWindowsXPX86() {
    assertThat(loadLibrary(new SQLiteLibraryLoader(WINDOWS), OS_NAME_WINDOWS_XP, OS_ARCH_X86))
        .isEqualTo("sqlite4java/win32-x86/sqlite4java.dll");
  }

  @Test
  public void shouldFindLibraryForWindows7X86() {
    assertThat(loadLibrary(new SQLiteLibraryLoader(WINDOWS), OS_NAME_WINDOWS_7, OS_ARCH_X86))
        .isEqualTo("sqlite4java/win32-x86/sqlite4java.dll");
  }

  @Test
  public void shouldFindLibraryForWindowsXPAmd64() {
    assertThat(loadLibrary(new SQLiteLibraryLoader(WINDOWS), OS_NAME_WINDOWS_XP, OS_ARCH_AMD64))
        .isEqualTo("sqlite4java/win32-x64/sqlite4java.dll");
  }

  @Test
  public void shouldFindLibraryForWindows7Amd64() {
    assertThat(loadLibrary(new SQLiteLibraryLoader(WINDOWS), OS_NAME_WINDOWS_7, OS_ARCH_AMD64))
        .isEqualTo("sqlite4java/win32-x64/sqlite4java.dll");
  }

  @Test
  public void shouldFindLibraryForWindows10Amd64() {
    assertThat(loadLibrary(new SQLiteLibraryLoader(WINDOWS), OS_NAME_WINDOWS_10, OS_ARCH_AMD64))
        .isEqualTo("sqlite4java/win32-x64/sqlite4java.dll");
  }

  @Test
  public void shouldFindLibraryForLinuxI386() {
    assertThat(loadLibrary(new SQLiteLibraryLoader(LINUX), OS_NAME_LINUX, OS_ARCH_I386))
        .isEqualTo("sqlite4java/linux-i386/libsqlite4java.so");
  }

  @Test
  public void shouldFindLibraryForLinuxX86() {
    assertThat(loadLibrary(new SQLiteLibraryLoader(LINUX), OS_NAME_LINUX, OS_ARCH_X86))
        .isEqualTo("sqlite4java/linux-i386/libsqlite4java.so");
  }

  @Test
  public void shouldFindLibraryForLinuxAmd64() {
    assertThat(loadLibrary(new SQLiteLibraryLoader(LINUX), OS_NAME_LINUX, OS_ARCH_AMD64))
        .isEqualTo("sqlite4java/linux-amd64/libsqlite4java.so");
  }

  @Test
  public void shouldFindLibraryForMacWithI386() {
    assertThat(loadLibrary(new SQLiteLibraryLoader(MAC), OS_NAME_MAC, OS_ARCH_I386))
        .isEqualTo("sqlite4java/osx/libsqlite4java.dylib");
  }

  @Test
  public void shouldFindLibraryForMacWithX86() {
    assertThat(loadLibrary(new SQLiteLibraryLoader(MAC), OS_NAME_MAC, OS_ARCH_X86))
        .isEqualTo("sqlite4java/osx/libsqlite4java.dylib");
  }

  @Test
  public void shouldFindLibraryForMacWithX64() {
    assertThat(loadLibrary(new SQLiteLibraryLoader(MAC), OS_NAME_MAC, OS_ARCH_X64))
        .isEqualTo("sqlite4java/osx/libsqlite4java.dylib");
  }

  @Test
  public void shouldNotFindLibraryForMacWithARM64() {
    assertThrows(
        UnsupportedOperationException.class,
        () -> loadLibrary(new SQLiteLibraryLoader(MAC), OS_NAME_MAC, OS_ARCH_ARM64));
  }

  @Test
  public void shouldThrowExceptionIfUnknownNameAndArch() {
    assertThrows(
        UnsupportedOperationException.class,
        () -> loadLibrary(new SQLiteLibraryLoader(LINUX), "ACME Electronic", "FooBar2000"));
  }

  @Test
  public void shouldNotSupportMacOSWithArchArm64() {
    setNameAndArch(OS_NAME_MAC, OS_ARCH_ARM64);
    assertThat(SQLiteLibraryLoader.isOsSupported()).isFalse();
  }

  @Test
  public void shouldSupportMacOSWithArchX86() {
    setNameAndArch(OS_NAME_MAC, OS_ARCH_X86);
    assertThat(SQLiteLibraryLoader.isOsSupported()).isTrue();
  }

  @Test
  public void shouldSupportMacOSWithArchX64() {
    setNameAndArch(OS_NAME_MAC, OS_ARCH_X64);
    assertThat(SQLiteLibraryLoader.isOsSupported()).isTrue();
  }

  @Test
  public void shouldSupportWindowsXPWithArchX86() {
    setNameAndArch(OS_NAME_WINDOWS_XP, OS_ARCH_X86);
    assertThat(SQLiteLibraryLoader.isOsSupported()).isTrue();
  }

  @Test
  public void shouldSupportWindowsXPWithArcAMD64() {
    setNameAndArch(OS_NAME_WINDOWS_XP, OS_ARCH_AMD64);
    assertThat(SQLiteLibraryLoader.isOsSupported()).isTrue();
  }

  @Test
  public void shouldSupportWindows7WithArchX86() {
    setNameAndArch(OS_NAME_WINDOWS_7, OS_ARCH_X86);
    assertThat(SQLiteLibraryLoader.isOsSupported()).isTrue();
  }

  @Test
  public void shouldSupportWindows7WithAMD64() {
    setNameAndArch(OS_NAME_WINDOWS_7, OS_ARCH_AMD64);
    assertThat(SQLiteLibraryLoader.isOsSupported()).isTrue();
  }

  @Test
  public void shouldSupportWindows10WithAMD64() {
    setNameAndArch(OS_NAME_WINDOWS_10, OS_ARCH_AMD64);
    assertThat(SQLiteLibraryLoader.isOsSupported()).isTrue();
  }

  @Test
  public void shouldSupportLinuxWithI386() {
    setNameAndArch(OS_NAME_LINUX, OS_ARCH_I386);
    assertThat(SQLiteLibraryLoader.isOsSupported()).isTrue();
  }

  @Test
  public void shouldSupportLinuxWithX86() {
    setNameAndArch(OS_NAME_LINUX, OS_ARCH_X86);
    assertThat(SQLiteLibraryLoader.isOsSupported()).isTrue();
  }

  @Test
  public void shouldSupportLinuxWithX64() {
    setNameAndArch(OS_NAME_LINUX, OS_ARCH_X64);
    assertThat(SQLiteLibraryLoader.isOsSupported()).isTrue();
  }

  @Test
  public void shouldSupportLinuxWithAMD64() {
    setNameAndArch(OS_NAME_LINUX, OS_ARCH_AMD64);
    assertThat(SQLiteLibraryLoader.isOsSupported()).isTrue();
  }

  private String loadLibrary(SQLiteLibraryLoader loader, String name, String arch) {
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
    System.setProperty(SYSTEM_PROPERTY_OS_NAME, name);
    System.setProperty(SYSTEM_PROPERTY_OS_ARCH, arch);
  }
}
