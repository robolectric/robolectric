package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.app.Activity;
import android.icu.util.ULocale;
import android.os.Build.VERSION_CODES;
import android.view.translation.TranslationCapability;
import android.view.translation.TranslationManager;
import android.view.translation.TranslationSpec;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = VERSION_CODES.S)
public class ShadowTranslationManagerTest {
  private final TranslationManager translationManager =
      ApplicationProvider.getApplicationContext().getSystemService(TranslationManager.class);

  @Test
  public void getOnDeviceTranslationCapabilities_noCapabilitiesSet_returnsEmpty() {
    assertThat(
            translationManager.getOnDeviceTranslationCapabilities(
                TranslationSpec.DATA_FORMAT_TEXT, TranslationSpec.DATA_FORMAT_TEXT))
        .isEmpty();
  }

  @Test
  public void getOnDeviceTranslationCapabilities_returnsSetCapabilities() {
    ImmutableSet<TranslationCapability> capabilities =
        ImmutableSet.of(
            new TranslationCapability(
                TranslationCapability.STATE_NOT_AVAILABLE,
                new TranslationSpec(ULocale.JAPANESE, TranslationSpec.DATA_FORMAT_TEXT),
                new TranslationSpec(ULocale.ENGLISH, TranslationSpec.DATA_FORMAT_TEXT),
                /* uiTranslationEnabled= */ false,
                /* supportedTranslationFlags= */ 0),
            new TranslationCapability(
                TranslationCapability.STATE_ON_DEVICE,
                new TranslationSpec(ULocale.KOREAN, TranslationSpec.DATA_FORMAT_TEXT),
                new TranslationSpec(ULocale.FRENCH, TranslationSpec.DATA_FORMAT_TEXT),
                /* uiTranslationEnabled= */ true,
                /* supportedTranslationFlags= */ 0));
    ((ShadowTranslationManager) Shadow.extract(translationManager))
        .setOnDeviceTranslationCapabilities(
            TranslationSpec.DATA_FORMAT_TEXT, TranslationSpec.DATA_FORMAT_TEXT, capabilities);

    assertThat(
            translationManager.getOnDeviceTranslationCapabilities(
                TranslationSpec.DATA_FORMAT_TEXT, TranslationSpec.DATA_FORMAT_TEXT))
        .isEqualTo(capabilities);
  }

  @Test
  @Config(minSdk = VERSION_CODES.S)
  public void translationManager_activityContextEnabled_differentInstancesRetrieveCapabilities() {
    String originalProperty = System.getProperty("robolectric.createActivityContexts", "");
    System.setProperty("robolectric.createActivityContexts", "true");
    Activity activity = null;
    try {
      TranslationManager applicationTranslationManager =
          ApplicationProvider.getApplicationContext().getSystemService(TranslationManager.class);

      activity = Robolectric.setupActivity(Activity.class);
      TranslationManager activityTranslationManager =
          activity.getSystemService(TranslationManager.class);

      assertThat(applicationTranslationManager).isNotSameInstanceAs(activityTranslationManager);

      Set<TranslationCapability> applicationCapabilities =
          applicationTranslationManager.getOnDeviceTranslationCapabilities(1, 2);
      Set<TranslationCapability> activityCapabilities =
          activityTranslationManager.getOnDeviceTranslationCapabilities(1, 2);

      assertThat(activityCapabilities).isEqualTo(applicationCapabilities);
    } finally {
      if (activity != null) {
        activity.finish();
      }
      System.setProperty("robolectric.createActivityContexts", originalProperty);
    }
  }
}
