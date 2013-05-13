package org.robolectric.shadows;

import android.app.Activity;
import android.app.LocalActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 *
 */
@Implements(LocalActivityManager.class)
public class ShadowLocalActivityManager {

  @Implementation
  public Window startActivity(String id, Intent intent) {
    try {
      final String clazz = intent.getComponent().getClassName();
      final Class<? extends Activity> aClass = (Class<? extends Activity>) Class.forName(clazz);
      final Constructor<? extends Activity> ctor = aClass.getConstructor();
      Activity activity = ctor.newInstance();
      final Method onCreateMethod = aClass.getDeclaredMethod("onCreate", Bundle.class);
      onCreateMethod.setAccessible(true);
      onCreateMethod.invoke(activity, (Bundle) null);
      return activity.getWindow();
    } catch (Exception e) {
      throw new RuntimeException("Unable to create class", e);
    }
  }

}
