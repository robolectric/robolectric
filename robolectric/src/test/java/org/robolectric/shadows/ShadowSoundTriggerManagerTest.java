package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.robolectric.shadow.api.Shadow.extract;

import android.hardware.soundtrigger.SoundTrigger;
import android.media.soundtrigger.SoundTriggerManager;
import android.os.Build.VERSION_CODES;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(minSdk = VERSION_CODES.N)
public final class ShadowSoundTriggerManagerTest {
  private ShadowSoundTriggerManager instance;

  @Before
  public void setUp() {
    instance =
        extract(
            ApplicationProvider.getApplicationContext()
                .getSystemService(SoundTriggerManager.class));
  }

  @Config(sdk = VERSION_CODES.S)
  @Test
  public void getModuleProperties_nullModuleProperties() throws Exception {
    SoundTrigger.ModuleProperties moduleProperties = instance.getModuleProperties();
    assertThat(moduleProperties).isNull();
  }

  @Config(sdk = VERSION_CODES.R)
  @Test
  public void getModuleProperties_nullPointExceptionAtAndroidR() throws Exception {
    try {
      SoundTrigger.ModuleProperties unused = instance.getModuleProperties();
      fail("Expect NullPointException");
    } catch (NullPointerException e) {
      assertThat(e).isNotNull();
    }
  }

  @Config(sdk = VERSION_CODES.R)
  @Test
  public void getModuleProperties_nonNullProperties() throws Exception {
    instance.setModuleProperties(
        (SoundTrigger.ModuleProperties) getModuleProperties("supportedModelArch", 1234));
    SoundTrigger.ModuleProperties moduleProperties = instance.getModuleProperties();
    assertThat(moduleProperties).isNotNull();
    assertThat(moduleProperties.getSupportedModelArch()).isEqualTo("supportedModelArch");
    assertThat(moduleProperties.getVersion()).isEqualTo(1234);
  }

  // Construct a dummuy {@code SoundTrigger.ModuleProperties}. Return Object because {@code
  // SoundTrigger.ModuleProperties} is not exist in public Android SDK.
  private Object getModuleProperties(String supportedModelArch, int version) {
    return new SoundTrigger.ModuleProperties(
        /* id= */ 0,
        /* implementor= */ "implementor",
        /* description= */ "description",
        /* uuid= */ "11111111-1111-1111-1111-111111111111",
        version,
        supportedModelArch,
        /* maxSoundModels= */ 0,
        /* maxKeyphrases= */ 0,
        /* maxUsers= */ 0,
        /* recognitionModes= */ 0,
        /* supportsCaptureTransition= */ false,
        /* maxBufferMs= */ 0,
        /* supportsConcurrentCapture= */ false,
        /* powerConsumptionMw= */ 0,
        /* returnsTriggerInEvent= */ false,
        /* audioCapabilities= */ 0);
  }
}
