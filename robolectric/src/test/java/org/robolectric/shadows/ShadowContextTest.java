package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static com.google.common.truth.Truth.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
public class ShadowContextTest {
  private final Context context = ApplicationProvider.getApplicationContext();

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void createConfigurationContext() {
    Configuration configuration = new Configuration(context.getResources().getConfiguration());
    configuration.mcc = 234;

    Context configurationContext = context.createConfigurationContext(configuration);

    assertThat(configurationContext).isNotNull();
  }

  @Test
  @Config(minSdk = VERSION_CODES.O)
  public void startForegroundService() {
    Intent intent = new Intent();
    context.startForegroundService(intent);
    assertThat(ShadowApplication.getInstance().getNextStartedService()).isEqualTo(intent);
  }

  @Test
  public void shouldGetApplicationDataDirectory() throws IOException {
    File dataDir = context.getDir("data", Context.MODE_PRIVATE);
    assertThat(dataDir.exists()).isTrue();
  }

  @Test
  public void shouldCreateIfDoesNotExistAndGetApplicationDataDirectory() throws Exception {
    File dataDir = new File(context.getPackageManager()
        .getPackageInfo("org.robolectric", 0).applicationInfo.dataDir, "data");

    assertThat(dataDir.exists()).isFalse();

    dataDir = context.getDir("data", Context.MODE_PRIVATE);
    assertThat(dataDir.exists()).isTrue();
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
    assertThat(context.getCacheDir().exists()).isTrue();
  }

  @Test
  public void getExternalCacheDir_shouldCreateDirectory() throws Exception {
    assertThat(context.getExternalCacheDir().exists()).isTrue();
  }

  @Test
  public void shouldWriteToCacheDir() throws Exception {
    assertThat(context.getCacheDir()).isNotNull();
    File cacheTest = new File(context.getCacheDir(), "__test__");

    assertThat(cacheTest.getAbsolutePath())
      .startsWith(System.getProperty("java.io.tmpdir"));
    assertThat(cacheTest.getAbsolutePath())
        .endsWith(File.separator + "__test__");

    try (FileOutputStream fos = new FileOutputStream(cacheTest)) {
      fos.write("test".getBytes(UTF_8));
    }
    assertThat(cacheTest.exists()).isTrue();
  }

  @Test
  public void shouldWriteToExternalCacheDir() throws Exception {
    assertThat(context.getExternalCacheDir()).isNotNull();
    File cacheTest = new File(context.getExternalCacheDir(), "__test__");

    assertThat(cacheTest.getAbsolutePath())
      .startsWith(System.getProperty("java.io.tmpdir"));
    assertThat(cacheTest.getAbsolutePath())
      .endsWith(File.separator + "__test__");

    try (FileOutputStream fos = new FileOutputStream(cacheTest)) {
      fos.write("test".getBytes(UTF_8));
    }

    assertThat(cacheTest.exists()).isTrue();
  }

  @Test
  public void getFilesDir_shouldCreateDirectory() throws Exception {
    assertThat(context.getFilesDir().exists()).isTrue();
  }

  @Test
  public void fileList() throws Exception {
    assertThat(context.fileList()).isEqualTo(context.getFilesDir().list());
  }

  @Test
  public void getExternalFilesDir_shouldCreateDirectory() throws Exception {
    assertThat(context.getExternalFilesDir(null).exists()).isTrue();
  }

  @Test
  public void getExternalFilesDir_shouldCreateNamedDirectory() throws Exception {
    File f = context.getExternalFilesDir("__test__");
    assertThat(f.exists()).isTrue();
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
    try (FileOutputStream fileOutputStream = context.openFileOutput("__test__", Context.MODE_PRIVATE)) {
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
}
