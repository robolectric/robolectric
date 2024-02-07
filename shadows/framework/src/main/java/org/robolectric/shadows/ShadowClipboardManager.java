package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;
import static org.robolectric.RuntimeEnvironment.getApiLevel;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.ClipboardManager.OnPrimaryClipChangedListener;
import android.os.SystemClock;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.reflector.ForType;

@SuppressWarnings("UnusedDeclaration")
@Implements(ClipboardManager.class)
public class ShadowClipboardManager {
  @RealObject private ClipboardManager realClipboardManager;
  private final Collection<OnPrimaryClipChangedListener> listeners =
      new CopyOnWriteArrayList<OnPrimaryClipChangedListener>();
  private ClipData clip;

  @Implementation
  protected void setPrimaryClip(ClipData clip) {
    if (getApiLevel() >= O) {
      if (clip != null) {
        final ClipDescription description = clip.getDescription();
        if (description != null) {
          final long currentTimeMillis = SystemClock.uptimeMillis();
          ReflectionHelpers.callInstanceMethod(
              ClipDescription.class,
              description,
              "setTimestamp",
              ClassParameter.from(long.class, currentTimeMillis));
        }
      }
    }
    if (getApiLevel() >= N) {
      if (clip != null) {
        clip.prepareToLeaveProcess(true);
      }
    } else {
      if (clip != null) {
        ReflectionHelpers.callInstanceMethod(ClipData.class, clip, "prepareToLeaveProcess");
      }
    }

    this.clip = clip;

    for (OnPrimaryClipChangedListener listener : listeners) {
      listener.onPrimaryClipChanged();
    }
  }

  @Implementation(minSdk = P)
  protected void clearPrimaryClip() {
    setPrimaryClip(null);
  }

  @Implementation
  protected ClipData getPrimaryClip() {
    return clip;
  }

  @Implementation
  protected ClipDescription getPrimaryClipDescription() {
    return clip == null ? null : clip.getDescription();
  }

  @Implementation
  protected boolean hasPrimaryClip() {
    return clip != null;
  }

  @Implementation
  protected void addPrimaryClipChangedListener(OnPrimaryClipChangedListener listener) {
    listeners.add(listener);
  }

  @Implementation
  protected void removePrimaryClipChangedListener(OnPrimaryClipChangedListener listener) {
    listeners.remove(listener);
  }

  @Implementation
  protected void setText(CharSequence text) {
    setPrimaryClip(ClipData.newPlainText(null, text));
  }

  @Implementation
  protected boolean hasText() {
    CharSequence text = reflector(ClipboardManagerReflector.class, realClipboardManager).getText();
    return text != null && text.length() > 0;
  }

  @ForType(ClipboardManager.class)
  interface ClipboardManagerReflector {
    CharSequence getText();
  }
}
