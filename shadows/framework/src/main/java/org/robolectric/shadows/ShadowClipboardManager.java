package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.N;
import static org.robolectric.RuntimeEnvironment.getApiLevel;
import static org.robolectric.shadow.api.Shadow.directlyOn;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.ClipboardManager.OnPrimaryClipChangedListener;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;

@SuppressWarnings("UnusedDeclaration")
@Implements(ClipboardManager.class)
public class ShadowClipboardManager {
  @RealObject private ClipboardManager realClipboardManager;
  private final Collection<OnPrimaryClipChangedListener> listeners = new CopyOnWriteArrayList<OnPrimaryClipChangedListener>();
  private ClipData clip;

  @Implementation
  protected void setPrimaryClip(ClipData clip) {
    if (getApiLevel() >= N) {
      if (clip != null) {
        clip.prepareToLeaveProcess(true);
      }
    } else if (getApiLevel() >= JELLY_BEAN_MR2) {
      if (clip != null) {
        ReflectionHelpers.callInstanceMethod(ClipData.class, clip, "prepareToLeaveProcess");
      }
    }

    this.clip = clip;

    for (OnPrimaryClipChangedListener listener : listeners) {
      listener.onPrimaryClipChanged();
    }
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
    CharSequence text = directlyOn(realClipboardManager, ClipboardManager.class).getText();
    return text != null && text.length() > 0;
  }
}
