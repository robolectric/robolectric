package xxx;

import android.app.Application;
import android.graphics.drawable.Drawable;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * Fake {@link org.robolectric.internal.ShadowProvider} for testing
 * {@link org.robolectric.errorprone.bugpatterns.ShadowUsageCheck}.
 */
public class XShadows implements org.robolectric.internal.ShadowProvider {
  public static XShadowApplication shadowOf(Application actual) {
    return null;
  }

  public static XShadowDrawable shadowOf(Drawable actual) {
    return null;
  }

  public static XShadowViewGroup shadowOf(ViewGroup actual) {
    return null;
  }

  public static XShadowLinearLayout shadowOf(LinearLayout actual) {
    return null;
  }

  @Override
  public void reset() {}

  @Override
  public java.util.Map<String, String> getShadowMap() {
    return null;
  }

  @Override
  public String[] getProvidedPackageNames() {
    return null;
  }
}
