package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.shadows.ShadowPollingFrame.shadowOf;

import android.nfc.cardemulation.PollingFrame;
import android.os.Build;
import android.os.Parcel;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

/** Tests for {@link ShadowPollingFrame}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = Build.VERSION_CODES.VANILLA_ICE_CREAM)
public final class ShadowPollingFrameTest {

  @Test
  public void testGettersAndSetters() {
    PollingFrame frame = Shadow.newInstanceOf(PollingFrame.class);
    ShadowPollingFrame shadow = shadowOf(frame);
    byte[] data = new byte[] {0x01, 0x02, 0x03};
    shadow.setType(PollingFrame.POLLING_LOOP_TYPE_A);
    shadow.setData(data);
    shadow.setVendorSpecificGain(10);
    shadow.setTimestamp(12345L);
    shadow.setTriggeredAutoTransact(true);

    assertThat(frame.getType()).isEqualTo(PollingFrame.POLLING_LOOP_TYPE_A);
    assertThat(frame.getData()).isEqualTo(data);
    assertThat(frame.getVendorSpecificGain()).isEqualTo(10);
    assertThat(frame.getTimestamp()).isEqualTo(12345L);
    assertThat(frame.getTriggeredAutoTransact()).isTrue();
  }

  @Test
  public void testParceling_preservesAllFields() {
    PollingFrame frame = Shadow.newInstanceOf(PollingFrame.class);
    ShadowPollingFrame shadow = shadowOf(frame);
    byte[] data = new byte[] {0x0a, 0x0b};
    shadow.setType(PollingFrame.POLLING_LOOP_TYPE_B);
    shadow.setData(data);
    shadow.setVendorSpecificGain(42);
    shadow.setTimestamp(99999L);
    shadow.setTriggeredAutoTransact(false);

    PollingFrame copy = copyViaParcel(frame);

    assertThat(copy.getType()).isEqualTo(PollingFrame.POLLING_LOOP_TYPE_B);
    assertThat(copy.getData()).isEqualTo(data);
    assertThat(copy.getVendorSpecificGain()).isEqualTo(42);
    assertThat(copy.getTimestamp()).isEqualTo(99999L);
    assertThat(copy.getTriggeredAutoTransact()).isFalse();
  }

  private static PollingFrame copyViaParcel(PollingFrame orig) {
    Parcel parcel = Parcel.obtain();
    orig.writeToParcel(parcel, 0);
    parcel.setDataPosition(0);
    return PollingFrame.CREATOR.createFromParcel(parcel);
  }
}
