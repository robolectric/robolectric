package test;

import static org.assertj.core.api.Assertions.assertThat;
import org.robolectric.RobolectricTestRunner;
import static org.junit.Assert.fail;


@org.junit.runner.RunWith(RobolectricTestRunner.class)
public class ThingTest {
  @org.junit.Test
  public void shouldFail() throws Exception {
    fail("message");
  }
}