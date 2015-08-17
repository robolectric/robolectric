package org.robolectric.shadows;

import android.view.View;
import android.view.LayoutInflater;
import android.widget.Adapter;
import android.widget.ListView;
import com.android.internal.app.AlertController;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;

import java.lang.reflect.InvocationTargetException;

import static org.robolectric.internal.Shadow.directlyOn;

/**
* Shadow for {@link com.android.internal.app.AlertController}.
*/
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
    directlyOn(realAlertController, AlertController.class).setTitle(title);
  }

  public CharSequence getTitle() {
    return title == null ? "" : title;
  }

  @Implementation
  public void setCustomTitle(View customTitleView) {
    this.customTitleView = customTitleView;
    directlyOn(realAlertController, AlertController.class).setCustomTitle(customTitleView);
  }

  public View getCustomTitleView() {
    return customTitleView;
  }

  @Implementation
  public void setMessage(CharSequence message) {
    this.message = message;
    directlyOn(realAlertController, AlertController.class).setMessage(message);
  }

  public CharSequence getMessage() {
    return message == null ? "" : message;
  }

  @Implementation
  public void setView(View view) {
    this.view = view;
    directlyOn(realAlertController, AlertController.class).setView(view);
  }

#if ($api >= 19)
  @Implementation
  public void setView(int resourceId) {
    setView(LayoutInflater.from(RuntimeEnvironment.application).inflate(resourceId, null));
  }
#end

  @Implementation
  public void setIcon(int iconId) {
    this.iconId = iconId;
    directlyOn(realAlertController, AlertController.class).setIcon(iconId);
  }

  public int getIconId() {
    return iconId;
  }

  public View getView() {
    return view;
  }

  public Adapter getAdapter() {
    return ReflectionHelpers.<ListView>callInstanceMethod(realAlertController, "getListView").getAdapter();
  }
}
