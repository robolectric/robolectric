package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.hardware.camera2.extension.RequestProcessor;
import android.os.Build.VERSION_CODES;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = VERSION_CODES.VANILLA_ICE_CREAM)
public final class RequestProcessorBuilderTest {

  @Test
  public void build_returnsNonNullRequestProcessor() {
    RequestProcessor requestProcessor = RequestProcessorBuilder.newBuilder().build();
    assertThat(requestProcessor).isNotNull();
  }

  @Test
  public void requestProcessor_methodsDoNotThrowNpe() {
    RequestProcessor requestProcessor = RequestProcessorBuilder.newBuilder().build();
    requestProcessor.abortCaptures();
    requestProcessor.stopRepeating();
  }
}
