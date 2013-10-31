package org.robolectric.shadows;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.robolectric.res.Attribute;
import org.robolectric.res.PackageResourceLoader;
import org.robolectric.res.ResourceLoader;
import org.robolectric.util.CustomView;
import org.robolectric.util.TestUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import static java.util.Arrays.asList;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.util.TestUtil.TEST_PACKAGE;
import static org.robolectric.util.TestUtil.TEST_RESOURCE_PATH;

@RunWith(TestRunners.WithDefaults.class)
public class ContextTest {
  private Context context;

  @Before
  public void setUp() throws Exception {
    context = Robolectric.application;
    deleteDir(context.getFilesDir());
    deleteDir(context.getCacheDir());
    deleteDir(ShadowContext.DATABASE_DIR);

    File[] files = context.getFilesDir().listFiles();
    assertNotNull(files);
    assertThat(files.length).isEqualTo(0);

    File[] cachedFiles = context.getFilesDir().listFiles();
    assertNotNull(cachedFiles);
    assertThat(cachedFiles.length).isEqualTo(0);
  }

  @After
  public void after() {
    deleteDir(context.getFilesDir());
    deleteDir(context.getCacheDir());
    deleteDir(context.getExternalCacheDir());
    deleteDir(context.getExternalFilesDir(null));
    deleteDir(ShadowContext.DATABASE_DIR);
  }

  public void deleteDir(File path) {
    if (path.isDirectory()) {
      File[] files = path.listFiles();
      assertNotNull(files);
      for (File f : files) {
        deleteDir(f);
      }
    }
    path.delete();
  }

  @Test
  public void shouldGetApplicationDataDirectory() throws IOException {
    File dataDir = new File(ShadowContext.FILES_DIR, "data");
    assertThat(dataDir.mkdir()).isTrue();

    dataDir = context.getDir("data", Context.MODE_PRIVATE);
    assertThat(dataDir).isNotNull();
    assertThat(dataDir.exists()).isTrue();
  }


  @Test
  public void shouldCreateIfDoesNotExistAndGetApplicationDataDirectory() {
    File dataDir = new File(ShadowContext.FILES_DIR, "data");
    assertThat(dataDir.exists()).isFalse();

    dataDir = context.getDir("data", Context.MODE_PRIVATE);
    assertThat(dataDir).isNotNull();
    assertThat(dataDir.exists()).isTrue();
  }

  @Test
  public void shouldStubThemeStuff() throws Exception {
    assertThat(context.obtainStyledAttributes(null)).isNotNull();
    assertThat(context.obtainStyledAttributes(0, null)).isNotNull();
    assertThat(context.obtainStyledAttributes(null, null)).isNotNull();
    assertThat(context.obtainStyledAttributes(null, null, 0, 0)).isNotNull();
  }

  @Test
  public void getCacheDir_shouldCreateDirectory() throws Exception {
    assertTrue(context.getCacheDir().exists());
  }

  @Test
  public void getExternalCacheDir_shouldCreateDirectory() throws Exception {
    assertTrue(context.getExternalCacheDir().exists());
  }

  @Test
  public void shouldWriteToCacheDir() throws Exception {
    assertNotNull(context.getCacheDir());
    File cacheTest = new File(context.getCacheDir(), "__test__");

    assertThat(cacheTest.getAbsolutePath()).startsWith(System.getProperty("java.io.tmpdir"));
    assertThat(cacheTest.getAbsolutePath()).contains("android-cache");
    assertThat(cacheTest.getAbsolutePath()).endsWith(File.separator + "__test__");

    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(cacheTest);
      fos.write("test".getBytes());
    } finally {
      if (fos != null)
        fos.close();
    }
    assertTrue(cacheTest.exists());
  }

  @Test
  public void shouldWriteToExternalCacheDir() throws Exception {
    assertNotNull(context.getExternalCacheDir());
    File cacheTest = new File(context.getExternalCacheDir(), "__test__");

    assertThat(cacheTest.getAbsolutePath()).startsWith(System.getProperty("java.io.tmpdir"));
    assertThat(cacheTest.getAbsolutePath()).contains("android-external-cache");
    assertThat(cacheTest.getAbsolutePath()).endsWith(File.separator + "__test__");

    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(cacheTest);
      fos.write("test".getBytes());
    } finally {
      if (fos != null)
        fos.close();
    }

    assertTrue(cacheTest.exists());
  }

  @Test
  public void getFilesDir_shouldCreateDirectory() throws Exception {
    assertTrue(context.getFilesDir().exists());
  }

  @Test
  public void fileList() throws Exception {
    assertThat(context.fileList()).isEqualTo(context.getFilesDir().list());
  }

  @Test
  public void getExternalFilesDir_shouldCreateDirectory() throws Exception {
    assertTrue(context.getExternalFilesDir(null).exists());
  }

  @Test
  public void getExternalFilesDir_shouldCreateNamedDirectory() throws Exception {
    File f = context.getExternalFilesDir("__test__");
    assertTrue(f.exists());
    assertTrue(f.getAbsolutePath().endsWith("__test__"));
  }

  @Test
  public void getDatabasePath_shouldCreateDirectory() {
    assertFalse(ShadowContext.DATABASE_DIR.exists());
    String testDBName = "abc.db";
    File dbFile = context.getDatabasePath(testDBName);
    assertTrue(ShadowContext.DATABASE_DIR.exists());
    assertEquals(ShadowContext.DATABASE_DIR, dbFile.getParentFile());
  }

  @Test
  public void getDatabasePath_shouldAllowAbsolutePaths() throws Exception {
      String testDbName;

      if (System.getProperty("os.name").startsWith("Windows")) {
        testDbName = "C:\\absolute\\full\\path\\to\\db\\abc.db";
      } else {
        testDbName = "/absolute/full/path/to/db/abc.db";
      }
      File dbFile = context.getDatabasePath(testDbName);
      assertEquals(dbFile, new File(testDbName));
  }

  @Test
  public void openFileInput_shouldReturnAFileInputStream() throws Exception {
    String fileContents = "blah";

    File file = new File(context.getFilesDir(), "__test__");
    FileWriter fileWriter = new FileWriter(file);
    fileWriter.write(fileContents);
    fileWriter.close();

    FileInputStream fileInputStream = null;
    try {
      fileInputStream = context.openFileInput("__test__");

      byte[] bytes = new byte[fileContents.length()];
      fileInputStream.read(bytes);
      assertThat(bytes).isEqualTo(fileContents.getBytes());
    } finally {
      if (fileInputStream != null)
        fileInputStream.close();
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void openFileInput_shouldNotAcceptPathsWithSeparatorCharacters() throws Exception {
    FileInputStream fileInputStream = null;
    try {
      fileInputStream = context.openFileInput("data" + File.separator + "test");
    } finally {
      if (fileInputStream != null)
        fileInputStream.close();
    }
  }

  @Test
  public void openFileOutput_shouldReturnAFileOutputStream() throws Exception {
    File file = new File("__test__");
    String fileContents = "blah";
    FileOutputStream fileOutputStream = null;
    try {
      fileOutputStream = context.openFileOutput("__test__", -1);
      fileOutputStream.write(fileContents.getBytes());
    } finally {
      if (fileOutputStream != null)
        fileOutputStream.close();
    }
    FileInputStream fileInputStream = null;
    try {
      fileInputStream = new FileInputStream(new File(context.getFilesDir(), file.getName()));
      byte[] readBuffer = new byte[fileContents.length()];
      fileInputStream.read(readBuffer);
      assertThat(new String(readBuffer)).isEqualTo(fileContents);
    } finally {
      if (fileInputStream != null)
        fileInputStream.close();
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void openFileOutput_shouldNotAcceptPathsWithSeparatorCharacters() throws Exception {
    FileOutputStream fos = null;
    try {
      fos = context.openFileOutput(File.separator + "data" + File.separator + "test" + File.separator + "hi", 0);
    } finally {
      if (fos != null)
        fos.close();
    }
  }

  @Test
  public void deleteFile_shouldReturnTrue() throws IOException {
    File filesDir = context.getFilesDir();
    File file = new File(filesDir, "test.txt");
    boolean successfully = file.createNewFile();
    assertThat(successfully).isTrue();
    successfully = context.deleteFile(file.getName());
    assertThat(successfully).isTrue();
  }

  @Test
  public void deleteFile_shouldReturnFalse() throws IOException {
    File filesDir = context.getFilesDir();
    File file = new File(filesDir, "test.txt");
    boolean successfully = context.deleteFile(file.getName());
    assertThat(successfully).isFalse();
  }

  @Test
  public void obtainStyledAttributes_shouldExtractAttributesFromAttributeSet() throws Exception {
    ResourceLoader resourceLoader = new PackageResourceLoader(TEST_RESOURCE_PATH);
    Resources resources = TestUtil.createResourcesFor(resourceLoader);

    RoboAttributeSet roboAttributeSet = new RoboAttributeSet(asList(
        new Attribute(TEST_PACKAGE + ":attr/itemType", "ungulate", TEST_PACKAGE),
        new Attribute(TEST_PACKAGE + ":attr/scrollBars", "horizontal|vertical", TEST_PACKAGE),
        new Attribute(TEST_PACKAGE + ":attr/quitKeyCombo", "^q", TEST_PACKAGE),
        new Attribute(TEST_PACKAGE + ":attr/aspectRatio", "1.5", TEST_PACKAGE),
        new Attribute(TEST_PACKAGE + ":attr/aspectRatioEnabled", "true", TEST_PACKAGE)
    ), resources, CustomView.class);

    TypedArray a = context.obtainStyledAttributes(roboAttributeSet, R.styleable.CustomView);
    assertThat(a.getInt(R.styleable.CustomView_itemType, -1234)).isEqualTo(1 /* ungulate */);
    assertThat(a.getInt(R.styleable.CustomView_scrollBars, -1234)).isEqualTo(0x300);
    assertThat(a.getString(R.styleable.CustomView_quitKeyCombo)).isEqualTo("^q");
    assertThat(a.getText(R.styleable.CustomView_quitKeyCombo).toString()).isEqualTo("^q");
    assertThat(a.getFloat(R.styleable.CustomView_aspectRatio, 1f)).isEqualTo(1.5f);
    assertThat(a.getBoolean(R.styleable.CustomView_aspectRatioEnabled, false)).isTrue();

    TypedArray typedArray = context.obtainStyledAttributes(roboAttributeSet, new int[]{R.attr.quitKeyCombo, R.attr.itemType});
    assertThat(typedArray.getString(0)).isEqualTo("^q");
    assertThat(typedArray.getInt(1, -1234)).isEqualTo(1 /* ungulate */);
  }
}
