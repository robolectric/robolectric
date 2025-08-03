package org.robolectric.fakes;

import android.app.PendingIntent;
import android.content.Context;
import android.content.IIntentSender;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Handler;
import java.util.Objects;

/** Robolectric implementation of {@link android.content.IntentSender}. */
public class RoboIntentSender extends IntentSender {
  public Intent intent;
  private final PendingIntent pendingIntent;

  public RoboIntentSender(PendingIntent pendingIntent) {
    super((IIntentSender) null);
    this.pendingIntent = pendingIntent;
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof RoboIntentSender)) {
      return false;
    }
    return Objects.equals(pendingIntent, ((RoboIntentSender) other).pendingIntent);
  }

  @Override
  public int hashCode() {
    return pendingIntent.hashCode();
  }

  @Override
  public void sendIntent(
      Context context,
      int code,
      Intent intent,
      final OnFinished onFinished,
      Handler handler,
      String requiredPermission)
      throws SendIntentException {
    try {
      pendingIntent.send(
          context,
          code,
          intent,
          new PendingIntent.OnFinished() {
            @Override
            public void onSendFinished(
                PendingIntent pendingIntent,
                Intent intent,
                int resultCode,
                String resultData,
                Bundle resultExtras) {
              onFinished.onSendFinished(
                  RoboIntentSender.this, intent, resultCode, resultData, resultExtras);
            }
          },
          handler,
          requiredPermission);
    } catch (PendingIntent.CanceledException e) {
      throw new SendIntentException(e);
    }
  }

  @Override
  public void sendIntent(
      Context context, int code, Intent intent, OnFinished onFinished, Handler handler)
      throws SendIntentException {
    sendIntent(context, code, intent, onFinished, handler, null);
  }

  public PendingIntent getPendingIntent() {
    return pendingIntent;
  }
}
