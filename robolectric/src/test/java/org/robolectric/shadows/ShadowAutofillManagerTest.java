package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.view.autofill.AutofillManager;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
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

  @Test
  public void setAutofillSupported() {
    assertThat(autofillManager.isAutofillSupported()).isFalse();

    shadowOf(autofillManager).setAutofillSupported(true);
    assertThat(autofillManager.isAutofillSupported()).isTrue();

    shadowOf(autofillManager).setAutofillSupported(false);
    assertThat(autofillManager.isAutofillSupported()).isFalse();
  }

  @Test
  public void setEnabled() {
    assertThat(autofillManager.isEnabled()).isFalse();

    shadowOf(autofillManager).setEnabled(true);
    assertThat(autofillManager.isEnabled()).isTrue();

    shadowOf(autofillManager).setEnabled(false);
    assertThat(autofillManager.isEnabled()).isFalse();
  }

  @Test
  @Config(minSdk = O)
  public void autofillManager_activityContextEnabled_differentInstancesRetrieveSameInfo() {
    String originalProperty = System.getProperty("robolectric.createActivityContexts", "");
    System.setProperty("robolectric.createActivityContexts", "true");
    try (ActivityController<Activity> controller =
        Robolectric.buildActivity(Activity.class).setup()) {
      AutofillManager applicationAutofillManager = context.getSystemService(AutofillManager.class);

      Activity activity = controller.get();
      AutofillManager activityAutofillManager = activity.getSystemService(AutofillManager.class);

      assertNotSame(applicationAutofillManager, activityAutofillManager);

      assertEquals(
          applicationAutofillManager.isAutofillSupported(),
          activityAutofillManager.isAutofillSupported());
      assertEquals(applicationAutofillManager.isEnabled(), activityAutofillManager.isEnabled());

    } finally {
      System.setProperty("robolectric.createActivityContexts", originalProperty);
    }
  }
}
