package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;

import android.os.TestLooperManager;
import org.robolectric.annotation.Implements;

/**
 * A shadow of {@link TestLooperManager} that supports acquiring a TestLooperManager from the
 * looper's current thread.
 *
 * <p>This is necessary because Robolectric tests run in the main thread, and need to acquire the
 * TestLooperManager for the main looper. But when acquired in this mode,a TestLooperManager can
 * then *only* be interacted with from the main thread.
 *
 * <p>In real Android, the typical use case is to acquire the loopermanager from the
 * instrumentation/test thread.
 */
@Implements(value = TestLooperManager.class, minSdk = O)
public class ShadowTestLooperManager {

  //   private boolean currentThreadLooper = false;
  //   private @RealObject TestLooperManager realObject;
  //
  //   @Implementation
  //   protected void __constructor__(Looper looper) {
  //     if (looper.isCurrentThread()) {
  //       currentThreadLooper = true;
  //       // bypass the real constructor to avoid posting the forever looping LooperHolder
  //       _TestLooperManager_ _testLooperManager_ = reflector(_TestLooperManager_.class,
  // realObject);
  //       ArraySet<Looper> heldLoopers = reflector(_TestLooperManager_.class).getHeldLoopers();
  //       synchronized (heldLoopers) {
  //         if (heldLoopers.contains(looper)) {
  //           throw new RuntimeException("TestLooperManager already held for this looper");
  //         }
  //         heldLoopers.add(looper);
  //       }
  //       _testLooperManager_.setLooper(looper);
  //       _testLooperManager_.setQueue(looper.getQueue());
  //       _testLooperManager_.setLooperBlocked(true);
  //       _testLooperManager_.setExecuteQueue(new LinkedBlockingQueue());
  //     } else {
  //       invokeConstructor(TestLooperManager.class, realObject, from(Looper.class, looper));
  //     }
  //   }
  //
  //   /**
  //    * Throws IllegalStateException if this TestLooperManager was acquired on the looper thread,
  // and
  //    * next is called from a non looper thread
  //    */
  //   @Implementation
  //   protected Message next() {
  //     _TestLooperManager_ _testLooperManager_ = reflector(_TestLooperManager_.class, realObject);
  //     if (currentThreadLooper && _testLooperManager_.getLooper().isCurrentThread()) {
  //       throw new IllegalStateException(
  //           "This TestLooperManager can only be advanced from its Looper thread");
  //     }
  //     return directlyOn(realObject, TestLooperManager.class).next();
  //   }
  //
  //   @ForType(TestLooperManager.class)
  //   interface _TestLooperManager_ {
  //
  //     @Static
  //     @Accessor("sHeldLoopers")
  //     public ArraySet<Looper> getHeldLoopers();
  //
  //     @Static
  //     @Accessor("sHeldLoopers")
  //     public void setHeldLoopers(ArraySet<Looper> set);
  //
  //     @Accessor("mLooper")
  //     public void setLooper(Looper looper);
  //
  //     @Accessor("mLooper")
  //     public Looper getLooper();
  //
  //     @Accessor("mQueue")
  //     public void setQueue(MessageQueue queue);
  //
  //     @Accessor("mLooperBlocked")
  //     public void setLooperBlocked(boolean blocked);
  //
  //     @Accessor("mExecuteQueue")
  //     public void setExecuteQueue(LinkedBlockingQueue queue);
  //  }
}
