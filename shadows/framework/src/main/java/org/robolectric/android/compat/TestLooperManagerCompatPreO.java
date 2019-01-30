package org.robolectric.android.compat;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import org.robolectric.util.reflector.ForType;

/** A copy of the P version of TestLooperManager for older APIs */
class TestLooperManagerCompatPreO implements TestLooperManagerCompat {

  private static final Set<Looper> sHeldLoopers = new HashSet<>();

  private final MessageQueue mQueue;
  private final Looper mLooper;
  private final LinkedBlockingQueue<MessageExecution> mExecuteQueue = new LinkedBlockingQueue<>();

  private boolean mReleased;
  private boolean mLooperBlocked;

  public TestLooperManagerCompatPreO(Looper looper) {
    synchronized (sHeldLoopers) {
      if (sHeldLoopers.contains(looper)) {
        throw new RuntimeException("TestLooperManager already held for this looper");
      }
      sHeldLoopers.add(looper);
    }
    mLooper = looper;
    mQueue = mLooper.getQueue();
    // Post a message that will keep the looper blocked as long as we are dispatching.
    new Handler(looper).post(new LooperHolder());
  }

  /** Returns the {@link MessageQueue} this object is wrapping. */
  public MessageQueue getMessageQueue() {
    checkReleased();
    return mQueue;
  }

  /**
   * Returns the next message that should be executed by this queue, may block if no messages are
   * ready.
   *
   * <p>Callers should always call {@link #recycle(Message)} on the message when all interactions
   * with it have completed.
   */
  public Message next() {
    // Wait for the looper block to come up, to make sure we don't accidentally get
    // the message for the block.
    while (!mLooperBlocked) {
      synchronized (this) {
        try {
          wait();
        } catch (InterruptedException e) {
        }
      }
    }
    checkReleased();
    return reflector(_MessageQueue_.class, mQueue).next();
  }

  /**
   * Releases the looper to continue standard looping and processing of messages, no further
   * interactions with TestLooperManager will be allowed after release() has been called.
   */
  public void release() {
    synchronized (sHeldLoopers) {
      sHeldLoopers.remove(mLooper);
    }
    checkReleased();
    mReleased = true;
    mExecuteQueue.add(new MessageExecution());
  }

  /**
   * Executes the given message on the Looper thread this wrapper is attached to.
   *
   * <p>Execution will happen on the Looper's thread (whether it is the current thread or not), but
   * all RuntimeExceptions encountered while executing the message will be thrown on the calling
   * thread.
   */
  public void execute(Message message) {
    checkReleased();
    if (Looper.myLooper() == mLooper) {
      // This is being called from the thread it should be executed on, we can just dispatch.
      message.getTarget().dispatchMessage(message);
    } else {
      MessageExecution execution = new MessageExecution();
      execution.m = message;
      synchronized (execution) {
        mExecuteQueue.add(execution);
        // Wait for the message to be executed.
        try {
          execution.wait();
        } catch (InterruptedException e) {
        }
        if (execution.response != null) {
          throw new RuntimeException(execution.response);
        }
      }
    }
  }

  /**
   * Called to indicate that a Message returned by {@link #next()} has been parsed and should be
   * recycled.
   */
  public void recycle(Message msg) {
    checkReleased();
    MessageCompat.recycleUnchecked(msg);
  }

  private void checkReleased() {
    if (mReleased) {
      throw new RuntimeException("release() has already be called");
    }
  }

  private class LooperHolder implements Runnable {
    @Override
    public void run() {
      synchronized (TestLooperManagerCompatPreO.this) {
        mLooperBlocked = true;
        TestLooperManagerCompatPreO.this.notify();
      }
      while (!mReleased) {
        try {
          final MessageExecution take = mExecuteQueue.take();
          if (take.m != null) {
            processMessage(take);
          }
        } catch (InterruptedException e) {
        }
      }
      synchronized (TestLooperManagerCompatPreO.this) {
        mLooperBlocked = false;
      }
    }

    private void processMessage(MessageExecution mex) {
      synchronized (mex) {
        try {
          mex.m.getTarget().dispatchMessage(mex.m);
          mex.response = null;
        } catch (Throwable t) {
          mex.response = t;
        }
        mex.notifyAll();
      }
    }
  }

  private static class MessageExecution {
    private Message m;
    private Throwable response;
  }

  /** Accessor interface for {@link Message}'s internals. */
  @ForType(MessageQueue.class)
  private interface _MessageQueue_ {

    Message next();
  }
}
