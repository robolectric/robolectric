package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
public class ShadowContextTest {
  private final Context context = RuntimeEnvironment.application;

  @Before
  public void setUp() throws Exception {
    File dataDir = new File(context.getPackageManager()
        .getPackageInfo("org.robolectric", 0).applicationInfo.dataDir);

    File[] files = dataDir.listFiles();
    assertThat(files)
      .isNotNull()
      .isEmpty();
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void createConfigurationContext() {
    assertThat(RuntimeEnvironment.application.createConfigurationContext(new Configuration())).isNotNull();
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
    File dataDir = new File(context.getPackageManager()
        .getPackageInfo("org.robolectric", 0).applicationInfo.dataDir, "data");

    assertThat(dataDir).doesNotExist();

    dataDir = context.getDir("data", Context.MODE_PRIVATE);
    assertThat(dataDir)
      .isNotNull()
      .exists();
  }

  @Test
  public void shouldStubThemeStuff() throws Exception {
    assertThat(context.obtainStyledAttributes(new int[0])).isNotNull();
    assertThat(context.obtainStyledAttributes(0, new int[0])).isNotNull();
    assertThat(context.obtainStyledAttributes(null, new int[0])).isNotNull();
    assertThat(context.obtainStyledAttributes(null, new int[0], 0, 0)).isNotNull();
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
      fos.write("test".getBytes(UTF_8));
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
      fos.write("test".getBytes(UTF_8));
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
    try (Writer fileWriter = Files.newBufferedWriter(file.toPath(), UTF_8)) {
      fileWriter.write(fileContents);
    }

    try (FileInputStream fileInputStream = context.openFileInput("__test__")) {
      byte[] bytes = new byte[fileContents.length()];
      fileInputStream.read(bytes);
      assertThat(bytes).isEqualTo(fileContents.getBytes(UTF_8));
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
      fileOutputStream.write(fileContents.getBytes(UTF_8));
    }
    try (FileInputStream fileInputStream = new FileInputStream(new File(context.getFilesDir(), file.getName()))) {
      byte[] readBuffer = new byte[fileContents.length()];
      fileInputStream.read(readBuffer);
      assertThat(new String(readBuffer, UTF_8)).isEqualTo(fileContents);
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
      fileOutputStream.write(initialFileContents.getBytes(UTF_8));
    }
    try (FileOutputStream fileOutputStream = context.openFileOutput("__test__", Context.MODE_APPEND)) {
      fileOutputStream.write(appendedFileContents.getBytes(UTF_8));
    }
    try (FileInputStream fileInputStream = new FileInputStream(new File(context.getFilesDir(), file.getName()))) {
      byte[] readBuffer = new byte[finalFileContents.length()];
      fileInputStream.read(readBuffer);
      assertThat(new String(readBuffer, UTF_8)).isEqualTo(finalFileContents);
    }
  }

  @Test
  public void openFileOutput_shouldOverwriteData() throws Exception {
    File file = new File("__test__");
    String initialFileContents = "foo";
    String newFileContents = "bar";
    try (FileOutputStream fileOutputStream = context.openFileOutput("__test__", 0)) {
      fileOutputStream.write(initialFileContents.getBytes(UTF_8));
    }
    try (FileOutputStream fileOutputStream = context.openFileOutput("__test__", 0)) {
      fileOutputStream.write(newFileContents.getBytes(UTF_8));
    }
    try (FileInputStream fileInputStream = new FileInputStream(new File(context.getFilesDir(), file.getName()))) {
      byte[] readBuffer = new byte[newFileContents.length()];
      fileInputStream.read(readBuffer);
      assertThat(new String(readBuffer, UTF_8)).isEqualTo(newFileContents);
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
    AttributeSet roboAttributeSet = Robolectric.buildAttributeSet()
        .addAttribute(R.attr.itemType, "ungulate")
        .addAttribute(R.attr.scrollBars, "horizontal|vertical")
        .addAttribute(R.attr.quitKeyCombo, "^q")
        .addAttribute(R.attr.aspectRatio, "1.5")
        .addAttribute(R.attr.aspectRatioEnabled, "true")
        .build();

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

  @Test
  public void whenStyleParentIsReference_obtainStyledAttributes_shouldResolveParent() throws Exception {
    RuntimeEnvironment.application.setTheme(R.style.Theme_ThemeReferredToByParentAttrReference);

    AttributeSet roboAttributeSet = Robolectric.buildAttributeSet()
        .setStyleAttribute("@style/Theme.ThemeWithAttrReferenceAsParent")
        .build();

    TypedArray a = context.obtainStyledAttributes(roboAttributeSet, new int[] { R.attr.string1, R.attr.string2 });
    assertThat(a.getString(0)).isEqualTo("string 1 from Theme.ThemeWithAttrReferenceAsParent");
    assertThat(a.getString(1)).isEqualTo("string 2 from StyleReferredToByParentAttrReference");
  }
}
