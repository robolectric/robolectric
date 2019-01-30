package org.robolectric.android.compat;

import android.os.Message;
import android.os.MessageQueue;
import android.os.TestLooperManager;

class TestLooperManagerCompatO implements TestLooperManagerCompat {

  private final TestLooperManager testLooperManager;

  TestLooperManagerCompatO(TestLooperManager looperManager) {
    this.testLooperManager = looperManager;
  }

  public MessageQueue getMessageQueue() {
    return testLooperManager.getMessageQueue();
  }

  @Override
  public Message next() {
    return testLooperManager.next();
  }

  @Override
  public void release() {
    testLooperManager.release();
  }

  @Override
  public void execute(Message message) {
    testLooperManager.execute(message);
  }

  @Override
  public void recycle(Message msg) {
    testLooperManager.recycle(msg);
  }
}
