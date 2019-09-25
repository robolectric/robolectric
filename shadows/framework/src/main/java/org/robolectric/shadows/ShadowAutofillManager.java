package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;

import android.content.ComponentName;
import android.service.autofill.FillEventHistory;
import androidx.annotation.Nullable;
import android.view.autofill.AutofillManager;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Robolectric implementation of {@link android.os.AutofillManager}.
 */
@Implements(value = AutofillManager.class, minSdk = O)
public class ShadowAutofillManager {
  @Nullable private ComponentName autofillServiceComponentName = null;

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

  /**
   * Overrides the component name of the autofill service enabled for the current user. See {@link
   * AutofillManager#getAutofillServiceComponentName()}.
   */
  public void setAutofillServiceComponentName(@Nullable ComponentName componentName) {
    this.autofillServiceComponentName = componentName;
  }
}
