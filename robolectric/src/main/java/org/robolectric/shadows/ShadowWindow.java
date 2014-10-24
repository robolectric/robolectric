package org.robolectric.shadows;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.res.ResName;
import org.robolectric.res.ResourceLoader;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import static org.robolectric.Robolectric.directlyOn;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Window.class)
public class ShadowWindow {
  @RealObject
  Window realWindow;

  private int flags;
  private int softInputMode;

  public static Window create(Context context) throws Exception {
    Class<?> phoneWindowClass = ShadowWindow.class.getClassLoader().loadClass(ShadowPhoneWindow.PHONE_WINDOW_CLASS_NAME);
    Constructor<?> constructor = phoneWindowClass.getConstructor(Context.class);
    return (Window) constructor.newInstance(context);
  }

  @Implementation
  public void setFlags(int flags, int mask) {
    this.flags = (this.flags & ~mask) | (flags & mask);
    directlyOn(realWindow, Window.class, "setFlags", new Robolectric.ClassParameter(int.class, flags), new Robolectric.ClassParameter(int.class, mask));
  }

  @Implementation
  public void setSoftInputMode(int softInputMode) {
    this.softInputMode = softInputMode;
    directlyOn(realWindow, Window.class, "setSoftInputMode", new Robolectric.ClassParameter(int.class, softInputMode));
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
      ViewGroup actionBarView;
      try {
        Field mActionBar = realWindow.getClass().getDeclaredField("mActionBar");
        mActionBar.setAccessible(true);
        actionBarView = (ViewGroup) mActionBar.get(realWindow);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      } catch (NoSuchFieldException e) {
        throw new RuntimeException(e);
      }
      return (ImageView) actionBarView.findViewById(resId);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("could not resolve ActionBarView");
    }
  }

  public Drawable getBackgroundDrawable() {
    return null;
  }

  public int getSoftInputMode() {
    return softInputMode;
  }

  public ProgressBar getProgressBar() {
    return null;
  }

  public ProgressBar getIndeterminateProgressBar() {
    return null;
  }
}
