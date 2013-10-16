package org.robolectric.shadows;

import android.content.Context;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.res.ResName;
import org.robolectric.res.ResourceLoader;

import java.lang.reflect.Constructor;

import static org.fest.reflect.core.Reflection.field;
import static org.fest.reflect.core.Reflection.type;
import static org.robolectric.Robolectric.directlyOn;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Window.class)
public class ShadowWindow {
  @RealObject
  Window realWindow;

  private int flags;

  public static Window create(Context context) throws Exception {
    Class<?> phoneWindowClass = type(ShadowPhoneWindow.PHONE_WINDOW_CLASS_NAME).load();
    Constructor<?> constructor = phoneWindowClass.getConstructor(Context.class);
    return (Window) constructor.newInstance(context);
  }

  @Implementation
  public void setFlags(int flags, int mask) {
    this.flags = (this.flags & ~mask) | (flags & mask);
    directlyOn(realWindow, Window.class, "setFlags", int.class, int.class).invoke(flags, mask);
  }

  public boolean getFlag(int flag) {
    return (flags & flag) == flag;
  }

  public CharSequence getTitle() {
    return "";
  }

  public ImageView getHomeIcon() {
    ResourceLoader resourceLoader = Robolectric.getShadowApplication().getResourceLoader();
    ResName internalResource = new ResName("android", "id", "home");
    Integer resId = resourceLoader.getResourceIndex().getResourceId(internalResource);
    try {
      Class<?> actionBarViewClass = Class.forName("com.android.internal.widget.ActionBarView");
      ViewGroup actionBarView = (ViewGroup) field("mActionBar").ofType(actionBarViewClass).in(realWindow).get();
      return (ImageView) actionBarView.findViewById(resId);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("could not resolve ActionBarView");
    }
  }
}
