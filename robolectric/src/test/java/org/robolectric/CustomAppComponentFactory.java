package org.robolectric;

import android.app.AppComponentFactory;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import org.robolectric.CustomConstructorReceiverWrapper.CustomConstructorWithEmptyActionReceiver;
import org.robolectric.CustomConstructorReceiverWrapper.CustomConstructorWithOneActionReceiver;
import org.robolectric.CustomConstructorServices.CustomConstructorIntentService;
import org.robolectric.CustomConstructorServices.CustomConstructorJobService;
import org.robolectric.CustomConstructorServices.CustomConstructorService;

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

  @Override
  public Service instantiateService(ClassLoader cl, String className, Intent intent)
      throws InstantiationException, IllegalAccessException, ClassNotFoundException {
    if (className != null) {
      if (className.contains(CustomConstructorService.class.getName())) {
        return new CustomConstructorService(100);
      } else if (className.contains(CustomConstructorIntentService.class.getName())) {
        return new CustomConstructorIntentService(100);
      } else if (className.contains(CustomConstructorJobService.class.getName())) {
        return new CustomConstructorJobService(100);
      }
    }
    return super.instantiateService(cl, className, intent);
  }
}
