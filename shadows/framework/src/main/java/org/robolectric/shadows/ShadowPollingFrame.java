package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM;

import android.nfc.cardemulation.PollingFrame;
import android.os.Parcel;
import android.os.Parcelable;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;

/** Shadow for {@link PollingFrame}. */
@Implements(value = PollingFrame.class, minSdk = VANILLA_ICE_CREAM)
public class ShadowPollingFrame {
  public static final Parcelable.Creator<PollingFrame> CREATOR =
      new Parcelable.Creator<PollingFrame>() {
        @Override
        public PollingFrame createFromParcel(Parcel in) {
          PollingFrame frame = Shadow.newInstanceOf(PollingFrame.class);
          ShadowPollingFrame shadow = Shadow.extract(frame);
          shadow.setType(in.readInt());
          int dataLen = in.readInt();
          byte[] data = new byte[dataLen];
          in.readByteArray(data);
          shadow.setData(data);
          shadow.setVendorSpecificGain(in.readInt());
          shadow.setTimestamp(in.readLong());
          shadow.setTriggeredAutoTransact(in.readBoolean());
          return frame;
        }

        @Override
        public PollingFrame[] newArray(int size) {
          return new PollingFrame[size];
        }
      };

  private int type;
  private byte[] data = new byte[0];
  private int vendorSpecificGain;
  private long timestamp;
  private boolean triggeredAutoTransact;

  public static ShadowPollingFrame shadowOf(PollingFrame frame) {
    return Shadow.extract(frame);
  }

  public void setType(int type) {
    this.type = type;
  }

  public void setData(byte[] data) {
    this.data = data;
  }

  public void setVendorSpecificGain(int vendorSpecificGain) {
    this.vendorSpecificGain = vendorSpecificGain;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  @SuppressWarnings("ProtectedImplementationLintCheck")
  @Implementation
  public void setTriggeredAutoTransact(boolean triggeredAutoTransact) {
    this.triggeredAutoTransact = triggeredAutoTransact;
  }

  @Implementation
  protected int getType() {
    return type;
  }

  @Implementation
  protected byte[] getData() {
    return data;
  }

  @Implementation
  protected int getVendorSpecificGain() {
    return vendorSpecificGain;
  }

  @Implementation
  protected long getTimestamp() {
    return timestamp;
  }

  @Implementation
  protected boolean getTriggeredAutoTransact() {
    return triggeredAutoTransact;
  }

  @Implementation
  protected void writeToParcel(Parcel parcel, int flags) {
    // Overwrite the CREATOR so that we can simulate reading from parcel.
    ReflectionHelpers.setStaticField(PollingFrame.class, "CREATOR", CREATOR);

    parcel.writeInt(type);
    parcel.writeInt(data != null ? data.length : 0);
    if (data != null) {
      parcel.writeByteArray(data);
    }
    parcel.writeInt(vendorSpecificGain);
    parcel.writeLong(timestamp);
    parcel.writeBoolean(triggeredAutoTransact);
  }
}
