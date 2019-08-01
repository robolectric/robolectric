package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.content.ComponentName;
import android.content.Context;
import android.view.autofill.AutofillManager;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Unit test for {@link ShadowAutofillManager}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = O)
public class ShadowAutofillManagerTest {
  private final Context context = ApplicationProvider.getApplicationContext();
  private final AutofillManager autofillManager = context.getSystemService(AutofillManager.class);

  @Test
  @Config(minSdk = P)
  public void setAutofillServiceComponentName() {
    assertThat(autofillManager.getAutofillServiceComponentName()).isNull();

    ComponentName componentName = new ComponentName("package", "class");
    shadowOf(autofillManager).setAutofillServiceComponentName(componentName);
    assertThat(autofillManager.getAutofillServiceComponentName()).isEqualTo(componentName);

    shadowOf(autofillManager).setAutofillServiceComponentName(null);
    assertThat(autofillManager.getAutofillServiceComponentName()).isNull();
  }
}
