package org.robolectric.shadows;

import android.app.Activity;
import android.content.res.AssetManager;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.robolectric.util.Strings;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.robolectric.util.TestUtil.joinPath;

@RunWith(TestRunners.WithDefaults.class)
public class AssetManagerTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  AssetManager assetManager;

  @Before
  public void setUp() throws Exception {
    assetManager = Robolectric.buildActivity(Activity.class).create().get().getAssets();
  }

  @Test
  public void assertGetAssetsNotNull() {
    assertNotNull(assetManager);

    assetManager = Robolectric.application.getAssets();
    assertNotNull(assetManager);

    assetManager = Robolectric.application.getResources().getAssets();
    assertNotNull(assetManager);
  }

  @Test
  public void assetsPathListing() throws IOException {
    List<String> files;
    String testPath;

    testPath = "";
    files = Arrays.asList(assetManager.list(testPath));
    assertTrue(files.contains("docs"));
    assertTrue(files.contains("assetsHome.txt"));

    testPath = "docs";
    files = Arrays.asList(assetManager.list(testPath));
    assertTrue(files.contains("extra"));

    testPath = joinPath("docs", "extra");
    files = Arrays.asList(assetManager.list(testPath));
    assertTrue(files.contains("testing"));

    testPath = joinPath("docs", "extra", "testing");
    files = Arrays.asList(assetManager.list(testPath));
    assertTrue(files.contains("hello.txt"));

    testPath = "assetsHome.txt";
    files = Arrays.asList(assetManager.list(testPath));
    assertFalse(files.contains(testPath));

    testPath = "bogus.file";
    files = Arrays.asList(assetManager.list(testPath));
    assertEquals(0, files.size());
  }

  @Test
  public void assetsInputStreams() throws IOException {
    String testPath;
    String fileContents;
    InputStream inputStream;

    testPath = "assetsHome.txt";
    inputStream = assetManager.open(testPath);
    fileContents = Strings.fromStream(inputStream);
    assertEquals("assetsHome!", fileContents);

    testPath = joinPath("docs", "extra", "testing", "hello.txt");
    inputStream = assetManager.open(testPath);
    fileContents = Strings.fromStream(inputStream);
    assertEquals("hello!", fileContents);
  }

  @Test
  public void openNonAssetShouldOpenRealAssetFromResources() throws IOException {
    InputStream inputStream = assetManager.openNonAsset(0, "./res/drawable/an_image.png", 0);

    ByteArrayInputStream byteArrayInputStream = (ByteArrayInputStream) inputStream;
    assertThat(byteArrayInputStream.available()).isEqualTo(6559);
  }

  @Test
  public void openNonAssetShouldOpenRealAssetFromAndroidJar() throws IOException {
    // Not the real full path (it's in .m2/repository), but it only cares about the last folder and file name
    final String jarFile = "jar:/android-all-4.3_r2-robolectric-0.jar!/res/drawable/overscroll_edge.png";

    InputStream inputStream = assetManager.openNonAsset(0, jarFile, 0);

    ByteArrayInputStream byteArrayInputStream = (ByteArrayInputStream) inputStream;
    assertThat(byteArrayInputStream.available()).isEqualTo(1345);
  }

  @Test
  public void openNonAssetShouldThrowExceptionWhenFileDoesNotExist() throws IOException {
    expectedException.expect(IOException.class);
    expectedException.expectMessage("Unable to find resource for ./res/drawable/does_not_exist.png");

    assetManager.openNonAsset(0, "./res/drawable/does_not_exist.png", 0);
  }

  @Test
  public void openNonAssetShouldOpenCorrectAssetBasedOnQualifierMdpi() throws IOException {
    Robolectric.shadowOf(assetManager).setQualifiers("mdpi");

    InputStream inputStream = assetManager.openNonAsset(0, "./res/drawable/robolectric.png", 0);

    ByteArrayInputStream byteArrayInputStream = (ByteArrayInputStream) inputStream;
    assertThat(byteArrayInputStream.available()).isEqualTo(8141);
  }

  @Test
  public void openNonAssetShouldOpenCorrectAssetBasedOnQualifierHdpi() throws IOException {
    Robolectric.shadowOf(assetManager).setQualifiers("hdpi");

    InputStream inputStream = assetManager.openNonAsset(0, "./res/drawable/robolectric.png", 0);

    ByteArrayInputStream byteArrayInputStream = (ByteArrayInputStream) inputStream;
    assertThat(byteArrayInputStream.available()).isEqualTo(23447);
  }
}
