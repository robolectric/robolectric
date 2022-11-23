package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.media.AudioDeviceInfo;
import android.os.Build.VERSION_CODES;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = VERSION_CODES.M)
public final class ShadowAudioDeviceInfoTest {

  private AudioDeviceInfo audioDeviceInfo;

  @Before
  public void setUp() {
    audioDeviceInfo = Shadow.newInstanceOf(AudioDeviceInfo.class);
  }

  @Test
  public void getId_alwaysReturnsZero() {
    assertThat(audioDeviceInfo.getId()).isEqualTo(0);
  }

  @Test
  public void getType_shouldReturnTheStubbedType() {
    int type = AudioDeviceInfo.TYPE_FM;

    shadowOf(audioDeviceInfo).setType(type);

    assertThat(audioDeviceInfo.getType()).isEqualTo(type);
  }

  @Test
  public void setIsSource_stubsIsSourceAndIsSink() {
    shadowOf(audioDeviceInfo).setIsSource();

    assertThat(audioDeviceInfo.isSource()).isTrue();
    assertThat(audioDeviceInfo.isSink()).isFalse();
  }

  @Test
  public void setIsSink_stubsIsSourceAndIsSink() {
    shadowOf(audioDeviceInfo).setIsSink();

    assertThat(audioDeviceInfo.isSource()).isFalse();
    assertThat(audioDeviceInfo.isSink()).isTrue();
  }

  @Test
  public void setProductName_stubsTheProductName() {
    String productName = "test-product";

    shadowOf(audioDeviceInfo).setProductName(productName);

    assertThat(audioDeviceInfo.getProductName()).isEqualTo(productName);
  }

  @Test
  public void setAddress_stubsTheAddress() {
    String address = "test-address";

    shadowOf(audioDeviceInfo).setAddress(address);

    assertThat(audioDeviceInfo.getAddress()).isEqualTo(address);
  }
}
