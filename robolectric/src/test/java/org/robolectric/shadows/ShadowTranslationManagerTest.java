package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.shadow.api.Shadow.extract;

import android.icu.util.ULocale;
import android.os.Build.VERSION_CODES;
import android.view.translation.TranslationCapability;
import android.view.translation.TranslationManager;
import android.view.translation.TranslationSpec;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = VERSION_CODES.S)
public class ShadowTranslationManagerTest {
  private ShadowTranslationManager instance;

  @Before
  public void setUp() {
    instance =
        extract(
            ApplicationProvider.getApplicationContext().getSystemService(TranslationManager.class));
  }

  @Test
  public void getOnDeviceTranslationCapabilities_noCapabilitiesSet_returnsEmpty() {
    assertThat(
            instance.getOnDeviceTranslationCapabilities(
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
    instance.setOnDeviceTranslationCapabilities(
        TranslationSpec.DATA_FORMAT_TEXT, TranslationSpec.DATA_FORMAT_TEXT, capabilities);

    assertThat(
            instance.getOnDeviceTranslationCapabilities(
                TranslationSpec.DATA_FORMAT_TEXT, TranslationSpec.DATA_FORMAT_TEXT))
        .isEqualTo(capabilities);
  }
}
