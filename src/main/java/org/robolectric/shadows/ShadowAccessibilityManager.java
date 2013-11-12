package org.robolectric.shadows;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;
import org.fest.reflect.field.Invoker;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.internal.HiddenApi;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.fest.reflect.core.Reflection.field;
import static org.fest.reflect.core.Reflection.method;

@Implements(AccessibilityManager.class)
public class ShadowAccessibilityManager {

  @HiddenApi @Implementation
  public static AccessibilityManager getInstance(Context context) throws Exception {
    AccessibilityManager accessibilityManager = Robolectric.newInstance(AccessibilityManager.class, new Class[0], new Object[0]);
    Handler handler = new MyHandler(context.getMainLooper(), accessibilityManager);
    Invoker<Handler> mHandlerField = field("mHandler").ofType(Handler.class).in(AccessibilityManager.class);
    makeNonFinal(mHandlerField.info()).set(accessibilityManager, handler);
    return accessibilityManager;
  }

  @Implementation
  public boolean addAccessibilityStateChangeListener(AccessibilityManager.AccessibilityStateChangeListener listener) {
    return true;
  }

  @Implementation
  public boolean removeAccessibilityStateChangeListener(AccessibilityManager.AccessibilityStateChangeListener listener) {
    return true;
  }

  static Field makeNonFinal(Field field) {
    try {
      field.setAccessible(true);
      Field modifiersField = Field.class.getDeclaredField("modifiers");
      modifiersField.setAccessible(true);
      modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
      return field;
    } catch (NoSuchFieldException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }


  // yuck, copied over from AccessibilityManager
  static class MyHandler extends Handler {
    private static final int DO_SET_STATE = 10;
    private final AccessibilityManager accessibilityManager;

    MyHandler(Looper mainLooper, AccessibilityManager accessibilityManager) {
      super(mainLooper);
      this.accessibilityManager = accessibilityManager;
    }

    @Override
    public void handleMessage(Message message) {
      switch (message.what) {
        case DO_SET_STATE:
          method("setState").withParameterTypes(int.class).in(accessibilityManager).invoke(message.arg1);
          return;
        default:
          Log.w("AccessibilityManager", "Unknown message type: " + message.what);
      }
    }
  }

}
