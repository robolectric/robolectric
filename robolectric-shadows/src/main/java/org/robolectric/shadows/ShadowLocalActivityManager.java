package org.robolectric.shadows;

import android.app.LocalActivityManager;
import org.robolectric.annotation.Implements;

/**
 *
 */
@Implements(LocalActivityManager.class)
public class ShadowLocalActivityManager {

  //@Implementation
  //public Window startActivity(String id, Intent intent) {
  //  try {
  //    final String clazz = intent.getComponent().getClassName();
  //    final Class<? extends Activity> aClass = (Class<? extends Activity>) Class.forName(clazz);
  //    final Constructor<? extends Activity> ctor = aClass.getConstructor();
  //    Activity activity = ctor.newInstance();
  //    final Method onCreateMethod = aClass.getDeclaredMethod("onCreate", Bundle.class);
  //    onCreateMethod.setAccessible(true);
  //    onCreateMethod.invoke(activity, (Bundle) null);
  //    return activity.getWindow();
  //  } catch (Exception e) {
  //    throw new RuntimeException("Unable to create class", e);
  //  }
  //}

}
