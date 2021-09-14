package org.robolectric.shadows;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A pointer registration system used to associate real (long) pointers with fake 32-bit pointers
 * used in pre-lollipop.
 */
class PreLPointers {
  static final Map<Integer, Long> preLPointers = new ConcurrentHashMap<>();
  private static final AtomicInteger nextPreLPointer = new AtomicInteger(1);

  private PreLPointers() {}

  static int register(long realPtr) {
    int nextPtr = nextPreLPointer.incrementAndGet();
    preLPointers.put(nextPtr, realPtr);
    return nextPtr;
  }

  @SuppressWarnings("AndroidJdkLibsChecker")
  static long get(int fakePtr) {
    return preLPointers.computeIfAbsent(
        fakePtr,
        integer -> {
          throw new AssertionError("Missing pre-L pointer " + fakePtr);
        });
  }

  static void remove(int fakePtr) {
    if (!preLPointers.containsKey(fakePtr)) {
      throw new AssertionError("Missing pre-L pointer " + fakePtr);
    }
    preLPointers.remove(fakePtr);
  }
}
