package org.robolectric.integrationtests.sdkcompat;

import android.app.Activity;
import android.app.AppComponentFactory;
import android.app.Application;
import android.content.Intent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("NewApi")
public class TestAppComponentFactory extends AppComponentFactory {

  @Override
  public Application instantiateApplication(ClassLoader cl, String className)
      throws ClassNotFoundException, IllegalAccessException, InstantiationException {
    Application app = super.instantiateApplication(cl, className);
    ((TestApp) app).instantiatedWithAppFactory = true;
    return app;
  }

  @NotNull
  @Override
  public Activity instantiateActivity(
      @NotNull ClassLoader cl, @NotNull String className, @Nullable Intent intent)
      throws ClassNotFoundException, IllegalAccessException, InstantiationException {
    if (className.equals(MainActivity.class.getName())) {
      return new MainActivity(MainActivity.CreationSource.CUSTOM_CONSTRUCTOR);
    }
    return super.instantiateActivity(cl, className, intent);
  }
}
