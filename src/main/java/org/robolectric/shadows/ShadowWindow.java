package org.robolectric.shadows;

import android.content.Context;
import android.view.Window;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import java.lang.reflect.Constructor;

import static org.fest.reflect.core.Reflection.type;
import static org.robolectric.Robolectric.directlyOn;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(value = Window.class)
public class ShadowWindow {
  @RealObject
  Window realWindow;

  private int flags;

  public static Window create(Context context) throws Exception {
      Class<?> phoneWindowClass = type(ShadowPhoneWindow.PHONE_WINDOW_CLASS_NAME).load();
      Constructor<?> constructor = phoneWindowClass.getConstructor(Context.class);
      Window phoneWindow = (Window) constructor.newInstance(context);
      return phoneWindow;
  }

  @Implementation
  public void setFlags(int flags, int mask) {
    this.flags = (this.flags & ~mask) | (flags & mask);
    directlyOn(realWindow, Window.class, "setFlags", int.class, int.class).invoke(flags, mask);
  }

  public boolean getFlag(int flag) {
    return (flags & flag) == flag;
  }

  public void performLayout() {
    //((Window) realWindow).performLayout();
  }

  public CharSequence getTitle() {
    return "";
  }
}
