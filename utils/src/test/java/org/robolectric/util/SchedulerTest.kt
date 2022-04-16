package org.robolectric.util

import com.google.common.collect.ImmutableList
import com.google.common.collect.Iterables
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import java.util.ArrayList
import java.util.Random
import java.util.TreeMap
import java.util.concurrent.atomic.AtomicLong
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.robolectric.util.Scheduler.IdleState.CONSTANT_IDLE
import org.robolectric.util.Scheduler.IdleState.PAUSED
import org.robolectric.util.Scheduler.IdleState.UNPAUSED

@RunWith(JUnit4::class)
class SchedulerTest {
  private val scheduler = Scheduler()
  private val transcript: MutableList<String> = ArrayList()
  private var startTime: Long = 0

  @Before
  @Throws(Exception::class)
  fun setUp() {
    scheduler.pause()
    startTime = scheduler.currentTime
  }

  @Test
  fun whenIdleStateIsConstantIdle_isPausedReturnsFalse() {
    scheduler.idleState = CONSTANT_IDLE
    assertThat(scheduler.isPaused).isFalse()
  }

  @Test
  fun whenIdleStateIsUnPaused_isPausedReturnsFalse() {
    scheduler.idleState = UNPAUSED
    assertThat(scheduler.isPaused).isFalse()
  }

  @Test
  fun whenIdleStateIsPaused_isPausedReturnsTrue() {
    scheduler.idleState = PAUSED
    assertThat(scheduler.isPaused).isTrue()
  }

  @Test
  fun pause_setsIdleState() {
    scheduler.idleState = UNPAUSED
    scheduler.pause()
    assertThat(scheduler.idleState).isSameInstanceAs(PAUSED)
  }

  @Test
  @SuppressWarnings("deprecation")
  fun idleConstantly_setsIdleState() {
    scheduler.idleState = UNPAUSED
    scheduler.idleConstantly(true)
    assertThat(scheduler.idleState).isSameInstanceAs(CONSTANT_IDLE)
    scheduler.idleConstantly(false)
    assertThat(scheduler.idleState).isSameInstanceAs(UNPAUSED)
  }

  @Test
  fun unPause_setsIdleState() {
    scheduler.idleState = PAUSED
    scheduler.unPause()
    assertThat(scheduler.idleState).isSameInstanceAs(UNPAUSED)
  }

  @Test
  fun setIdleStateToUnPause_shouldRunPendingTasks() {
    scheduler.postDelayed(AddToTranscript("one"), 0)
    scheduler.postDelayed(AddToTranscript("two"), 0)
    scheduler.postDelayed(AddToTranscript("three"), 1000)
    assertThat(transcript).isEmpty()
    val time = scheduler.currentTime
    scheduler.idleState = UNPAUSED
    assertThat(transcript).containsExactly("one", "two")
    assertWithMessage("time").that(scheduler.currentTime).isEqualTo(time)
  }

  @Test
  fun setIdleStateToConstantIdle_shouldRunAllTasks() {
    scheduler.postDelayed(AddToTranscript("one"), 0)
    scheduler.postDelayed(AddToTranscript("two"), 0)
    scheduler.postDelayed(AddToTranscript("three"), 1000)
    assertThat(transcript).isEmpty()
    val time = scheduler.currentTime
    scheduler.idleState = CONSTANT_IDLE
    assertThat(transcript).containsExactly("one", "two", "three")
    assertWithMessage("time").that(scheduler.currentTime).isEqualTo(time + 1000)
  }

  @Test
  fun unPause_shouldRunPendingTasks() {
    scheduler.postDelayed(AddToTranscript("one"), 0)
    scheduler.postDelayed(AddToTranscript("two"), 0)
    scheduler.postDelayed(AddToTranscript("three"), 1000)
    assertThat(transcript).isEmpty()
    val time = scheduler.currentTime
    scheduler.unPause()
    assertThat(transcript).containsExactly("one", "two")
    assertWithMessage("time").that(scheduler.currentTime).isEqualTo(time)
  }

  @Test
  @SuppressWarnings("deprecation")
  fun idleConstantlyTrue_shouldRunAllTasks() {
    scheduler.postDelayed(AddToTranscript("one"), 0)
    scheduler.postDelayed(AddToTranscript("two"), 0)
    scheduler.postDelayed(AddToTranscript("three"), 1000)
    assertThat(transcript).isEmpty()
    val time = scheduler.currentTime
    scheduler.idleConstantly(true)
    assertThat(transcript).containsExactly("one", "two", "three")
    assertWithMessage("time").that(scheduler.currentTime).isEqualTo(time + 1000)
  }

  @Test
  @Throws(Exception::class)
  fun advanceTo_shouldAdvanceTimeEvenIfThereIsNoWork() {
    scheduler.advanceTo(1000)
    assertThat(scheduler.currentTime).isEqualTo(1000)
  }

  @Test
  @Throws(Exception::class)
  @SuppressWarnings("deprecation")
  fun advanceBy_returnsTrueIffSomeJobWasRun() {
    scheduler.postDelayed(AddToTranscript("one"), 0)
    scheduler.postDelayed(AddToTranscript("two"), 0)
    scheduler.postDelayed(AddToTranscript("three"), 1000)
    assertThat(scheduler.advanceBy(0)).isTrue()
    assertThat(transcript).containsExactly("one", "two")
    transcript.clear()
    assertThat(scheduler.advanceBy(0)).isFalse()
    assertThat(transcript).isEmpty()
    assertThat(scheduler.advanceBy(1000)).isTrue()
    assertThat(transcript).containsExactly("three")
  }

  @Test
  @Throws(Exception::class)
  @SuppressWarnings("deprecation")
  fun postDelayed_addsAJobToBeRunInTheFuture() {
    scheduler.postDelayed(AddToTranscript("one"), 1000)
    scheduler.postDelayed(AddToTranscript("two"), 2000)
    scheduler.postDelayed(AddToTranscript("three"), 3000)
    scheduler.advanceBy(1000)
    assertThat(transcript).containsExactly("one")
    transcript.clear()
    scheduler.advanceBy(500)
    assertThat(transcript).isEmpty()
    scheduler.advanceBy(501)
    assertThat(transcript).containsExactly("two")
    transcript.clear()
    scheduler.advanceBy(999)
    assertThat(transcript).containsExactly("three")
  }

  @Test
  fun postDelayed_whileIdlingConstantly_executesImmediately() {
    scheduler.idleState = CONSTANT_IDLE
    scheduler.postDelayed(AddToTranscript("one"), 1000)
    assertThat(transcript).containsExactly("one")
  }

  @Test
  fun postDelayed_whileIdlingConstantly_advancesTime() {
    scheduler.idleState = CONSTANT_IDLE
    scheduler.postDelayed(AddToTranscript("one"), 1000)
    assertThat(scheduler.currentTime).isEqualTo(1000 + startTime)
  }

  @Test
  @Throws(Exception::class)
  fun postAtFrontOfQueue_addsJobAtFrontOfQueue() {
    scheduler.post(AddToTranscript("one"))
    scheduler.post(AddToTranscript("two"))
    scheduler.postAtFrontOfQueue(AddToTranscript("three"))
    scheduler.runOneTask()
    assertThat(transcript).containsExactly("three")
    transcript.clear()
    scheduler.runOneTask()
    assertThat(transcript).containsExactly("one")
    transcript.clear()
    scheduler.runOneTask()
    assertThat(transcript).containsExactly("two")
  }

  @Test
  @Throws(Exception::class)
  fun postAtFrontOfQueue_whenUnpaused_runsJobs() {
    scheduler.unPause()
    scheduler.postAtFrontOfQueue(AddToTranscript("three"))
    assertThat(transcript).containsExactly("three")
  }

  @Test
  @Throws(Exception::class)
  @SuppressWarnings("deprecation")
  fun postDelayed_whenMoreItemsAreAdded_runsJobs() {
    scheduler.postDelayed(
      {
        transcript.add("one")
        scheduler.postDelayed(
          {
            transcript.add("two")
            scheduler.postDelayed(AddToTranscript("three"), 1000)
          },
          1000
        )
      },
      1000
    )
    scheduler.advanceBy(1000)
    assertThat(transcript).containsExactly("one")
    transcript.clear()
    scheduler.advanceBy(500)
    assertThat(transcript).isEmpty()
    scheduler.advanceBy(501)
    assertThat(transcript).containsExactly("two")
    transcript.clear()
    scheduler.advanceBy(999)
    assertThat(transcript).containsExactly("three")
  }

  @Test
  @Throws(Exception::class)
  fun remove_ShouldRemoveAllInstancesOfRunnableFromQueue() {
    scheduler.post(TestRunnable())
    val runnable = TestRunnable()
    scheduler.post(runnable)
    scheduler.post(runnable)
    assertThat(scheduler.size()).isEqualTo(3)
    scheduler.remove(runnable)
    assertThat(scheduler.size()).isEqualTo(1)
    scheduler.advanceToLastPostedRunnable()
    assertThat(runnable.wasRun).isFalse()
  }

  @Test
  @Throws(Exception::class)
  fun reset_shouldUnPause() {
    scheduler.pause()
    val runnable = TestRunnable()
    scheduler.post(runnable)
    assertThat(runnable.wasRun).isFalse()
    scheduler.reset()
    scheduler.post(runnable)
    assertThat(runnable.wasRun).isTrue()
  }

  @Test
  @Throws(Exception::class)
  fun reset_shouldClearPendingRunnables() {
    scheduler.pause()
    val runnable1 = TestRunnable()
    scheduler.post(runnable1)
    assertThat(runnable1.wasRun).isFalse()
    scheduler.reset()
    val runnable2 = TestRunnable()
    scheduler.post(runnable2)
    assertThat(runnable1.wasRun).isFalse()
    assertThat(runnable2.wasRun).isTrue()
  }

  @Test
  fun nestedPost_whilePaused_doesntAutomaticallyExecute() {
    val order: MutableList<Int> = ArrayList()
    scheduler.postDelayed(
      {
        order.add(1)
        scheduler.post { order.add(4) }
        order.add(2)
      },
      0
    )
    scheduler.postDelayed({ order.add(3) }, 0)
    scheduler.runOneTask()
    assertWithMessage("order:first run").that(order).containsExactly(1, 2)
    assertWithMessage("size:first run").that(scheduler.size()).isEqualTo(2)
    scheduler.runOneTask()
    assertWithMessage("order:second run").that(order).containsExactly(1, 2, 3)
    assertWithMessage("size:second run").that(scheduler.size()).isEqualTo(1)
    scheduler.runOneTask()
    assertWithMessage("order:third run").that(order).containsExactly(1, 2, 3, 4)
    assertWithMessage("size:second run").that(scheduler.size()).isEqualTo(0)
  }

  @Test
  fun nestedPost_whileUnpaused_automaticallyExecutes3After() {
    val order: MutableList<Int> = ArrayList()
    scheduler.unPause()
    scheduler.postDelayed(
      {
        order.add(1)
        scheduler.post { order.add(3) }
        order.add(2)
      },
      0
    )
    assertWithMessage("order").that(order).containsExactly(1, 2, 3)
    assertWithMessage("size").that(scheduler.size()).isEqualTo(0)
  }

  @Test
  fun nestedPostAtFront_whilePaused_runsBeforeSubsequentPost() {
    val order: MutableList<Int> = ArrayList()
    scheduler.postDelayed(
      {
        order.add(1)
        scheduler.postAtFrontOfQueue { order.add(3) }
        order.add(2)
      },
      0
    )
    scheduler.postDelayed({ order.add(4) }, 0)
    scheduler.advanceToLastPostedRunnable()
    assertWithMessage("order").that(order).containsExactly(1, 2, 3, 4)
    assertWithMessage("size").that(scheduler.size()).isEqualTo(0)
  }

  @Test
  fun nestedPostAtFront_whileUnpaused_runsAfter() {
    val order: MutableList<Int> = ArrayList()
    scheduler.unPause()
    scheduler.postDelayed(
      {
        order.add(1)
        scheduler.postAtFrontOfQueue { order.add(3) }
        order.add(2)
      },
      0
    )
    assertWithMessage("order").that(order).containsExactly(1, 2, 3)
    assertWithMessage("size").that(scheduler.size()).isEqualTo(0)
  }

  @Test
  fun nestedPostDelayed_whileUnpaused_doesntAutomaticallyExecute3() {
    val order: MutableList<Int> = ArrayList()
    scheduler.unPause()
    scheduler.postDelayed(
      {
        order.add(1)
        scheduler.postDelayed({ order.add(3) }, 1)
        order.add(2)
      },
      0
    )
    assertWithMessage("order:before").that(order).containsExactly(1, 2)
    assertWithMessage("size:before").that(scheduler.size()).isEqualTo(1)
    scheduler.advanceToLastPostedRunnable()
    assertWithMessage("order:after").that(order).containsExactly(1, 2, 3)
    assertWithMessage("size:after").that(scheduler.size()).isEqualTo(0)
    assertWithMessage("time:after").that(scheduler.currentTime).isEqualTo(1 + startTime)
  }

  @Test
  fun nestedPostDelayed_whenIdlingConstantly_automaticallyExecutes3After() {
    val order: MutableList<Int> = ArrayList()
    scheduler.idleState = CONSTANT_IDLE
    scheduler.postDelayed(
      {
        order.add(1)
        scheduler.postDelayed({ order.add(3) }, 1)
        order.add(2)
      },
      0
    )
    assertWithMessage("order").that(order).containsExactly(1, 2, 3)
    assertWithMessage("size").that(scheduler.size()).isEqualTo(0)
    assertWithMessage("time").that(scheduler.currentTime).isEqualTo(1 + startTime)
  }

  @Test
  @Throws(Exception::class)
  fun post_whenTheRunnableThrows_executesSubsequentRunnables() {
    val runnablesThatWereRun: MutableList<Int> = ArrayList()
    scheduler.post {
      runnablesThatWereRun.add(1)
      throw RuntimeException("foo")
    }
    try {
      scheduler.unPause()
    } catch (ignored: RuntimeException) {}
    scheduler.post { runnablesThatWereRun.add(2) }
    assertThat(runnablesThatWereRun).containsExactly(1, 2)
  }

  @Test
  @Throws(Exception::class)
  fun testTimeNotChangedByNegativeDelay() {
    val currentTime = scheduler.currentTime
    val observedTime = LongArray(1)
    scheduler.postDelayed({ observedTime[0] = scheduler.currentTime }, -1000)
    scheduler.advanceToLastPostedRunnable()
    assertThat(observedTime[0]).isEqualTo(currentTime)
    assertThat(scheduler.currentTime).isEqualTo(currentTime)
  }

  /** Tests for quadratic or exponential behavior in the scheduler, and stable sorting */
  @Test(timeout = 1000)
  fun schedulerWithManyRunnables() {
    val random = Random(0)
    val orderCheck: MutableMap<Int, MutableList<Int>> = TreeMap()
    val actualOrder: MutableList<Int> = ArrayList()
    for (i in 0..19999) {
      val delay = random.nextInt(10)
      var list = orderCheck[delay]
      if (list == null) {
        list = ArrayList()
        orderCheck[delay] = list
      }
      list.add(i)
      scheduler.postDelayed({ actualOrder.add(i) }, delay.toLong())
    }
    assertThat(actualOrder).isEmpty()
    scheduler.advanceToLastPostedRunnable()
    assertThat(actualOrder).isEqualTo(ImmutableList.copyOf(Iterables.concat(orderCheck.values)))
  }

  @Test(timeout = 1000)
  @Throws(InterruptedException::class)
  fun schedulerAllowsConcurrentTimeRead_whileLockIsHeld() {
    val l = AtomicLong()
    val t: Thread =
      object : Thread("schedulerAllowsConcurrentTimeRead") {
        override fun run() {
          l.set(scheduler.currentTime)
        }
      }
    // Grab the lock and then start a thread that tries to get the current time. The other thread
    // should not deadlock.
    synchronized(scheduler) {
      t.start()
      t.join()
    }
  }

  @Test(timeout = 1000)
  @Throws(InterruptedException::class)
  fun schedulerAllowsConcurrentStateRead_whileLockIsHeld() {
    val t: Thread =
      object : Thread("schedulerAllowsConcurrentStateRead") {
        override fun run() {
          scheduler.idleState
        }
      }
    // Grab the lock and then start a thread that tries to get the idle state. The other thread
    // should not deadlock.
    synchronized(scheduler) {
      t.start()
      t.join()
    }
  }

  @Test(timeout = 1000)
  @Throws(InterruptedException::class)
  fun schedulerAllowsConcurrentIsPaused_whileLockIsHeld() {
    val t: Thread =
      object : Thread("schedulerAllowsConcurrentIsPaused") {
        override fun run() {
          scheduler.isPaused
        }
      }
    // Grab the lock and then start a thread that tries to get the paused state. The other thread
    // should not deadlock.
    synchronized(scheduler) {
      t.start()
      t.join()
    }
  }

  private inner class AddToTranscript(private val event: String) : Runnable {
    override fun run() {
      transcript.add(event)
    }
  }
}
