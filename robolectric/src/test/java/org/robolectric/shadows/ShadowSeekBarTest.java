package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;

import android.widget.SeekBar;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ShadowSeekBarTest {

  private SeekBar seekBar;
  private ShadowSeekBar shadow;
  private SeekBar.OnSeekBarChangeListener listener;
  private List<String> transcript;

  @Before
  public void setup() {
    seekBar = new SeekBar(RuntimeEnvironment.application);
    shadow = Shadows.shadowOf(seekBar);
    listener = new TestSeekBarChangedListener();
    transcript = new ArrayList<>();
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
    assertThat(transcript).containsExactly("onProgressChanged() - 5");
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
