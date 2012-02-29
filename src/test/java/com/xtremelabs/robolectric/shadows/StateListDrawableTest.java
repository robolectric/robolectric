package com.xtremelabs.robolectric.shadows;

import android.R;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.StateSet;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
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
        assertThat(((ShadowBitmapDrawable) shadowOf(drawableForState)).getPath(), is("/foo"));
    }

    @Test
    public void testAddDrawableWithWildCardState() {
        Drawable drawable = ShadowDrawable.createFromPath("/foo");

        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(StateSet.WILD_CARD, drawable);

        ShadowStateListDrawable shadow = shadowOf(stateListDrawable);
        Drawable drawableForState = shadow.getDrawableForState(StateSet.WILD_CARD);
        assertNotNull(drawableForState);
        assertThat(((ShadowBitmapDrawable) shadowOf(drawableForState)).getPath(), is("/foo"));
    }
}
