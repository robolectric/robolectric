package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.R;

import android.net.wifi.ScanResult.InformationElement;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.robolectric.RuntimeEnvironment;

/** Builder for {@link InformationElement}. */
public class InformationElementBuilder {
  private int id;
  private int idExt;
  private byte[] bytes;

  private InformationElementBuilder() {}

  public static InformationElementBuilder newBuilder() {
    return new InformationElementBuilder();
  }

  @CanIgnoreReturnValue
  public InformationElementBuilder setId(int id) {
    this.id = id;
    return this;
  }

  @CanIgnoreReturnValue
  public InformationElementBuilder setIdExt(int idExt) {
    this.idExt = idExt;
    return this;
  }

  @CanIgnoreReturnValue
  public InformationElementBuilder setBytes(byte[] bytes) {
    this.bytes = bytes;
    return this;
  }

  public InformationElement build() {
    if (RuntimeEnvironment.getApiLevel() > R) {
      return new InformationElement(this.id, this.idExt, this.bytes);
    } else {
      InformationElement info = new InformationElement();
      info.id = this.id;
      info.idExt = this.idExt;
      info.bytes = this.bytes;
      return info;
    }
  }
}
