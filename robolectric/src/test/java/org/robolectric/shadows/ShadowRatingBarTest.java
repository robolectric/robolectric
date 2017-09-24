package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;

import android.widget.RatingBar;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ShadowRatingBarTest {

  private RatingBar ratingBar;
  private RatingBar.OnRatingBarChangeListener listener;
  private List<String> transcript;

  @Before
  public void setup() {
    ratingBar = new RatingBar(RuntimeEnvironment.application);
    listener = new TestRatingBarChangedListener();
    transcript = new ArrayList<>();
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
    assertThat(transcript).containsExactly("onRatingChanged() - 5.0");
  }

  private class TestRatingBarChangedListener implements RatingBar.OnRatingBarChangeListener {

    @Override
    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
      transcript.add("onRatingChanged() - " + rating);
    }
  }
}
