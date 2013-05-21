package org.robolectric.tester.android.view;

import android.app.Activity;
import android.widget.TextView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;
import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class RoboActionBarTest {

  private final RoboActionBar actionBar = new RoboActionBar();

  @Test
  public void shouldSetTheCustomView() throws Exception {
    TextView textView = new TextView(new Activity());
    actionBar.setCustomView(textView);
    assertThat(actionBar.getCustomView()).isSameAs(textView);
  }

  @Test
  public void setDisplayShowCustomEnabled_shouldWork() throws Exception {
    assertThat(actionBar.getDisplayShowCustomEnabled()).isFalse();
    actionBar.setDisplayShowCustomEnabled(true);
    assertThat(actionBar.getDisplayShowCustomEnabled()).isTrue();
  }

  @Test
  public void setDisplayShowTitleEnabled_shouldWork() throws Exception {
    assertThat(actionBar.getDisplayShowTitleEnabled()).isTrue();
    actionBar.setDisplayShowTitleEnabled(false);
    assertThat(actionBar.getDisplayShowTitleEnabled()).isFalse();
  }
}