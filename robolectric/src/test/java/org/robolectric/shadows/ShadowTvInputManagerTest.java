package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.media.tv.TvInputManager;
import android.os.Build.VERSION_CODES;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Unit tests for {@link org.robolectric.shadows.ShadowTvInputManager}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = VERSION_CODES.LOLLIPOP)
public class ShadowTvInputManagerTest {
  private TvInputManager tvInputManager;

  @Before
  public void setUp() {
    tvInputManager =
        (TvInputManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.TV_INPUT_SERVICE);
  }

  @Test
  public void getTvContentRatingSystemList_shouldReturnEmptyList() {
    assertThat(tvInputManager.getTvContentRatingSystemList()).isEmpty();
  }
}
