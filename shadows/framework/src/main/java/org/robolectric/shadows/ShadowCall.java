package org.robolectric.shadows;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.robolectric.shadow.api.Shadow.invokeConstructor;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.os.Build.VERSION_CODES;
import android.telecom.Call;
import android.telecom.Call.RttCall;
import android.telecom.InCallAdapter;
import android.util.Log;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/** Robolectric test for {@link android.telecom.Call}. */
@Implements(value = Call.class, minSdk = VERSION_CODES.LOLLIPOP)
public class ShadowCall {
  @RealObject Call realObject;
  private boolean hasSentRttRequest;
  private boolean hasRespondedToRttRequest;

  @Implementation(minSdk = VERSION_CODES.P)
  protected void sendRttRequest() {
    hasSentRttRequest = true;
    if (getInCallAdapter() == null) {
      return;
    }
    reflector(ReflectorCall.class, realObject).sendRttRequest();
  }

  /**
   * Determines whether sendRttRequest() was called.
   *
   * @return true if sendRttRequest() was called, false otherwise.
   */
  public boolean hasSentRttRequest() {
    return hasSentRttRequest;
  }

  /** "Forgets" that sendRttRequest() was called. */
  public void clearHasSentRttRequest() {
    hasSentRttRequest = false;
  }

  @Implementation(minSdk = VERSION_CODES.P)
  protected void respondToRttRequest(int id, boolean accept) {
    hasRespondedToRttRequest = true;
    if (getInCallAdapter() == null) {
      return;
    }
    reflector(ReflectorCall.class, realObject).respondToRttRequest(id, accept);
  }

  /**
   * Determines whether respondToRttRequest() was called.
   *
   * @return True if respondToRttRequest() was called, false otherwise.
   */
  public boolean hasRespondedToRttRequest() {
    return hasRespondedToRttRequest;
  }

  /** Robolectric test for {@link android.telecom.Call.RttCall}. */
  @Implements(value = Call.RttCall.class, minSdk = VERSION_CODES.O_MR1)
  public static class ShadowRttCall {
    private static final String TAG = "ShadowRttCall";
    @RealObject RttCall realRttCallObject;
    PipedOutputStream pipedOutputStream = new PipedOutputStream();

    @Implementation
    protected void __constructor__(
        String telecomCallId,
        InputStreamReader receiveStream,
        OutputStreamWriter transmitStream,
        int mode,
        InCallAdapter inCallAdapter) {
      PipedInputStream pipedInputStream = new PipedInputStream();
      try {
        pipedInputStream.connect(pipedOutputStream);
      } catch (IOException e) {
        Log.w(TAG, "Could not connect streams.");
      }
      invokeConstructor(
          RttCall.class,
          realRttCallObject,
          ClassParameter.from(String.class, telecomCallId),
          ClassParameter.from(
              InputStreamReader.class, new InputStreamReader(pipedInputStream, UTF_8)),
          ClassParameter.from(OutputStreamWriter.class, transmitStream),
          ClassParameter.from(int.class, mode),
          ClassParameter.from(InCallAdapter.class, inCallAdapter));
    }

    /**
     * Writes a message to the RttCall buffer. This simulates receiving a message from a sender
     * during an RTT call.
     *
     * @param message from sender.
     * @throws IOException if write to buffer fails.
     */
    public void writeRemoteMessage(String message) throws IOException {
      byte[] messageBytes = message.getBytes();
      pipedOutputStream.write(messageBytes, 0, messageBytes.length);
    }
  }

  public String getId() {
    return reflector(ReflectorCall.class, realObject).getId();
  }

  private InCallAdapter getInCallAdapter() {
    return reflector(ReflectorCall.class, realObject).getInCallAdapter();
  }

  @ForType(Call.class)
  interface ReflectorCall {

    @Direct
    void sendRttRequest();

    @Direct
    void respondToRttRequest(int id, boolean accept);

    @Accessor("mTelecomCallId")
    String getId();

    @Accessor("mInCallAdapter")
    InCallAdapter getInCallAdapter();
  }
}
