package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.R;

import android.content.IntentSender;
import android.content.integrity.AppIntegrityManager;
import android.content.integrity.RuleSet;
import java.util.Optional;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow of {@link AppIntegrityManager} */
@Implements(
    value = AppIntegrityManager.class,
    minSdk = R,
    looseSignatures = true,
    isInAndroidSdk = false)
public class ShadowAppIntegrityManager {

  private Optional<RuleSet> recordedRuleSet;

  /** Default shadow constructor that resets the {@code recordedRuleSet}. */
  public ShadowAppIntegrityManager() {
    recordedRuleSet = Optional.empty();
  }

  /**
   * Overrides the implementation of the {@code updateRuleSet} method so that a copy of the pushed
   * rule set is kept within the shadow class.
   */
  @Implementation
  protected void updateRuleSet(RuleSet updateRequest, IntentSender statusReceiver) {
    recordedRuleSet = Optional.of(updateRequest);
  }

  /**
   * Overrides the implementation of the {@code getCurrentRuleSetVersion} method to return the
   * version stored in the recorded rule set. The method returns "None" if there is no such rule set
   * available.
   */
  @Implementation
  protected String getCurrentRuleSetVersion() {
    return recordedRuleSet.isPresent() ? recordedRuleSet.get().getVersion() : "None";
  }

  /**
   * Overrides the implementation of the {@code getCurrentRuleSetProvider} method to return the
   * gmscore package name for all the requests when a rule set exists.
   */
  @Implementation
  protected String getCurrentRuleSetProvider() {
    return recordedRuleSet.isPresent() ? "com.google.android.gms" : "None";
  }
}
