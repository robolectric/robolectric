package org.robolectric.android.compat;

import static android.os.Build.VERSION_CODES.KITKAT;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.os.Build;
import android.os.Message;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

public class MessageCompat {

  public static void recycleUnchecked(Message msg) {
    if (Build.VERSION.SDK_INT <= KITKAT) {
      msg.recycle();
    } else {
      reflector(_Message_.class, msg).recycleUnchecked();
    }
  }

  /** Accessor interface for {@link Message}'s internals. */
  @ForType(Message.class)
  interface _Message_ {

    void recycleUnchecked();

    @Accessor("when")
    long getWhen();
  }
}
