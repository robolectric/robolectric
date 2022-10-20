package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.KITKAT_WATCH;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
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
import android.graphics.RectF;
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
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.view.WindowId;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import com.google.common.collect.ImmutableList;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.ReflectorObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.TimeUtils;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

@Implements(View.class)
@SuppressLint("NewApi")
public class ShadowView {

  @RealObject protected View realView;
  @ReflectorObject protected _View_ viewReflector;
  private static final List<View.OnClickListener> globalClickListeners =
      new CopyOnWriteArrayList<>();
  private static final List<View.OnLongClickListener> globalLongClickListeners =
      new CopyOnWriteArrayList<>();
  private View.OnClickListener onClickListener;
  private View.OnLongClickListener onLongClickListener;
  private View.OnFocusChangeListener onFocusChangeListener;
  private View.OnSystemUiVisibilityChangeListener onSystemUiVisibilityChangeListener;
  private final HashSet<View.OnAttachStateChangeListener> onAttachStateChangeListeners =
      new HashSet<>();
  private final HashSet<View.OnLayoutChangeListener> onLayoutChangeListeners = new HashSet<>();
  private boolean wasInvalidated;
  private View.OnTouchListener onTouchListener;
  protected AttributeSet attributeSet;
  public Point scrollToCoordinates = new Point();
  private boolean didRequestLayout;
  private MotionEvent lastTouchEvent;
  private int hapticFeedbackPerformed = -1;
  private boolean onLayoutWasCalled;
  private View.OnCreateContextMenuListener onCreateContextMenuListener;
  private Rect globalVisibleRect;
  private int layerType;
  private final ArrayList<Animation> animations = new ArrayList<>();
  private AnimationRunner animationRunner;

  /**
   * Calls {@code performClick()} on a {@code View} after ensuring that it and its ancestors are
   * visible and that it is enabled.
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
    if (!useRealGraphics()) {
      ShadowCanvas shadowCanvas = Shadow.extract(canvas);
      return shadowCanvas.getDescription();
    } else {
      return "";
    }
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

  // Only override up to kitkat, while this version exists after kitkat it just calls through to the
  // __constructor__(Context, AttributeSet, int, int) variant below.
  @Implementation(maxSdk = KITKAT)
  protected void __constructor__(Context context, AttributeSet attributeSet, int defStyle) {
    this.attributeSet = attributeSet;
    invokeConstructor(
        View.class,
        realView,
        ClassParameter.from(Context.class, context),
        ClassParameter.from(AttributeSet.class, attributeSet),
        ClassParameter.from(int.class, defStyle));
  }

  @Implementation(minSdk = KITKAT_WATCH)
  protected void __constructor__(
      Context context, AttributeSet attributeSet, int defStyleAttr, int defStyleRes) {
    this.attributeSet = attributeSet;
    invokeConstructor(
        View.class,
        realView,
        ClassParameter.from(Context.class, context),
        ClassParameter.from(AttributeSet.class, attributeSet),
        ClassParameter.from(int.class, defStyleAttr),
        ClassParameter.from(int.class, defStyleRes));
  }

  @Implementation
  protected void setLayerType(int layerType, Paint paint) {
    this.layerType = layerType;
    reflector(_View_.class, realView).setLayerType(layerType, paint);
  }

  @Implementation
  protected void setOnFocusChangeListener(View.OnFocusChangeListener l) {
    onFocusChangeListener = l;
    reflector(_View_.class, realView).setOnFocusChangeListener(l);
  }

  @Implementation
  protected void setOnClickListener(View.OnClickListener onClickListener) {
    this.onClickListener = onClickListener;
    reflector(_View_.class, realView).setOnClickListener(onClickListener);
  }

  @Implementation
  protected void setOnLongClickListener(View.OnLongClickListener onLongClickListener) {
    this.onLongClickListener = onLongClickListener;
    reflector(_View_.class, realView).setOnLongClickListener(onLongClickListener);
  }

  @Implementation
  protected void setOnSystemUiVisibilityChangeListener(
      View.OnSystemUiVisibilityChangeListener onSystemUiVisibilityChangeListener) {
    this.onSystemUiVisibilityChangeListener = onSystemUiVisibilityChangeListener;
    reflector(_View_.class, realView)
        .setOnSystemUiVisibilityChangeListener(onSystemUiVisibilityChangeListener);
  }

  @Implementation
  protected void setOnCreateContextMenuListener(
      View.OnCreateContextMenuListener onCreateContextMenuListener) {
    this.onCreateContextMenuListener = onCreateContextMenuListener;
    reflector(_View_.class, realView).setOnCreateContextMenuListener(onCreateContextMenuListener);
  }

  @Implementation
  protected void addOnAttachStateChangeListener(
      View.OnAttachStateChangeListener onAttachStateChangeListener) {
    onAttachStateChangeListeners.add(onAttachStateChangeListener);
    reflector(_View_.class, realView).addOnAttachStateChangeListener(onAttachStateChangeListener);
  }

  @Implementation
  protected void removeOnAttachStateChangeListener(
      View.OnAttachStateChangeListener onAttachStateChangeListener) {
    onAttachStateChangeListeners.remove(onAttachStateChangeListener);
    reflector(_View_.class, realView)
        .removeOnAttachStateChangeListener(onAttachStateChangeListener);
  }

  @Implementation
  protected void addOnLayoutChangeListener(View.OnLayoutChangeListener onLayoutChangeListener) {
    onLayoutChangeListeners.add(onLayoutChangeListener);
    reflector(_View_.class, realView).addOnLayoutChangeListener(onLayoutChangeListener);
  }

  @Implementation
  protected void removeOnLayoutChangeListener(View.OnLayoutChangeListener onLayoutChangeListener) {
    onLayoutChangeListeners.remove(onLayoutChangeListener);
    reflector(_View_.class, realView).removeOnLayoutChangeListener(onLayoutChangeListener);
  }

  @Implementation
  protected void draw(Canvas canvas) {
    Drawable background = realView.getBackground();
    if (background != null && !useRealGraphics()) {
      Object shadowCanvas = Shadow.extract(canvas);
      // Check that Canvas is not a Mockito mock
      if (shadowCanvas instanceof ShadowCanvas) {
        ((ShadowCanvas) shadowCanvas).appendDescription("background:");
      }
    }
    reflector(_View_.class, realView).draw(canvas);
  }

  @Implementation
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    onLayoutWasCalled = true;
    reflector(_View_.class, realView).onLayout(changed, left, top, right, bottom);
  }

  public boolean onLayoutWasCalled() {
    return onLayoutWasCalled;
  }

  @Implementation
  protected void requestLayout() {
    didRequestLayout = true;
    reflector(_View_.class, realView).requestLayout();
  }

  @Implementation
  protected boolean performClick() {
    for (View.OnClickListener listener : globalClickListeners) {
      listener.onClick(realView);
    }
    return reflector(_View_.class, realView).performClick();
  }

  /**
   * Registers an {@link View.OnClickListener} to the {@link ShadowView}.
   *
   * @param listener The {@link View.OnClickListener} to be registered.
   */
  public static void addGlobalPerformClickListener(View.OnClickListener listener) {
    ShadowView.globalClickListeners.add(listener);
  }

  /**
   * Removes an {@link View.OnClickListener} from the {@link ShadowView}.
   *
   * @param listener The {@link View.OnClickListener} to be removed.
   */
  public static void removeGlobalPerformClickListener(View.OnClickListener listener) {
    ShadowView.globalClickListeners.remove(listener);
  }

  @Implementation
  protected boolean performLongClick() {
    for (View.OnLongClickListener listener : globalLongClickListeners) {
      listener.onLongClick(realView);
    }
    return reflector(_View_.class, realView).performLongClick();
  }

  /**
   * Registers an {@link View.OnLongClickListener} to the {@link ShadowView}.
   *
   * @param listener The {@link View.OnLongClickListener} to be registered.
   */
  public static void addGlobalPerformLongClickListener(View.OnLongClickListener listener) {
    ShadowView.globalLongClickListeners.add(listener);
  }

  /**
   * Removes an {@link View.OnLongClickListener} from the {@link ShadowView}.
   *
   * @param listener The {@link View.OnLongClickListener} to be removed.
   */
  public static void removeGlobalPerformLongClickListener(View.OnLongClickListener listener) {
    ShadowView.globalLongClickListeners.remove(listener);
  }

  @Resetter
  public static void reset() {
    ShadowView.globalClickListeners.clear();
    ShadowView.globalLongClickListeners.clear();
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
    reflector(_View_.class, realView).invalidate();
  }

  @Implementation
  protected boolean onTouchEvent(MotionEvent event) {
    lastTouchEvent = event;
    return reflector(_View_.class, realView).onTouchEvent(event);
  }

  @Implementation
  protected void setOnTouchListener(View.OnTouchListener onTouchListener) {
    this.onTouchListener = onTouchListener;
    reflector(_View_.class, realView).setOnTouchListener(onTouchListener);
  }

  public MotionEvent getLastTouchEvent() {
    return lastTouchEvent;
  }

  /**
   * Returns a string representation of this {@code View}. Unless overridden, it will be an empty
   * string.
   *
   * <p>Robolectric extension.
   *
   * @return String representation of this view.
   */
  public String innerText() {
    return "";
  }

  /**
   * Dumps the status of this {@code View} to {@code System.out}
   *
   * @deprecated - Please use {@link androidx.test.espresso.util.HumanReadables#describe(View)}
   */
  @Deprecated
  public void dump() {
    dump(System.out, 0);
  }

  /**
   * Dumps the status of this {@code View} to {@code System.out} at the given indentation level
   *
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
      dumpAttribute(
          out, "id", realView.getContext().getResources().getResourceName(realView.getId()));
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

  /** Clears the wasInvalidated flag */
  public void clearWasInvalidated() {
    wasInvalidated = false;
  }

  /**
   * Utility method for clicking on views exposing testing scenarios that are not possible when
   * using the actual app.
   *
   * <p>If running with LooperMode PAUSED will also idle the main Looper.
   *
   * @throws RuntimeException if the view is disabled or if the view or any of its parents are not
   *     visible.
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
  @Implementation(minSdk = R)
  public View.OnLongClickListener getOnLongClickListener() {
    if (RuntimeEnvironment.getApiLevel() >= R) {
      return reflector(_View_.class, realView).getOnLongClickListener();
    } else {
      return onLongClickListener;
    }
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

  /**
   * @return Returns the attached listeners, or the empty set if none are present.
   */
  public Set<View.OnAttachStateChangeListener> getOnAttachStateChangeListeners() {
    return onAttachStateChangeListeners;
  }

  /**
   * @return Returns the layout change listeners, or the empty set if none are present.
   */
  public Set<View.OnLayoutChangeListener> getOnLayoutChangeListeners() {
    return onLayoutChangeListeners;
  }

  @Implementation
  protected boolean post(Runnable action) {
    if (ShadowLooper.looperMode() == LooperMode.Mode.PAUSED) {
      return reflector(_View_.class, realView).post(action);
    } else {
      ShadowApplication.getInstance().getForegroundThreadScheduler().post(action);
      return true;
    }
  }

  @Implementation
  protected boolean postDelayed(Runnable action, long delayMills) {
    if (ShadowLooper.looperMode() == LooperMode.Mode.PAUSED) {
      return reflector(_View_.class, realView).postDelayed(action, delayMills);
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
      reflector(_View_.class, realView).postInvalidateDelayed(delayMilliseconds);
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
      return reflector(_View_.class, realView).removeCallbacks(callback);
    } else {
      ShadowLegacyLooper shadowLooper = Shadow.extract(Looper.getMainLooper());
      shadowLooper.getScheduler().remove(callback);
      return true;
    }
  }

  @Implementation
  protected void scrollTo(int x, int y) {
    if (useRealGraphics()) {
      reflector(_View_.class, realView).scrollTo(x, y);
    } else {
      reflector(_View_.class, realView)
          .onScrollChanged(x, y, scrollToCoordinates.x, scrollToCoordinates.y);
      scrollToCoordinates = new Point(x, y);
      reflector(_View_.class, realView).setMemberScrollX(x);
      reflector(_View_.class, realView).setMemberScrollY(y);
    }
  }

  @Implementation
  protected void scrollBy(int x, int y) {
    if (useRealGraphics()) {
      reflector(_View_.class, realView).scrollBy(x, y);
    } else {
      scrollTo(getScrollX() + x, getScrollY() + y);
    }
  }

  @Implementation
  protected int getScrollX() {
    if (useRealGraphics()) {
      return reflector(_View_.class, realView).getScrollX();
    } else {
      return scrollToCoordinates != null ? scrollToCoordinates.x : 0;
    }
  }

  @Implementation
  protected int getScrollY() {
    if (useRealGraphics()) {
      return reflector(_View_.class, realView).getScrollY();
    } else {
      return scrollToCoordinates != null ? scrollToCoordinates.y : 0;
    }
  }

  @Implementation
  protected void setScrollX(int scrollX) {
    if (useRealGraphics()) {
      reflector(_View_.class, realView).setScrollX(scrollX);
    } else {
      scrollTo(scrollX, scrollToCoordinates.y);
    }
  }

  @Implementation
  protected void setScrollY(int scrollY) {
    if (useRealGraphics()) {
      reflector(_View_.class, realView).setScrollY(scrollY);
    } else {
      scrollTo(scrollToCoordinates.x, scrollY);
    }
  }

  @Implementation
  protected void getLocationOnScreen(int[] outLocation) {
    reflector(_View_.class, realView).getLocationOnScreen(outLocation);
    int[] windowLocation = getWindowLocation();
    outLocation[0] += windowLocation[0];
    outLocation[1] += windowLocation[1];
  }

  @Implementation(minSdk = O)
  protected void mapRectFromViewToScreenCoords(RectF rect, boolean clipToParent) {
    reflector(_View_.class, realView).mapRectFromViewToScreenCoords(rect, clipToParent);
    int[] windowLocation = getWindowLocation();
    rect.offset(windowLocation[0], windowLocation[1]);
  }

  // TODO(paulsowden): Should configure the correct frame on the ViewRootImpl instead and remove
  //  this.
  private int[] getWindowLocation() {
    int[] location = new int[2];
    LayoutParams rootParams = realView.getRootView().getLayoutParams();
    if (rootParams instanceof WindowManager.LayoutParams) {
      location[0] = ((WindowManager.LayoutParams) rootParams).x;
      location[1] = ((WindowManager.LayoutParams) rootParams).y;
    }
    return location;
  }

  @Implementation
  protected int getLayerType() {
    return this.layerType;
  }

  /** Returns a list of all animations that have been set on this view. */
  public ImmutableList<Animation> getAnimations() {
    return ImmutableList.copyOf(animations);
  }

  /** Resets the list returned by {@link #getAnimations()} to an empty list. */
  public void clearAnimations() {
    animations.clear();
  }

  @Implementation
  protected void setAnimation(final Animation animation) {
    reflector(_View_.class, realView).setAnimation(animation);

    if (animation != null) {
      animations.add(animation);
      if (animationRunner != null) {
        animationRunner.cancel();
      }
      animationRunner = new AnimationRunner(animation);
      animationRunner.start();
    }
  }

  @Implementation
  protected void clearAnimation() {
    reflector(_View_.class, realView).clearAnimation();

    if (animationRunner != null) {
      animationRunner.cancel();
      animationRunner = null;
    }
  }

  @Implementation
  protected boolean initialAwakenScrollBars() {
    // Temporarily allow disabling initial awaken of scroll bars to aid in migration of tests to
    // default to window's being marked visible, this will be removed once migration is complete.
    if (Boolean.getBoolean("robolectric.disableInitialAwakenScrollBars")) {
      return false;
    } else {
      return viewReflector.initialAwakenScrollBars();
    }
  }

  private class AnimationRunner implements Runnable {
    private final Animation animation;
    private final Transformation transformation = new Transformation();
    private long startTime;
    private long elapsedTime;
    private boolean canceled;

    AnimationRunner(Animation animation) {
      this.animation = animation;
    }

    private void start() {
      startTime = animation.getStartTime();
      long startOffset = animation.getStartOffset();
      long startDelay =
          startTime == Animation.START_ON_FIRST_FRAME
              ? startOffset
              : (startTime + startOffset) - SystemClock.uptimeMillis();
      Choreographer.getInstance()
          .postCallbackDelayed(Choreographer.CALLBACK_ANIMATION, this, null, startDelay);
    }

    private boolean step() {
      long animationTime =
          animation.getStartTime() == Animation.START_ON_FIRST_FRAME
              ? SystemClock.uptimeMillis()
              : (animation.getStartTime() + animation.getStartOffset() + elapsedTime);
      // Note in real android the parent is non-nullable, retain legacy robolectric behavior which
      // allows detached views to animate.
      if (!animation.isInitialized() && realView.getParent() != null) {
        View parent = (View) realView.getParent();
        animation.initialize(
            realView.getWidth(), realView.getHeight(), parent.getWidth(), parent.getHeight());
      }
      boolean next = animation.getTransformation(animationTime, transformation);
      // Note in real view implementation it doesn't check the animation equality before clearing,
      // but in the real implementation the animation listeners are posted so it doesn't race with
      // chained animations.
      if (realView.getAnimation() == animation && !next) {
        if (!animation.getFillAfter()) {
          realView.clearAnimation();
        }
      }
      // We can't handle infinitely repeating animations in the current scheduling model, so abort
      // after one iteration.
      return next
          && (animation.getRepeatCount() != Animation.INFINITE
              || elapsedTime < animation.getDuration());
    }

    @Override
    public void run() {
      // Abort if start time has been messed with, as this simulation is only designed to handle
      // standard situations.
      if (!canceled && animation.getStartTime() == startTime && step()) {
        // Start time updates for repeating animations and if START_ON_FIRST_FRAME.
        startTime = animation.getStartTime();
        elapsedTime +=
            ShadowLooper.looperMode().equals(LooperMode.Mode.LEGACY)
                ? ShadowChoreographer.getFrameInterval() / TimeUtils.NANOS_PER_MS
                : ShadowChoreographer.getFrameDelay().toMillis();
        Choreographer.getInstance().postCallback(Choreographer.CALLBACK_ANIMATION, this, null);
      } else if (animationRunner == this) {
        animationRunner = null;
      }
    }

    public void cancel() {
      this.canceled = true;
      Choreographer.getInstance()
          .removeCallbacks(Choreographer.CALLBACK_ANIMATION, animationRunner, null);
    }
  }

  @Implementation(minSdk = KITKAT)
  protected boolean isAttachedToWindow() {
    return getAttachInfo() != null;
  }

  private Object getAttachInfo() {
    return reflector(_View_.class, realView).getAttachInfo();
  }

  /** Reflector interface for {@link View}'s internals. */
  @ForType(View.class)
  private interface _View_ {

    @Direct
    void draw(Canvas canvas);

    @Direct
    void onLayout(boolean changed, int left, int top, int right, int bottom);

    void assignParent(ViewParent viewParent);

    @Direct
    void setOnFocusChangeListener(View.OnFocusChangeListener l);

    @Direct
    void setLayerType(int layerType, Paint paint);

    @Direct
    void setOnClickListener(View.OnClickListener onClickListener);

    @Direct
    void setOnLongClickListener(View.OnLongClickListener onLongClickListener);

    @Direct
    View.OnLongClickListener getOnLongClickListener();

    @Direct
    void setOnSystemUiVisibilityChangeListener(
        View.OnSystemUiVisibilityChangeListener onSystemUiVisibilityChangeListener);

    @Direct
    void setOnCreateContextMenuListener(
        View.OnCreateContextMenuListener onCreateContextMenuListener);

    @Direct
    void addOnAttachStateChangeListener(
        View.OnAttachStateChangeListener onAttachStateChangeListener);

    @Direct
    void removeOnAttachStateChangeListener(
        View.OnAttachStateChangeListener onAttachStateChangeListener);

    @Direct
    void addOnLayoutChangeListener(View.OnLayoutChangeListener onLayoutChangeListener);

    @Direct
    void removeOnLayoutChangeListener(View.OnLayoutChangeListener onLayoutChangeListener);

    @Direct
    void requestLayout();

    @Direct
    boolean performClick();

    @Direct
    boolean performLongClick();

    @Direct
    void invalidate();

    @Direct
    boolean onTouchEvent(MotionEvent event);

    @Direct
    void setOnTouchListener(View.OnTouchListener onTouchListener);

    @Direct
    boolean post(Runnable action);

    @Direct
    boolean postDelayed(Runnable action, long delayMills);

    @Direct
    void postInvalidateDelayed(long delayMilliseconds);

    @Direct
    boolean removeCallbacks(Runnable callback);

    @Direct
    void setAnimation(final Animation animation);

    @Direct
    void clearAnimation();

    @Direct
    boolean getGlobalVisibleRect(Rect rect, Point globalOffset);

    @Direct
    WindowId getWindowId();

    @Accessor("mAttachInfo")
    Object getAttachInfo();

    void onAttachedToWindow();

    void onDetachedFromWindow();

    void onScrollChanged(int l, int t, int oldl, int oldt);

    @Direct
    void getLocationOnScreen(int[] outLocation);

    @Direct
    void mapRectFromViewToScreenCoords(RectF rect, boolean clipToParent);

    @Direct
    int getSourceLayoutResId();

    @Direct
    boolean initialAwakenScrollBars();

    @Accessor("mScrollX")
    void setMemberScrollX(int value);

    @Accessor("mScrollY")
    void setMemberScrollY(int value);

    @Direct
    void scrollTo(int x, int y);

    @Direct
    void scrollBy(int x, int y);

    @Direct
    int getScrollX();

    @Direct
    int getScrollY();

    @Direct
    void setScrollX(int value);

    @Direct
    void setScrollY(int value);
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
      return reflector(_View_.class, realView).getGlobalVisibleRect(rect, globalOffset);
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
    reflector(_View_.class, realView).assignParent(viewParent);
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

  /**
   * Returns the layout resource id this view was inflated from. Backwards compatible version of
   * {@link View#getSourceLayoutResId()}, passes through to the underlying implementation on API
   * levels where it is supported.
   */
  @Implementation(minSdk = Q)
  public int getSourceLayoutResId() {
    if (RuntimeEnvironment.getApiLevel() >= Q) {
      return reflector(_View_.class, realView).getSourceLayoutResId();
    } else {
      return ShadowResources.getAttributeSetSourceResId(attributeSet);
    }
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

      return reflector(_View_.class, shadowView.realView).getWindowId();
    }

    private static class MyIWindowIdStub extends IWindowId.Stub {
      @Override
      public void registerFocusObserver(IWindowFocusObserver iWindowFocusObserver)
          throws RemoteException {}

      @Override
      public void unregisterFocusObserver(IWindowFocusObserver iWindowFocusObserver)
          throws RemoteException {}

      @Override
      public boolean isFocused() throws RemoteException {
        return true;
      }
    }
  }

  /** Reflector interface for android.view.View.AttachInfo's internals. */
  @ForType(className = "android.view.View$AttachInfo")
  interface _AttachInfo_ {

    @Accessor("mIWindowId")
    void setIWindowId(IWindowId iWindowId);

    @Accessor("mWindowId")
    void setWindowId(WindowId windowId);
  }

  static boolean useRealGraphics() {
    return Boolean.getBoolean("robolectric.nativeruntime.enableGraphics");
  }
}
