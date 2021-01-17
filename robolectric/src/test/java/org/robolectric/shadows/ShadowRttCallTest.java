package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.os.Build.VERSION_CODES;
import android.telecom.Call.RttCall;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowCall.ShadowRttCall;

/** Test of ShadowRttCall. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = VERSION_CODES.O_MR1)
public class ShadowRttCallTest {

  @Test
  public void storesOneIncomingMessage() throws Exception {
    List<String> messages = new ArrayList<>();
    messages.add("hi");
    RttCall call = createRttCall(messages);
    String message = call.readImmediately();
    assertThat(message).matches("hi");
  }

  @Test
  public void storeMultipleIncomingMessages() throws Exception {
    List<String> messages = new ArrayList<>();
    messages.add("hi");
    messages.add("how are you");
    messages.add("where are you");
    RttCall call = createRttCall(messages);
    String message = call.readImmediately();
    assertThat(message).matches("hihow are youwhere are you");
  }

  @Test
  public void emptyBuffer_returnsNull() throws Exception {
    RttCall call = createRttCall(new ArrayList<>());
    assertThat(call.readImmediately()).isNull();
  }

  @Test
  public void emptiesBufferAfterRead() throws Exception {
    List<String> messages = new ArrayList<>();
    messages.add("hi");
    RttCall call = createRttCall(messages);
    call.readImmediately();
    assertThat(call.readImmediately()).isNull();
  }

  private RttCall createRttCall(List<String> messages) throws IOException {
    RttCall rttCall = new RttCall(null, null, null, 0, null);
    ShadowRttCall shadowRttCall = shadowOf(rttCall);
    for (String message : messages) {
      shadowRttCall.writeRemoteMessage(message);
    }
    return rttCall;
  }
}
