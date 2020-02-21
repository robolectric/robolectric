package org.robolectric.shadows;

import static org.robolectric.shadow.api.Shadow.invokeConstructor;

import android.graphics.Rect;
import android.view.TouchDelegate;
import android.view.View;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

@Implements(TouchDelegate.class)
public class ShadowTouchDelegate {
  @RealObject private TouchDelegate realObject;
  private Rect bounds;
  private View delegateView;

  @Implementation
  protected void __constructor__(Rect bounds, View delegateView) {
    this.bounds = bounds;
    this.delegateView = delegateView;
    invokeConstructor(TouchDelegate.class, realObject,
        ClassParameter.from(Rect.class, bounds),
        ClassParameter.from(View.class, delegateView));
  }

  public Rect getBounds() {
    return this.bounds;
  }

  public View getDelegateView() {
    return this.delegateView;
  }
}
