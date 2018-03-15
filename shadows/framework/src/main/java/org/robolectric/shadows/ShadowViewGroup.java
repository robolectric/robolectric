package org.robolectric.shadows;

import static org.robolectric.shadow.api.Shadow.directlyOn;

import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.LayoutAnimationController;
import java.io.PrintStream;
import org.robolectric.Shadows;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

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
    Shadows.shadowOf(Looper.getMainLooper()).runPaused(new Runnable() {
      @Override public void run() {
        directlyOn(realViewGroup, ViewGroup.class, "addView",
            ClassParameter.from(View.class, child),
            ClassParameter.from(int.class, index),
            ClassParameter.from(ViewGroup.LayoutParams.class, params));
      }
    });
  }

  /**
   * Returns a string representation of this {@code ViewGroup} by concatenating all of the
   * strings contained in all of the descendants of this {@code ViewGroup}.
   */
  @Override
  public String innerText() {
    String innerText = "";
    String delimiter = "";

    for (int i = 0; i < realViewGroup.getChildCount(); i++) {
      View child = realViewGroup.getChildAt(i);
      String childText = Shadows.shadowOf(child).innerText();
      if (childText.length() > 0) {
        innerText += delimiter;
        delimiter = " ";
      }
      innerText += childText;
    }
    return innerText;
  }

  /**
   * Dumps the state of this {@code ViewGroup} to {@code System.out}.
   */
  @Override
  public void dump(PrintStream out, int indent) {
    dumpFirstPart(out, indent);
    if (realViewGroup.getChildCount() > 0) {
      out.println(">");

      for (int i = 0; i < realViewGroup.getChildCount(); i++) {
        View child = realViewGroup.getChildAt(i);
        Shadows.shadowOf(child).dump(out, indent + 2);
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
    if (isAttachedToWindow()) Shadows.shadowOf(child).callOnDetachedFromWindow();
  }

  public MotionEvent getInterceptedTouchEvent() {
    return interceptedTouchEvent;
  }

  @Implementation
  public boolean onInterceptTouchEvent(MotionEvent ev) {
    interceptedTouchEvent = ev;
    return false;
  }
}
