package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.shadow.api.Shadow.directlyOn;
import static org.robolectric.shadow.api.Shadow.invokeConstructor;
import static org.robolectric.util.ReflectionHelpers.getField;
import static org.robolectric.util.ReflectionHelpers.setField;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.os.RemoteException;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Choreographer;
import android.view.IWindowFocusObserver;
import android.view.IWindowId;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.WindowId;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import java.io.PrintStream;
import java.lang.reflect.Method;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.AccessibilityUtil;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.TimeUtils;

@Implements(View.class)
public class ShadowView {

  @RealObject
  protected View realView;

  private View.OnClickListener onClickListener;
  private View.OnLongClickListener onLongClickListener;
  private View.OnFocusChangeListener onFocusChangeListener;
  private View.OnSystemUiVisibilityChangeListener onSystemUiVisibilityChangeListener;
  private boolean wasInvalidated;
  private View.OnTouchListener onTouchListener;
  protected AttributeSet attributeSet;
  public Point scrollToCoordinates = new Point();
  private boolean didRequestLayout;
  private MotionEvent lastTouchEvent;
  private float scaleX = 1.0f;
  private float scaleY = 1.0f;
  private int hapticFeedbackPerformed = -1;
  private boolean onLayoutWasCalled;
  private View.OnCreateContextMenuListener onCreateContextMenuListener;
  private Rect globalVisibleRect;

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
   * @return Textual representation of the appearance of the object.
   */
  public static String visualize(View view) {
    Canvas canvas = new Canvas();
    view.draw(canvas);
    return shadowOf(canvas).getDescription();
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
   * @return Text contained within this view.
   */
  @SuppressWarnings("UnusedDeclaration")
  public static String innerText(View view) {
    return shadowOf(view).innerText();
  }

  @Implementation
  public void __constructor__(Context context, AttributeSet attributeSet, int defStyle) {
    if (context == null) throw new NullPointerException("no context");
    this.attributeSet = attributeSet;
    invokeConstructor(View.class, realView,
        ClassParameter.from(Context.class, context),
        ClassParameter.from(AttributeSet.class, attributeSet),
        ClassParameter.from(int.class, defStyle));
  }

  /**
   * Build drawable, either LayerDrawable or BitmapDrawable.
   *
   * @param resourceId Resource id
   * @return Drawable
   */
  protected Drawable buildDrawable(int resourceId) {
    return realView.getResources().getDrawable(resourceId);
  }

  /**
   * This will be removed in Robolectric 3.4 use {@link RuntimeEnvironment#getQualifiers()} instead, however
   * the correct way to configure qualifiers is using {@link Config#qualifiers()} so a constant can be used if this
   * is important to your tests. However, qualifier strings are typically just used to initialize the test environment
   * in a certain configuration. {@link android.content.res.Configuration} changes should be managed through
   * {@link org.robolectric.android.controller.ActivityController#configurationChange(android.content.res.Configuration)}
   * @deprecated
   */
  @Deprecated
  protected String getQualifiers() {
    return RuntimeEnvironment.getQualifiers();
  }

  /**
   * @return the resource ID of this view's background
   * @deprecated Use FEST assertions instead.
   */
  @Deprecated
  public int getBackgroundResourceId() {
    Drawable drawable = realView.getBackground();
    return drawable instanceof BitmapDrawable
        ? shadowOf(((BitmapDrawable) drawable).getBitmap()).getCreatedFromResId()
        : -1;
  }

  /**
   * @return the color of this view's background, or 0 if it's not a solid color
   * @deprecated Use FEST assertions instead.
   */
  @Deprecated
  public int getBackgroundColor() {
    Drawable drawable = realView.getBackground();
    return drawable instanceof ColorDrawable ? ((ColorDrawable) drawable).getColor() : 0;
  }

  @HiddenApi
  @Implementation
  public void computeOpaqueFlags() {
  }

  @Implementation
  public void setOnFocusChangeListener(View.OnFocusChangeListener l) {
    onFocusChangeListener = l;
    directly().setOnFocusChangeListener(l);
  }

  @Implementation
  public void setOnClickListener(View.OnClickListener onClickListener) {
    this.onClickListener = onClickListener;
    directly().setOnClickListener(onClickListener);
  }

  @Implementation
  public void setOnLongClickListener(View.OnLongClickListener onLongClickListener) {
    this.onLongClickListener = onLongClickListener;
    directly().setOnLongClickListener(onLongClickListener);
  }

  @Implementation
  public void setOnSystemUiVisibilityChangeListener(View.OnSystemUiVisibilityChangeListener onSystemUiVisibilityChangeListener) {
    this.onSystemUiVisibilityChangeListener = onSystemUiVisibilityChangeListener;
    directly().setOnSystemUiVisibilityChangeListener(onSystemUiVisibilityChangeListener);
  }

  @Implementation
  public void setOnCreateContextMenuListener(View.OnCreateContextMenuListener onCreateContextMenuListener) {
    this.onCreateContextMenuListener = onCreateContextMenuListener;
    directly().setOnCreateContextMenuListener(onCreateContextMenuListener);
  }

  @Implementation
  public void draw(android.graphics.Canvas canvas) {
    Drawable background = realView.getBackground();
    if (background != null) {
      shadowOf(canvas).appendDescription("background:");
      background.draw(canvas);
    }
  }

  @Implementation
  public void onLayout(boolean changed, int left, int top, int right, int bottom) {
    onLayoutWasCalled = true;
    directlyOn(realView, View.class, "onLayout",
        ClassParameter.from(boolean.class, changed),
        ClassParameter.from(int.class, left),
        ClassParameter.from(int.class, top),
        ClassParameter.from(int.class, right),
        ClassParameter.from(int.class, bottom));
  }

  public boolean onLayoutWasCalled() {
    return onLayoutWasCalled;
  }

  @Implementation
  public void requestLayout() {
    didRequestLayout = true;
    directly().requestLayout();
  }

  public boolean didRequestLayout() {
    return didRequestLayout;
  }

  public void setDidRequestLayout(boolean didRequestLayout) {
    this.didRequestLayout = didRequestLayout;
  }

  public void setViewFocus(boolean hasFocus) {
    if (onFocusChangeListener != null) {
      onFocusChangeListener.onFocusChange(realView, hasFocus);
    }
  }

  @Implementation
  public void invalidate() {
    wasInvalidated = true;
    directly().invalidate();
  }

  @Implementation
  public boolean onTouchEvent(MotionEvent event) {
    lastTouchEvent = event;
    return directly().onTouchEvent(event);
  }

  @Implementation
  public void setOnTouchListener(View.OnTouchListener onTouchListener) {
    this.onTouchListener = onTouchListener;
    directly().setOnTouchListener(onTouchListener);
  }

  public MotionEvent getLastTouchEvent() {
    return lastTouchEvent;
  }

  /**
   * Returns a string representation of this {@code View}. Unless overridden, it will be an empty string.
   *
   * Robolectric extension.
   * @return String representation of this view.
   */
  public String innerText() {
    return "";
  }

  /**
   * Dumps the status of this {@code View} to {@code System.out}
   */
  public void dump() {
    dump(System.out, 0);
  }

  /**
   * Dumps the status of this {@code View} to {@code System.out} at the given indentation level
   * @param out Output stream.
   * @param indent Indentation level.
   */
  public void dump(PrintStream out, int indent) {
    dumpFirstPart(out, indent);
    out.println("/>");
  }

  protected void dumpFirstPart(PrintStream out, int indent) {
    dumpIndent(out, indent);

    out.print("<" + realView.getClass().getSimpleName());
    dumpAttributes(out);
  }

  protected void dumpAttributes(PrintStream out) {
    if (realView.getId() > 0) {
      dumpAttribute(out, "id", realView.getContext().getResources().getResourceName(realView.getId()));
    }

    switch (realView.getVisibility()) {
      case View.VISIBLE:
        break;
      case View.INVISIBLE:
        dumpAttribute(out, "visibility", "INVISIBLE");
        break;
      case View.GONE:
        dumpAttribute(out, "visibility", "GONE");
        break;
    }
  }

  protected void dumpAttribute(PrintStream out, String name, String value) {
    out.print(" " + name + "=\"" + (value == null ? null : TextUtils.htmlEncode(value)) + "\"");
  }

  protected void dumpIndent(PrintStream out, int indent) {
    for (int i = 0; i < indent; i++) out.print(" ");
  }

  /**
   * @return whether or not {@link #invalidate()} has been called
   */
  public boolean wasInvalidated() {
    return wasInvalidated;
  }

  /**
   * Clears the wasInvalidated flag
   */
  public void clearWasInvalidated() {
    wasInvalidated = false;
  }

  /**
   * Utility method for clicking on views exposing testing scenarios that are not possible when using the actual app.
   *
   * @throws RuntimeException if the view is disabled or if the view or any of its parents are not visible.
   * @return Return value of the underlying click operation.
   */
  public boolean checkedPerformClick() {
    if (!realView.isShown()) {
      throw new RuntimeException("View is not visible and cannot be clicked");
    }
    if (!realView.isEnabled()) {
      throw new RuntimeException("View is not enabled and cannot be clicked");
    }

    AccessibilityUtil.checkViewIfCheckingEnabled(realView);
    return realView.performClick();
  }

  /**
   * @return Touch listener, if set.
   */
  public View.OnTouchListener getOnTouchListener() {
    return onTouchListener;
  }

  /**
   * @return Returns click listener, if set.
   */
  public View.OnClickListener getOnClickListener() {
    return onClickListener;
  }

  /**
   * @return Returns long click listener, if set.
   */
  public View.OnLongClickListener getOnLongClickListener() {
    return onLongClickListener;
  }

  /**
   * @return Returns system ui visibility change listener.
   */
  public View.OnSystemUiVisibilityChangeListener getOnSystemUiVisibilityChangeListener() {
    return onSystemUiVisibilityChangeListener;
  }

  /**
   * @return Returns create ContextMenu listener, if set.
   */
  public View.OnCreateContextMenuListener getOnCreateContextMenuListener() {
    return onCreateContextMenuListener;
  }

  @Implementation
  public Bitmap getDrawingCache() {
    return ReflectionHelpers.callConstructor(Bitmap.class);
  }

  @Implementation
  public void post(Runnable action) {
    ShadowApplication.getInstance().getForegroundThreadScheduler().post(action);
  }

  @Implementation
  public void postDelayed(Runnable action, long delayMills) {
    ShadowApplication.getInstance().getForegroundThreadScheduler().postDelayed(action, delayMills);
  }

  @Implementation
  public void postInvalidateDelayed(long delayMilliseconds) {
    ShadowApplication.getInstance().getForegroundThreadScheduler().postDelayed(new Runnable() {
      @Override
      public void run() {
        realView.invalidate();
      }
    }, delayMilliseconds);
  }

  @Implementation
  public void removeCallbacks(Runnable callback) {
    shadowOf(Looper.getMainLooper()).getScheduler().remove(callback);
  }

  @Implementation
  public void scrollTo(int x, int y) {
    try {
      Method method = View.class.getDeclaredMethod("onScrollChanged", new Class[]{int.class, int.class, int.class, int.class});
      method.setAccessible(true);
      method.invoke(realView, x, y, scrollToCoordinates.x, scrollToCoordinates.y);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    scrollToCoordinates = new Point(x, y);
  }

  @Implementation
  public int getScrollX() {
    return scrollToCoordinates != null ? scrollToCoordinates.x : 0;
  }

  @Implementation
  public int getScrollY() {
    return scrollToCoordinates != null ? scrollToCoordinates.y : 0;
  }

  @Implementation
  public void setScrollX(int scrollX) {
    scrollTo(scrollX, scrollToCoordinates.y);
  }

  @Implementation
  public void setScrollY(int scrollY) {
    scrollTo(scrollToCoordinates.x, scrollY);
  }

  @Implementation
  public void setAnimation(final Animation animation) {
    directly().setAnimation(animation);

    if (animation != null) {
      new AnimationRunner(animation);
    }
  }

  private AnimationRunner animationRunner;

  private class AnimationRunner implements Runnable {
    private final Animation animation;
    private long startTime, startOffset, elapsedTime;

    AnimationRunner(Animation animation) {
      this.animation = animation;
      start();
    }

    private void start() {
      startTime = animation.getStartTime();
      startOffset = animation.getStartOffset();
      Choreographer choreographer = ShadowChoreographer.getInstance();
      if (animationRunner != null) {
        choreographer.removeCallbacks(Choreographer.CALLBACK_ANIMATION, animationRunner, null);
      }
      animationRunner = this;
      int startDelay;
      if (startTime == Animation.START_ON_FIRST_FRAME) {
        startDelay = (int) startOffset;
      } else {
        startDelay = (int) ((startTime + startOffset) - SystemClock.uptimeMillis());
      }
      choreographer.postCallbackDelayed(Choreographer.CALLBACK_ANIMATION, this, null, startDelay);
    }

    @Override
    public void run() {
      // Abort if start time has been messed with, as this simulation is only designed to handle
      // standard situations.
      if ((animation.getStartTime() == startTime && animation.getStartOffset() == startOffset) &&
          animation.getTransformation(startTime == Animation.START_ON_FIRST_FRAME ?
              SystemClock.uptimeMillis() : (startTime + startOffset + elapsedTime), new Transformation()) &&
              // We can't handle infinitely repeating animations in the current scheduling model,
              // so abort after one iteration.
              !(animation.getRepeatCount() == Animation.INFINITE && elapsedTime >= animation.getDuration())) {
        // Update startTime if it had a value of Animation.START_ON_FIRST_FRAME
        startTime = animation.getStartTime();
        elapsedTime += ShadowChoreographer.getFrameInterval() / TimeUtils.NANOS_PER_MS;
        ShadowChoreographer.getInstance().postCallback(Choreographer.CALLBACK_ANIMATION, this, null);
      } else {
        animationRunner = null;
      }
    }
  }

  @Implementation
  public boolean isAttachedToWindow() {
    return getAttachInfo() != null;
  }

  private Object getAttachInfo() {
    return getField(realView, "mAttachInfo");
  }

  public void callOnAttachedToWindow() {
    invokeReflectively("onAttachedToWindow");
  }

  public void callOnDetachedFromWindow() {
    invokeReflectively("onDetachedFromWindow");
  }

  @Implementation(minSdk = JELLY_BEAN_MR2)
  public Object getWindowId() {
    return WindowIdHelper.getWindowId(this);
  }

  private void invokeReflectively(String methodName) {
    ReflectionHelpers.callInstanceMethod(realView, methodName);
  }

  @Implementation
  public boolean performHapticFeedback(int hapticFeedbackType) {
    hapticFeedbackPerformed = hapticFeedbackType;
    return true;
  }

  @Implementation
  public boolean getGlobalVisibleRect(Rect rect, Point globalOffset) {
    if (globalVisibleRect == null) {
      return directly().getGlobalVisibleRect(rect, globalOffset);
    }

    if (!globalVisibleRect.isEmpty()) {
      rect.set(globalVisibleRect);
      if (globalOffset != null) {
        rect.offset(-globalOffset.x, -globalOffset.y);
      }
      return true;
    }
    rect.setEmpty();
    return false;
  }

  public void setGlobalVisibleRect(Rect rect) {
    if (rect != null) {
      globalVisibleRect = new Rect();
      globalVisibleRect.set(rect);
    } else {
      globalVisibleRect = null;
    }
  }

  public int lastHapticFeedbackPerformed() {
    return hapticFeedbackPerformed;
  }

  public void setMyParent(ViewParent viewParent) {
    directlyOn(realView, View.class, "assignParent", ClassParameter.from(ViewParent.class, viewParent));
  }

  private View directly() {
    return directlyOn(realView, View.class);
  }

  public static class WindowIdHelper {
    public static Object getWindowId(ShadowView shadowView) {
      if (shadowView.isAttachedToWindow()) {
        Object attachInfo = shadowView.getAttachInfo();
        if (getField(attachInfo, "mWindowId") == null) {
          IWindowId iWindowId = new MyIWindowIdStub();
          setField(attachInfo, "mWindowId", new WindowId(iWindowId));
          setField(attachInfo, "mIWindowId", iWindowId);
        }
      }

      return shadowView.directly().getWindowId();
    }

    private static class MyIWindowIdStub extends IWindowId.Stub {
      @Override
      public void registerFocusObserver(IWindowFocusObserver iWindowFocusObserver) throws RemoteException {
      }

      @Override
      public void unregisterFocusObserver(IWindowFocusObserver iWindowFocusObserver) throws RemoteException {
      }

      @Override
      public boolean isFocused() throws RemoteException {
        return true;
      }
    }
  }
}
