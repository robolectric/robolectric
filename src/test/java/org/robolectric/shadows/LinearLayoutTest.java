package org.robolectric.shadows;

import android.view.Gravity;
import android.widget.LinearLayout;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertSame;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class LinearLayoutTest {
  private LinearLayout linearLayout;
  private ShadowLinearLayout shadow;

  @Before
  public void setup() throws Exception {
    linearLayout = new LinearLayout(Robolectric.application);
    shadow = shadowOf(linearLayout);
  }

  @Test
  public void getLayoutParams_shouldReturnTheSameLinearLayoutParamsFromTheSetter() throws Exception {
    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(1, 2);
    linearLayout.setLayoutParams(params);

    assertSame(params, linearLayout.getLayoutParams());
  }

  @Test
  public void canAnswerOrientation() throws Exception {
    assertThat(linearLayout.getOrientation()).isEqualTo(LinearLayout.HORIZONTAL);
    linearLayout.setOrientation(LinearLayout.VERTICAL);
    assertThat(linearLayout.getOrientation()).isEqualTo(LinearLayout.VERTICAL);
    linearLayout.setOrientation(LinearLayout.HORIZONTAL);
    assertThat(linearLayout.getOrientation()).isEqualTo(LinearLayout.HORIZONTAL);
  }

  @Test
  public void canAnswerGravity() throws Exception {
    assertThat(shadow.getGravity()).isEqualTo(Gravity.TOP | Gravity.START);
    linearLayout.setGravity(Gravity.CENTER_VERTICAL); // Only affects horizontal.
    assertThat(shadow.getGravity()).isEqualTo(Gravity.CENTER_VERTICAL | Gravity.START);
    linearLayout.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL); // Affects both directions.
    assertThat(shadow.getGravity()).isEqualTo(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
  }
}
