package org.robolectric.android;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;
import org.robolectric.R;

public class CustomStateView extends TextView {

  private static final int[] STATE_FOO = {R.attr.stateFoo};

  public boolean isFoo;

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
    if (isFoo) {
      mergeDrawableStates(drawableState, STATE_FOO);
    }
    return drawableState;
  }
}
