package xxx;

import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Looper;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import java.util.Collection;
import java.util.Map;

/**
 * Fake {@link org.robolectric.internal.ShadowProvider} for testing
 * {@link org.robolectric.errorprone.bugpatterns.ShadowUsageCheck}.
 */
public class XShadows implements org.robolectric.internal.ShadowProvider {
  public static XShadowAlertDialog shadowOf(AlertDialog actual) {
    return null;
  }

  public static XShadowApplication shadowOf(Application actual) {
    return null;
  }

  public static XShadowConnectivityManager shadowOf(ConnectivityManager actual) {
    return null;
  }

  public static XShadowDialog shadowOf(Dialog actual) {
    return null;
  }

  public static XShadowDrawable shadowOf(Drawable actual) {
    return null;
  }

  public static XShadowLooper shadowOf(Looper actual) {
    return null;
  }

  public static XShadowLinearLayout shadowOf(LinearLayout actual) {
    return null;
  }

  public static XShadowNetworkInfo shadowOf(NetworkInfo actual) {
    return null;
  }

  public static XShadowPopupMenu shadowOf(PopupMenu actual) {
    return null;
  }

  public static XShadowViewGroup shadowOf(ViewGroup actual) {
    return null;
  }

  @Override
  public void reset() {}

  @Override
  public Collection<Map.Entry<String, String>> getShadows() {
    return null;
  }

  @Override
  public String[] getProvidedPackageNames() {
    return null;
  }
}
