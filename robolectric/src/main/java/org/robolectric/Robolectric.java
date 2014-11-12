package org.robolectric;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Looper;
import android.view.Display;
import android.view.View;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implements;
import org.robolectric.bytecode.RobolectricInternals;
import org.robolectric.bytecode.ShadowWrangler;
import org.robolectric.internal.ReflectionHelpers;
import org.robolectric.res.ResourceLoader;
import org.robolectric.res.builder.RobolectricPackageManager;
import org.robolectric.shadows.HttpResponseGenerator;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowDefaultRequestDirector;
import org.robolectric.shadows.ShadowLooper;
import org.robolectric.tester.org.apache.http.FakeHttpLayer;
import org.robolectric.tester.org.apache.http.HttpRequestInfo;
import org.robolectric.tester.org.apache.http.RequestMatcher;
import org.robolectric.util.ActivityController;
import org.robolectric.util.Scheduler;
import org.robolectric.util.ServiceController;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import static org.robolectric.Shadows.shadowOf;

public class Robolectric {
  public static Application application;
  public static Object activityThread;
  public static RobolectricPackageManager packageManager;

  public static <T> T newInstanceOf(Class<T> clazz) {
    return RobolectricInternals.newInstanceOf(clazz);
  }

  public static Object newInstanceOf(String className) {
    try {
      Class<?> clazz = Class.forName(className);
      if (clazz != null) {
        return newInstanceOf(clazz);
      }
    } catch (ClassNotFoundException e) {
    }
    return null;
  }

  public static <T> T newInstance(Class<T> clazz, Class[] parameterTypes, Object[] params) {
    return RobolectricInternals.newInstance(clazz, parameterTypes, params);
  }

  public static <T> T directlyOn(T shadowedObject, Class<T> clazz) {
    return RobolectricInternals.directlyOn(shadowedObject, clazz);
  }

  public static <R> R directlyOn(Object shadowedObject, String clazzName, String methodName, ReflectionHelpers.ClassParameter... paramValues) {
    try {
      Class<Object> aClass = (Class<Object>) shadowedObject.getClass().getClassLoader().loadClass(clazzName);
      return directlyOn(shadowedObject, aClass, methodName, paramValues);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public static <R, T> R directlyOn(T shadowedObject, Class<T> clazz, String methodName, ReflectionHelpers.ClassParameter... paramValues) {
    return directlyOnInternal(shadowedObject, clazz, methodName, shadowedObject.getClass(), paramValues);
  }

  public static <R, T> R directlyOn(Class<T> clazz, String methodName, ReflectionHelpers.ClassParameter... paramValues) {
    return directlyOnInternal(null, clazz, methodName, clazz, paramValues);
  }

  private static <R, T> R directlyOnInternal(Object shadowedObject, Class<T> clazz, String methodName, Class classHierarchyStart, ReflectionHelpers.ClassParameter... paramValues) {
    String directMethodName = RobolectricInternals.directMethodName(clazz.getName(), methodName);
    try {
      Class[] classes = ReflectionHelpers.ClassParameter.getClasses(paramValues);
      Object[] values = ReflectionHelpers.ClassParameter.getValues(paramValues);

      Class hierarchyTraversalClass = classHierarchyStart;
      while(hierarchyTraversalClass != null) {
        try {
          Method declaredMethod = hierarchyTraversalClass.getDeclaredMethod(directMethodName, classes);
          declaredMethod.setAccessible(true);
          return (R) declaredMethod.invoke(shadowedObject, values);
        } catch (NoSuchMethodException e) {
          hierarchyTraversalClass = hierarchyTraversalClass.getSuperclass();
        }
      }
      throw new RuntimeException(new NoSuchMethodException());
    } catch (InvocationTargetException e) {
      throw (RuntimeException) e.getTargetException();
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Runs any background tasks previously queued by {@link android.os.AsyncTask#execute(Object[])}.
   * <p/>
   * <p/>
   * Note: calling this method does not pause or un-pause the scheduler.
   */
  public static void runBackgroundTasks() {
    getBackgroundScheduler().advanceBy(0);
  }

  /**
   * Runs any immediately runnable tasks previously queued on the UI thread,
   * e.g. by {@link Activity#runOnUiThread(Runnable)} or {@link android.os.AsyncTask#onPostExecute(Object)}.
   * <p/>
   * <p/>
   * Note: calling this method does not pause or un-pause the scheduler.
   */
  public static void runUiThreadTasks() {
    getUiThreadScheduler().advanceBy(0);
  }

  public static void runUiThreadTasksIncludingDelayedTasks() {
    getUiThreadScheduler().advanceToLastPostedRunnable();
  }

  /**
   * Sets up an HTTP response to be returned by calls to Apache's {@code HttpClient} implementers.
   *
   * @param statusCode   the status code of the response
   * @param responseBody the body of the response
   * @param headers      optional headers for the request
   */
  public static void addPendingHttpResponse(int statusCode, String responseBody, Header... headers) {
    getFakeHttpLayer().addPendingHttpResponse(statusCode, responseBody, headers);
  }

  /**
   * Sets up an HTTP response to be returned by calls to Apache's {@code HttpClient} implementers.
   *
   * @param statusCode   the status code of the response
   * @param responseBody the body of the response
   * @param contentType  the contentType of the response
   * @deprecated use {@link #addPendingHttpResponse(int, String, Header...)} instead
   */
  public static void addPendingHttpResponseWithContentType(int statusCode, String responseBody, Header contentType) {
    getFakeHttpLayer().addPendingHttpResponse(statusCode, responseBody, contentType);
  }

  /**
   * Sets up an HTTP response to be returned by calls to Apache's {@code HttpClient} implementers.
   *
   * @param httpResponse the response
   */
  public static void addPendingHttpResponse(HttpResponse httpResponse) {
    getFakeHttpLayer().addPendingHttpResponse(httpResponse);
  }

  /**
   * Sets up an HTTP response to be returned by calls to Apache's {@code HttpClient} implementers.
   *
   * @param httpResponseGenerator an HttpResponseGenerator that will provide responses
   */
  public static void addPendingHttpResponse(HttpResponseGenerator httpResponseGenerator) {
    getFakeHttpLayer().addPendingHttpResponse(httpResponseGenerator);
  }

  /**
   * Accessor to obtain HTTP requests made during the current test in the order in which they were made.
   *
   * @param index index of the request to retrieve.
   * @return the requested request.
   */
  public static HttpRequest getSentHttpRequest(int index) {
    return getFakeHttpLayer().getSentHttpRequestInfo(index).getHttpRequest();
  }

  public static HttpRequest getLatestSentHttpRequest() {
    return ShadowDefaultRequestDirector.getLatestSentHttpRequest();
  }

  /**
   * Accessor to find out if HTTP requests were made during the current test.
   *
   * @return whether a request was made.
   */
  public static boolean httpRequestWasMade() {
    return getShadowApplication().getFakeHttpLayer().hasRequestInfos();
  }

  public static boolean httpRequestWasMade(String uri) {
    return getShadowApplication().getFakeHttpLayer().hasRequestMatchingRule(
        new FakeHttpLayer.UriRequestMatcher(uri));
  }

  /**
   * Accessor to obtain metadata for an HTTP request made during the current test in the order in which they were made.
   *
   * @param index index of the request to retrieve.
   * @return the requested request metadata.
   */
  public static HttpRequestInfo getSentHttpRequestInfo(int index) {
    return getFakeHttpLayer().getSentHttpRequestInfo(index);
  }

  /**
   * Accessor to obtain HTTP requests made during the current test in the order in which they were made.
   *
   * @return the requested request or null if there are none.
   */
  public static HttpRequest getNextSentHttpRequest() {
    HttpRequestInfo httpRequestInfo = getFakeHttpLayer().getNextSentHttpRequestInfo();
    return httpRequestInfo == null ? null : httpRequestInfo.getHttpRequest();
  }

  /**
   * Accessor to obtain metadata for an HTTP request made during the current test in the order in which they were made.
   *
   * @return the requested request metadata or null if there are none.
   */
  public static HttpRequestInfo getNextSentHttpRequestInfo() {
    return getFakeHttpLayer().getNextSentHttpRequestInfo();
  }

  /**
   * Adds an HTTP response rule. The response will be returned when the rule is matched.
   *
   * @param method   method to match.
   * @param uri      uri to match.
   * @param response response to return when a match is found.
   */
  public static void addHttpResponseRule(String method, String uri, HttpResponse response) {
    getFakeHttpLayer().addHttpResponseRule(method, uri, response);
  }

  /**
   * Adds an HTTP response rule with a default method of GET. The response will be returned when the rule is matched.
   *
   * @param uri      uri to match.
   * @param response response to return when a match is found.
   */
  public static void addHttpResponseRule(String uri, HttpResponse response) {
    getFakeHttpLayer().addHttpResponseRule(uri, response);
  }

  /**
   * Adds an HTTP response rule. The response will be returned when the rule is matched.
   *
   * @param uri      uri to match.
   * @param response response to return when a match is found.
   */
  public static void addHttpResponseRule(String uri, String response) {
    getFakeHttpLayer().addHttpResponseRule(uri, response);
  }

  /**
   * Adds an HTTP response rule. The response will be returned when the rule is matched.
   *
   * @param requestMatcher custom {@code RequestMatcher}.
   * @param response       response to return when a match is found.
   */
  public static void addHttpResponseRule(RequestMatcher requestMatcher, HttpResponse response) {
    getFakeHttpLayer().addHttpResponseRule(requestMatcher, response);
  }

  /**
   * Adds an HTTP response rule. For each time the rule is matched, responses will be shifted
   * off the list and returned. When all responses have been given and the rule is matched again,
   * an exception will be thrown.
   *
   * @param requestMatcher custom {@code RequestMatcher}.
   * @param responses      responses to return in order when a match is found.
   */
  public static void addHttpResponseRule(RequestMatcher requestMatcher, List<? extends HttpResponse> responses) {
    getFakeHttpLayer().addHttpResponseRule(requestMatcher, responses);
  }

  public static FakeHttpLayer getFakeHttpLayer() {
    return getShadowApplication().getFakeHttpLayer();
  }

  public static void setDefaultHttpResponse(int statusCode, String responseBody) {
    getFakeHttpLayer().setDefaultHttpResponse(statusCode, responseBody);
  }

  public static void setDefaultHttpResponse(HttpResponse defaultHttpResponse) {
    getFakeHttpLayer().setDefaultHttpResponse(defaultHttpResponse);
  }

  public static void clearHttpResponseRules() {
    getFakeHttpLayer().clearHttpResponseRules();
  }

  public static void clearPendingHttpResponses() {
    getFakeHttpLayer().clearPendingHttpResponses();
  }

  public static void pauseLooper(Looper looper) {
    ShadowLooper.pauseLooper(looper);
  }

  public static void unPauseLooper(Looper looper) {
    ShadowLooper.unPauseLooper(looper);
  }

  public static void pauseMainLooper() {
    ShadowLooper.pauseMainLooper();
  }

  public static void unPauseMainLooper() {
    ShadowLooper.unPauseMainLooper();
  }

  public static void idleMainLooper(long interval) {
    ShadowLooper.idleMainLooper(interval);
  }

  public static void idleMainLooperConstantly(boolean shouldIdleConstantly) {
    ShadowLooper.idleMainLooperConstantly(shouldIdleConstantly);
  }

  public static Scheduler getUiThreadScheduler() {
    return shadowOf(Looper.getMainLooper()).getScheduler();
  }

  public static Scheduler getBackgroundScheduler() {
    return getShadowApplication().getBackgroundScheduler();
  }

  public static ShadowApplication getShadowApplication() {
    return Robolectric.application == null ? null : shadowOf(Robolectric.application);
  }

  public static void setDisplayMetricsDensity(float densityMultiplier) {
    shadowOf(getShadowApplication().getResources()).setDensity(densityMultiplier);
  }

  public static void setDefaultDisplay(Display display) {
    shadowOf(getShadowApplication().getResources()).setDisplay(display);
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
    return shadowOf(view).checkedPerformClick();
  }

  /**
   * Returns a textual representation of the appearance of the object.
   *
   * @param view the view to visualize
   */
  public static String visualize(View view) {
    Canvas canvas = new Canvas();
    view.draw(canvas);
    return shadowOf(canvas).getDescription();
  }

  /**
   * Returns a textual representation of the appearance of the object.
   *
   * @param canvas the canvas to visualize
   */
  public static String visualize(Canvas canvas) {
    return shadowOf(canvas).getDescription();
  }

  /**
   * Returns a textual representation of the appearance of the object.
   *
   * @param bitmap the bitmap to visualize
   */
  public static String visualize(Bitmap bitmap) {
    return shadowOf(bitmap).getDescription();
  }

  /**
   * Emits an xml-like representation of the view to System.out.
   *
   * @param view the view to dump
   */
  @SuppressWarnings("UnusedDeclaration")
  public static void dump(View view) {
    shadowOf(view).dump();
  }

  /**
   * Returns the text contained within this view.
   *
   * @param view the view to scan for text
   */
  @SuppressWarnings("UnusedDeclaration")
  public static String innerText(View view) {
    return shadowOf(view).innerText();
  }

  public static ResourceLoader getResourceLoader(Context context) {
    return shadowOf(context.getApplicationContext()).getResourceLoader();
  }

  public static void reset(Config config) {
    Robolectric.application = null;
    Robolectric.packageManager = null;
    Robolectric.activityThread = null;

    Shadows.reset();
  }

  public static <T extends Service> ServiceController<T> buildService(Class<T> serviceClass) {
    return ServiceController.of(serviceClass);
  }

  public static <T extends Service> T setupService(Class<T> serviceClass) {
    return ServiceController.of(serviceClass).attach().create().get();
  }

  public static <T extends Activity> ActivityController<T> buildActivity(Class<T> activityClass) {
    return ActivityController.of(activityClass);
  }

  public static <T extends Activity> T setupActivity(Class<T> activityClass) {
    return ActivityController.of(activityClass).setup().get();
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
    shadowOf(application).checkActivities(checkActivities);
  }

  /**
   * Reflection helper methods.
   */
  public static class Reflection {
    public static <T> T newInstanceOf(Class<T> clazz) {
      return Robolectric.newInstanceOf(clazz);
    }

    public static Object newInstanceOf(String className) {
      return Robolectric.newInstanceOf(className);
    }

    public static void setFinalStaticField(Class classWhichContainsField, String fieldName, Object newValue) {
      ReflectionHelpers.setStaticFieldReflectively(classWhichContainsField, fieldName, newValue);
    }

    public static Object setFinalStaticField(Field field, Object newValue) {
      Object oldValue;

      try {
        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        oldValue = field.get(null);
        field.set(null, newValue);
      } catch (NoSuchFieldException e) {
        throw new RuntimeException(e);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }

      return oldValue;
    }
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
