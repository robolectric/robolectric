package org.robolectric.res;

import org.junit.After;
import org.junit.Test;
import org.robolectric.Robolectric;

import java.io.File;

import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

  @After
  public void tearDown() throws Exception {
    if (originalSeparator != null) {
      Robolectric.Reflection.setFinalStaticField(File.class.getDeclaredField("separator"), originalSeparator);
      originalSeparator = null;
    }
  }

  @Test
  public void shouldFindDrawableResourcesWorkWithUnixJarFilePath() throws Exception {
    setFileSeparator(UNIX_SEPARATOR);

    Fs.JarFs.JarFsFile mockTestFile = mock(Fs.JarFs.JarFsFile.class);
    when(mockTestFile.getName()).thenReturn("foo.png");
    when(mockTestFile.getBaseName()).thenReturn("bar.png");

    Fs.JarFs.JarFsFile mockTestDir = mock(Fs.JarFs.JarFsFile.class);
    when(mockTestDir.toString()).thenReturn(JAR_PATH_ON_UNIX);
    when(mockTestDir.getName()).thenReturn(DRAWABLE_DIR);
    when(mockTestDir.listFiles()).thenReturn(new FsFile[]{mockTestFile});
    when(mockTestDir.isDirectory()).thenReturn(true);
    FsFile mockTestBaseDir = mock(FsFile.class);
    when(mockTestBaseDir.listFiles()).thenReturn(new FsFile[]{mockTestDir});
    ResourcePath mockResourcePath = mock(ResourcePath.class);
    field("resourceBase").ofType(FsFile.class).in(mockResourcePath).set(mockTestBaseDir);

    ResBundle<DrawableNode> bundle = mock(ResBundle.class);
    DrawableResourceLoader testLoader = new DrawableResourceLoader(bundle);
    testLoader.findDrawableResources(mockResourcePath);

    verify(bundle).put(eq("drawable"), eq("bar.png"), (DrawableNode) any(), (XmlLoader.XmlContext) any());
  }

  @Test
  public void shouldFindDrawableResourcesWorkWithUnixFilePath() throws Exception {
    setFileSeparator(UNIX_SEPARATOR);

    FileFsFile mockTestFile = mock(FileFsFile.class);
    when(mockTestFile.getName()).thenReturn("foo.png");
    when(mockTestFile.getBaseName()).thenReturn("bar.png");

    FileFsFile mockTestDir = mock(FileFsFile.class);
    when(mockTestDir.toString()).thenReturn(FILE_PATH_ON_UNIX);
    when(mockTestDir.getName()).thenReturn(DRAWABLE_DIR);
    when(mockTestDir.listFiles()).thenReturn(new FsFile[]{mockTestFile});
    when(mockTestDir.isDirectory()).thenReturn(true);
    FsFile mockTestBaseDir = mock(FsFile.class);
    when(mockTestBaseDir.listFiles()).thenReturn(new FsFile[]{mockTestDir});
    ResourcePath mockResourcePath = mock(ResourcePath.class);
    field("resourceBase").ofType(FsFile.class).in(mockResourcePath).set(mockTestBaseDir);

    ResBundle<DrawableNode> bundle = mock(ResBundle.class);
    DrawableResourceLoader testLoader = new DrawableResourceLoader(bundle);
    testLoader.findDrawableResources(mockResourcePath);

    verify(bundle).put(eq("drawable"), eq("bar.png"), (DrawableNode) any(), (XmlLoader.XmlContext) any());
  }

  @Test
  public void shouldFindDrawableResourcesWorkWithWindowsJarFilePath() throws Exception {
    setFileSeparator(WINDOWS_SEPARATOR);

    Fs.JarFs.JarFsFile mockTestFile = mock(Fs.JarFs.JarFsFile.class);
    when(mockTestFile.getName()).thenReturn("foo.png");
    when(mockTestFile.getBaseName()).thenReturn("bar.png");

    Fs.JarFs.JarFsFile mockTestDir = mock(Fs.JarFs.JarFsFile.class);
    when(mockTestDir.toString()).thenReturn(JAR_PATH_ON_WINDOWS);
    when(mockTestDir.getName()).thenReturn(DRAWABLE_DIR);
    when(mockTestDir.listFiles()).thenReturn(new FsFile[]{mockTestFile});
    when(mockTestDir.isDirectory()).thenReturn(true);
    FsFile mockTestBaseDir = mock(FsFile.class);
    when(mockTestBaseDir.listFiles()).thenReturn(new FsFile[]{mockTestDir});
    ResourcePath mockResourcePath = mock(ResourcePath.class);
    field("resourceBase").ofType(FsFile.class).in(mockResourcePath).set(mockTestBaseDir);

    ResBundle<DrawableNode> bundle = mock(ResBundle.class);
    DrawableResourceLoader testLoader = new DrawableResourceLoader(bundle);
    testLoader.findDrawableResources(mockResourcePath);

    verify(bundle).put(eq("drawable"), eq("bar.png"), (DrawableNode) any(), (XmlLoader.XmlContext) any());
  }

  @Test
  public void shouldFindDrawableResourcesWorkWithWindowsFilePath() throws Exception {
    setFileSeparator(WINDOWS_SEPARATOR);

    FileFsFile mockTestFile = mock(FileFsFile.class);
    when(mockTestFile.getName()).thenReturn("foo.png");
    when(mockTestFile.getBaseName()).thenReturn("bar.png");

    FileFsFile mockTestDir = mock(FileFsFile.class);
    when(mockTestDir.toString()).thenReturn(FILE_PATH_ON_WINDOWS);
    when(mockTestDir.getName()).thenReturn(DRAWABLE_DIR);
    when(mockTestDir.listFiles()).thenReturn(new FsFile[]{mockTestFile});
    when(mockTestDir.isDirectory()).thenReturn(true);
    FsFile mockTestBaseDir = mock(FsFile.class);
    when(mockTestBaseDir.listFiles()).thenReturn(new FsFile[]{mockTestDir});
    ResourcePath mockResourcePath = mock(ResourcePath.class);
    field("resourceBase").ofType(FsFile.class).in(mockResourcePath).set(mockTestBaseDir);

    ResBundle<DrawableNode> bundle = mock(ResBundle.class);
    DrawableResourceLoader testLoader = new DrawableResourceLoader(bundle);
    testLoader.findDrawableResources(mockResourcePath);

    verify(bundle).put(eq("drawable"), eq("bar.png"), (DrawableNode) any(), (XmlLoader.XmlContext) any());
  }

  private void setFileSeparator(String separator) throws Exception {
    originalSeparator = (String) Robolectric.Reflection.setFinalStaticField(File.class.getDeclaredField("separator"), separator);
  }
}
