package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static com.google.common.truth.Truth.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.content.Context;
import android.os.DropBoxManager;
import android.os.DropBoxManager.Entry;
import android.os.SystemClock;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

/** Unit tests for {@see ShadowDropboxManager}. */
@RunWith(AndroidJUnit4.class)
public class ShadowDropBoxManagerTest {

  private static final String TAG = "TAG";
  private static final String ANOTHER_TAG = "ANOTHER_TAG";
  private static final byte[] DATA = "HELLO WORLD".getBytes(UTF_8);

  private DropBoxManager manager;
  private ShadowDropBoxManager shadowDropBoxManager;

  @Before
  public void setup() {
    manager =
        (DropBoxManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.DROPBOX_SERVICE);
    shadowDropBoxManager = shadowOf(manager);
  }

  @Test
  public void emptyDropbox() {
    assertThat(manager.getNextEntry(null, 0)).isNull();
  }

  @Test
  public void dataExpected() throws Exception {
    shadowDropBoxManager.addData(TAG, 1, DATA);

    Entry entry = manager.getNextEntry(null, 0);
    assertThat(entry).isNotNull();
    assertThat(entry.getTag()).isEqualTo(TAG);
    assertThat(entry.getTimeMillis()).isEqualTo(1);
    assertThat(new BufferedReader(new InputStreamReader(entry.getInputStream(), UTF_8)).readLine())
        .isEqualTo(new String(DATA, UTF_8));
    assertThat(entry.getText(100)).isEqualTo(new String(DATA, UTF_8));
  }

  /** Checks that we retrieve the first entry <em>after</em> the specified time. */
  @Test
  public void dataNotExpected_timestampSameAsEntry() {
    shadowDropBoxManager.addData(TAG, 1, DATA);

    assertThat(manager.getNextEntry(null, 1)).isNull();
  }

  @Test
  public void dataNotExpected_timestampAfterEntry() {
    shadowDropBoxManager.addData(TAG, 1, DATA);

    assertThat(manager.getNextEntry(null, 2)).isNull();
  }

  @Test
  public void dataNotExpected_wrongTag() {
    shadowDropBoxManager.addData(TAG, 1, DATA);

    assertThat(manager.getNextEntry(ANOTHER_TAG, 0)).isNull();
  }

  @Test
  public void dataExpectedWithSort() {
    shadowDropBoxManager.addData(TAG, 3, DATA);
    shadowDropBoxManager.addData(TAG, 1, new byte[] {(byte) 0x0});

    Entry entry = manager.getNextEntry(null, 2);
    assertThat(entry).isNotNull();
    assertThat(entry.getText(100)).isEqualTo(new String(DATA, UTF_8));
    assertThat(entry.getTimeMillis()).isEqualTo(3);
  }

  @Test
  public void resetClearsData() {
    shadowDropBoxManager.addData(TAG, 1, DATA);

    ShadowDropBoxManager.reset();

    assertThat(manager.getNextEntry(null, 0)).isNull();
  }

  @Test
  public void testAddText() {
    long baseTimestamp = 55000L;
    ShadowSystemClock.advanceBy(55000 - SystemClock.uptimeMillis(), TimeUnit.MILLISECONDS);
    manager.addText(TAG, "HELLO WORLD");
    ShadowSystemClock.advanceBy(100, TimeUnit.MILLISECONDS);
    manager.addText(TAG, "GOODBYE WORLD");

    Entry entry = manager.getNextEntry(null, 0);
    assertThat(entry).isNotNull();
    assertThat(entry.getText(1024)).isEqualTo("HELLO WORLD");
    assertThat(entry.getTimeMillis()).isEqualTo(baseTimestamp);

    entry = manager.getNextEntry(null, baseTimestamp + 1);
    assertThat(entry.getText(1024)).isEqualTo("GOODBYE WORLD");
    assertThat(entry.getTimeMillis()).isEqualTo(baseTimestamp + 100);

    assertThat(manager.getNextEntry(null, baseTimestamp + 99)).isNotNull();
    assertThat(manager.getNextEntry(null, baseTimestamp + 100)).isNull();
  }

  @Test
  @Config(minSdk = O)
  public void dropBoxManager_activityContextEnabled_differentInstancesVerifyTagEnabled() {
    String originalProperty = System.getProperty("robolectric.createActivityContexts", "");
    System.setProperty("robolectric.createActivityContexts", "true");
    try (ActivityController<Activity> controller =
        Robolectric.buildActivity(Activity.class).setup()) {
      DropBoxManager applicationDropBoxManager =
          (DropBoxManager)
              ApplicationProvider.getApplicationContext().getSystemService(Context.DROPBOX_SERVICE);

      String tag = "testTag";
      String data = "testData";
      applicationDropBoxManager.addText(tag, data);

      Activity activity = controller.get();
      DropBoxManager activityDropBoxManager =
          (DropBoxManager) activity.getSystemService(Context.DROPBOX_SERVICE);

      boolean applicationTagEnabled = applicationDropBoxManager.isTagEnabled(tag);
      boolean activityTagEnabled = activityDropBoxManager.isTagEnabled(tag);

      assertThat(activityTagEnabled).isEqualTo(applicationTagEnabled);
    } finally {
      System.setProperty("robolectric.createActivityContexts", originalProperty);
    }
  }
}
