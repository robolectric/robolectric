package org.robolectric.shadows;

import android.app.Activity;
import android.content.Context;
import android.widget.AbsSeekBar;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class AbsSeekBarTest {

  @Test
  public void testInheritance() {
    TestAbsSeekBar seekBar = new TestAbsSeekBar(new Activity());
    ShadowAbsSeekBar shadow = Robolectric.shadowOf(seekBar);
    assertThat(shadow).isInstanceOf(ShadowProgressBar.class);
  }

  private static class TestAbsSeekBar extends AbsSeekBar {

    public TestAbsSeekBar(Context context) {
      super(context);
    }
  }
}
