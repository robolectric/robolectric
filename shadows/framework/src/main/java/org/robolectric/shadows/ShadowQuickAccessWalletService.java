package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.R;

import android.annotation.SystemApi;
import android.service.quickaccesswallet.QuickAccessWalletService;
import android.service.quickaccesswallet.WalletServiceEvent;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

/** Shadow of {@link QuickAccessWalletService} */
@Implements(
    value = QuickAccessWalletService.class,
    minSdk = R,
    // turn off shadowOf generation
    isInAndroidSdk = false)
public class ShadowQuickAccessWalletService extends ShadowService {

  private static final List<WalletServiceEvent> serviceEvents = new ArrayList<>(0);

  /** Capture events sent by the service to SysUI */
  @Implementation
  @SystemApi
  public final void sendWalletServiceEvent(@Nonnull WalletServiceEvent serviceEvent) {
    serviceEvents.add(serviceEvent);
  }

  /** Returns a list of service events sent with {@link #sendWalletServiceEvent} */
  public static List<WalletServiceEvent> getServiceEvents() {
    return new ArrayList<>(serviceEvents);
  }

  @Resetter
  public static void reset() {
    serviceEvents.clear();
  }
}
