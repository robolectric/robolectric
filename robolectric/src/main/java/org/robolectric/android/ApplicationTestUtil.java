package org.robolectric.android;

import android.app.Application;
import android.content.Context;
import org.robolectric.util.ReflectionHelpers;

public class ApplicationTestUtil {

  /**
   * Creates a new {@link Application} and attaches it with a base context obtained from context.
   */
  public static <T extends Application> T buildApplication(Class<T> applicationClass, Context context) {
    T application = newApplication(applicationClass);
    attach(application, context);
    return application;
  }


  public static <T extends Application> T newApplication(Class<T> applicationClass) {
    return ReflectionHelpers.callConstructor(applicationClass);
  }

  /**
   * Attaches an application to a base context.
   * @param application The application to attach.
   * @param context The context with which to initialize the application, whose base context will
   *                be attached to the application
   */
  public static void attach(Application application, Context context) {
    ReflectionHelpers.callInstanceMethod(Application.class, application, "attach",
        ReflectionHelpers.ClassParameter.from(Context.class, context));
  }
}
