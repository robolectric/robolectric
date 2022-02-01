package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;

import android.os.BugreportManager;
import android.os.BugreportManager.BugreportCallback;
import android.os.BugreportParams;
import android.os.ParcelFileDescriptor;
import androidx.annotation.Nullable;
import java.io.IOException;
import java.util.concurrent.Executor;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Implementation of {@link android.os.BugreportManager}.
 *
 * <p>This class is not available in the public Android SDK, but it is available for system apps.
 */
@Implements(value = BugreportManager.class, minSdk = Q, isInAndroidSdk = false)
public class ShadowBugreportManager {

  private boolean hasPermission = true;

  @Nullable private ParcelFileDescriptor bugreportFd;
  @Nullable private ParcelFileDescriptor screenshotFd;
  @Nullable private Executor executor;
  @Nullable private BugreportCallback callback;
  private boolean bugreportRequested;
  @Nullable private CharSequence shareTitle;
  @Nullable private CharSequence shareDescription;

  /**
   * Starts a bugreport with which can execute callback methods on the provided executor.
   *
   * <p>If bugreport already in progress, {@link BugreportCallback#onError} will be executed.
   */
  @Implementation
  protected void startBugreport(
      ParcelFileDescriptor bugreportFd,
      ParcelFileDescriptor screenshotFd,
      BugreportParams params,
      Executor executor,
      BugreportCallback callback) {
    enforcePermission("startBugreport");
    if (isBugreportInProgress()) {
      executor.execute(
          () -> callback.onError(BugreportCallback.BUGREPORT_ERROR_ANOTHER_REPORT_IN_PROGRESS));
    } else {
      this.bugreportFd = bugreportFd;
      this.screenshotFd = screenshotFd;
      this.executor = executor;
      this.callback = callback;
    }
  }

  /**
   * Normally requests the platform/system to take a bugreport and make the final bugreport
   * available to the user.
   *
   * <p>This implementation just sets a boolean recording that the method was invoked, and the share
   * title and description.
   */
  @Implementation(minSdk = R)
  protected void requestBugreport(
      BugreportParams params, CharSequence shareTitle, CharSequence shareDescription) {
    this.bugreportRequested = true;
    this.shareTitle = shareTitle;
    this.shareDescription = shareDescription;
  }

  /** Cancels bugreport in progress and executes {@link BugreportCallback#onError}. */
  @Implementation
  protected void cancelBugreport() {
    enforcePermission("cancelBugreport");
    executeOnError(BugreportCallback.BUGREPORT_ERROR_RUNTIME);
  }

  /** Executes {@link BugreportCallback#onProgress} on the provided Executor. */
  public void executeOnProgress(float progress) {
    if (isBugreportInProgress()) {
      BugreportCallback callback = this.callback;
      executor.execute(() -> callback.onProgress(progress));
    }
  }

  /** Executes {@link BugreportCallback#onError} on the provided Executor. */
  public void executeOnError(int errorCode) {
    if (isBugreportInProgress()) {
      BugreportCallback callback = this.callback;
      executor.execute(() -> callback.onError(errorCode));
    }
    resetParams();
  }

  /** Executes {@link BugreportCallback#onFinished} on the provided Executor. */
  public void executeOnFinished() {
    if (isBugreportInProgress()) {
      BugreportCallback callback = this.callback;
      executor.execute(callback::onFinished);
    }
    resetParams();
  }

  public boolean isBugreportInProgress() {
    return executor != null && callback != null;
  }

  /** Returns true if {@link #requestBugreport} was called. */
  public boolean wasBugreportRequested() {
    return bugreportRequested;
  }

  /**
   * Simulates if the calling process has the required permissions to call BugreportManager methods.
   *
   * <p>If {@code hasPermission} is false, {@link #startBugreport} and {@link #cancelBugreport} will
   * throw {@link SecurityException}.
   */
  public void setHasPermission(boolean hasPermission) {
    this.hasPermission = hasPermission;
  }

  private void enforcePermission(String message) {
    if (!hasPermission) {
      throw new SecurityException(message);
    }
  }

  /** Returns the title of the bugreport if set with {@code requestBugreport}, else null. */
  @Nullable
  public CharSequence getShareTitle() {
    return shareTitle;
  }

  /** Returns the description of the bugreport if set with {@code requestBugreport}, else null. */
  @Nullable
  public CharSequence getShareDescription() {
    return shareDescription;
  }

  private void resetParams() {
    try {
      bugreportFd.close();
      if (screenshotFd != null) {
        screenshotFd.close();
      }
    } catch (IOException e) {
      // ignore.
    }
    bugreportFd = null;
    screenshotFd = null;
    executor = null;
    callback = null;
    bugreportRequested = false;
    shareTitle = null;
    shareDescription = null;
  }
}
