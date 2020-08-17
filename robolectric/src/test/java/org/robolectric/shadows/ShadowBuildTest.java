package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.O;
import static com.google.common.truth.Truth.assertThat;

import android.os.Build;
import android.os.Build.VERSION;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
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
  public void setProduct() {
    ShadowBuild.setProduct("robo_product");
    assertThat(Build.PRODUCT).isEqualTo("robo_product");
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
  @Config(minSdk = M)
  public void setVersionSecurityPatch() {
    ShadowBuild.setVersionSecurityPatch("2019-02-05");
    assertThat(VERSION.SECURITY_PATCH).isEqualTo("2019-02-05");
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
  public void setBrand() {
    ShadowBuild.setBrand("robo_brand");
    assertThat(Build.BRAND).isEqualTo("robo_brand");
  }

  @Test
  public void setHardware() {
    ShadowBuild.setHardware("robo_hardware");
    assertThat(Build.HARDWARE).isEqualTo("robo_hardware");
  }

  @Test
  public void setTags() {
    ShadowBuild.setTags("robo_tags");
    assertThat(Build.TAGS).isEqualTo("robo_tags");
  }

  @Test
  public void setType() {
    ShadowBuild.setType("robo_type");
    assertThat(Build.TYPE).isEqualTo("robo_type");
  }

  @Test
  public void resetPerTest() {
    checkValues();
  }

  @Test
  public void resetPerTest2() {
    checkValues();
  }

  @Test
  @Config(minSdk = O)
  public void getSerial() {
    assertThat(Build.getSerial()).isEqualTo(Build.UNKNOWN);
  }

  /** Verifies that each test gets a fresh set of Build values. */
  private void checkValues() {
    assertThat(Build.FINGERPRINT).isEqualTo("robolectric");
    // set fingerprint value here. It should be reset before next test executes.
    ShadowBuild.setFingerprint("test_fingerprint");
  }
}
