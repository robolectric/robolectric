package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.media.MediaRouter2;
import android.media.MediaRouter2.ScanRequest;
import android.media.MediaRouter2.ScanToken;
import android.os.Build;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.robolectric.annotation.Filter;
import org.robolectric.annotation.Filter.Order;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

/** Shadow class for {@link android.media.MediaRouter2}. */
@Implements(value = MediaRouter2.class, minSdk = Build.VERSION_CODES.R)
public class ShadowMediaRouter2 {

  private final List<ScanRequest> scanRequests = new ArrayList<>();
  private final List<ScanToken> cancelledScanTokens = new ArrayList<>();
  @Nullable private SecurityException scanSecurityException;

  @Resetter
  public static void reset() {
    reflector(MediaRouter2Reflector.class).setInstance(null);
  }

  @Filter(minSdk = VANILLA_ICE_CREAM, order = Order.BEFORE)
  protected void requestScan(ScanRequest request) {
    if (scanSecurityException != null) {
      throw scanSecurityException;
    }
    scanRequests.add(request);
  }

  @Filter(minSdk = VANILLA_ICE_CREAM, order = Order.BEFORE)
  protected void cancelScanRequest(ScanToken token) {
    if (scanSecurityException != null) {
      throw scanSecurityException;
    }
    cancelledScanTokens.add(token);
  }

  /** Returns all active scan requests. */
  public List<ScanRequest> getScanRequests() {
    return ImmutableList.copyOf(scanRequests);
  }

  /** Returns all scan tokens that have been cancelled. */
  public List<ScanToken> getCancelledScanTokens() {
    return ImmutableList.copyOf(cancelledScanTokens);
  }

  /** Sets a SecurityException to be thrown when requestScan or cancelScanRequest are called. */
  public void setScanSecurityException(@Nullable SecurityException exception) {
    this.scanSecurityException = exception;
  }

  @ForType(MediaRouter2.class)
  interface MediaRouter2Reflector {
    @Static
    @Accessor("sInstance")
    void setInstance(MediaRouter2 instance);
  }
}
