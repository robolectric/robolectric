package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.R;
import static java.lang.Math.max;
import static java.lang.Math.round;

import android.graphics.Rect;
import android.hardware.display.DisplayManagerGlobal;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.InsetsState;
import android.view.Surface;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowWindowManagerGlobal.WindowInfo;
import org.robolectric.shadows.SystemUi.SystemBar.Side;

/**
 * State holder for the Android system UI.
 *
 * <p>The system UI is configured per display and the system UI can be retrieved for the default
 * display using {@link #systemUiForDefaultDisplay()} or for a display identified by its ID using
 * {@link #systemUiForDisplay(int)}.
 *
 * <p>For backwards compatibility with previous Robolectric versions by default the system UIs are
 * configured with no status bar or navigation insets, to apply a "standard" phone setup configure a
 * status bar and navigation bar behavior e.g. in your test setup:
 *
 * <pre>{@code
 * systemUiForDefaultDisplay()
 *     .setBehavior(SystemUi.STANDARD_STATUS_BAR, SystemUi.GESTURAL_NAVIGATION);
 * }</pre>
 *
 * <p>{@link SystemUi} includes the most common Android system UI behaviors including:
 *
 * <ul>
 *   <li>{@link #NO_STATUS_BAR} - The default, no status bar insets reserved.
 *   <li>{@link #STANDARD_STATUS_BAR} - A standard status bar that grows if a top cutout is present.
 *   <li>{@link #GESTURAL_NAVIGATION} - Standard gestural navigation with bottom inset and gestural
 *       areas on the bottom and sides of the screen.
 *   <li>{@link #THREE_BUTTON_NAVIGATION} - Standard three button navigation bar that aligns to the
 *       bottom of the screen, and on smaller screens moves to the sides when rotated.
 *   <li>{@link #GESTURAL_NAVIGATION} - Standard two button navigation bar with similar alignment to
 *       the three button bar but also reserves a gestural area at the bottom of the screen.
 * </ul>
 *
 * <p>It's recommended to use the predefined behaviors which attempt to align with real Android
 * behavior, but if necessary custom system bar and navigation bar behaviors can be defined by
 * implementing the {@link StatusBarBehavior} and {@link NavigationBarBehavior} interfaces
 * respectively.
 */
// TODO: Make public when we're happy with the implementation/api/behavior
final class SystemUi {
  /** Default status bar behavior which renders a 0 height status bar. */
  public static final StatusBarBehavior NO_STATUS_BAR = new NoStatusBarBehavior();

  /** Standard Android status bar behavior which behaves similarly to real Android. */
  public static final StatusBarBehavior STANDARD_STATUS_BAR = new StandardStatusBarBehavior();

  /** Default navigation bar behavior which renders a 0 height navigation bar. */
  public static final NavigationBarBehavior NO_NAVIGATION_BAR = new NoNavigationBarBehavior();

  /** Standard Android gestural navigation bar behavior. */
  public static final NavigationBarBehavior GESTURAL_NAVIGATION =
      new GesturalNavigationBarBehavior();

  /** Standard Android three button navigation bar behavior. */
  public static final NavigationBarBehavior THREE_BUTTON_NAVIGATION =
      new ButtonNavigationBarBehavior();

  private final int displayId;
  private final StatusBar statusBar;
  private final NavigationBar navigationBar;
  private final ImmutableList<SystemBar> systemsBars;

  interface OnChangeListener {
    void onChange();
  }

  private final List<OnChangeListener> listeners = new ArrayList<>();

  /** Returns the {@link SystemUi} for the default display. */
  public static SystemUi systemUiForDefaultDisplay() {
    return systemUiForDisplay(Display.DEFAULT_DISPLAY);
  }

  /** Returns the {@link SystemUi} for the given display. */
  public static SystemUi systemUiForDisplay(int displayId) {
    return Shadow.<ShadowDisplayManagerGlobal>extract(DisplayManagerGlobal.getInstance())
        .getSystemUi(displayId);
  }

  SystemUi(int displayId) {
    this.displayId = displayId;
    statusBar = new StatusBar(displayId);
    navigationBar = new NavigationBar(displayId);
    systemsBars = ImmutableList.of(statusBar, navigationBar);
  }

  int getDisplayId() {
    return displayId;
  }

  void addListener(OnChangeListener listener) {
    listeners.add(listener);
  }

  public StatusBar getStatusBar() {
    return statusBar;
  }

  /** Returns the status bar behavior. The default status bar behavior is {@link #NO_STATUS_BAR}. */
  public StatusBarBehavior getStatusBarBehavior() {
    return statusBar.getBehavior();
  }

  /**
   * Sets the status bar behavior.
   *
   * <p>The default behavior is {@link #NO_STATUS_BAR}, use {@link #STANDARD_STATUS_BAR} for a
   * standard Android status bar behavior.
   */
  public void setStatusBarBehavior(StatusBarBehavior statusBarBehavior) {
    statusBar.setBehavior(statusBarBehavior);
  }

  public NavigationBar getNavigationBar() {
    return navigationBar;
  }

  /**
   * Returns the navigation bar behavior. The default navigation bar behavior is {@link
   * #NO_NAVIGATION_BAR}.
   */
  public NavigationBarBehavior getNavigationBarBehavior() {
    return navigationBar.getBehavior();
  }

  /**
   * Sets the navigation bar behavior.
   *
   * <p>The default behavior is {@link #NO_NAVIGATION_BAR}, use {@link #GESTURAL_NAVIGATION} or
   * {@link #THREE_BUTTON_NAVIGATION} for a standard on screen Android navigation bar behavior.
   */
  public void setNavigationBarBehavior(NavigationBarBehavior statusBarBehavior) {
    navigationBar.setBehavior(statusBarBehavior);
  }

  public void setBehavior(
      StatusBarBehavior statusBarBehavior, NavigationBarBehavior navigationBarBehavior) {
    setStatusBarBehavior(statusBarBehavior);
    setNavigationBarBehavior(navigationBarBehavior);
  }

  @SuppressWarnings("deprecation") // Back compat support for system ui visibility
  void adjustFrameForInsets(WindowManager.LayoutParams attrs, Rect outFrame) {
    boolean hideStatusBar;
    boolean hideNavigationBar;
    if (RuntimeEnvironment.getApiLevel() >= R) {
      hideStatusBar = (attrs.getFitInsetsTypes() & WindowInsets.Type.statusBars()) != 0;
      hideNavigationBar = (attrs.getFitInsetsTypes() & WindowInsets.Type.navigationBars()) != 0;
    } else {
      int systemUiVisibility = attrs.systemUiVisibility | attrs.subtreeSystemUiVisibility;
      hideStatusBar =
          (systemUiVisibility & View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN) == 0
              && (attrs.flags & WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN) == 0
              && (attrs.flags & WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS) == 0;
      hideNavigationBar =
          (systemUiVisibility & View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION) == 0
              && (attrs.flags & WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION) == 0;
    }
    if (hideStatusBar) {
      statusBar.insetFrame(outFrame);
    }
    if (hideNavigationBar) {
      navigationBar.insetFrame(outFrame);
    }
  }

  void putInsets(WindowInfo windowInfo) {
    putInsets(windowInfo, windowInfo.contentInsets, /* includeNotVisible= */ false);
    putInsets(windowInfo, windowInfo.visibleInsets, /* includeNotVisible= */ false);
    putInsets(windowInfo, windowInfo.stableInsets, /* includeNotVisible= */ true);
    if (windowInfo.insetsState != null) {
      putInsetsState(windowInfo, windowInfo.insetsState);
    }
  }

  private void putInsets(WindowInfo info, Rect outInsets, boolean includeNotVisible) {
    outInsets.set(0, 0, 0, 0);
    for (SystemBar bar : systemsBars) {
      if (includeNotVisible || bar.isVisible()) {
        bar.putInsets(info.displayFrame, info.frame, outInsets);
      }
    }
  }

  private void putInsetsState(WindowInfo info, InsetsState outInsetsState) {
    outInsetsState.setDisplayFrame(info.frame);
    ShadowInsetsState outShadowInsetsState = Shadow.extract(outInsetsState);
    for (SystemBar bar : systemsBars) {
      Shadow.<ShadowInsetsSource>extract(outShadowInsetsState.getOrCreateSource(bar.getId()))
          .setFrame(bar.inFrame(info.displayFrame, info.frame))
          .setVisible(bar.isVisible());
    }
  }

  private static int dpToPx(int px, int displayId) {
    return dpToPx(px, DisplayManagerGlobal.getInstance().getDisplayInfo(displayId));
  }

  private static int dpToPx(int px, DisplayInfo displayInfo) {
    float density = displayInfo.logicalDensityDpi / (float) DisplayMetrics.DENSITY_DEFAULT;
    return round(density * px);
  }

  /**
   * Base interface for behavior for a system bar such as status bar or navigation bar. See the
   * specific interfaces {@link StatusBarBehavior} and {@link NavigationBarBehavior}.
   */
  public interface SystemBarBehavior {
    /**
     * Returns which side of the screen this system bar should be attached to when rendered on the
     * given display ID. The implementation may look up the size of the display to determine the
     * side.
     */
    Side calculateSide(int displayId);

    /**
     * Returns the size of the this system bar when rendered on the given display ID. This is either
     * the height or the width based on the return value from {@link #calculateSide(int)}. The
     * implementation may look up the size of the display to determine the side.
     */
    int calculateSize(int displayId);
  }

  /**
   * Interface for status bar behavior. See {@link #STANDARD_STATUS_BAR} and {@link #NO_STATUS_BAR}
   * for default implementations. Custom status bar behavior can be provided by implementing this
   * interface and calling {@link SystemUi#setStatusBarBehavior(StatusBarBehavior)}.
   */
  public interface StatusBarBehavior extends SystemBarBehavior {}

  /**
   * Interface for navigation bar behavior. See {@link #GESTURAL_NAVIGATION}, {@link
   * #THREE_BUTTON_NAVIGATION}, and {@link #NO_NAVIGATION_BAR} for default implementations. Custom
   * status bar behavior can be provided by implementing this interface and calling {@link
   * SystemUi#setNavigationBarBehavior(NavigationBarBehavior)}.
   */
  public interface NavigationBarBehavior extends SystemBarBehavior {}

  /** Base class for a system bar. See {@link StatusBar} and {@link NavigationBar}. */
  public abstract static class SystemBar {
    /** Side of the screen a system bar is attached to. */
    public enum Side {
      LEFT,
      TOP,
      RIGHT,
      BOTTOM
    }

    SystemBar() {}

    abstract int getId();

    /** Returns which side of the screen this bar is attached to. */
    public abstract Side getSide();

    /**
     * Returns the size of this status bar. Depending on which side of the screen the bar is
     * attached to this is either the height (for top and bottom) or width (for left or right).
     */
    public abstract int getSize();

    /**
     * Returns true if this status bar is currently visible. Note that this is still tracked even if
     * the status bar has 0 size.
     */
    public abstract boolean isVisible();

    void insetFrame(Rect outFrame) {
      switch (getSide()) {
        case LEFT:
          outFrame.left += getSize();
          break;
        case TOP:
          outFrame.top += getSize();
          break;
        case RIGHT:
          outFrame.right -= getSize();
          break;
        case BOTTOM:
          outFrame.bottom -= getSize();
          break;
      }
    }

    Rect inFrame(Rect displayFrame, Rect frame) {
      switch (getSide()) {
        case LEFT:
          return new Rect(0, 0, max(0, getSize() - frame.left), frame.bottom);
        case TOP:
          return new Rect(0, 0, frame.right, max(0, getSize() - frame.top));
        case RIGHT:
          int rightSize = max(0, getSize() - (displayFrame.right - frame.right));
          return new Rect(frame.right - rightSize, 0, frame.right, frame.bottom);
        case BOTTOM:
          int bottomSize = max(0, getSize() - (displayFrame.bottom - frame.bottom));
          return new Rect(0, frame.bottom - bottomSize, frame.right, frame.bottom);
      }
      throw new IllegalStateException();
    }

    void putInsets(Rect displayFrame, Rect frame, Rect insets) {
      switch (getSide()) {
        case LEFT:
          insets.left = max(insets.left, getSize() - frame.left);
          break;
        case TOP:
          insets.top = max(insets.top, getSize() - frame.top);
          break;
        case RIGHT:
          insets.right = max(insets.right, getSize() - (displayFrame.right - frame.right));
          break;
        case BOTTOM:
          insets.bottom = max(insets.bottom, getSize() - (displayFrame.bottom - frame.bottom));
          break;
      }
    }
  }

  /** Represents the system status bar. */
  public static final class StatusBar extends SystemBar {
    private final int displayId;
    private StatusBarBehavior behavior = NO_STATUS_BAR;
    private boolean isVisible = true;

    StatusBar(int displayId) {
      this.displayId = displayId;
    }

    @Override
    int getId() {
      return ShadowInsetsState.STATUS_BARS;
    }

    StatusBarBehavior getBehavior() {
      return behavior;
    }

    void setBehavior(StatusBarBehavior behavior) {
      this.behavior = behavior;
    }

    @Override
    public boolean isVisible() {
      return isVisible;
    }

    boolean setVisible(boolean isVisible) {
      boolean didChange = this.isVisible != isVisible;
      this.isVisible = isVisible;
      return didChange;
    }

    @Override
    public Side getSide() {
      return behavior.calculateSide(displayId);
    }

    @Override
    public int getSize() {
      return behavior.calculateSize(displayId);
    }

    @Nonnull
    @Override
    public String toString() {
      return "StatusBar{isVisible=" + isVisible + "}";
    }
  }

  static final class NoStatusBarBehavior implements StatusBarBehavior {
    @Override
    public Side calculateSide(int displayId) {
      return Side.TOP;
    }

    @Override
    public int calculateSize(int displayId) {
      return 0;
    }
  }

  static final class StandardStatusBarBehavior implements StatusBarBehavior {
    private static final int HEIGHT_DP = 24;

    @Override
    public Side calculateSide(int displayId) {
      return Side.TOP;
    }

    @Override
    public int calculateSize(int displayId) {
      return dpToPx(HEIGHT_DP, displayId);
    }
  }

  /** Represents the system navigation bar. */
  public static final class NavigationBar extends SystemBar {
    private final int displayId;
    private NavigationBarBehavior behavior = NO_NAVIGATION_BAR;
    private boolean isVisible = true;

    NavigationBar(int displayId) {
      this.displayId = displayId;
    }

    @Override
    int getId() {
      return ShadowInsetsState.NAVIGATION_BARS;
    }

    NavigationBarBehavior getBehavior() {
      return behavior;
    }

    void setBehavior(NavigationBarBehavior behavior) {
      this.behavior = behavior;
    }

    @Override
    public boolean isVisible() {
      return isVisible;
    }

    boolean setVisible(boolean isVisible) {
      boolean didChange = this.isVisible != isVisible;
      this.isVisible = isVisible;
      return didChange;
    }

    @Override
    public Side getSide() {
      return behavior.calculateSide(displayId);
    }

    @Override
    public int getSize() {
      return behavior.calculateSize(displayId);
    }

    @Nonnull
    @Override
    public String toString() {
      return "NavigationBar{isVisible=" + isVisible + "}";
    }
  }

  private static class NoNavigationBarBehavior implements NavigationBarBehavior {
    @Override
    public Side calculateSide(int displayId) {
      return Side.BOTTOM;
    }

    @Override
    public int calculateSize(int displayId) {
      return 0;
    }
  }

  private static class GesturalNavigationBarBehavior implements NavigationBarBehavior {
    private static final int HEIGHT_DP = 24;

    @Override
    public Side calculateSide(int displayId) {
      return Side.BOTTOM;
    }

    @Override
    public int calculateSize(int displayId) {
      return dpToPx(HEIGHT_DP, displayId);
    }
  }

  private static class ButtonNavigationBarBehavior implements NavigationBarBehavior {
    private static final int BOTTOM_HEIGHT_DP = 48;
    private static final int SIDE_HEIGHT_DP = 42;
    private static final int LARGE_SCREEN_DP = 600;
    private static final int LARGE_SCREEN_HEIGHT_DP = 56;

    @Override
    public Side calculateSide(int displayId) {
      return calculateSide(DisplayManagerGlobal.getInstance().getDisplayInfo(displayId));
    }

    private Side calculateSide(DisplayInfo info) {
      if (isLargeScreen(info)) {
        return Side.BOTTOM;
      } else {
        switch (info.rotation) {
          case Surface.ROTATION_90:
            return Side.LEFT;
          case Surface.ROTATION_180:
            return Side.RIGHT;
          default:
            return Side.BOTTOM;
        }
      }
    }

    @Override
    public int calculateSize(int displayId) {
      DisplayInfo displayInfo = DisplayManagerGlobal.getInstance().getDisplayInfo(displayId);
      int sizeDp =
          isLargeScreen(displayInfo)
              ? LARGE_SCREEN_HEIGHT_DP
              : (calculateSide(displayInfo) == Side.BOTTOM ? BOTTOM_HEIGHT_DP : SIDE_HEIGHT_DP);
      return dpToPx(sizeDp, displayInfo);
    }

    private boolean isLargeScreen(DisplayInfo info) {
      return max(info.logicalWidth, info.logicalHeight) >= dpToPx(LARGE_SCREEN_DP, info);
    }
  }
}
