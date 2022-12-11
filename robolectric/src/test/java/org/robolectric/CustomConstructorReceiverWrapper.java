package org.robolectric;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class CustomConstructorReceiverWrapper {
  private static class CustomConstructorReceiver extends BroadcastReceiver {
    private final int intValue;

    public CustomConstructorReceiver(int intValue) {
      // We don't use intValue actually, and we only want to use this class to test the
      // initialization of BroadcastReceiver with a custom constructor.
      this.intValue = intValue;
    }

    @Override
    public void onReceive(Context context, Intent intent) {}
  }

  public static class CustomConstructorWithOneActionReceiver extends CustomConstructorReceiver {
    public CustomConstructorWithOneActionReceiver(int intValue) {
      super(intValue);
    }
  }

  public static class CustomConstructorWithEmptyActionReceiver extends CustomConstructorReceiver {
    public CustomConstructorWithEmptyActionReceiver(int intValue) {
      super(intValue);
    }
  }
}
