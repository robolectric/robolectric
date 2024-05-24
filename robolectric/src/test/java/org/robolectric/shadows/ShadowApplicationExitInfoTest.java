package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.R;
import static com.google.common.truth.Truth.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;

import android.app.ApplicationExitInfo;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.primitives.Bytes;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = R)
public final class ShadowApplicationExitInfoTest {

  @Test
  public void setTraceInputStream_traceInputStreamSet() throws Exception {
    byte[] testBytes = "hello".getBytes(UTF_8);
    InputStream in = new ByteArrayInputStream(testBytes);

    ApplicationExitInfo info = new ApplicationExitInfo();
    ShadowApplicationExitInfo shadow = Shadow.extract(info);

    shadow.setTraceInputStream(in);

    assertThat(info.getTraceInputStream()).isNotNull();

    byte[] bytesFromAppExitStream = new byte[testBytes.length];
    assertThat(info.getTraceInputStream().read(bytesFromAppExitStream)).isEqualTo(testBytes.length);
    assertThat(bytesFromAppExitStream)
        .asList()
        .containsExactlyElementsIn(Bytes.asList(testBytes))
        .inOrder();
  }
}
