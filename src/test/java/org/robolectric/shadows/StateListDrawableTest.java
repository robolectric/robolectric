package org.robolectric.shadows;

import android.R;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.StateSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class StateListDrawableTest {

  @Test
  public void testAddStateWithDrawable() {
    Drawable drawable = ShadowDrawable.createFromPath("/foo");

    StateListDrawable stateListDrawable = new StateListDrawable();
    int[] states = {R.attr.state_pressed};
    stateListDrawable.addState(states, drawable);

    ShadowStateListDrawable shadow = shadowOf(stateListDrawable);
    Drawable drawableForState = shadow.getDrawableForState(states);
    assertNotNull(drawableForState);
    assertThat(((ShadowBitmapDrawable) shadowOf(drawableForState)).getPath()).isEqualTo("/foo");
  }

  @Test
  public void testAddDrawableWithWildCardState() {
    Drawable drawable = ShadowDrawable.createFromPath("/foo");

    StateListDrawable stateListDrawable = new StateListDrawable();
    stateListDrawable.addState(StateSet.WILD_CARD, drawable);

    ShadowStateListDrawable shadow = shadowOf(stateListDrawable);
    Drawable drawableForState = shadow.getDrawableForState(StateSet.WILD_CARD);
    assertNotNull(drawableForState);
    assertThat(((ShadowBitmapDrawable) shadowOf(drawableForState)).getPath()).isEqualTo("/foo");
  }
}
