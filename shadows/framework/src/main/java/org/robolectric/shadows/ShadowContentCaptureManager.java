package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.os.ParcelFileDescriptor;
import android.view.contentcapture.ContentCaptureCondition;
import android.view.contentcapture.ContentCaptureManager;
import android.view.contentcapture.ContentCaptureManager.DataShareError;
import android.view.contentcapture.DataRemovalRequest;
import android.view.contentcapture.DataShareRequest;
import android.view.contentcapture.DataShareWriteAdapter;
import java.util.Set;
import java.util.concurrent.Executor;
import javax.annotation.Nullable;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** A Shadow for android.view.contentcapture.ContentCaptureManager added in Android R. */
@Implements(value = ContentCaptureManager.class, minSdk = Q, isInAndroidSdk = false)
public class ShadowContentCaptureManager {

  @Nullable private Set<ContentCaptureCondition> contentCaptureConditions;
  @Nullable private ComponentName serviceComponentName;
  private boolean isContentCaptureEnabled = false;
  @Nullable private ParcelFileDescriptor parcelFileDescriptor;
  @DataShareError private int dataShareErrorCode = -1;
  private boolean shouldRejectRequest = false;

  /**
   * Configures the set of {@link ContentCaptureCondition} that will be returned when calling {@link
   * #getContentCaptureConditions()}.
   */
  public void setContentCaptureConditions(Set<ContentCaptureCondition> contentCaptureConditions) {
    this.contentCaptureConditions = contentCaptureConditions;
  }

  /**
   * Configures the {@link ComponentName} that will be returned when calling {@link
   * #getServiceComponentName()}.
   */
  public void setServiceComponentName(ComponentName serviceComponentName) {
    this.serviceComponentName = serviceComponentName;
  }

  /** Configures whether {@link #isContentCaptureEnabled()} returns true or false. */
  public void setIsContentCaptureEnabled(boolean isContentCaptureEnabled) {
    this.isContentCaptureEnabled = isContentCaptureEnabled;
  }

  /**
   * Configures {@link DataShareError} to be raised on calls to {@link #shareData(DataShareRequest,
   * Executor, DataShareWriteAdapter)}.
   */
  @TargetApi(R)
  public void setDataShareErrorCode(@DataShareError int dataShareErrorCode) {
    this.dataShareErrorCode = dataShareErrorCode;
  }

  /**
   * Configures whether or not to raise request rejection on calls to {@link
   * #shareData(DataShareRequest, Executor, DataShareWriteAdapter)}.
   */
  @TargetApi(R)
  public void setShouldRejectRequest(boolean shouldRejectRequest) {
    this.shouldRejectRequest = shouldRejectRequest;
  }

  /**
   * Configures the {@link ParcelFileDescriptor} that {@link
   * DataShareWriteAdapter#onWrite(ParcelFileDescriptor)} will receive on calls to {@link
   * #shareData(DataShareRequest, Executor, DataShareWriteAdapter)}.
   */
  @TargetApi(R)
  public void setShareDataParcelFileDescriptor(ParcelFileDescriptor parcelFileDescriptor) {
    this.parcelFileDescriptor = parcelFileDescriptor;
  }

  @Implementation
  protected Set<ContentCaptureCondition> getContentCaptureConditions() {
    return contentCaptureConditions;
  }

  @Implementation
  protected ComponentName getServiceComponentName() {
    return serviceComponentName;
  }

  @Implementation
  protected boolean isContentCaptureEnabled() {
    return isContentCaptureEnabled;
  }

  @Implementation
  protected void setContentCaptureEnabled(boolean enabled) {
    isContentCaptureEnabled = enabled;
  }

  @Implementation
  protected void removeData(DataRemovalRequest request) {}

  @Implementation(minSdk = R)
  protected void shareData(
      DataShareRequest request, Executor executor, DataShareWriteAdapter dataShareWriteAdapter) {
    if (shouldRejectRequest) {
      dataShareWriteAdapter.onRejected();
      return;
    }

    if (dataShareErrorCode >= 0) {
      dataShareWriteAdapter.onError(dataShareErrorCode);
      return;
    }

    dataShareWriteAdapter.onWrite(parcelFileDescriptor);
  }
}
