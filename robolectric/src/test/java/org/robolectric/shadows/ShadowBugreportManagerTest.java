package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.util.concurrent.MoreExecutors.directExecutor;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.robolectric.shadows.ShadowLooper.shadowMainLooper;

import android.content.Context;
import android.os.BugreportManager.BugreportCallback;
import android.os.BugreportParams;
import android.os.ParcelFileDescriptor;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.File;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

/** Tests for {@link ShadowBugreportManager}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = Q)
public final class ShadowBugreportManagerTest {

  private ShadowBugreportManager shadowBugreportManager;
  private final Context context = ApplicationProvider.getApplicationContext();

  @Before
  public void setUp() {
    shadowBugreportManager = Shadow.extract(context.getSystemService(Context.BUGREPORT_SERVICE));
  }

  @Test
  public void requestBugreport() {
    shadowBugreportManager.requestBugreport(
        new BugreportParams(BugreportParams.BUGREPORT_MODE_INTERACTIVE), "title", "description");

    assertThat(shadowBugreportManager.wasBugreportRequested()).isTrue();
  }

  @Test
  public void startBugreport() throws Exception {
    BugreportCallback callback = mock(BugreportCallback.class);
    shadowBugreportManager.startBugreport(
        createWriteFile("bugreport"),
        createWriteFile("screenshot"),
        new BugreportParams(BugreportParams.BUGREPORT_MODE_FULL),
        directExecutor(),
        callback);
    shadowMainLooper().idle();

    assertThat(shadowBugreportManager.isBugreportInProgress()).isTrue();
    verify(callback, never()).onFinished();
    verify(callback, never()).onError(anyInt());
  }

  @Test
  public void startBugreport_noPermission() throws Exception {
    BugreportCallback callback = mock(BugreportCallback.class);
    shadowBugreportManager.setHasPermission(false);

    // TODO(b/179958637) switch to assertThrows once ThrowingRunnable no longer causes a test
    // instantiation failure.
    try {
      shadowBugreportManager.startBugreport(
          createWriteFile("bugreport"),
          createWriteFile("screenshot"),
          new BugreportParams(BugreportParams.BUGREPORT_MODE_FULL),
          directExecutor(),
          callback);
      fail("Expected SecurityException");
    } catch (SecurityException expected) {
    }
    shadowMainLooper().idle();

    assertThat(shadowBugreportManager.isBugreportInProgress()).isFalse();
    verifyNoMoreInteractions(callback);
  }

  @Test
  public void startTwoBugreports() throws Exception {
    BugreportCallback callback = mock(BugreportCallback.class);
    shadowBugreportManager.startBugreport(
        createWriteFile("bugreport"),
        createWriteFile("screenshot"),
        new BugreportParams(BugreportParams.BUGREPORT_MODE_FULL),
        directExecutor(),
        callback);
    shadowMainLooper().idle();

    assertThat(shadowBugreportManager.isBugreportInProgress()).isTrue();
    verify(callback, never()).onFinished();
    verify(callback, never()).onError(anyInt());

    BugreportCallback newCallback = mock(BugreportCallback.class);
    shadowBugreportManager.startBugreport(
        createWriteFile("bugreport_new"),
        createWriteFile("screenshot_new"),
        new BugreportParams(BugreportParams.BUGREPORT_MODE_FULL),
        directExecutor(),
        newCallback);
    shadowMainLooper().idle();

    assertThat(shadowBugreportManager.isBugreportInProgress()).isTrue();
    verify(newCallback).onError(BugreportCallback.BUGREPORT_ERROR_ANOTHER_REPORT_IN_PROGRESS);
    verify(callback, never()).onFinished();
    verify(callback, never()).onError(anyInt());

    shadowBugreportManager.executeOnFinished();
    shadowMainLooper().idle();

    assertThat(shadowBugreportManager.isBugreportInProgress()).isFalse();
    verify(callback).onFinished();
    verify(callback, never()).onError(anyInt());
  }

  @Test
  public void cancelBugreport() throws Exception {
    BugreportCallback callback = mock(BugreportCallback.class);
    shadowBugreportManager.startBugreport(
        createWriteFile("bugreport"),
        createWriteFile("screenshot"),
        new BugreportParams(BugreportParams.BUGREPORT_MODE_FULL),
        directExecutor(),
        callback);
    shadowMainLooper().idle();

    assertThat(shadowBugreportManager.isBugreportInProgress()).isTrue();
    verify(callback, never()).onFinished();
    verify(callback, never()).onError(anyInt());

    shadowBugreportManager.cancelBugreport();
    shadowMainLooper().idle();

    assertThat(shadowBugreportManager.isBugreportInProgress()).isFalse();
    verify(callback).onError(BugreportCallback.BUGREPORT_ERROR_RUNTIME);
  }

  @Test
  public void cancelBugreport_noPermission() throws Exception {
    BugreportCallback callback = mock(BugreportCallback.class);
    shadowBugreportManager.startBugreport(
        createWriteFile("bugreport"),
        createWriteFile("screenshot"),
        new BugreportParams(BugreportParams.BUGREPORT_MODE_FULL),
        directExecutor(),
        callback);
    shadowMainLooper().idle();
    // Loss of permission between start and cancel is theoretically possible, particularly if using
    // carrier privileges instead of DUMP.
    shadowBugreportManager.setHasPermission(false);

    assertThat(shadowBugreportManager.isBugreportInProgress()).isTrue();

    assertThrows(SecurityException.class, shadowBugreportManager::cancelBugreport);
    shadowMainLooper().idle();

    assertThat(shadowBugreportManager.isBugreportInProgress()).isTrue();
    verifyNoMoreInteractions(callback);
  }

  @Test
  public void executeOnError() throws Exception {
    BugreportCallback callback = mock(BugreportCallback.class);
    shadowBugreportManager.startBugreport(
        createWriteFile("bugreport"),
        createWriteFile("screenshot"),
        new BugreportParams(BugreportParams.BUGREPORT_MODE_FULL),
        directExecutor(),
        callback);
    shadowMainLooper().idle();

    assertThat(shadowBugreportManager.isBugreportInProgress()).isTrue();
    verify(callback, never()).onFinished();
    verify(callback, never()).onError(anyInt());

    shadowBugreportManager.executeOnError(BugreportCallback.BUGREPORT_ERROR_INVALID_INPUT);
    shadowMainLooper().idle();

    assertThat(shadowBugreportManager.isBugreportInProgress()).isFalse();
    verify(callback).onError(BugreportCallback.BUGREPORT_ERROR_INVALID_INPUT);
  }

  @Test
  public void executeOnFinished() throws Exception {
    BugreportCallback callback = mock(BugreportCallback.class);
    shadowBugreportManager.startBugreport(
        createWriteFile("bugreport"),
        createWriteFile("screenshot"),
        new BugreportParams(BugreportParams.BUGREPORT_MODE_FULL),
        directExecutor(),
        callback);
    shadowMainLooper().idle();

    assertThat(shadowBugreportManager.isBugreportInProgress()).isTrue();
    verify(callback, never()).onFinished();
    verify(callback, never()).onError(anyInt());

    shadowBugreportManager.executeOnFinished();
    shadowMainLooper().idle();

    assertThat(shadowBugreportManager.isBugreportInProgress()).isFalse();
    verify(callback).onFinished();
    verify(callback, never()).onError(anyInt());
  }

  @Test
  public void executeOnProgress() throws Exception {
    // Not reported without a callback attached.
    shadowBugreportManager.executeOnProgress(0.0f);

    BugreportCallback callback = mock(BugreportCallback.class);
    shadowBugreportManager.startBugreport(
        createWriteFile("bugreport"),
        createWriteFile("screenshot"),
        new BugreportParams(BugreportParams.BUGREPORT_MODE_FULL),
        directExecutor(),
        callback);
    shadowMainLooper().idle();

    assertThat(shadowBugreportManager.isBugreportInProgress()).isTrue();
    verify(callback, never()).onProgress(anyFloat());
    verify(callback, never()).onFinished();
    verify(callback, never()).onError(anyInt());

    shadowBugreportManager.executeOnProgress(50.0f);
    shadowMainLooper().idle();

    assertThat(shadowBugreportManager.isBugreportInProgress()).isTrue();
    verify(callback).onProgress(50.0f);
    verify(callback, never()).onFinished();
    verify(callback, never()).onError(anyInt());

    shadowBugreportManager.executeOnFinished();
    shadowMainLooper().idle();
    // Won't be reported after the callback is notified with #onFinished.
    shadowBugreportManager.executeOnProgress(101.0f);
    shadowMainLooper().idle();

    assertThat(shadowBugreportManager.isBugreportInProgress()).isFalse();
    verify(callback).onFinished();
    verify(callback, never()).onError(anyInt());
    verifyNoMoreInteractions(callback);
  }

  @Test
  public void isBugreportInProgress() throws Exception {
    assertThat(shadowBugreportManager.isBugreportInProgress()).isFalse();

    BugreportCallback callback = mock(BugreportCallback.class);
    shadowBugreportManager.startBugreport(
        createWriteFile("bugreport"),
        createWriteFile("screenshot"),
        new BugreportParams(BugreportParams.BUGREPORT_MODE_FULL),
        directExecutor(),
        callback);
    shadowMainLooper().idle();

    assertThat(shadowBugreportManager.isBugreportInProgress()).isTrue();
    verify(callback, never()).onFinished();
    verify(callback, never()).onError(anyInt());

    shadowBugreportManager.executeOnFinished();
    shadowMainLooper().idle();

    assertThat(shadowBugreportManager.isBugreportInProgress()).isFalse();
    verify(callback).onFinished();
    verify(callback, never()).onError(anyInt());
  }

  private ParcelFileDescriptor createWriteFile(String fileName) throws IOException {
    File f = new File(context.getFilesDir(), fileName);
    if (f.exists()) {
      f.delete();
    }
    f.createNewFile();
    return ParcelFileDescriptor.open(
        f, ParcelFileDescriptor.MODE_WRITE_ONLY | ParcelFileDescriptor.MODE_APPEND);
  }
}
