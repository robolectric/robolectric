package org.robolectric.shadows;

import android.graphics.Rect;
import android.graphics.Region;
import android.os.Build;
import android.view.accessibility.AccessibilityWindowInfo;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import javax.annotation.Nonnull;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/** Builder for {@link AccessibilityWindowInfo}. */
public class AccessibilityWindowInfoBuilder {
  private Rect boundsInScreen;
  private Region regionInScreen;
  private boolean focused;
  private int id = -1;
  private CharSequence title;

  private AccessibilityWindowInfoBuilder() {}

  public static AccessibilityWindowInfoBuilder newBuilder() {
    return new AccessibilityWindowInfoBuilder();
  }

  /**
   * Sets the bounds of the window in the screen.
   *
   * <p>This corresponds to the hidden {@code setBoundsInScreen(Rect)} method in {@link
   * AccessibilityWindowInfo}. This method is only available on pre-R devices.
   *
   * @param bounds The bounds to set.
   * @return This builder.
   */
  @CanIgnoreReturnValue
  public AccessibilityWindowInfoBuilder setBoundsInScreen(@Nonnull Rect bounds) {
    this.boundsInScreen = bounds;
    return this;
  }

  /**
   * Sets the touchable region of the window in the screen.
   *
   * <p>This corresponds to the hidden {@code setRegionInScreen(Region)} method in {@link
   * AccessibilityWindowInfo}. This method is available on R+ devices.
   *
   * @param region The region to set.
   * @return This builder.
   */
  @CanIgnoreReturnValue
  public AccessibilityWindowInfoBuilder setRegionInScreen(@Nonnull Region region) {
    this.regionInScreen = region;
    return this;
  }

  /**
   * Sets the focused state of the window.
   *
   * <p>This corresponds to the hidden {@code setFocused(boolean)} method in {@link
   * AccessibilityWindowInfo}.
   *
   * @param focused The focused state.
   * @return This builder.
   */
  @CanIgnoreReturnValue
  public AccessibilityWindowInfoBuilder setFocused(boolean focused) {
    this.focused = focused;
    return this;
  }

  /**
   * Sets the ID of the window.
   *
   * <p>This corresponds to the private {@code mId} field in {@link AccessibilityWindowInfo}.
   *
   * @param id The ID.
   * @return This builder.
   */
  @CanIgnoreReturnValue
  public AccessibilityWindowInfoBuilder setId(int id) {
    this.id = id;
    return this;
  }

  /**
   * Sets the title of the window.
   *
   * <p>This corresponds to the private {@code mTitle} field in {@link AccessibilityWindowInfo}.
   * This method is only available on N+ devices.
   *
   * @param title The title.
   * @return This builder.
   */
  @CanIgnoreReturnValue
  public AccessibilityWindowInfoBuilder setTitle(CharSequence title) {
    this.title = title;
    return this;
  }

  /**
   * Builds the {@link AccessibilityWindowInfo} instance.
   *
   * @return The constructed {@link AccessibilityWindowInfo}.
   */
  public AccessibilityWindowInfo build() {
    AccessibilityWindowInfo windowInfo =
        ReflectionHelpers.callConstructor(AccessibilityWindowInfo.class);

    if (id != -1) {
      windowInfo.setId(id);
    }
    if (title != null && RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.N) {
      ReflectionHelpers.setField(windowInfo, "mTitle", title);
    }
    windowInfo.setFocused(focused);

    if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.R) {
      Region regionToUse = null;
      if (regionInScreen != null) {
        regionToUse = regionInScreen;
      } else if (boundsInScreen != null) {
        regionToUse = new Region(boundsInScreen);
      }

      if (regionToUse != null) {
        // The method 'setRegionInScreen' is hidden (@hide) and was added in R.
        windowInfo.setRegionInScreen(regionToUse);
      }
    } else if (boundsInScreen != null) {
      // The method 'setBoundsInScreen' is hidden (@hide) and was removed in R.
      ReflectionHelpers.callInstanceMethod(
          windowInfo, "setBoundsInScreen", ClassParameter.from(Rect.class, boundsInScreen));
    }

    return windowInfo;
  }
}
