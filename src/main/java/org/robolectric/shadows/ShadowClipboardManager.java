package org.robolectric.shadows;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.ClipboardManager.OnPrimaryClipChangedListener;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import java.util.ArrayList;
import java.util.Collection;

import static org.robolectric.Robolectric.directlyOn;

@SuppressWarnings("UnusedDeclaration")
@Implements(ClipboardManager.class)
public class ShadowClipboardManager {

  @RealObject
  private ClipboardManager realClipboardManager;

  private final Collection<OnPrimaryClipChangedListener> listeners =
      new ArrayList<OnPrimaryClipChangedListener>();
  private ClipData clip;

  @Implementation
  public void setPrimaryClip(ClipData clip) {
    if (clip != null) {
       clip.prepareToLeaveProcess();
    }
    this.clip = clip;

    // Synchronously copy the listeners, then handle the change event for each.
    OnPrimaryClipChangedListener[] listenersCopy;
    synchronized (listeners) {
      listenersCopy = new OnPrimaryClipChangedListener[listeners.size()];
      listenersCopy = listeners.toArray(listenersCopy);
    }
    for (OnPrimaryClipChangedListener listener : listenersCopy) {
      listener.onPrimaryClipChanged();
    }
  }

  @Implementation
  public ClipData getPrimaryClip() {
    return clip;
  }

  @Implementation
  public ClipDescription getPrimaryClipDescription() {
    return clip == null ? null : clip.getDescription();
  }

  @Implementation
  public boolean hasPrimaryClip() {
    return clip != null;
  }

  @Implementation
  public void addPrimaryClipChangedListener(OnPrimaryClipChangedListener what) {
    synchronized (listeners) {
      listeners.add(what);
    }
  }

  @Implementation
  public void removePrimaryClipChangedListener(OnPrimaryClipChangedListener what) {
    synchronized (listeners) {
      listeners.remove(what);
    }
  }

  @Implementation
  public void setText(CharSequence text) {
    setPrimaryClip(ClipData.newPlainText(null, text));
  }

  @Implementation
  public boolean hasText() {
    CharSequence text = directlyOn(realClipboardManager, ClipboardManager.class).getText();
    return text != null && text.length() > 0;
  }
}
