package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;

import android.annotation.Nullable;
import android.content.ComponentName;
import android.service.autofill.FillEventHistory;
import android.view.autofill.AutofillManager;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

/** Robolectric implementation of {@link android.os.AutofillManager}. */
@Implements(value = AutofillManager.class, minSdk = O)
public class ShadowAutofillManager {
  @Nullable private static ComponentName autofillServiceComponentName = null;
  private static boolean autofillSupported = false;
  private static boolean enabled = false;

  @Resetter
  public static void reset() {
    autofillServiceComponentName = null;
    autofillSupported = false;
    enabled = false;
  }

  @Implementation
  protected FillEventHistory getFillEventHistory() {
    return null;
  }

  /**
   * Returns the overridden value set by {@link #setAutofillServiceComponentName(ComponentName)}.
   */
  @Nullable
  @Implementation(minSdk = P)
  protected ComponentName getAutofillServiceComponentName() {
    return autofillServiceComponentName;
  }

  /** Returns the overridden value set by {@link #setAutofillSupported(boolean)}. */
  @Implementation
  protected boolean isAutofillSupported() {
    return autofillSupported;
  }

  /** Returns the overridden value set by {@link #setEnabled(boolean)}. */
  @Implementation
  protected boolean isEnabled() {
    return enabled;
  }

  /**
   * Overrides the component name of the autofill service enabled for the current user. See {@link
   * AutofillManager#getAutofillServiceComponentName()}.
   */
  public void setAutofillServiceComponentName(@Nullable ComponentName componentName) {
    this.autofillServiceComponentName = componentName;
  }

  /**
   * Overrides the autofill supported state for the current device and current user. See {@link
   * AutofillManager#isAutofillSupported()}.
   */
  public void setAutofillSupported(boolean supported) {
    this.autofillSupported = supported;
  }

  /**
   * Overrides the autofill enabled state for the current user. See {@link
   * AutofillManager#isEnabled()}.
   */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }
}
