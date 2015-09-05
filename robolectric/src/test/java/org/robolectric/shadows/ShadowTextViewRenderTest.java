package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Robolectric.buildActivity;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewRootImpl;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.TextView;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

@RunWith(TestRunners.MultiApiWithDefaults.class)
@Config(rendering=true,sdk=21)
public class ShadowTextViewRenderTest {
  @Test
  @Ignore
  // This doesn't work yet, for viewroot and theme reasons
  public void shouldSetTextAndTextColorWhileInflatingXmlLayout() throws Exception {
    Activity activity = buildActivity(Activity.class).create().get();
    activity.setContentView(R.layout.text_views);

    TextView black = (TextView) activity.findViewById(R.id.black_text_view);
    assertThat(black.getText().toString()).isEqualTo("Black Text");
    assertThat(black.getCurrentTextColor()).isEqualTo(0xff000000);

    TextView white = (TextView) activity.findViewById(R.id.white_text_view);
    assertThat(white.getText().toString()).isEqualTo("White Text");
    assertThat(white.getCurrentTextColor())
        .isEqualTo(activity.getResources().getColor(android.R.color.white));

    TextView grey = (TextView) activity.findViewById(R.id.grey_text_view);
    assertThat(grey.getText().toString()).isEqualTo("Grey Text");
    assertThat(grey.getCurrentTextColor())
        .isEqualTo(activity.getResources().getColor(R.color.grey42));
  }
}

