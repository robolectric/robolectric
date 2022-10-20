package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.robolectric.Shadows.shadowOf;

import android.R;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.StateSet;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

@RunWith(AndroidJUnit4.class)
public class ShadowStateListDrawableTest {

  @Test
  public void testAddStateWithDrawable() {
    Drawable drawable =
        RuntimeEnvironment.getApplication()
            .getResources()
            .getDrawable(android.R.drawable.ic_delete);
    StateListDrawable stateListDrawable = new StateListDrawable();
    int[] states = {R.attr.state_pressed};
    stateListDrawable.addState(states, drawable);

    ShadowStateListDrawable shadow = shadowOf(stateListDrawable);
    Drawable drawableForState = shadow.getDrawableForState(states);

    assertNotNull(drawableForState);
    assertThat(((ShadowBitmapDrawable) shadowOf(drawableForState)).getCreatedFromResId())
        .isEqualTo(android.R.drawable.ic_delete);
  }

  @Test
  public void testAddDrawableWithWildCardState() {
    Drawable drawable =
        RuntimeEnvironment.getApplication()
            .getResources()
            .getDrawable(android.R.drawable.ic_delete);

    StateListDrawable stateListDrawable = new StateListDrawable();
    stateListDrawable.addState(StateSet.WILD_CARD, drawable);

    ShadowStateListDrawable shadow = shadowOf(stateListDrawable);
    Drawable drawableForState = shadow.getDrawableForState(StateSet.WILD_CARD);
    assertNotNull(drawableForState);
    assertThat(((ShadowBitmapDrawable) shadowOf(drawableForState)).getCreatedFromResId())
        .isEqualTo(android.R.drawable.ic_delete);
  }
}
