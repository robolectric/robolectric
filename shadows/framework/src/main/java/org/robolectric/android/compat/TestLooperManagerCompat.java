package org.robolectric.android.compat;

import android.os.Message;
import android.os.MessageQueue;

public interface TestLooperManagerCompat {

  public MessageQueue getMessageQueue();

  public Message next();

  public void release();

  public void execute(Message message);

  public void recycle(Message msg);

  // TODO: consider supporting hasMessages
  //   public boolean hasMessages(Handler h, Object object, int what);
  //
  //   public boolean hasMessages(Handler h, Object object, Runnable r);
}
