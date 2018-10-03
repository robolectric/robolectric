package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static org.robolectric.RuntimeEnvironment.getApiLevel;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

@Implements(BroadcastReceiver.PendingResult.class)
public final class ShadowBroadcastPendingResult {
  @RealObject BroadcastReceiver.PendingResult pendingResult;

  static BroadcastReceiver.PendingResult create(int resultCode, String resultData, Bundle resultExtras, boolean ordered) {
    try {
      if (getApiLevel() <= JELLY_BEAN) {
        return BroadcastReceiver.PendingResult.class
            .getConstructor(int.class, String.class, Bundle.class, int.class, boolean.class, boolean.class, IBinder.class)
            .newInstance(
                resultCode,
                resultData,
                resultExtras,
                0 /* type */,
                ordered,
                false /*sticky*/,
                null /* ibinder token */);
      } else if (getApiLevel() <= LOLLIPOP_MR1) {
        return BroadcastReceiver.PendingResult.class
            .getConstructor(int.class, String.class, Bundle.class, int.class, boolean.class, boolean.class, IBinder.class, int.class)
            .newInstance(
                resultCode,
                resultData,
                resultExtras,
                0 /* type */,
                ordered,
                false /*sticky*/,
                null /* ibinder token */,
                0 /* userid */);

      } else {
        return new BroadcastReceiver.PendingResult(
            resultCode,
            resultData,
            resultExtras,
            0 /* type */,
            ordered,
            false /*sticky*/,
            null /* ibinder token */,
            0 /* userid */,
            0 /* flags */);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  static BroadcastReceiver.PendingResult createSticky(Intent intent) {
    try {
      if (getApiLevel() <= JELLY_BEAN) {
        return BroadcastReceiver.PendingResult.class
            .getConstructor(
                int.class,
                String.class,
                Bundle.class,
                int.class,
                boolean.class,
                boolean.class,
                IBinder.class)
            .newInstance(
                0 /*resultCode*/,
                intent.getDataString(),
                intent.getExtras(),
                0 /* type */,
                false /*ordered*/,
                true /*sticky*/,
                null /* ibinder token */);
      } else if (getApiLevel() <= LOLLIPOP_MR1) {
        return BroadcastReceiver.PendingResult.class
            .getConstructor(
                int.class,
                String.class,
                Bundle.class,
                int.class,
                boolean.class,
                boolean.class,
                IBinder.class,
                int.class)
            .newInstance(
                0 /*resultCode*/,
                intent.getDataString(),
                intent.getExtras(),
                0 /* type */,
                false /*ordered*/,
                true /*sticky*/,
                null /* ibinder token */,
                0 /* userid */);

      } else {
        return new BroadcastReceiver.PendingResult(
            0 /*resultCode*/,
            intent.getDataString(),
            intent.getExtras(),
            0 /* type */,
            false /*ordered*/,
            true /*sticky*/,
            null /* ibinder token */,
            0 /* userid */,
            0 /* flags */);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private final SettableFuture<BroadcastReceiver.PendingResult> finished = SettableFuture.create();

  @Implementation
  protected final void finish() {
    Preconditions.checkState(finished.set(pendingResult), "Broadcast already finished");
  }

  public ListenableFuture<BroadcastReceiver.PendingResult> getFuture() {
    return finished;
  }
}
