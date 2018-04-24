package org.robolectric.shadows.support.v4;

import static com.google.common.truth.Truth.assertThat;

import android.os.Build;
import android.support.v4.os.BuildCompat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.util.TestRunnerWithManifest;

@RunWith(TestRunnerWithManifest.class)
public class BuildCompatTest {

  @Test
  @Config(minSdk = Build.VERSION_CODES.N)
  public void isAtLeastN() {
    assertThat(BuildCompat.isAtLeastN()).isTrue();
  }

  @Test
  @Config(maxSdk = Build.VERSION_CODES.M)
  public void isAtLeastN_preN() {
    assertThat(BuildCompat.isAtLeastN()).isFalse();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.N_MR1)
  public void isAtLeastNMR1() {
    assertThat(BuildCompat.isAtLeastNMR1()).isTrue();
  }

  @Test
  @Config(maxSdk = Build.VERSION_CODES.N)
  public void isAtLeastNMR1_preNMR1() {
    assertThat(BuildCompat.isAtLeastNMR1()).isFalse();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.O)
  public void isAtLeastO() {
    assertThat(BuildCompat.isAtLeastO()).isTrue();
  }

  @Test
  @Config(maxSdk = Build.VERSION_CODES.N_MR1)
  public void isAtLeastO_preO() {
    assertThat(BuildCompat.isAtLeastO()).isFalse();
  }
}
