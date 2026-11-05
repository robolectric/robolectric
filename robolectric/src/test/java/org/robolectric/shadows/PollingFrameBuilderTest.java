package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.nfc.cardemulation.PollingFrame;
import android.os.Build.VERSION_CODES;
import android.os.Parcel;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Tests for {@link PollingFrameBuilder}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = VERSION_CODES.VANILLA_ICE_CREAM)
public final class PollingFrameBuilderTest {

  @Test
  public void build_setsAllFields() {
    byte[] data = new byte[] {0x01, 0x02, 0x03};
    PollingFrame frame =
        PollingFrameBuilder.newBuilder()
            .setType(PollingFrame.POLLING_LOOP_TYPE_A)
            .setData(data)
            .setVendorSpecificGain(10)
            .setTimestamp(12345L)
            .setTriggeredAutoTransact(true)
            .build();

    assertThat(frame.getType()).isEqualTo(PollingFrame.POLLING_LOOP_TYPE_A);
    assertThat(frame.getData()).isEqualTo(data);
    assertThat(frame.getVendorSpecificGain()).isEqualTo(10);
    assertThat(frame.getTimestamp()).isEqualTo(12345L);
    assertThat(frame.getTriggeredAutoTransact()).isTrue();
  }

  @Test
  public void build_defaultValues() {
    PollingFrame frame = PollingFrameBuilder.newBuilder().build();

    assertThat(frame.getType()).isEqualTo(0);
    assertThat(frame.getData()).isEmpty();
    assertThat(frame.getVendorSpecificGain()).isEqualTo(0);
    assertThat(frame.getTimestamp()).isEqualTo(0L);
    assertThat(frame.getTriggeredAutoTransact()).isFalse();
  }

  @Test
  public void parceling_preservesAllFields() {
    byte[] data = new byte[] {0x0a, 0x0b};
    PollingFrame frame =
        PollingFrameBuilder.newBuilder()
            .setType(PollingFrame.POLLING_LOOP_TYPE_B)
            .setData(data)
            .setVendorSpecificGain(42)
            .setTimestamp(99999L)
            .setTriggeredAutoTransact(false)
            .build();

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
