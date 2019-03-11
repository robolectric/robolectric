package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;

import android.telephony.euicc.EuiccManager;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(value = EuiccManager.class, minSdk = P)
public class ShadowEuiccManager {

  private boolean enabled;
  private String eid;

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
