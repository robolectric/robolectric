package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static org.robolectric.shadow.api.Shadow.directlyOn;

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

  @Implementation(minSdk = LOLLIPOP)
  public void setView(int resourceId) {
    setView(LayoutInflater.from(RuntimeEnvironment.application).inflate(resourceId, null));
  }

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
