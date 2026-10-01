package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.R;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.versioning.VersionCalculator.POST_CINNAMON_BUN;

import android.graphics.Rect;
import android.graphics.Region;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

@RunWith(AndroidJUnit4.class)
public class ShadowAccessibilityWindowInfoTest {
  private AccessibilityWindowInfo window;
  private ShadowAccessibilityWindowInfo shadow;

  @Before
  public void setUp() {
    window = AccessibilityWindowInfo.obtain();
    assertThat(window).isNotNull();
    shadow = shadowOf(window);
  }

  @Test
  public void shouldNotHaveRootNode() {
    assertThat(shadow.getRoot() == null).isTrue();
  }

  @Test
  public void shouldHaveAssignedRoot() {
    AccessibilityNodeInfo node = AccessibilityNodeInfo.obtain();
    shadow.setRoot(node);
    assertThat(shadow.getRoot()).isEqualTo(node);
  }

  @Test
  public void testSetAnchor() {
    AccessibilityNodeInfo node = AccessibilityNodeInfo.obtain();
    shadow.setAnchor(node);
    assertThat(shadow.getAnchor()).isEqualTo(node);
  }

  @Config(minSdk = N)
  @Test
  public void testSetTitle() {
    assertThat(window.getTitle()).isNull();
    CharSequence title = "Title";
    window.setTitle(title);
    assertThat(window.getTitle().toString()).isEqualTo(title.toString());
  }

  @Test
  public void testSetChild() {
    AccessibilityWindowInfo window = AccessibilityWindowInfo.obtain();
    shadow.addChild(window);
    assertThat(shadow.getChild(0)).isEqualTo(window);
  }

  @Config(minSdk = O)
  @Test
  public void testSetPictureInPicture() {
    assertThat(window.isInPictureInPictureMode()).isFalse();
    window.setPictureInPicture(true);
    assertThat(window.isInPictureInPictureMode()).isTrue();
  }

  @Test
  public void shadowFieldsClearedAfterRecycle() {
    AccessibilityWindowInfo window2 = AccessibilityWindowInfo.obtain();
    shadow.addChild(window2);
    assertThat(shadow.getChild(0)).isEqualTo(window2);
    window.recycle();
    assertThat(shadow.getChild(0)).isNull();
  }

  @Config(minSdk = R)
  @Test
  public void testGetRegionInScreen() {
    Rect bounds = new Rect(0, 0, 100, 100);
    shadow.setBoundsInScreen(bounds);
    Region outRegion = new Region();
    window.getRegionInScreen(outRegion);
    assertThat(outRegion).isEqualTo(new Region(bounds));
  }

  @Config(minSdk = POST_CINNAMON_BUN)
  @Test
  public void getControllingWindow_defaultsToNull() {
    assertThat(getControllingWindow(window)).isNull();
  }

  @Config(minSdk = POST_CINNAMON_BUN)
  @Test
  public void setControllingWindow_returnsControllingWindow() {
    AccessibilityWindowInfo control = AccessibilityWindowInfo.obtain();
    shadowOf(control).addControlledWindow(window);
    assertThat(getControllingWindow(window)).isEqualTo(control);
  }

  @Config(minSdk = POST_CINNAMON_BUN)
  @Test
  public void getControlledWindowsCount_defaultsToZero() {
    assertThat(getControlledWindowsCount(window)).isEqualTo(0);
  }

  @Config(minSdk = POST_CINNAMON_BUN)
  @Test
  public void addControlledWindow_returnsControlledWindowsAndLinksControllingWindow() {
    AccessibilityWindowInfo controlled1 = AccessibilityWindowInfo.obtain();
    AccessibilityWindowInfo controlled2 = AccessibilityWindowInfo.obtain();
    shadow.addControlledWindow(controlled1);
    shadow.addControlledWindow(controlled2);

    assertThat(getControlledWindowsCount(window)).isEqualTo(2);
    assertThat(getControlledWindow(window, 0)).isEqualTo(controlled1);
    assertThat(getControlledWindow(window, 1)).isEqualTo(controlled2);
    assertThat(getControllingWindow(controlled1)).isEqualTo(window);
    assertThat(getControllingWindow(controlled2)).isEqualTo(window);
  }

  @Config(minSdk = POST_CINNAMON_BUN)
  @Test
  public void addControlledWindow_null_returnsNullControlledWindow() {
    shadow.addControlledWindow(null);

    assertThat(getControlledWindowsCount(window)).isEqualTo(1);
    assertThat(getControlledWindow(window, 0)).isNull();
  }

  @Config(minSdk = POST_CINNAMON_BUN)
  @Test
  public void getControlledWindow_noControlledWindows_throws() {
    assertThrows(IndexOutOfBoundsException.class, () -> getControlledWindow(window, 0));
  }

  @Config(minSdk = POST_CINNAMON_BUN)
  @Test
  public void obtain_copiesControlAssociations() {
    AccessibilityWindowInfo control = AccessibilityWindowInfo.obtain();
    AccessibilityWindowInfo controlled = AccessibilityWindowInfo.obtain();
    shadowOf(control).addControlledWindow(window);
    shadow.addControlledWindow(controlled);

    AccessibilityWindowInfo copy = AccessibilityWindowInfo.obtain(window);

    assertThat(getControllingWindow(copy)).isEqualTo(control);
    assertThat(getControlledWindowsCount(copy)).isEqualTo(1);
    assertThat(getControlledWindow(copy, 0)).isEqualTo(controlled);
  }

  @Config(minSdk = POST_CINNAMON_BUN)
  @Test
  public void recycle_clearsControlAssociations() {
    shadowOf(AccessibilityWindowInfo.obtain()).addControlledWindow(window);
    shadow.addControlledWindow(AccessibilityWindowInfo.obtain());

    window.recycle();

    assertThat(getControllingWindow(window)).isNull();
    assertThat(getControlledWindowsCount(window)).isEqualTo(0);
  }

  // TODO: Eliminate reflection once this test compiles against a POST_CINNAMON_BUN SDK.
  private static AccessibilityWindowInfo getControllingWindow(AccessibilityWindowInfo window) {
    return ReflectionHelpers.callInstanceMethod(window, "getControllingWindow");
  }

  private static int getControlledWindowsCount(AccessibilityWindowInfo window) {
    return ReflectionHelpers.callInstanceMethod(window, "getControlledWindowsCount");
  }

  private static AccessibilityWindowInfo getControlledWindow(
      AccessibilityWindowInfo window, int index) {
    return ReflectionHelpers.callInstanceMethod(
        window, "getControlledWindow", ClassParameter.from(int.class, index));
  }
}
