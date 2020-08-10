package org.robolectric.internal.dependency;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class KeyStoreUtilTest {
  @Test
  public void defaultType_isJKS() throws Exception {
    assertThat(KeyStoreUtil.getKeyStore().getType()).isEqualTo("jks");
  }
}
