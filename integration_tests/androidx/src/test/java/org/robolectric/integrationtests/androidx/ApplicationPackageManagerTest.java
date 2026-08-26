package org.robolectric.integrationtests.androidx;

import static com.google.common.truth.Truth.assertThat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import androidx.core.content.FileProvider;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests for PackageManager that are specific to Robolectric. */
@RunWith(AndroidJUnit4.class)
public final class ApplicationPackageManagerTest {

  private PackageManager packageManager =
      ApplicationProvider.getApplicationContext().getPackageManager();
  private final Context testContext = InstrumentationRegistry.getInstrumentation().getContext();
  private final String testPackageName = testContext.getPackageName();

  @SuppressWarnings("unchecked")
  @Before
  public void setUp() {
    // Clear the FileProvider cache.
    // In emulators, this is getting cleared by it being refreshed. In Robolectric, it's not
    // being cleared because it's the same JVM. This is a workaround to clear it.
    try {
      Field cacheField = FileProvider.class.getDeclaredField("sCache");
      cacheField.setAccessible(true);
      Map<String, Object> cache = (Map<String, Object>) cacheField.get(null);
      if (cache != null) {
        cache.clear();
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void testQueryIntentActivities_implicit_dataThenType_returnsActivity() throws IOException {
    Uri videoUri = createTempVideoFile();

    Intent intent = new Intent(Intent.ACTION_SEND);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
    intent.setData(videoUri);
    intent.setType("video/*");
    intent.putExtra(Intent.EXTRA_STREAM, videoUri);
    intent.setPackage(testPackageName);

    ActivityInfo activityInfo = intent.resolveActivityInfo(packageManager, 0);

    assertThat(activityInfo).isNotNull();
    assertThat(activityInfo.name)
        .isEqualTo(
            "org.robolectric.integrationtests.androidx.ApplicationPackageManagerTest$TestActivity");
  }

  @Test
  public void testQueryIntentActivities_implicit_typeThenData_returnsActivity() throws IOException {
    Uri videoUri = createTempVideoFile();

    Intent intent = new Intent(Intent.ACTION_SEND);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
    // Calling setType before setData causes the type be cleared, even on real Android.
    // Real android will use resolveTypeIfNeeded to find the type anyways.
    intent.setType("video/*");
    intent.setData(videoUri);
    intent.putExtra(Intent.EXTRA_STREAM, videoUri);
    intent.setPackage(testPackageName);

    ActivityInfo activityInfo = intent.resolveActivityInfo(packageManager, 0);

    assertThat(activityInfo).isNotNull();
    assertThat(activityInfo.name)
        .isEqualTo(
            "org.robolectric.integrationtests.androidx.ApplicationPackageManagerTest$TestActivity");
  }

  @Test
  public void testQueryIntentActivities_implicit_dataAndType_returnsActivity() throws IOException {

    Uri videoUri = createTempVideoFile();

    Intent intent = new Intent(Intent.ACTION_SEND);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
    // Calling setType before setData causes the type be cleared, even on real Android.
    // Real android will use resolveTypeIfNeeded to find the type anyways.
    intent.setDataAndType(videoUri, "video/*");
    intent.putExtra(Intent.EXTRA_STREAM, videoUri);
    intent.setPackage(testPackageName);

    ActivityInfo activityInfo = intent.resolveActivityInfo(packageManager, 0);

    assertThat(activityInfo).isNotNull();
    assertThat(activityInfo.name)
        .isEqualTo(
            "org.robolectric.integrationtests.androidx.ApplicationPackageManagerTest$TestActivity");
  }

  private Uri createTempVideoFile() throws IOException {
    Context context = ApplicationProvider.getApplicationContext();
    File videoFile = File.createTempFile("sample", ".mp4", context.getCacheDir());
    new FileOutputStream(videoFile).close();

    return FileProvider.getUriForFile(
        context, "org.robolectric.integrationtests.androidx.fileprovider", videoFile);
  }

  /**
   * Test activity for testing implicit intents. In the AndroidManifest, this has a DEFAULT intent
   * filter that requires ACTION_SEND and video/* mime type.
   */
  public static class TestActivity extends Activity {}
}
