package org.robolectric.shadows.util;

import android.content.Context;
import android.os.Looper;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.res.ResourceLoader;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.tester.org.apache.http.FakeHttpLayer;
import org.robolectric.util.Scheduler;

import static org.robolectric.Shadows.shadowOf;

//TODO: Give me a better name
public class MagicObject {

  public static Scheduler getUiThreadScheduler() {
    return shadowOf(Looper.getMainLooper()).getScheduler();
  }

  public static Scheduler getBackgroundScheduler() {
    return getShadowApplication().getBackgroundScheduler();
  }

  public static FakeHttpLayer getFakeHttpLayer() {
    return getShadowApplication().getFakeHttpLayer();
  }

  public static ResourceLoader getResourceLoader() {
    return getShadowApplication().getResourceLoader();
  }

  public static ShadowApplication getShadowApplication() {
    return RuntimeEnvironment.application == null ? null : shadowOf(RuntimeEnvironment.application);
  }
}
