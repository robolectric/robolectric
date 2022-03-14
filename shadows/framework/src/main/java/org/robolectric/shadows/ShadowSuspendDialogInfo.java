package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.R;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.DrawableRes;
import android.annotation.StringRes;
import android.content.pm.SuspendDialogInfo;
import android.os.Build;
import javax.annotation.Nullable;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/** Shadow of {@link SuspendDialogInfo} to expose hidden methods. */
@Implements(value = SuspendDialogInfo.class, isInAndroidSdk = false, minSdk = Build.VERSION_CODES.Q)
public class ShadowSuspendDialogInfo {

  @RealObject protected SuspendDialogInfo realInfo;

  /** Returns the resource id of the icon to be used with the dialog. */
  @Implementation
  @HiddenApi
  @DrawableRes
  public int getIconResId() {
    return reflector(SuspendDialogInfoReflector.class, realInfo).getIconResId();
  }

  /** Returns the resource id of the title to be used with the dialog. */
  @Implementation
  @HiddenApi
  @StringRes
  public int getTitleResId() {
    return reflector(SuspendDialogInfoReflector.class, realInfo).getTitleResId();
  }

  /** Returns the resource id of the text to be shown in the dialog's body. */
  @Implementation
  @HiddenApi
  @StringRes
  public int getDialogMessageResId() {
    return reflector(SuspendDialogInfoReflector.class, realInfo).getDialogMessageResId();
  }

  /**
   * Returns the text to be shown in the dialog's body, or {@code null} if {@link
   * #getDialogMessageResId()} returns a valid resource id.
   */
  @Implementation
  @HiddenApi
  @Nullable
  public String getDialogMessage() {
    return reflector(SuspendDialogInfoReflector.class, realInfo).getDialogMessage();
  }

  /** Returns the text to be shown. */
  @Implementation
  @HiddenApi
  @StringRes
  public int getNeutralButtonTextResId() {
    return reflector(SuspendDialogInfoReflector.class, realInfo).getNeutralButtonTextResId();
  }

  /**
   * Returns the action expected to happen on neutral button tap.
   *
   * @return {@link SuspendDialogInfo.BUTTON_ACTION_MORE_DETAILS} or {@link
   *     SuspendDialogInfo.BUTTON_ACTION_UNSUSPEND}
   */
  @Implementation(minSdk = R)
  public int getNeutralButtonAction() {
    return reflector(SuspendDialogInfoReflector.class, realInfo).getNeutralButtonAction();
  }

  @ForType(SuspendDialogInfo.class)
  interface SuspendDialogInfoReflector {

    @Direct
    int getIconResId();

    @Direct
    int getTitleResId();

    @Direct
    int getDialogMessageResId();

    @Direct
    String getDialogMessage();

    @Direct
    int getNeutralButtonTextResId();

    @Direct
    int getNeutralButtonAction();
  }
}
