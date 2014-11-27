package org.robolectric;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.Display;
import android.view.View;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implements;
import org.robolectric.res.ResourceLoader;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.util.ActivityController;
import org.robolectric.util.Scheduler;
import org.robolectric.util.ServiceController;
import org.robolectric.util.ShadowsAdapter;

public class Robolectric {

  /**
   * Runs any background tasks previously queued by {@link android.os.AsyncTask#execute(Object[])}.
   * <p/>
   * <p/>
   * Note: calling this method does not pause or un-pause the scheduler.
   */
  public static void runBackgroundTasks() {
    getBackgroundScheduler().advanceBy(0);
  }

  public static Scheduler getBackgroundScheduler() {
    return ShadowApplication.getInstance().getBackgroundScheduler();
  }

  public static ShadowApplication getShadowApplication() {
    return ShadowApplication.getInstance();
  }

  public static void setDisplayMetricsDensity(float densityMultiplier) {
    Shadows.shadowOf(getShadowApplication().getResources()).setDensity(densityMultiplier);
  }

  public static void setDefaultDisplay(Display display) {
    Shadows.shadowOf(getShadowApplication().getResources()).setDisplay(display);
  }

  /**
   * Calls {@code performClick()} on a {@code View} after ensuring that it and its ancestors are visible and that it
   * is enabled.
   *
   * @param view the view to click on
   * @return true if {@code View.OnClickListener}s were found and fired, false otherwise.
   * @throws RuntimeException if the preconditions are not met.
   */
  public static boolean clickOn(View view) {
    return Shadows.shadowOf(view).checkedPerformClick();
  }

  /**
   * Returns a textual representation of the appearance of the object.
   *
   * @param view the view to visualize
   */
  public static String visualize(View view) {
    Canvas canvas = new Canvas();
    view.draw(canvas);
    return Shadows.shadowOf(canvas).getDescription();
  }

  /**
   * Returns a textual representation of the appearance of the object.
   *
   * @param canvas the canvas to visualize
   */
  public static String visualize(Canvas canvas) {
    return Shadows.shadowOf(canvas).getDescription();
  }

  /**
   * Returns a textual representation of the appearance of the object.
   *
   * @param bitmap the bitmap to visualize
   */
  public static String visualize(Bitmap bitmap) {
    return Shadows.shadowOf(bitmap).getDescription();
  }

  /**
   * Emits an xml-like representation of the view to System.out.
   *
   * @param view the view to dump
   */
  @SuppressWarnings("UnusedDeclaration")
  public static void dump(View view) {
    org.robolectric.Shadows.shadowOf(view).dump();
  }

  /**
   * Returns the text contained within this view.
   *
   * @param view the view to scan for text
   */
  @SuppressWarnings("UnusedDeclaration")
  public static String innerText(View view) {
    return Shadows.shadowOf(view).innerText();
  }

  public static ResourceLoader getResourceLoader() {
    return ShadowApplication.getInstance().getResourceLoader();
  }

  public static void reset(Config config) {
    RuntimeEnvironment.application = null;
    RuntimeEnvironment.setRobolectricPackageManager(null);
    RuntimeEnvironment.setActivityThread(null);

    new ShadowsAdapter().reset();
  }

  public static <T extends Service> ServiceController<T> buildService(Class<T> serviceClass) {
    return ServiceController.of(new ShadowsAdapter(), serviceClass);
  }

  public static <T extends Service> T setupService(Class<T> serviceClass) {
    return ServiceController.of(new ShadowsAdapter(), serviceClass).attach().create().get();
  }

  public static <T extends Activity> ActivityController<T> buildActivity(Class<T> activityClass) {
    return ActivityController.of(new ShadowsAdapter(), activityClass);
  }

  public static <T extends Activity> T setupActivity(Class<T> activityClass) {
    return ActivityController.of(new ShadowsAdapter(), activityClass).setup().get();
  }

  /**
   * Set to true if you'd like Robolectric to strictly simulate the real Android behavior when
   * calling {@link Context#startActivity(android.content.Intent)}. Real Android throws a
   * {@link android.content.ActivityNotFoundException} if given
   * an {@link Intent} that is not known to the {@link android.content.pm.PackageManager}
   *
   * By default, this behavior is off (false).
   *
   * @param checkActivities
   */
  public static void checkActivities(boolean checkActivities) {
    Shadows.shadowOf(RuntimeEnvironment.application).checkActivities(checkActivities);
  }

  /**
   * Marker for shadow classes when the implementation class is unlinkable
   * @deprecated simply use the {@link Implements#className} attribute with no
   * {@link Implements#value} set.
   */
  @Deprecated 
  public interface Anything {
  }
}
