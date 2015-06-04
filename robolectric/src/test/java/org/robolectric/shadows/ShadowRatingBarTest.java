package org.robolectric.shadows;

import android.widget.RatingBar;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.TestRunners;
import org.robolectric.util.Transcript;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(TestRunners.MultiApiWithDefaults.class)
public class ShadowRatingBarTest {

  private RatingBar ratingBar;
  private RatingBar.OnRatingBarChangeListener listener;
  private Transcript transcript;

  @Before
  public void setup() {
    ratingBar = new RatingBar(RuntimeEnvironment.application);
    listener = new TestRatingBarChangedListener();
    transcript = new Transcript();
    ratingBar.setOnRatingBarChangeListener(listener);
  }

  @Test
  public void testOnSeekBarChangedListener() {
    assertThat(ratingBar.getOnRatingBarChangeListener()).isSameAs(listener);
    ratingBar.setOnRatingBarChangeListener(null);
    assertThat(ratingBar.getOnRatingBarChangeListener()).isNull();
  }

  @Test
  public void testOnChangeNotification() {
    ratingBar.setRating(5.0f);
    transcript.assertEventsSoFar("onRatingChanged() - 5.0");
  }

  private class TestRatingBarChangedListener implements RatingBar.OnRatingBarChangeListener {

    @Override
    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
      transcript.add("onRatingChanged() - " + rating);
    }
  }
}
