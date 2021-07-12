package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;

import android.telephony.euicc.EuiccManager;
import java.util.HashMap;
import java.util.Map;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(value = EuiccManager.class, minSdk = P)
public class ShadowEuiccManager {

  private final Map<Integer, EuiccManager> cardIdsToEuiccManagers = new HashMap<>();
  private boolean enabled;
  private String eid;

  @Implementation(minSdk = Q)
  protected EuiccManager createForCardId(int cardId) {
    return cardIdsToEuiccManagers.get(cardId);
  }

  /** Sets the value returned by {@link EuiccManager#createForCardId(int)}. */
  public void setEuiccManagerForCardId(int cardId, EuiccManager euiccManager) {
    cardIdsToEuiccManagers.put(cardId, euiccManager);
  }

  /** Returns {@code false}, or the value specified by calling {@link #setIsEnabled}. */
  @Implementation
  protected boolean isEnabled() {
    return enabled;
  }

  /** Set the value to be returned by {@link EuiccManager#isEnabled}. */
  public void setIsEnabled(boolean isEnabled) {
    enabled = isEnabled;
  }

  @Implementation
  protected String getEid() {
    return eid;
  }

  /** Set the value to be returned by {@link EuiccManager#getEid}. */
  public void setEid(String eid) {
    this.eid = eid;
  }
}
