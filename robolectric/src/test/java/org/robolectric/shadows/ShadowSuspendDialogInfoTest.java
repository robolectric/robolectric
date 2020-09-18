package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.R;
import static com.google.common.truth.Truth.assertThat;

import android.content.pm.SuspendDialogInfo;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

/** Tests for {@link ShadowSuspendDialogInfo} */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = R)
public class ShadowSuspendDialogInfoTest {
  @Test
  public void getNeutralActionButton_notSet_shouldReturnMoreDetails() {
    SuspendDialogInfo dialogInfo = new SuspendDialogInfo.Builder().build();
    ShadowSuspendDialogInfo shadowDialogInfo = Shadow.extract(dialogInfo);
    assertThat(shadowDialogInfo.getNeutralButtonAction())
        .isEqualTo(SuspendDialogInfo.BUTTON_ACTION_MORE_DETAILS);
  }

  @Test
  public void getNeutralActionButton_setToUnsuspend_shouldReturnUnsuspend() {
    SuspendDialogInfo dialogInfo =
        new SuspendDialogInfo.Builder()
            .setNeutralButtonAction(SuspendDialogInfo.BUTTON_ACTION_UNSUSPEND)
            .build();
    ShadowSuspendDialogInfo shadowDialogInfo = Shadow.extract(dialogInfo);
    assertThat(shadowDialogInfo.getNeutralButtonAction())
        .isEqualTo(SuspendDialogInfo.BUTTON_ACTION_UNSUSPEND);
  }
}
