package org.robolectric.shadows;

import android.annotation.SuppressLint;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import java.io.IOException;
import java.util.Queue;
import javax.annotation.Nullable;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;

/**
 * Extends IsoDep to allow for testing.
 *
 * <p>Control the allowed packet size with {@link #setExtendedLengthApduSupported} and {@link
 * #setMaxTransceiveLength}. Note that extended Apdu packets have a max transceive length of 0x10008
 * but most hardware implementations will have a lower limit. If extended length apdus are not
 * supported, the theoretical max transceive length is 0x105 but, again, may be lower in practice.
 *
 * <p>Dictate the Apdu response returned in {@link #transceive} via {@link
 * #setTransceiveResponseQueue}, {@link #setTransceiveResponse}, or {@link
 * #setNextTransceiveResponse}. {@link #setTransceiveResponse} will be returned with every call to
 * transceive while {@link #setNextTransceiveResponse} will be returned only once. If a queue is set
 * via {@link #setTransceiveResponseQueue}, responses are polled in order and a null entry throws an
 * {@link IOException}. If none is set, transceive will throw an {@link IOException}.
 */
@Implements(IsoDep.class)
public class ShadowIsoDep extends ShadowBasicTagTechnology {

  @SuppressLint("PrivateApi")
  public static IsoDep newInstance() {
    return Shadow.newInstance(IsoDep.class, new Class<?>[] {Tag.class}, new Object[] {null});
  }

  private byte[] transceiveResponse = null;
  private byte[] nextTransceiveResponse = null;
  private Queue<byte[]> transceiveResponseQueue = null;
  private boolean isExtendedLengthApduSupported = true;
  private int timeout = 300; // Default timeout in AOSP
  private int maxTransceiveLength = 0xFEFF; // Default length in AOSP

  @Implementation
  protected void __constructor__(Tag tag) {}

  @Implementation
  protected byte[] transceive(byte[] data) throws IOException {
    if (transceiveResponseQueue != null && !transceiveResponseQueue.isEmpty()) {
      byte[] response = transceiveResponseQueue.poll();
      if (response == null) {
        throw new IOException();
      }
      return response;
    }
    if (nextTransceiveResponse != null) {
      try {
        return nextTransceiveResponse;
      } finally {
        nextTransceiveResponse = null;
      }
    }
    if (transceiveResponse != null) {
      return transceiveResponse;
    }
    throw new IOException();
  }

  public void setTransceiveResponse(byte[] response) {
    transceiveResponse = response;
  }

  public void setNextTransceiveResponse(byte[] response) {
    nextTransceiveResponse = response;
  }

  public void setTransceiveResponseQueue(@Nullable Queue<byte[]> responseQueue) {
    transceiveResponseQueue = responseQueue;
  }

  @Implementation
  protected void setTimeout(int timeoutMillis) {
    timeout = timeoutMillis;
  }

  @Implementation
  protected int getTimeout() {
    return timeout;
  }

  @Implementation
  protected int getMaxTransceiveLength() {
    return maxTransceiveLength;
  }

  public void setMaxTransceiveLength(int length) {
    maxTransceiveLength = length;
  }

  @Implementation
  protected boolean isExtendedLengthApduSupported() {
    return isExtendedLengthApduSupported;
  }

  public void setExtendedLengthApduSupported(boolean supported) {
    isExtendedLengthApduSupported = supported;
  }
}
