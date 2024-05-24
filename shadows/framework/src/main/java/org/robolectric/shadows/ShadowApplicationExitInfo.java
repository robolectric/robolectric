package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.R;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.app.ApplicationExitInfo;
import java.io.InputStream;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/** Shadow for {@link android.app.ApplicationExitInfo} */
@Implements(value = ApplicationExitInfo.class, minSdk = R, isInAndroidSdk = false)
public class ShadowApplicationExitInfo {

  @RealObject ApplicationExitInfo applicationExitInfo;
  private InputStream traceInputStream = null;

  @Implementation
  protected InputStream getTraceInputStream() {
    if (this.traceInputStream == null) {
      return reflector(ApplicationExitInfoReflector.class, applicationExitInfo)
          .getTraceInputStream();
    }
    return this.traceInputStream;
  }

  /**
   * When called with a non-null InputStream, Overrides the stream returned by {@link
   * ApplicationExitInfo#getTraceInputStream()}
   */
  public void setTraceInputStream(InputStream in) {
    this.traceInputStream = in;
  }

  @ForType(ApplicationExitInfo.class)
  private interface ApplicationExitInfoReflector {
    @Direct
    InputStream getTraceInputStream();
  }
}
