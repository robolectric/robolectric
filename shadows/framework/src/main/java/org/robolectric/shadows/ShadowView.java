package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.N;
import static org.robolectric.shadow.api.Shadow.directlyOn;
import static org.robolectric.shadow.api.Shadow.invokeConstructor;
import static org.robolectric.shadows.ShadowLooper.shadowMainLooper;
import static org.robolectric.util.ReflectionHelpers.getField;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
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
import java.util.HashSet;
import java.util.Set;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.TimeUtils;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

@Implements(View.class)
@SuppressLint("NewApi")
public class ShadowView {

  @RealObject
  protected View realView;

  private View.OnClickListener onClickListener;
  private View.OnLongClickListener onLongClickListener;
  private View.OnFocusChangeListener onFocusChangeListener;
  private View.OnSystemUiVisibilityChangeListener onSystemUiVisibilityChangeListener;
  private final HashSet<View.OnAttachStateChangeListener> onAttachStateChangeListeners =
      new HashSet<>();
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
  private int layerType;

  /**
   * Calls {@code performClick()} on a {@code View} after ensuring that it and its ancestors are visible and that it
   * is enabled.
   *
   * @param view the view to click on
   * @return true if {@code View.OnClickListener}s were found and fired, false otherwise.
   * @throws RuntimeException if the preconditions are not met.
   * @deprecated Please use Espresso for view interactions
   */
  @Deprecated
  public static boolean clickOn(View view) {
    ShadowView shadowView = Shadow.extract(view);
    return shadowView.checkedPerformClick();
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
    ShadowCanvas shadowCanvas = Shadow.extract(canvas);
    return shadowCanvas.getDescription();
  }

  /**
   * Emits an xml-like representation of the view to System.out.
   *
   * @param view the view to dump.
   * @deprecated - Please use {@link androidx.test.espresso.util.HumanReadables#describe(View)}
   */
  @SuppressWarnings("UnusedDeclaration")
  @Deprecated
  public static void dump(View view) {
    ShadowView shadowView = Shadow.extract(view);
    shadowView.dump();
  }

  /**
   * Returns the text contained within this view.
   *
   * @param view the view to scan for text
   * @return Text contained within this view.
   */
  @SuppressWarnings("UnusedDeclaration")
  public static String innerText(View view) {
    ShadowView shadowView = Shadow.extract(view);
    return shadowView.innerText();
  }

  @Implementation
  protected void __constructor__(Context context, AttributeSet attributeSet, int defStyle) {
    if (context == null) throw new NullPointerException("no context");
    this.attributeSet = attributeSet;
    invokeConstructor(View.class, realView,
        ClassParameter.from(Context.class, context),
        ClassParameter.from(AttributeSet.class, attributeSet),
        ClassParameter.from(int.class, defStyle));
  }

  @Implementation
  protected void setLayerType(int layerType, Paint paint) {
    this.layerType = layerType;
  }

  @Implementation
  protected void setOnFocusChangeListener(View.OnFocusChangeListener l) {
    onFocusChangeListener = l;
    directly().setOnFocusChangeListener(l);
  }

  @Implementation
  protected void setOnClickListener(View.OnClickListener onClickListener) {
    this.onClickListener = onClickListener;
    directly().setOnClickListener(onClickListener);
  }

  @Implementation
  protected void setOnLongClickListener(View.OnLongClickListener onLongClickListener) {
    this.onLongClickListener = onLongClickListener;
    directly().setOnLongClickListener(onLongClickListener);
  }

  @Implementation
  protected void setOnSystemUiVisibilityChangeListener(
      View.OnSystemUiVisibilityChangeListener onSystemUiVisibilityChangeListener) {
    this.onSystemUiVisibilityChangeListener = onSystemUiVisibilityChangeListener;
    directly().setOnSystemUiVisibilityChangeListener(onSystemUiVisibilityChangeListener);
  }

  @Implementation
  protected void setOnCreateContextMenuListener(
      View.OnCreateContextMenuListener onCreateContextMenuListener) {
    this.onCreateContextMenuListener = onCreateContextMenuListener;
    directly().setOnCreateContextMenuListener(onCreateContextMenuListener);
  }

  @Implementation
  protected void addOnAttachStateChangeListener(
      View.OnAttachStateChangeListener onAttachStateChangeListener) {
    onAttachStateChangeListeners.add(onAttachStateChangeListener);
    directly().addOnAttachStateChangeListener(onAttachStateChangeListener);
  }

  @Implementation
  protected void removeOnAttachStateChangeListener(
      View.OnAttachStateChangeListener onAttachStateChangeListener) {
    onAttachStateChangeListeners.remove(onAttachStateChangeListener);
    directly().removeOnAttachStateChangeListener(onAttachStateChangeListener);
  }

  @Implementation
  protected void draw(android.graphics.Canvas canvas) {
    Drawable background = realView.getBackground();
    if (background != null) {
      ShadowCanvas shadowCanvas = Shadow.extract(canvas);
      shadowCanvas.appendDescription("background:");
      background.draw(canvas);
    }
  }

  @Implementation
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
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
  protected void requestLayout() {
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
  protected void invalidate() {
    wasInvalidated = true;
    directly().invalidate();
  }

  @Implementation
  protected boolean onTouchEvent(MotionEvent event) {
    lastTouchEvent = event;
    return directly().onTouchEvent(event);
  }

  @Implementation
  protected void setOnTouchListener(View.OnTouchListener onTouchListener) {
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
   * @deprecated - Please use {@link androidx.test.espresso.util.HumanReadables#describe(View)}
   */
  @Deprecated
  public void dump() {
    dump(System.out, 0);
  }

  /**
   * Dumps the status of this {@code View} to {@code System.out} at the given indentation level
   * @param out Output stream.
   * @param indent Indentation level.
   * @deprecated - Please use {@link androidx.test.espresso.util.HumanReadables#describe(View)}
   */
  @Deprecated
  public void dump(PrintStream out, int indent) {
    dumpFirstPart(out, indent);
    out.println("/>");
  }

  @Deprecated
  protected void dumpFirstPart(PrintStream out, int indent) {
    dumpIndent(out, indent);

    out.print("<" + realView.getClass().getSimpleName());
    dumpAttributes(out);
  }

  @Deprecated
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

  @Deprecated
  protected void dumpAttribute(PrintStream out, String name, String value) {
    out.print(" " + name + "=\"" + (value == null ? null : TextUtils.htmlEncode(value)) + "\"");
  }

  @Deprecated
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
   * If running with LooperMode PAUSED will also idle the main Looper.
   *
   * @throws RuntimeException if the view is disabled or if the view or any of its parents are not visible.
   * @return Return value of the underlying click operation.
   * @deprecated - Please use Espresso for View interactions.
   */
  @Deprecated
  public boolean checkedPerformClick() {
    if (!realView.isShown()) {
      throw new RuntimeException("View is not visible and cannot be clicked");
    }
    if (!realView.isEnabled()) {
      throw new RuntimeException("View is not enabled and cannot be clicked");
    }
    boolean res = realView.performClick();
    shadowMainLooper().idleIfPaused();
    return res;
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

  /** @return Returns the attached listeners, or the empty set if none are present. */
  public Set<View.OnAttachStateChangeListener> getOnAttachStateChangeListeners() {
    return onAttachStateChangeListeners;
  }

  // @Implementation
  // protected Bitmap getDrawingCache() {
  //   return ReflectionHelpers.callConstructor(Bitmap.class);
  // }

  @Implementation
  protected boolean post(Runnable action) {
    if (ShadowLooper.looperMode() == LooperMode.Mode.PAUSED) {
      return directly().post(action);
    } else {
      ShadowApplication.getInstance().getForegroundThreadScheduler().post(action);
      return true;
    }
  }

  @Implementation
  protected boolean postDelayed(Runnable action, long delayMills) {
    if (ShadowLooper.looperMode() == LooperMode.Mode.PAUSED) {
      return directly().postDelayed(action, delayMills);
    } else {
      ShadowApplication.getInstance()
          .getForegroundThreadScheduler()
          .postDelayed(action, delayMills);
      return true;
    }
  }

  @Implementation
  protected void postInvalidateDelayed(long delayMilliseconds) {
    if (ShadowLooper.looperMode() == LooperMode.Mode.PAUSED) {
      directly().postInvalidateDelayed(delayMilliseconds);
    } else {
      ShadowApplication.getInstance()
          .getForegroundThreadScheduler()
          .postDelayed(
              new Runnable() {
                @Override
                public void run() {
                  realView.invalidate();
                }
              },
              delayMilliseconds);
    }
  }

  @Implementation
  protected boolean removeCallbacks(Runnable callback) {
    if (ShadowLooper.looperMode() == LooperMode.Mode.PAUSED) {
      return directlyOn(realView, View.class).removeCallbacks(callback);
    } else {
      ShadowLegacyLooper shadowLooper = Shadow.extract(Looper.getMainLooper());
      shadowLooper.getScheduler().remove(callback);
      return true;
    }
  }

  @Implementation
  protected void scrollTo(int x, int y) {
    try {
      Method method = View.class.getDeclaredMethod("onScrollChanged", new Class[]{int.class, int.class, int.class, int.class});
      method.setAccessible(true);
      method.invoke(realView, x, y, scrollToCoordinates.x, scrollToCoordinates.y);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    scrollToCoordinates = new Point(x, y);
    ReflectionHelpers.setField(realView, "mScrollX", x);
    ReflectionHelpers.setField(realView, "mScrollY", y);
  }

  @Implementation
  protected void scrollBy(int x, int y) {
    scrollTo(getScrollX() + x, getScrollY() + y);
  }

  @Implementation
  protected int getScrollX() {
    return scrollToCoordinates != null ? scrollToCoordinates.x : 0;
  }

  @Implementation
  protected int getScrollY() {
    return scrollToCoordinates != null ? scrollToCoordinates.y : 0;
  }

  @Implementation
  protected void setScrollX(int scrollX) {
    scrollTo(scrollX, scrollToCoordinates.y);
  }

  @Implementation
  protected void setScrollY(int scrollY) {
    scrollTo(scrollToCoordinates.x, scrollY);
  }

  @Implementation
  protected int getLayerType() {
    return this.layerType;
  }

  @Implementation
  protected void setAnimation(final Animation animation) {
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
      Choreographer choreographer = Choreographer.getInstance();
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
        // TODO: get the correct value for ShadowPausedLooper mode
        elapsedTime += ShadowChoreographer.getFrameInterval() / TimeUtils.NANOS_PER_MS;
        Choreographer.getInstance().postCallback(Choreographer.CALLBACK_ANIMATION, this, null);
      } else {
        animationRunner = null;
      }
    }
  }

  @Implementation(minSdk = KITKAT)
  protected boolean isAttachedToWindow() {
    return getAttachInfo() != null;
  }

  private Object getAttachInfo() {
    return reflector(_View_.class, realView).getAttachInfo();
  }

  /** Accessor interface for {@link View}'s internals. */
  @ForType(View.class)
  private interface _View_ {
    @Accessor("mAttachInfo")
    Object getAttachInfo();

    void onAttachedToWindow();

    void onDetachedFromWindow();
  }

  public void callOnAttachedToWindow() {
    reflector(_View_.class, realView).onAttachedToWindow();
  }

  public void callOnDetachedFromWindow() {
    reflector(_View_.class, realView).onDetachedFromWindow();
  }

  @Implementation(minSdk = JELLY_BEAN_MR2)
  protected WindowId getWindowId() {
    return WindowIdHelper.getWindowId(this);
  }

  @Implementation
  protected boolean performHapticFeedback(int hapticFeedbackType) {
    hapticFeedbackPerformed = hapticFeedbackType;
    return true;
  }

  @Implementation
  protected boolean getGlobalVisibleRect(Rect rect, Point globalOffset) {
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

  @Implementation
  protected void getWindowVisibleDisplayFrame(Rect outRect) {
    // TODO: figure out how to simulate this logic instead
    // if (mAttachInfo != null) {
    //   mAttachInfo.mSession.getDisplayFrame(mAttachInfo.mWindow, outRect);

    ShadowDisplay.getDefaultDisplay().getRectSize(outRect);
  }

  @Implementation(minSdk = N)
  protected void getWindowDisplayFrame(Rect outRect) {
    // TODO: figure out how to simulate this logic instead
    // if (mAttachInfo != null) {
    //   mAttachInfo.mSession.getDisplayFrame(mAttachInfo.mWindow, outRect);

    ShadowDisplay.getDefaultDisplay().getRectSize(outRect);
  }

  private View directly() {
    return directlyOn(realView, View.class);
  }

  public static class WindowIdHelper {
    public static WindowId getWindowId(ShadowView shadowView) {
      if (shadowView.isAttachedToWindow()) {
        Object attachInfo = shadowView.getAttachInfo();
        if (getField(attachInfo, "mWindowId") == null) {
          IWindowId iWindowId = new MyIWindowIdStub();
          reflector(_AttachInfo_.class, attachInfo).setWindowId(new WindowId(iWindowId));
          reflector(_AttachInfo_.class, attachInfo).setIWindowId(iWindowId);
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

  /** Accessor interface for android.view.View.AttachInfo's internals. */
  @ForType(className = "android.view.View$AttachInfo")
  interface _AttachInfo_ {

    @Accessor("mIWindowId")
    void setIWindowId(IWindowId iWindowId);

    @Accessor("mWindowId")
    void setWindowId(WindowId windowId);
  }
}
