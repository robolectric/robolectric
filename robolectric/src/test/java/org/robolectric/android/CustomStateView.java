package org.robolectric.android;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class CustomStateView extends TextView {

  public Integer extraAttribute;

  public CustomStateView(Context context) {
    super(context);
  }

  public CustomStateView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public CustomStateView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override
  protected int[] onCreateDrawableState(int extraSpace) {
    final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
    if (extraAttribute != null) {
      mergeDrawableStates(drawableState, new int[]{extraAttribute});
    }
    return drawableState;
  }
}
