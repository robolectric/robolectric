package org.robolectric.shadows;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

public class RoboLayoutInflater extends LayoutInflater {
  private static final String[] sClassPrefixList = {
      "android.widget.",
      "android.webkit."
  };

  /**
   * Instead of instantiating directly, you should retrieve an instance
   * through {@link android.content.Context#getSystemService}
   *
   * @param context The Context in which in which to find resources and other
   *                application-specific things.
   *
   * @see android.content.Context#getSystemService
   */
  public RoboLayoutInflater(Context context) {
    super(context);
  }

  RoboLayoutInflater(LayoutInflater original, Context newContext) {
    super(original, newContext);
  }

  /** Override onCreateView to instantiate names that correspond to the
   widgets known to the Widget factory. If we don't find a match,
   call through to our super class.
   */
  @Override protected View onCreateView(String name, AttributeSet attrs) throws ClassNotFoundException {
    for (String prefix : sClassPrefixList) {
      try {
        View view = createView(name, prefix, attrs);
        if (view != null) {
          return view;
        }
      } catch (ClassNotFoundException e) {
        // In this case we want to let the base class take a crack
        // at it.
      }
    }

    return super.onCreateView(name, attrs);
  }

  public LayoutInflater cloneInContext(Context newContext) {
    return new RoboLayoutInflater(this, newContext);
  }
}
