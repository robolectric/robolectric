package org.robolectric.shadows;

import android.annotation.SuppressLint;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import java.io.IOException;
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
 * <p>Dictate the Apdu response returned in {@link transceive} via {@link #setTransceiveResponse} or
 * {@link #setNextTransceiveResponse}. The former will be returned with every call to transceive
 * while the later will be returned only once. If neither is set, transceive will throw an
 * IOException.
 */
@Implements(IsoDep.class)
public class ShadowIsoDep extends ShadowBasicTagTechnology {

  @SuppressLint("PrivateApi")
  @SuppressWarnings("unchecked")
  public static IsoDep newInstance() {
    return Shadow.newInstance(IsoDep.class, new Class<?>[] {Tag.class}, new Object[] {null});
  }

  private byte[] transceiveResponse = null;
  private byte[] nextTransceiveResponse = null;
  private boolean isExtendedLengthApduSupported = true;
  private int timeout = 300; // Default timeout in AOSP
  private int maxTransceiveLength = 0xFEFF; // Default length in AOSP

  @Implementation
  protected void __constructor__(Tag tag) {}

  @Implementation
  protected byte[] transceive(byte[] data) throws IOException {
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
