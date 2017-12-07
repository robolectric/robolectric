package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;

import android.os.Build;
import android.os.Build.VERSION;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ShadowBuildTest {

  @Test
  public void setDevice() {
    ShadowBuild.setDevice("test_device");
    assertThat(Build.DEVICE).isEqualTo("test_device");
  }

  @Test
  public void setFingerprint() {
    ShadowBuild.setFingerprint("test_fingerprint");
    assertThat(Build.FINGERPRINT).isEqualTo("test_fingerprint");
  }

  @Test
  public void getRadioVersion() {
    ShadowBuild.setRadioVersion("robo_radio");
    assertThat(Build.getRadioVersion()).isEqualTo("robo_radio");
  }

  @Test
  public void setId() {
    ShadowBuild.setId("robo_id");
    assertThat(Build.ID).isEqualTo("robo_id");
  }

  @Test
  public void setVersionRelease() {
    ShadowBuild.setVersionRelease("robo_release");
    assertThat(VERSION.RELEASE).isEqualTo("robo_release");
  }

  @Test
  public void setVersionIncremental() {
    ShadowBuild.setVersionIncremental("robo_incremental");
    assertThat(VERSION.INCREMENTAL).isEqualTo("robo_incremental");
  }

  @Test
  public void setModel() {
    ShadowBuild.setModel("robo_model");
    assertThat(Build.MODEL).isEqualTo("robo_model");
  }

  @Test
  public void setManufacturer() {
    ShadowBuild.setManufacturer("robo_manufacturer");
    assertThat(Build.MANUFACTURER).isEqualTo("robo_manufacturer");
  }

  @Test
  public void setTags() {
    ShadowBuild.setTags("robo_tags");
    assertThat(Build.TAGS).isEqualTo("robo_tags");
  }

  @Test
  public void resetPerTest() {
    checkValues();
  }

  @Test
  public void resetPerTest2() {
    checkValues();
  }

  /** Verifies that each test gets a fresh set of Build values. */
  private void checkValues() {
    assertThat(Build.FINGERPRINT).isEqualTo("robolectric");
    // set fingerprint value here. It should be reset before next test executes.
    ShadowBuild.setFingerprint("test_fingerprint");
  }
}
