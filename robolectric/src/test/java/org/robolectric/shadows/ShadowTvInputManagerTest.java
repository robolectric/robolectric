package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;

import android.content.Context;
import android.media.tv.TvInputInfo;
import android.media.tv.TvInputManager;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowTvInputManager;

/** Tests for {@link ShadowTvInputManager}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = Q)
public final class ShadowTvInputManagerTest {
  private TvInputManager tvManager;
  private static final String INPUT_ID_1 = "input_id_1";
  private static final String INPUT_ID_2 = "input_id_2";
  private TvInputInfo tvInputInfo1;
  private TvInputInfo tvInputInfo2;

  @Before
  public void setUp() {
    tvInputInfo1 = mock(TvInputInfo.class);
    tvInputInfo2 = mock(TvInputInfo.class);
    tvManager = (TvInputManager) ApplicationProvider.getApplicationContext().getSystemService(Context.TV_INPUT_SERVICE);
    ShadowTvInputManager.setTvInputInfo(INPUT_ID_1, tvInputInfo1);
    ShadowTvInputManager.setTvInputInfo(INPUT_ID_2, tvInputInfo2);
    ShadowTvInputManager.setInputState(INPUT_ID_1, TvInputManager.INPUT_STATE_CONNECTED);
    ShadowTvInputManager.setInputState(INPUT_ID_2, TvInputManager.INPUT_STATE_DISCONNECTED);
  }

  @Test
  public void getTvInputInfo_expectedResult() {
    assertThat(tvManager.getTvInputInfo(INPUT_ID_1)).isEqualTo(tvInputInfo1);
    assertThat(tvManager.getTvInputInfo(INPUT_ID_2)).isEqualTo(tvInputInfo2);
  }

  @Test
  public void getTvInputList_expectedResult() {
    assertThat(tvManager.getTvInputList()).containsExactly(tvInputInfo1, tvInputInfo2);
  }

  @Test
  public void getInputState_expectedResult() {
    assertThat(tvManager.getInputState(INPUT_ID_1)).isEqualTo(TvInputManager.INPUT_STATE_CONNECTED);
    assertThat(tvManager.getInputState(INPUT_ID_2)).isEqualTo(TvInputManager.INPUT_STATE_DISCONNECTED);
  }
}
