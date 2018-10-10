package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;

import android.service.autofill.FillEventHistory;
import android.view.autofill.AutofillManager;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Robolectric implementation of {@link android.os.AutofillManager}.
 */
@Implements(value = AutofillManager.class, minSdk = O)
public class ShadowAutofillManager {

  @Implementation
  protected FillEventHistory getFillEventHistory() {
    return null;
  }
}

