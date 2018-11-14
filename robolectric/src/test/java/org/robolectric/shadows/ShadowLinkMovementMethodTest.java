package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.text.method.LinkMovementMethod;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowLinkMovementMethodTest {

  @Test
  public void getInstance_shouldReturnAnInstanceOf_LinkedMovementMethod() throws Exception {
    assertThat(LinkMovementMethod.getInstance()).isInstanceOf(LinkMovementMethod.class);
  }

}
