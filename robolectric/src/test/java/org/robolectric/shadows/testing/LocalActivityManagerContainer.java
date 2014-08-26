package org.robolectric.shadows.testing;

import android.app.Activity;
import android.app.LocalActivityManager;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import org.robolectric.R;

@SuppressWarnings("UnusedDeclaration")
// Used in lam_outer.xml
public class LocalActivityManagerContainer extends LinearLayout {
  public LocalActivityManagerContainer(Context context) {
    super(context);
    init();
  }

  public LocalActivityManagerContainer(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public LocalActivityManagerContainer(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init();
  }

  private void init() {
    LocalActivityManager lam = new LocalActivityManager((Activity) getContext(), true);
    lam.dispatchCreate(null);
    final Window window = lam.startActivity("foo", new Intent(getContext(), InnerActivity.class));
    // Add the decorView's child to this LinearLayout's children.
    final View innerContents = window.getDecorView().findViewById(R.id.lam_inner_contents);
    ((ViewGroup) innerContents.getParent()).removeView(innerContents);
    addView(innerContents);
  }

}
