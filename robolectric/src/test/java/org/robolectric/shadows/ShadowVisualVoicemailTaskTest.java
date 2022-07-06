package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.os.Build.VERSION_CODES;
import android.telephony.VisualVoicemailService.VisualVoicemailTask;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

/** Tests for {@link ShadowVisualVoicemailTask} */
@RunWith(RobolectricTestRunner.class)
@Config(minSdk = VERSION_CODES.O)
public class ShadowVisualVoicemailTaskTest {

  private VisualVoicemailTask task;
  private ShadowVisualVoicemailTask shadowTask;

  @Before
  public void setup() {
    task = Shadow.newInstanceOf(VisualVoicemailTask.class);
    shadowTask = Shadow.extract(task);
  }

  @Test
  public void isFinished_defaultFalse() {
    assertThat(shadowTask.isFinished()).isFalse();
  }

  @Test
  public void finish_setsIsFinishedTrue() {
    task.finish();

    assertThat(shadowTask.isFinished()).isTrue();
  }
}
