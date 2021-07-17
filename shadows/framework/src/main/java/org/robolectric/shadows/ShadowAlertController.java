package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Adapter;
import android.widget.ListView;
import com.android.internal.app.AlertController;
import java.lang.reflect.InvocationTargetException;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

@Implements(value = AlertController.class, isInAndroidSdk = false)
public class ShadowAlertController {

  @RealObject AlertController realAlertController;

  private CharSequence title;
  private CharSequence message;
  private View view;
  private View customTitleView;
  private int iconId;

  @Implementation
  public void setTitle(CharSequence title) throws InvocationTargetException, IllegalAccessException {
    this.title = title;
    reflector(AlertControllerReflector.class, realAlertController).setTitle(title);
  }

  public CharSequence getTitle() {
    return title == null ? "" : title;
  }

  @Implementation
  public void setCustomTitle(View customTitleView) {
    this.customTitleView = customTitleView;
    reflector(AlertControllerReflector.class, realAlertController).setCustomTitle(customTitleView);
  }

  public View getCustomTitleView() {
    return customTitleView;
  }

  @Implementation
  public void setMessage(CharSequence message) {
    this.message = message;
    reflector(AlertControllerReflector.class, realAlertController).setMessage(message);
  }

  public CharSequence getMessage() {
    return message == null ? "" : message;
  }

  @Implementation
  public void setView(View view) {
    this.view = view;
    reflector(AlertControllerReflector.class, realAlertController).setView(view);
  }

  @Implementation(minSdk = LOLLIPOP)
  public void setView(int resourceId) {
    setView(LayoutInflater.from(RuntimeEnvironment.getApplication()).inflate(resourceId, null));
  }

  @Implementation
  public void setIcon(int iconId) {
    this.iconId = iconId;
    reflector(AlertControllerReflector.class, realAlertController).setIcon(iconId);
  }

  public int getIconId() {
    return iconId;
  }

  public View getView() {
    return view;
  }

  public Adapter getAdapter() {
    return ReflectionHelpers.<ListView>callInstanceMethod(realAlertController, "getListView")
        .getAdapter();
  }

  @ForType(AlertController.class)
  interface AlertControllerReflector {

    @Direct
    void setTitle(CharSequence title);

    @Direct
    void setCustomTitle(View customTitleView);

    @Direct
    void setMessage(CharSequence message);

    @Direct
    void setView(View view);

    @Direct
    void setIcon(int iconId);
  }
}
