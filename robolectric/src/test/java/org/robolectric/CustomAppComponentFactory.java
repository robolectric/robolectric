package org.robolectric;

import android.app.Activity;
import android.app.AppComponentFactory;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentProvider;
import android.content.Intent;
import javax.annotation.Nonnull;
import org.robolectric.CustomConstructorReceiverWrapper.CustomConstructorWithEmptyActionReceiver;
import org.robolectric.CustomConstructorReceiverWrapper.CustomConstructorWithOneActionReceiver;
import org.robolectric.CustomConstructorServices.CustomConstructorIntentService;
import org.robolectric.CustomConstructorServices.CustomConstructorJobService;
import org.robolectric.CustomConstructorServices.CustomConstructorService;

public final class CustomAppComponentFactory extends AppComponentFactory {
  @Nonnull
  @Override
  public BroadcastReceiver instantiateReceiver(
      @Nonnull ClassLoader cl, @Nonnull String className, Intent intent)
      throws InstantiationException, IllegalAccessException, ClassNotFoundException {
    if (className.contains(CustomConstructorWithOneActionReceiver.class.getName())) {
      return new CustomConstructorWithOneActionReceiver(100);
    } else if (className.contains(CustomConstructorWithEmptyActionReceiver.class.getName())) {
      return new CustomConstructorWithEmptyActionReceiver(100);
    }
    return super.instantiateReceiver(cl, className, intent);
  }

  @Nonnull
  @Override
  public Service instantiateService(
      @Nonnull ClassLoader cl, @Nonnull String className, Intent intent)
      throws InstantiationException, IllegalAccessException, ClassNotFoundException {
    if (className.contains(CustomConstructorService.class.getName())) {
      return new CustomConstructorService(100);
    } else if (className.contains(CustomConstructorIntentService.class.getName())) {
      return new CustomConstructorIntentService(100);
    } else if (className.contains(CustomConstructorJobService.class.getName())) {
      return new CustomConstructorJobService(100);
    }
    return super.instantiateService(cl, className, intent);
  }

  @Nonnull
  @Override
  public ContentProvider instantiateProvider(@Nonnull ClassLoader cl, @Nonnull String className)
      throws InstantiationException, IllegalAccessException, ClassNotFoundException {
    if (className.contains(CustomConstructorContentProvider.class.getName())) {
      return new CustomConstructorContentProvider(100);
    }
    return super.instantiateProvider(cl, className);
  }

  @Nonnull
  @Override
  public Activity instantiateActivity(
      @Nonnull ClassLoader cl, @Nonnull String className, Intent intent)
      throws InstantiationException, IllegalAccessException, ClassNotFoundException {
    if (className.contains(CustomConstructorActivity.class.getName())) {
      return new CustomConstructorActivity(100);
    }
    return super.instantiateActivity(cl, className, intent);
  }
}
