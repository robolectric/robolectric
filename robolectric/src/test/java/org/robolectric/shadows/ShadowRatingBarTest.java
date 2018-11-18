package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.widget.RatingBar;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowRatingBarTest {

  private RatingBar ratingBar;
  private RatingBar.OnRatingBarChangeListener listener;
  private List<String> transcript;

  @Before
  public void setup() {
    ratingBar = new RatingBar(ApplicationProvider.getApplicationContext());
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
