package org.robolectric.shadows;

import android.net.wifi.ScanResult;
import android.os.Build.VERSION_CODES;
import org.robolectric.annotation.Implements;

/** Shadow for {@link android.net.wifi.ScanResult.InformationElement}. */
@Implements(value = ScanResult.InformationElement.class, minSdk = VERSION_CODES.R)
public class ShadowInformationElement {
  /**
   * A builder for creating ShadowInformationElement objects. Use build() to return the
   * InformationElement object.
   */
  public static class Builder {
    private final ScanResult.InformationElement informationElement;

    public Builder() {
      informationElement = new ScanResult.InformationElement();
    }

    public Builder setId(int id) {
      informationElement.id = id;
      return this;
    }

    public Builder setIdExt(int idExt) {
      informationElement.idExt = idExt;
      return this;
    }

    public Builder setBytes(byte[] bytes) {
      informationElement.bytes = bytes;
      return this;
    }

    public ScanResult.InformationElement build() {
      return informationElement;
    }
  }
}
