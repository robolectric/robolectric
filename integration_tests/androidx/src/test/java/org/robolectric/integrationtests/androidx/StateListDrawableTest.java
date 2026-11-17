package org.robolectric.integrationtests.androidx;

import static android.R.attr.state_checked;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.StateSet;
import android.widget.ToggleButton;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowStateListDrawable;

@RunWith(AndroidJUnit4.class)
@Config(sdk = Config.ALL_SDKS)
public class StateListDrawableTest {
  private final Context context = ApplicationProvider.getApplicationContext();

  @Test
  public void loadedAndCachedSelectorsPreserveResourceIds() {
    assertResourceIds(loadSelector());
    assertResourceIds(loadSelector());
  }

  @Test
  public void shadowReadsStatesFromInflatedSelector() {
    ShadowStateListDrawable shadow = shadowOf(loadSelector());
    Drawable checked = shadow.getDrawableForState(new int[] {state_checked});
    Drawable unchecked = shadow.getDrawableForState(StateSet.WILD_CARD);
    assertThat(checked).isNotNull();
    assertThat(unchecked).isNotNull();
    assertThat(shadowOf(checked).getCreatedFromResId()).isEqualTo(R.drawable.state_list_checked);
    assertThat(shadowOf(unchecked).getCreatedFromResId())
        .isEqualTo(R.drawable.state_list_unchecked);
  }

  @Test
  public void constantStateCopiesPreserveResourceIds() {
    StateListDrawable original = loadSelector();
    Drawable.ConstantState state = original.getConstantState();
    Resources resources = context.getResources();
    for (Drawable copy :
        Arrays.asList(
            state.newDrawable(),
            state.newDrawable(resources),
            state.newDrawable(resources, context.getTheme()))) {
      assertThat(copy).isNotSameInstanceAs(original);
      assertResourceIds((StateListDrawable) copy);
    }
  }

  @Test
  public void mutatePreservesResourceIds() {
    StateListDrawable drawable = loadSelector();
    drawable.mutate();
    assertResourceIds(drawable);
  }

  @Test
  public void toggleButtonChangesDrawableResource() {
    ToggleButton button = new ToggleButton(context);
    button.setBackgroundResource(R.drawable.state_list_drawable);
    assertThat(shadowOf(button.getBackground().getCurrent()).getCreatedFromResId())
        .isEqualTo(R.drawable.state_list_unchecked);
    button.setChecked(true);
    assertThat(shadowOf(button.getBackground().getCurrent()).getCreatedFromResId())
        .isEqualTo(R.drawable.state_list_checked);
  }

  private StateListDrawable loadSelector() {
    return (StateListDrawable) context.getDrawable(R.drawable.state_list_drawable);
  }

  private void assertResourceIds(StateListDrawable drawable) {
    assertThat(shadowOf(drawable).getCreatedFromResId()).isEqualTo(R.drawable.state_list_drawable);
    drawable.setState(StateSet.WILD_CARD);
    assertThat(shadowOf(drawable.getCurrent()).getCreatedFromResId())
        .isEqualTo(R.drawable.state_list_unchecked);
    drawable.setState(new int[] {state_checked});
    assertThat(shadowOf(drawable.getCurrent()).getCreatedFromResId())
        .isEqualTo(R.drawable.state_list_checked);
  }
}
