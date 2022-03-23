package org.robolectric.internal.bytecode;

import static com.google.common.truth.Truth.assertThat;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link ClassValueMap} */
@RunWith(JUnit4.class)
public class ClassValueMapTest {

  private final ClassValueMap<String> map =
      new ClassValueMap<String>() {
        @Override
        protected String computeValue(Class<?> type) {
          return type.toString();
        }
      };

  @Test
  @SuppressWarnings("FutureReturnValueIgnored")
  public void testConcurrency() throws Exception {
    Random r = new Random();
    ExecutorService executor = Executors.newFixedThreadPool(4);
    int n = 10000;
    CountDownLatch latch = new CountDownLatch(n);
    AtomicInteger failures = new AtomicInteger(0);
    for (int i = 0; i < n; i++) {
      executor.submit(
          () -> {
            latch.countDown();
            if (r.nextInt(2) == 0) {
              if (map.get(Object.class) == null) {
                failures.incrementAndGet();
              }
            } else {
              // Simulate GC of weak references
              map.clear();
            }
          });
    }
    latch.await();
    executor.shutdown();
    assertThat(failures.get()).isEqualTo(0);
  }
}
