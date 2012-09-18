package com.xtremelabs.robolectric.shadows;

import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class ShadowBase64Test {

  @Test
  public void testEncodeToString_alwaysReturnsEmptyString() throws Exception {
      assertThat(ShadowBase64.encodeToString(null, -1), equalTo(""));
      assertThat(ShadowBase64.encodeToString(null, -1, -1, -1), equalTo(""));
  }

  @Test
  public void testEncodeAlwaysReturnsEmptyByteArray() throws Exception {
      assertThat(ShadowBase64.encode(null, -1), equalTo(new byte[0]));
      assertThat(ShadowBase64.encode(null, -1, -1, -1), equalTo(new byte[0]));
  }

  @Test
  public void testDecodeAlwaysReturnsEmptyByteArray() throws Exception {
      assertThat(ShadowBase64.decode((String) null, -1), equalTo(new byte[0]));
      assertThat(ShadowBase64.decode((byte[]) null, -1), equalTo(new byte[0]));
      assertThat(ShadowBase64.decode(null, -1, -1, -1), equalTo(new byte[0]));
  }
}
