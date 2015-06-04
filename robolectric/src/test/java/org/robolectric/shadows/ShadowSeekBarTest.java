package org.robolectric.shadows;

import android.widget.SeekBar;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.TestRunners;
import org.robolectric.util.Transcript;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(TestRunners.MultiApiWithDefaults.class)
public class ShadowSeekBarTest {

  private SeekBar seekBar;
  private ShadowSeekBar shadow;
  private SeekBar.OnSeekBarChangeListener listener;
  private Transcript transcript;

  @Before
  public void setup() {
    seekBar = new SeekBar(RuntimeEnvironment.application);
    shadow = Shadows.shadowOf(seekBar);
    listener = new TestSeekBarChangedListener();
    transcript = new Transcript();
    seekBar.setOnSeekBarChangeListener(listener);
  }

  @Test
  public void testOnSeekBarChangedListener() {
    assertThat(shadow.getOnSeekBarChangeListener()).isSameAs(listener);
    seekBar.setOnSeekBarChangeListener(null);
    assertThat(shadow.getOnSeekBarChangeListener()).isNull();
  }

  @Test
  public void testOnChangeNotification() {
    seekBar.setProgress(5);
    transcript.assertEventsSoFar("onProgressChanged() - 5");
  }

  private class TestSeekBarChangedListener implements SeekBar.OnSeekBarChangeListener {

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
      transcript.add("onProgressChanged() - " + progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }
  }
}
