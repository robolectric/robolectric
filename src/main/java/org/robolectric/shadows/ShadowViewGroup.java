package org.robolectric.shadows;

import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.LayoutAnimationController;
import java.io.PrintStream;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import static org.robolectric.Robolectric.directlyOn;
import static org.robolectric.Robolectric.shadowOf;

/**
 * Shadow for {@code ViewGroup} that simulates its implementation
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(ViewGroup.class)
public class ShadowViewGroup extends ShadowView {
  @RealObject protected ViewGroup realViewGroup;

  private AnimationListener animListener;
  private LayoutAnimationController layoutAnim;
  private boolean disallowInterceptTouchEvent = false;
  private MotionEvent interceptedTouchEvent;

  @Implementation
  public void addView(final View child, final int index, final ViewGroup.LayoutParams params) {
    shadowOf(Looper.getMainLooper()).runPaused(new Runnable() {
      @Override public void run() {
        directlyOn(realViewGroup, ViewGroup.class, "addView", View.class, int.class, ViewGroup.LayoutParams.class)
            .invoke(child, index, params);
      }
    });
  }

  /**
   * Returns a string representation of this {@code ViewGroup} by concatenating all of the strings contained in all
   * of the descendants of this {@code ViewGroup}.
   * <p/>
   * Robolectric extension.
   */
  @Override
  public String innerText() {
    String innerText = "";
    String delimiter = "";

    for (int i = 0; i < realViewGroup.getChildCount(); i++) {
      View child = realViewGroup.getChildAt(i);
      String childText = shadowOf(child).innerText();
      if (childText.length() > 0) {
        innerText += delimiter;
        delimiter = " ";
      }
      innerText += childText;
    }
    return innerText;
  }

  /**
   * Non-Android method that dumps the state of this {@code ViewGroup} to {@code System.out}
   */
  @Override
  public void dump(PrintStream out, int indent) {
    dumpFirstPart(out, indent);
    if (realViewGroup.getChildCount() > 0) {
      out.println(">");

      for (int i = 0; i < realViewGroup.getChildCount(); i++) {
        View child = realViewGroup.getChildAt(i);
        shadowOf(child).dump(out, indent + 2);
      }

      dumpIndent(out, indent);
      out.println("</" + realView.getClass().getSimpleName() + ">");
    } else {
      out.println("/>");
    }
  }

  @Implementation
  public void setLayoutAnimationListener(AnimationListener listener) {
    animListener = listener;
  }

  @Implementation
  public AnimationListener getLayoutAnimationListener() {
    return animListener;
  }

  @Implementation
  public void setLayoutAnimation(LayoutAnimationController layoutAnim) {
    this.layoutAnim = layoutAnim;
  }

  @Implementation
  public LayoutAnimationController getLayoutAnimation() {
    return layoutAnim;
  }

  @Implementation
  public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    disallowInterceptTouchEvent = disallowIntercept;
  }

  public boolean getDisallowInterceptTouchEvent() {
    return disallowInterceptTouchEvent;
  }

  protected void removedChild(View child) {
    if (isAttachedToWindow()) shadowOf(child).callOnDetachedFromWindow();
  }

  public MotionEvent getInterceptedTouchEvent() {
    return interceptedTouchEvent;
  }

  @Implementation
  public boolean onInterceptTouchEvent(MotionEvent ev) {
    interceptedTouchEvent = ev;
    return false;
  }

  // todo: remove?
  @SuppressWarnings({"UnusedDeclaration"})
  @Implements(ViewGroup.LayoutParams.class)
  public static class ShadowLayoutParams {
    @RealObject private ViewGroup.LayoutParams realLayoutParams;

    public void __constructor__(int w, int h) {
      realLayoutParams.width = w;
      realLayoutParams.height = h;
    }

    public void __constructor__(ViewGroup.LayoutParams source) {
      __constructor__(source.width, source.height);
    }
  }

  // todo: remove?
  /**
   * Shadow for {@link android.view.ViewGroup.MarginLayoutParams} that simulates its implementation.
   */
  @SuppressWarnings("UnusedDeclaration")
  @Implements(ViewGroup.MarginLayoutParams.class)
  public static class ShadowMarginLayoutParams extends ShadowLayoutParams {

    @RealObject
    private ViewGroup.MarginLayoutParams realMarginLayoutParams;

    @Implementation
    public void setMargins(int left, int top, int right, int bottom) {
      realMarginLayoutParams.leftMargin = left;
      realMarginLayoutParams.topMargin = top;
      realMarginLayoutParams.rightMargin = right;
      realMarginLayoutParams.bottomMargin = bottom;
    }
  }
}
