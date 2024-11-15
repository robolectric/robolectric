package org.robolectric.integrationtests.androidx;

import static com.google.common.truth.Truth.assertThat;

import android.os.Build;
import androidx.core.os.BuildCompat;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Compatibility test for {@link BuildCompat} */
@RunWith(AndroidJUnit4.class)
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

  @Test
  @Config(minSdk = Build.VERSION_CODES.O_MR1)
  public void isAtLeastOMR1() {
    assertThat(BuildCompat.isAtLeastOMR1()).isTrue();
  }

  @Test
  @Config(maxSdk = Build.VERSION_CODES.O)
  public void isAtLeastOMR1_preOMR1() {
    assertThat(BuildCompat.isAtLeastOMR1()).isFalse();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.P)
  public void isAtLeastP() {
    assertThat(BuildCompat.isAtLeastP()).isTrue();
  }

  @Test
  @Config(maxSdk = Build.VERSION_CODES.O)
  public void isAtLeastP_preP() {
    assertThat(BuildCompat.isAtLeastP()).isFalse();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.Q)
  public void isAtLeastQ() {
    assertThat(BuildCompat.isAtLeastQ()).isTrue();
  }

  @Test
  @Config(maxSdk = Build.VERSION_CODES.P)
  public void isAtLeastQ_preQ() {
    assertThat(BuildCompat.isAtLeastQ()).isFalse();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.R)
  public void isAtLeastR() {
    assertThat(BuildCompat.isAtLeastR()).isTrue();
  }

  @Test
  @Config(maxSdk = Build.VERSION_CODES.Q)
  public void isAtLeastR_preR() {
    assertThat(BuildCompat.isAtLeastR()).isFalse();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.S)
  public void isAtLeastS() {
    assertThat(BuildCompat.isAtLeastS()).isTrue();
  }

  @Test
  @Config(maxSdk = Build.VERSION_CODES.R)
  public void isAtLeastS_preS() {
    assertThat(BuildCompat.isAtLeastS()).isFalse();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.S_V2)
  public void isAtLeastSv2() {
    assertThat(BuildCompat.isAtLeastSv2()).isTrue();
  }

  @Test
  @Config(maxSdk = Build.VERSION_CODES.S)
  public void isAtLeastSv2_preSv2() {
    assertThat(BuildCompat.isAtLeastSv2()).isFalse();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.TIRAMISU)
  public void isAtLeastT() {
    assertThat(BuildCompat.isAtLeastT()).isTrue();
  }

  @Test
  @Config(maxSdk = Build.VERSION_CODES.S_V2)
  public void isAtLeastT_preT() {
    assertThat(BuildCompat.isAtLeastT()).isFalse();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
  public void isAtLeastU() {
    assertThat(BuildCompat.isAtLeastU()).isTrue();
  }

  @Test
  @Config(maxSdk = Build.VERSION_CODES.TIRAMISU)
  public void isAtLeastU_preU() {
    assertThat(BuildCompat.isAtLeastU()).isFalse();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.VANILLA_ICE_CREAM)
  public void isAtLeastV() {
    assertThat(BuildCompat.isAtLeastV()).isTrue();
  }

  @Test
  @Config(maxSdk = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
  public void isAtLeastV_preV() {
    assertThat(BuildCompat.isAtLeastV()).isFalse();
  }
}
