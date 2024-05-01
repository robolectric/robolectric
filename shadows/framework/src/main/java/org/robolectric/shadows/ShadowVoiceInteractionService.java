package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.Q;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.service.voice.VoiceInteractionService;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/** Shadow implementation of {@link android.service.voice.VoiceInteractionService}. */
@Implements(value = VoiceInteractionService.class)
public class ShadowVoiceInteractionService extends ShadowService {

  private final List<Bundle> hintBundles = Collections.synchronizedList(new ArrayList<>());
  private final List<Bundle> sessionBundles = Collections.synchronizedList(new ArrayList<>());
  @RealObject private VoiceInteractionService realVic;

  /**
   * Sets return value for {@link VoiceInteractionService#isActiveService(Context context,
   * ComponentName componentName)} method.
   */
  public static void setActiveService(@Nullable ComponentName activeService) {
    Settings.Secure.putString(
        RuntimeEnvironment.getApplication().getContentResolver(),
        Settings.Secure.VOICE_INTERACTION_SERVICE,
        activeService == null ? "" : activeService.flattenToString());
  }

  @Implementation(minSdk = Q)
  protected void setUiHints(Bundle hints) {
    reflector(VoiceInteractionServiceReflector.class, realVic).setUiHints(hints);
    hintBundles.add(hints);
  }

  @Implementation(minSdk = M)
  protected void showSession(Bundle args, int flags) {
    reflector(VoiceInteractionServiceReflector.class, realVic).showSession(args, flags);
    sessionBundles.add(args);
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

  /** Accessor interface for VoiceInteractionService's internals. */
  @ForType(VoiceInteractionService.class)
  interface VoiceInteractionServiceReflector {

    @Direct
    void showSession(Bundle args, int flags);

    @Direct
    void setUiHints(Bundle hints);
  }
}
