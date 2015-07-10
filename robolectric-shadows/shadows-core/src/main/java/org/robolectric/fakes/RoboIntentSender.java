package org.robolectric.fakes;

import android.content.IIntentSender;
import android.content.Intent;
import android.content.IntentSender;

/**
 * Robolectric implementation of {@link android.content.IntentSender}.
 */
public class RoboIntentSender extends IntentSender {
  public Intent intent;

  public RoboIntentSender() {
    super((IIntentSender)null);
  }
}
