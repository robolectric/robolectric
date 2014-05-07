package org.robolectric.shadows;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import static org.robolectric.Robolectric.directlyOn;

@SuppressWarnings("UnusedDeclaration")
@Implements(ClipboardManager.class)
public class ShadowClipboardManager {

  @RealObject
  private ClipboardManager realClipboardManager;

  private ClipData clip;

  @Implementation
  public void setPrimaryClip(ClipData clip) {
    if (clip != null) {
       clip.prepareToLeaveProcess();
    }
    this.clip = clip;
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
  public boolean hasText() {
    CharSequence text = directlyOn(realClipboardManager, ClipboardManager.class).getText();
    return text != null && text.length() > 0;
  }
}
