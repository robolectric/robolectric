package org.robolectric;

import android.app.AppComponentFactory;
import android.content.BroadcastReceiver;
import android.content.Intent;
import org.robolectric.CustomConstructorReceiverWrapper.CustomConstructorWithEmptyActionReceiver;
import org.robolectric.CustomConstructorReceiverWrapper.CustomConstructorWithOneActionReceiver;

public final class CustomAppComponentFactory extends AppComponentFactory {
  @Override
  public BroadcastReceiver instantiateReceiver(ClassLoader cl, String className, Intent intent)
      throws InstantiationException, IllegalAccessException, ClassNotFoundException {
    if (className != null) {
      if (className.contains(CustomConstructorWithOneActionReceiver.class.getName())) {
        return new CustomConstructorWithOneActionReceiver(100);
      } else if (className.contains(CustomConstructorWithEmptyActionReceiver.class.getName())) {
        return new CustomConstructorWithEmptyActionReceiver(100);
      }
    }
    return super.instantiateReceiver(cl, className, intent);
  }
}
