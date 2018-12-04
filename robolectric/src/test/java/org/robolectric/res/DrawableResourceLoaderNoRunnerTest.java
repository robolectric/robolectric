package org.robolectric.res;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.res.android.ResTable_config;
import org.robolectric.util.ReflectionHelpers;

@RunWith(JUnit4.class)
public class DrawableResourceLoaderNoRunnerTest {
  private static final String JAR_SEPARATOR = "/";
  private static final String UNIX_SEPARATOR = "/";
  private static final String WINDOWS_SEPARATOR = "\\";
  private static final String JAR_SCHEME = "jar:";
  private static final String DRAWABLE_DIR = "drawable";
  private static final String FILE_PATH_ON_UNIX = UNIX_SEPARATOR + "foo"
      + UNIX_SEPARATOR + DRAWABLE_DIR;
  private static final String FILE_PATH_ON_WINDOWS = "C:"
      + WINDOWS_SEPARATOR + "foo"
      + WINDOWS_SEPARATOR + DRAWABLE_DIR;
  private static final String JAR_PATH_ON_UNIX = JAR_SCHEME + UNIX_SEPARATOR + "foo"
      + UNIX_SEPARATOR + "bar.jar!"
      + JAR_SEPARATOR + DRAWABLE_DIR;
  private static final String JAR_PATH_ON_WINDOWS = JAR_SCHEME + "C:"
      + WINDOWS_SEPARATOR + "foo"
      + WINDOWS_SEPARATOR + "bar.jar!"
      + JAR_SEPARATOR + DRAWABLE_DIR;

  private String originalSeparator;
  private PackageResourceTable resourceTable;

  @Before
  public void setUp() {
    resourceTable = new ResourceTableFactory().newResourceTable("org.robolectric");
  }

  @After
  public void tearDown() throws Exception {
    if (originalSeparator != null) {
      Field field = File.class.getDeclaredField("separator");
      ReflectionHelpers.setStaticField(field, originalSeparator);
      originalSeparator = null;
    }
  }

  @Test
  public void shouldFindDrawableResourcesWorkWithUnixJarFilePath() throws Exception {
    setFileSeparator(UNIX_SEPARATOR);

    Path mockTestFile = mock(Path.class);
    when(mockTestFile.getFileName()).thenReturn(Paths.get("foo.png"));

    Path mockTestDir = mock(Path.class);
    when(mockTestDir.toString()).thenReturn(JAR_PATH_ON_UNIX);
    when(mockTestDir.getFileName()).thenReturn(Paths.get(DRAWABLE_DIR));

    FsFile mockTestBaseDir = mock(FsFile.class);
    when(Fs.listFiles(mockTestBaseDir)).thenReturn(new Path[]{mockTestDir});
    ResourcePath resourcePath = new ResourcePath(null, mockTestBaseDir, null);

    DrawableResourceLoader testLoader = new DrawableResourceLoader(resourceTable);
    testLoader.findDrawableResources(resourcePath);

    assertThat(resourceTable.getValue(new ResName("org.robolectric", "drawable", "foo"), new ResTable_config()).isFile()).isTrue();
  }

  @Test
  public void shouldFindDrawableResourcesWorkWithUnixFilePath() throws Exception {
    setFileSeparator(UNIX_SEPARATOR);

    Path mockTestFile = mock(Path.class);
    when(mockTestFile.getFileName()).thenReturn(Paths.get("foo.png"));

    Path mockTestDir = mock(Path.class);
    when(mockTestDir.toString()).thenReturn(FILE_PATH_ON_UNIX);
    when(mockTestDir.getFileName()).thenReturn(Paths.get(DRAWABLE_DIR));

    FsFile mockTestBaseDir = mock(FsFile.class);
    when(Fs.listFiles(mockTestBaseDir)).thenReturn(new Path[]{mockTestDir});
    ResourcePath resourcePath = new ResourcePath(null, mockTestBaseDir, null);

    DrawableResourceLoader testLoader = new DrawableResourceLoader(resourceTable);
    testLoader.findDrawableResources(resourcePath);

    assertThat(resourceTable.getValue(new ResName("org.robolectric", "drawable", "foo"), new ResTable_config()).isFile()).isTrue();
  }

  @Test
  public void shouldFindDrawableResourcesWorkWithWindowsJarFilePath() throws Exception {
    setFileSeparator(WINDOWS_SEPARATOR);

    Path mockTestFile = mock(Path.class);
    when(mockTestFile.getFileName()).thenReturn(Paths.get("foo.png"));

    Path mockTestDir = mock(Path.class);
    when(mockTestDir.toString()).thenReturn(JAR_PATH_ON_WINDOWS);
    when(mockTestDir.getFileName()).thenReturn(Paths.get(DRAWABLE_DIR));
    // when(mockTestDir.listFiles()).thenReturn(new FsFile[]{mockTestFile});
    // when(mockTestDir.isDirectory()).thenReturn(true);
    FsFile mockTestBaseDir = mock(FsFile.class);
    when(Fs.listFiles(mockTestBaseDir)).thenReturn(new Path[]{mockTestDir});
    ResourcePath resourcePath = new ResourcePath(null, mockTestBaseDir, null);

    DrawableResourceLoader testLoader = new DrawableResourceLoader(resourceTable);
    testLoader.findDrawableResources(resourcePath);

    assertThat(resourceTable.getValue(new ResName("org.robolectric", "drawable", "foo"), new ResTable_config()).isFile()).isTrue();
  }

  @Test
  public void shouldFindDrawableResourcesWorkWithWindowsFilePath() throws Exception {
    setFileSeparator(WINDOWS_SEPARATOR);

    Path mockTestFile = mock(Path.class);
    when(mockTestFile.getFileName()).thenReturn(Paths.get("foo.png"));

    Path mockTestDir = mock(Path.class);
    when(mockTestDir.toString()).thenReturn(FILE_PATH_ON_WINDOWS);
    when(mockTestDir.getFileName()).thenReturn(Paths.get(DRAWABLE_DIR));
    // when(mockTestDir.listFiles()).thenReturn(new FsFile[]{mockTestFile});
    // when(mockTestDir.isDirectory()).thenReturn(true);
    FsFile mockTestBaseDir = mock(FsFile.class);
    when(Fs.listFiles(mockTestBaseDir)).thenReturn(new Path[]{mockTestDir});
    ResourcePath resourcePath = new ResourcePath(null, mockTestBaseDir, null);

    DrawableResourceLoader testLoader = new DrawableResourceLoader(resourceTable);
    testLoader.findDrawableResources(resourcePath);

    assertThat(resourceTable.getValue(new ResName("org.robolectric", "drawable", "foo"), new ResTable_config()).isFile()).isTrue();
  }

  private void setFileSeparator(String separator) throws Exception {
    Field field = File.class.getDeclaredField("separator");
    originalSeparator = ReflectionHelpers.getStaticField(field);
    ReflectionHelpers.setStaticField(field, separator);
  }
}
