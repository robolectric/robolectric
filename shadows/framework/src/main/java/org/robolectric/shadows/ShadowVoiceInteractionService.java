package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.Q;

import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.service.voice.VoiceInteractionService;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

/** Shadow implementation of {@link android.service.voice.VoiceInteractionService}. */
@Implements(value = VoiceInteractionService.class, minSdk = LOLLIPOP)
public class ShadowVoiceInteractionService extends ShadowService {

  @Nullable private static ComponentName activeService = null;

  private final List<Bundle> hintBundles = Collections.synchronizedList(new ArrayList<>());
  private final List<Bundle> sessionBundles = Collections.synchronizedList(new ArrayList<>());
  private boolean isReady = false;

  /**
   * Sets return value for {@link #isActiveService(Context context, ComponentName componentName)}
   * method.
   */
  public static void setActiveService(@Nullable ComponentName activeService) {
    ShadowVoiceInteractionService.activeService = activeService;
  }

  @Implementation
  protected void onReady() {
    isReady = true;
  }

  @Implementation(minSdk = Q)
  protected void setUiHints(Bundle hints) {
    // The actual implementation of this code on Android will also throw the exception if the
    // service isn't ready.
    // Throwing here will hopefully make sure these issues are caught before production.
    if (!isReady) {
      throw new NullPointerException(
          "setUiHints() called before onReady() callback for VoiceInteractionService!");
    }

    if (hints != null) {
      hintBundles.add(hints);
    }
  }

  @Implementation(minSdk = M)
  protected void showSession(Bundle args, int flags) {
    if (!isReady) {
      throw new NullPointerException(
          "showSession() called before onReady() callback for VoiceInteractionService!");
    }

    if (args != null) {
      sessionBundles.add(args);
    }
  }

  @Implementation
  protected static boolean isActiveService(Context context, ComponentName componentName) {
    return componentName.equals(activeService);
  }

  /**
   * Returns list of bundles provided with calls to {@link #setUiHints(Bundle bundle)} in invocation
   * order.
   */
  public List<Bundle> getPreviousUiHintBundles() {
    return Collections.unmodifiableList(hintBundles);
  }

  /**
   * Returns the last Bundle object set via {@link #setUiHints(Bundle bundle)} or null if there
   * wasn't any.
   */
  @Nullable
  public Bundle getLastUiHintBundle() {
    if (hintBundles.isEmpty()) {
      return null;
    }

    return hintBundles.get(hintBundles.size() - 1);
  }

  /**
   * Returns the last Bundle object set via {@link #setUiHints(Bundle bundle)} or null if there
   * wasn't any.
   */
  @Nullable
  public Bundle getLastSessionBundle() {
    return Iterables.getLast(sessionBundles, null);
  }

  /** Resets this shadow instance. */
  @Resetter
  public static void reset() {
    activeService = null;
  }
}
