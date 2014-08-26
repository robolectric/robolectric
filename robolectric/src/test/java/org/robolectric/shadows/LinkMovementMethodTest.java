package org.robolectric.shadows;

import android.text.method.LinkMovementMethod;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class LinkMovementMethodTest {

  @Test
  public void getInstance_shouldReturnAnInstanceOf_LinkedMovementMethod() throws Exception {
    assertThat(LinkMovementMethod.getInstance()).isInstanceOf(LinkMovementMethod.class);
  }

}
