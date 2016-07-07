package org.robolectric.shadows;

import android.content.Context;
import android.widget.AbsSeekBar;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.TestRunners;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(TestRunners.MultiApiWithDefaults.class)
public class ShadowAbsSeekBarTest {

  @Test
  public void testInheritance() {
    // TODO: this seems to test static typing - compiler enforces this ;)
    TestAbsSeekBar seekBar = new TestAbsSeekBar(RuntimeEnvironment.application);
    ShadowAbsSeekBar shadow = Shadows.shadowOf(seekBar);
    assertThat(shadow).isInstanceOf(ShadowProgressBar.class);
  }

  private static class TestAbsSeekBar extends AbsSeekBar {

    public TestAbsSeekBar(Context context) {
      super(context);
    }
  }
}
