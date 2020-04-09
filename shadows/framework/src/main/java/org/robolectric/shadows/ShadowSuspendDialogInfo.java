package org.robolectric.shadows;

import static org.robolectric.shadow.api.Shadow.directlyOn;

import android.annotation.DrawableRes;
import android.annotation.StringRes;
import android.content.pm.SuspendDialogInfo;
import android.os.Build;
import javax.annotation.Nullable;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

/** Shadow of {@link SuspendDialogInfo} to expose hidden methods. */
@Implements(value = SuspendDialogInfo.class, isInAndroidSdk = false, minSdk = Build.VERSION_CODES.Q)
public class ShadowSuspendDialogInfo {

  @RealObject protected SuspendDialogInfo realInfo;

  /** Returns the resource id of the icon to be used with the dialog. */
  @Implementation
  @HiddenApi
  @DrawableRes
  public int getIconResId() {
    return directly().getIconResId();
  }

  /** Returns the resource id of the title to be used with the dialog. */
  @Implementation
  @HiddenApi
  @StringRes
  public int getTitleResId() {
    return directly().getTitleResId();
  }

  /** Returns the resource id of the text to be shown in the dialog's body. */
  @Implementation
  @HiddenApi
  @StringRes
  public int getDialogMessageResId() {
    return directly().getDialogMessageResId();
  }

  /**
   * Returns the text to be shown in the dialog's body, or {@code null} if {@link
   * #getDialogMessageResId()} returns a valid resource id.
   */
  @Implementation
  @HiddenApi
  @Nullable
  public String getDialogMessage() {
    return directly().getDialogMessage();
  }

  /** Returns the text to be shown. */
  @Implementation
  @HiddenApi
  @StringRes
  public int getNeutralButtonTextResId() {
    return directly().getNeutralButtonTextResId();
  }

  private SuspendDialogInfo directly() {
    return directlyOn(realInfo, SuspendDialogInfo.class);
  }
}
