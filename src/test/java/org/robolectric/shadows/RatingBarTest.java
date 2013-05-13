package org.robolectric.shadows;

import android.app.Activity;
import android.content.Context;
import android.widget.RatingBar;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.robolectric.util.Transcript;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class RatingBarTest {

  private RatingBar ratingBar;
  private ShadowRatingBar shadow;
  private RatingBar.OnRatingBarChangeListener listener;
  private Transcript transcript;

  @Before
  public void setup() {
    ratingBar = new RatingBar(new Activity());
    shadow = Robolectric.shadowOf(ratingBar);
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

  @Test
  public void testInheritance() {
    TestRatingBar ratingBar = new TestRatingBar(new Activity());
    ShadowRatingBar shadow = Robolectric.shadowOf(ratingBar);
    assertThat(shadow).isInstanceOf(ShadowAbsSeekBar.class);
  }

  private static class TestRatingBar extends RatingBar {

    public TestRatingBar(Context context) {
      super(context);
    }
  }
}
