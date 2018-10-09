package org.robolectric.shadows;

import static org.robolectric.shadow.api.Shadow.directlyOn;

import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import java.io.PrintStream;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(ViewGroup.class)
public class ShadowViewGroup extends ShadowView {
  @RealObject protected ViewGroup realViewGroup;

  private boolean disallowInterceptTouchEvent = false;
  private MotionEvent interceptedTouchEvent;

  @Implementation
  protected void addView(final View child, final int index, final ViewGroup.LayoutParams params) {
    ShadowLooper shadowLooper = Shadow.extract(Looper.getMainLooper());
    shadowLooper.runPaused(() ->
        directlyOn(realViewGroup, ViewGroup.class, "addView",
            ClassParameter.from(View.class, child),
            ClassParameter.from(int.class, index),
            ClassParameter.from(ViewGroup.LayoutParams.class, params)));
  }

  /**
   * Returns a string representation of this {@code ViewGroup} by concatenating all of the
   * strings contained in all of the descendants of this {@code ViewGroup}.
   */
  @Override
  public String innerText() {
    StringBuilder innerText = new StringBuilder();
    String delimiter = "";

    for (int i = 0; i < realViewGroup.getChildCount(); i++) {
      View child = realViewGroup.getChildAt(i);
      ShadowView shadowView = Shadow.extract(child);
      String childText = shadowView.innerText();
      if (childText.length() > 0) {
        innerText.append(delimiter);
        delimiter = " ";
      }
      innerText.append(childText);
    }
    return innerText.toString();
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
        ShadowView shadowChild = Shadow.extract(child);
        shadowChild.dump(out, indent + 2);
      }

      dumpIndent(out, indent);
      out.println("</" + realView.getClass().getSimpleName() + ">");
    } else {
      out.println("/>");
    }
  }

  @Implementation
  protected void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    disallowInterceptTouchEvent = disallowIntercept;
  }

  public boolean getDisallowInterceptTouchEvent() {
    return disallowInterceptTouchEvent;
  }

  protected void removedChild(View child) {
    if (isAttachedToWindow()) {
      ShadowView shadowView = Shadow.extract(child);
      shadowView.callOnDetachedFromWindow();
    }
  }

  public MotionEvent getInterceptedTouchEvent() {
    return interceptedTouchEvent;
  }

  @Implementation
  protected boolean onInterceptTouchEvent(MotionEvent ev) {
    interceptedTouchEvent = ev;
    return false;
  }
}
