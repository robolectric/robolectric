package org.robolectric.shadows;

import android.content.Context;
import android.content.res.TypedArray;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.TestRunners;
import org.robolectric.res.Attribute;
import org.robolectric.res.PackageResourceLoader;
import org.robolectric.res.ResourceLoader;
import org.robolectric.util.TestUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.util.TestUtil.TEST_PACKAGE;
import static org.robolectric.util.TestUtil.TEST_RESOURCE_PATH;

@RunWith(TestRunners.MultiApiWithDefaults.class)
public class ShadowContextTest {
  private final Context context = RuntimeEnvironment.application;

  @Before
  public void setUp() throws Exception {
    File dataDir = new File(RuntimeEnvironment.getPackageManager()
        .getPackageInfo("org.robolectric", 0).applicationInfo.dataDir);

    File[] files = dataDir.listFiles();
    assertThat(files)
      .isNotNull()
      .isEmpty();
  }

  @Test
  public void shouldGetApplicationDataDirectory() throws IOException {
    File dataDir = context.getDir("data", Context.MODE_PRIVATE);
    assertThat(dataDir)
      .isNotNull()
      .exists();
  }

  @Test
  public void shouldCreateIfDoesNotExistAndGetApplicationDataDirectory() throws Exception {
    File dataDir = new File(RuntimeEnvironment.getPackageManager()
        .getPackageInfo("org.robolectric", 0).applicationInfo.dataDir, "data");

    assertThat(dataDir).doesNotExist();

    dataDir = context.getDir("data", Context.MODE_PRIVATE);
    assertThat(dataDir)
      .isNotNull()
      .exists();
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
    assertThat(context.getCacheDir()).exists();
  }

  @Test
  public void getExternalCacheDir_shouldCreateDirectory() throws Exception {
    assertThat(context.getExternalCacheDir()).exists();
  }

  @Test
  public void shouldWriteToCacheDir() throws Exception {
    assertThat(context.getCacheDir()).isNotNull();
    File cacheTest = new File(context.getCacheDir(), "__test__");

    assertThat(cacheTest.getAbsolutePath())
      .startsWith(System.getProperty("java.io.tmpdir"))
      .endsWith(File.separator + "__test__");

    try (FileOutputStream fos = new FileOutputStream(cacheTest)) {
      fos.write("test".getBytes());
    }
    assertThat(cacheTest).exists();
  }

  @Test
  public void shouldWriteToExternalCacheDir() throws Exception {
    assertThat(context.getExternalCacheDir()).isNotNull();
    File cacheTest = new File(context.getExternalCacheDir(), "__test__");

    assertThat(cacheTest.getAbsolutePath())
      .startsWith(System.getProperty("java.io.tmpdir"))
      .endsWith(File.separator + "__test__");

    try (FileOutputStream fos = new FileOutputStream(cacheTest)) {
      fos.write("test".getBytes());
    }

    assertThat(cacheTest).exists();
  }

  @Test
  public void getFilesDir_shouldCreateDirectory() throws Exception {
    assertThat(context.getFilesDir()).exists();
  }

  @Test
  public void fileList() throws Exception {
    assertThat(context.fileList()).isEqualTo(context.getFilesDir().list());
  }

  @Test
  public void getExternalFilesDir_shouldCreateDirectory() throws Exception {
    assertThat(context.getExternalFilesDir(null)).exists();
  }

  @Test
  public void getExternalFilesDir_shouldCreateNamedDirectory() throws Exception {
    File f = context.getExternalFilesDir("__test__");
    assertThat(f).exists();
    assertThat(f.getAbsolutePath()).endsWith("__test__");
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
      assertThat(dbFile).isEqualTo(new File(testDbName));
  }

  @Test
  public void openFileInput_shouldReturnAFileInputStream() throws Exception {
    String fileContents = "blah";

    File file = new File(context.getFilesDir(), "__test__");
    try (FileWriter fileWriter = new FileWriter(file)) {
      fileWriter.write(fileContents);
    }

    try (FileInputStream fileInputStream = context.openFileInput("__test__")) {
      byte[] bytes = new byte[fileContents.length()];
      fileInputStream.read(bytes);
      assertThat(bytes).isEqualTo(fileContents.getBytes());
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void openFileInput_shouldNotAcceptPathsWithSeparatorCharacters() throws Exception {
    try (FileInputStream fileInputStream = context.openFileInput("data" + File.separator + "test")) {}
  }

  @Test
  public void openFileOutput_shouldReturnAFileOutputStream() throws Exception {
    File file = new File("__test__");
    String fileContents = "blah";
    try (FileOutputStream fileOutputStream = context.openFileOutput("__test__", -1)) {
      fileOutputStream.write(fileContents.getBytes());
    }
    try (FileInputStream fileInputStream = new FileInputStream(new File(context.getFilesDir(), file.getName()))) {
      byte[] readBuffer = new byte[fileContents.length()];
      fileInputStream.read(readBuffer);
      assertThat(new String(readBuffer)).isEqualTo(fileContents);
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void openFileOutput_shouldNotAcceptPathsWithSeparatorCharacters() throws Exception {
    try (FileOutputStream fos = context.openFileOutput(File.separator + "data" + File.separator + "test" + File.separator + "hi", 0)) {}
  }

  @Test
  public void openFileOutput_shouldAppendData() throws Exception {
    File file = new File("__test__");
    String initialFileContents = "foo";
    String appendedFileContents = "bar";
    String finalFileContents = initialFileContents + appendedFileContents;
    try (FileOutputStream fileOutputStream = context.openFileOutput("__test__", Context.MODE_APPEND)) {
      fileOutputStream.write(initialFileContents.getBytes());
    }
    try (FileOutputStream fileOutputStream = context.openFileOutput("__test__", Context.MODE_APPEND)) {
      fileOutputStream.write(appendedFileContents.getBytes());
    }
    try (FileInputStream fileInputStream = new FileInputStream(new File(context.getFilesDir(), file.getName()))) {
      byte[] readBuffer = new byte[finalFileContents.length()];
      fileInputStream.read(readBuffer);
      assertThat(new String(readBuffer)).isEqualTo(finalFileContents);
    }
  }

  @Test
  public void openFileOutput_shouldOverwriteData() throws Exception {
    File file = new File("__test__");
    String initialFileContents = "foo";
    String newFileContents = "bar";
    try (FileOutputStream fileOutputStream = context.openFileOutput("__test__", 0)) {
      fileOutputStream.write(initialFileContents.getBytes());
    }
    try (FileOutputStream fileOutputStream = context.openFileOutput("__test__", 0)) {
      fileOutputStream.write(newFileContents.getBytes());
    }
    try (FileInputStream fileInputStream = new FileInputStream(new File(context.getFilesDir(), file.getName()))) {
      byte[] readBuffer = new byte[newFileContents.length()];
      fileInputStream.read(readBuffer);
      assertThat(new String(readBuffer)).isEqualTo(newFileContents);
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
    TestUtil.createResourcesFor(resourceLoader);

    RoboAttributeSet roboAttributeSet = new RoboAttributeSet(asList(
        new Attribute(TEST_PACKAGE + ":attr/itemType", "ungulate", TEST_PACKAGE),
        new Attribute(TEST_PACKAGE + ":attr/scrollBars", "horizontal|vertical", TEST_PACKAGE),
        new Attribute(TEST_PACKAGE + ":attr/quitKeyCombo", "^q", TEST_PACKAGE),
        new Attribute(TEST_PACKAGE + ":attr/aspectRatio", "1.5", TEST_PACKAGE),
        new Attribute(TEST_PACKAGE + ":attr/aspectRatioEnabled", "true", TEST_PACKAGE)
    ), resourceLoader);

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
