package org.robolectric.integrationtests.securityproviders;

import static com.google.common.truth.Truth.assertThat;

import java.security.MessageDigest;
import java.util.Random;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/** MD5-related tests. */
@RunWith(RobolectricTestRunner.class)
public class Md5Test {
  @Ignore("Re-enable when performing a benchmark.")
  @Test
  public void md5Benchmark() throws Exception {
    Random random = new Random(100);
    MessageDigest messageDigest = MessageDigest.getInstance("MD5");
    for (int i = 0; i < 5_000_000; i++) {
      byte[] bytes = new byte[1024];
      random.nextBytes(bytes);
      messageDigest.update(bytes);
      byte[] hash = messageDigest.digest();
      assertThat(hash).hasLength(16);
      messageDigest.reset();
    }
  }
}
