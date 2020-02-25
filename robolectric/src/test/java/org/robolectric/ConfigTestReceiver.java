package org.robolectric;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import java.util.ArrayList;
import java.util.List;

public class ConfigTestReceiver extends BroadcastReceiver {

  public List<Intent> intentsReceived = new ArrayList<>();

  @Override
  public void onReceive(Context context, Intent intent) {
    intentsReceived.add(intent);
  }

  static public class InnerReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

    }
  }
}
