package org.robolectric.android.internal;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.N_MR1;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.Q;

import android.view.Display;
import android.view.DisplayInfo;
import android.view.Surface;
import java.util.Arrays;
import java.util.Objects;
import org.robolectric.RuntimeEnvironment;

/**
 * Describes the characteristics of a particular logical display.
 *
 * Robolectric internal (for now), do not use.
 */
public final class DisplayConfig {
  /**
   * The surface flinger layer stack associated with this logical display.
   */
  public int layerStack;

  /**
   * Display flags.
   */
  public int flags;

  /**
   * Display type.
   */
  public int type;

  /**
   * Display address, or null if none.
   * Interpretation varies by display type.
   */
  // public String address;

  /**
   * The human-readable name of the display.
   */
  public String name;

  /**
   * Unique identifier for the display. Shouldn't be displayed to the user.
   */
  public String uniqueId;

  /**
   * The width of the portion of the display that is available to applications, in pixels.
   * Represents the size of the display minus any system decorations.
   */
  public int appWidth;

  /**
   * The height of the portion of the display that is available to applications, in pixels.
   * Represents the size of the display minus any system decorations.
   */
  public int appHeight;

  /**
   * The smallest value of {@link #appWidth} that an application is likely to encounter,
   * in pixels, excepting cases where the width may be even smaller due to the presence
   * of a soft keyboard, for example.
   */
  public int smallestNominalAppWidth;

  /**
   * The smallest value of {@link #appHeight} that an application is likely to encounter,
   * in pixels, excepting cases where the height may be even smaller due to the presence
   * of a soft keyboard, for example.
   */
  public int smallestNominalAppHeight;

  /**
   * The largest value of {@link #appWidth} that an application is likely to encounter,
   * in pixels, excepting cases where the width may be even larger due to system decorations
   * such as the status bar being hidden, for example.
   */
  public int largestNominalAppWidth;

  /**
   * The largest value of {@link #appHeight} that an application is likely to encounter,
   * in pixels, excepting cases where the height may be even larger due to system decorations
   * such as the status bar being hidden, for example.
   */
  public int largestNominalAppHeight;

  /**
   * The logical width of the display, in pixels.
   * Represents the usable size of the display which may be smaller than the
   * physical size when the system is emulating a smaller display.
   */
  public int logicalWidth;

  /**
   * The logical height of the display, in pixels.
   * Represents the usable size of the display which may be smaller than the
   * physical size when the system is emulating a smaller display.
   */
  public int logicalHeight;

  /**
   * @hide
   * Number of overscan pixels on the left side of the display.
   */
  public int overscanLeft;

  /**
   * @hide
   * Number of overscan pixels on the top side of the display.
   */
  public int overscanTop;

  /**
   * @hide
   * Number of overscan pixels on the right side of the display.
   */
  public int overscanRight;

  /**
   * @hide
   * Number of overscan pixels on the bottom side of the display.
   */
  public int overscanBottom;

  /**
   * The rotation of the display relative to its natural orientation.
   * May be one of {@link Surface#ROTATION_0},
   * {@link Surface#ROTATION_90}, {@link Surface#ROTATION_180},
   * {@link Surface#ROTATION_270}.
   * <p>
   * The value of this field is indeterminate if the logical display is presented on
   * more than one physical display.
   * </p>
   */
  @Surface.Rotation
  public int rotation;

  /**
   * The active display mode.
   */
  public int modeId;

  /**
   * The default display mode.
   */
  public int defaultModeId;

  /**
   * The supported modes of this display.
   */
  public Display.Mode[] supportedModes = new Display.Mode[0];

  /** The active color mode. */
  public int colorMode;

  /** The list of supported color modes */
  public int[] supportedColorModes = { Display.COLOR_MODE_DEFAULT };

  /** The display's HDR capabilities */
  public Display.HdrCapabilities hdrCapabilities;

  /**
   * The logical display density which is the basis for density-independent
   * pixels.
   */
  public int logicalDensityDpi;

  /**
   * The exact physical pixels per inch of the screen in the X dimension.
   * <p>
   * The value of this field is indeterminate if the logical display is presented on
   * more than one physical display.
   * </p>
   */
  public float physicalXDpi;

  /**
   * The exact physical pixels per inch of the screen in the Y dimension.
   * <p>
   * The value of this field is indeterminate if the logical display is presented on
   * more than one physical display.
   * </p>
   */
  public float physicalYDpi;

  /**
   * This is a positive value indicating the phase offset of the VSYNC events provided by
   * Choreographer relative to the display refresh.  For example, if Choreographer reports
   * that the refresh occurred at time N, it actually occurred at (N - appVsyncOffsetNanos).
   */
  public long appVsyncOffsetNanos;

  /**
   * This is how far in advance a buffer must be queued for presentation at
   * a given time.  If you want a buffer to appear on the screen at
   * time N, you must submit the buffer before (N - bufferDeadlineNanos).
   */
  public long presentationDeadlineNanos;

  /**
   * The state of the display, such as {@link Display#STATE_ON}.
   */
  public int state;

  /**
   * The UID of the application that owns this display, or zero if it is owned by the system.
   * <p>
   * If the display is private, then only the owner can use it.
   * </p>
   */
  public int ownerUid;

  /**
   * The package name of the application that owns this display, or null if it is
   * owned by the system.
   * <p>
   * If the display is private, then only the owner can use it.
   * </p>
   */
  public String ownerPackageName;

  /**
   * @hide
   * Get current remove mode of the display - what actions should be performed with the display's
   * content when it is removed.
   *
   * @see Display#getRemoveMode()
   */
  public int removeMode = Display.REMOVE_MODE_MOVE_CONTENT_TO_PRIMARY;

  public DisplayConfig() {
  }

  public DisplayConfig(DisplayConfig other) {
    copyFrom(other);
  }

  public DisplayConfig(DisplayInfo other) {
    layerStack = other.layerStack;
    flags = other.flags;
    type = other.type;
    // address = other.address;
    name = other.name;
    if (RuntimeEnvironment.getApiLevel() >= LOLLIPOP_MR1) {
      uniqueId = other.uniqueId;
    }
    appWidth = other.appWidth;
    appHeight = other.appHeight;
    smallestNominalAppWidth = other.smallestNominalAppWidth;
    smallestNominalAppHeight = other.smallestNominalAppHeight;
    largestNominalAppWidth = other.largestNominalAppWidth;
    largestNominalAppHeight = other.largestNominalAppHeight;
    logicalWidth = other.logicalWidth;
    logicalHeight = other.logicalHeight;
    if (RuntimeEnvironment.getApiLevel() >= JELLY_BEAN_MR2
        && RuntimeEnvironment.getApiLevel() <= Q) {
      overscanLeft = other.overscanLeft;
      overscanTop = other.overscanTop;
      overscanRight = other.overscanRight;
      overscanBottom = other.overscanBottom;
    }
    rotation = other.rotation;
    if (RuntimeEnvironment.getApiLevel() >= M) {
      modeId = other.modeId;
      defaultModeId = other.defaultModeId;
      supportedModes = Arrays.copyOf(other.supportedModes, other.supportedModes.length);
    }
    if (RuntimeEnvironment.getApiLevel() >= N_MR1) {
      colorMode = other.colorMode;
      supportedColorModes = Arrays.copyOf(
          other.supportedColorModes, other.supportedColorModes.length);
    }
    if (RuntimeEnvironment.getApiLevel() >= N) {
      hdrCapabilities = other.hdrCapabilities;
    }
    logicalDensityDpi = other.logicalDensityDpi;
    physicalXDpi = other.physicalXDpi;
    physicalYDpi = other.physicalYDpi;
    if (RuntimeEnvironment.getApiLevel() >= LOLLIPOP) {
      appVsyncOffsetNanos = other.appVsyncOffsetNanos;
      presentationDeadlineNanos = other.presentationDeadlineNanos;
      state = other.state;
    }
    if (RuntimeEnvironment.getApiLevel() >= KITKAT) {
      ownerUid = other.ownerUid;
      ownerPackageName = other.ownerPackageName;
    }
    if (RuntimeEnvironment.getApiLevel() >= O) {
      removeMode = other.removeMode;
    }
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof DisplayConfig && equals((DisplayConfig)o);
  }

  @SuppressWarnings("NonOverridingEquals")
  public boolean equals(DisplayConfig other) {
    return other != null
        && layerStack == other.layerStack
        && flags == other.flags
        && type == other.type
        // && Objects.equals(address, other.address)
        && Objects.equals(uniqueId, other.uniqueId)
        && appWidth == other.appWidth
        && appHeight == other.appHeight
        && smallestNominalAppWidth == other.smallestNominalAppWidth
        && smallestNominalAppHeight == other.smallestNominalAppHeight
        && largestNominalAppWidth == other.largestNominalAppWidth
        && largestNominalAppHeight == other.largestNominalAppHeight
        && logicalWidth == other.logicalWidth
        && logicalHeight == other.logicalHeight
        && overscanLeft == other.overscanLeft
        && overscanTop == other.overscanTop
        && overscanRight == other.overscanRight
        && overscanBottom == other.overscanBottom
        && rotation == other.rotation
        && modeId == other.modeId
        && defaultModeId == other.defaultModeId
        && colorMode == other.colorMode
        && Arrays.equals(supportedColorModes, other.supportedColorModes)
        && Objects.equals(hdrCapabilities, other.hdrCapabilities)
        && logicalDensityDpi == other.logicalDensityDpi
        && physicalXDpi == other.physicalXDpi
        && physicalYDpi == other.physicalYDpi
        && appVsyncOffsetNanos == other.appVsyncOffsetNanos
        && presentationDeadlineNanos == other.presentationDeadlineNanos
        && state == other.state
        && ownerUid == other.ownerUid
        && Objects.equals(ownerPackageName, other.ownerPackageName)
        && removeMode == other.removeMode;
  }

  @Override
  public int hashCode() {
    return 0; // don't care
  }

  public void copyFrom(DisplayConfig other) {
    layerStack = other.layerStack;
    flags = other.flags;
    type = other.type;
    // address = other.address;
    name = other.name;
    uniqueId = other.uniqueId;
    appWidth = other.appWidth;
    appHeight = other.appHeight;
    smallestNominalAppWidth = other.smallestNominalAppWidth;
    smallestNominalAppHeight = other.smallestNominalAppHeight;
    largestNominalAppWidth = other.largestNominalAppWidth;
    largestNominalAppHeight = other.largestNominalAppHeight;
    logicalWidth = other.logicalWidth;
    logicalHeight = other.logicalHeight;
    overscanLeft = other.overscanLeft;
    overscanTop = other.overscanTop;
    overscanRight = other.overscanRight;
    overscanBottom = other.overscanBottom;
    rotation = other.rotation;
    modeId = other.modeId;
    defaultModeId = other.defaultModeId;
    supportedModes = Arrays.copyOf(other.supportedModes, other.supportedModes.length);
    colorMode = other.colorMode;
    supportedColorModes = Arrays.copyOf(
        other.supportedColorModes, other.supportedColorModes.length);
    hdrCapabilities = other.hdrCapabilities;
    logicalDensityDpi = other.logicalDensityDpi;
    physicalXDpi = other.physicalXDpi;
    physicalYDpi = other.physicalYDpi;
    appVsyncOffsetNanos = other.appVsyncOffsetNanos;
    presentationDeadlineNanos = other.presentationDeadlineNanos;
    state = other.state;
    ownerUid = other.ownerUid;
    ownerPackageName = other.ownerPackageName;
    removeMode = other.removeMode;
  }

  public void copyTo(DisplayInfo other) {
    other.layerStack = layerStack;
    other.flags = flags;
    other.type = type;
    // other.address = address;
    other.name = name;
    if (RuntimeEnvironment.getApiLevel() >= LOLLIPOP_MR1) {
      other.uniqueId = uniqueId;
    }
    other.appWidth = appWidth;
    other.appHeight = appHeight;
    other.smallestNominalAppWidth = smallestNominalAppWidth;
    other.smallestNominalAppHeight = smallestNominalAppHeight;
    other.largestNominalAppWidth = largestNominalAppWidth;
    other.largestNominalAppHeight = largestNominalAppHeight;
    other.logicalWidth = logicalWidth;
    other.logicalHeight = logicalHeight;
    if (RuntimeEnvironment.getApiLevel() >= JELLY_BEAN_MR2
        && RuntimeEnvironment.getApiLevel() <= Q) {
      other.overscanLeft = overscanLeft;
      other.overscanTop = overscanTop;
      other.overscanRight = overscanRight;
      other.overscanBottom = overscanBottom;
    }
    other.rotation = rotation;
    if (RuntimeEnvironment.getApiLevel() >= M) {
      other.modeId = modeId;
      other.defaultModeId = defaultModeId;
      other.supportedModes = Arrays.copyOf(supportedModes, supportedModes.length);
    }
    if (RuntimeEnvironment.getApiLevel() >= N_MR1) {
      other.colorMode = colorMode;
      other.supportedColorModes = Arrays.copyOf(
          supportedColorModes, supportedColorModes.length);
    }
    if (RuntimeEnvironment.getApiLevel() >= N) {
      other.hdrCapabilities = hdrCapabilities;
    }
    other.logicalDensityDpi = logicalDensityDpi;
    other.physicalXDpi = physicalXDpi;
    other.physicalYDpi = physicalYDpi;
    if (RuntimeEnvironment.getApiLevel() >= LOLLIPOP) {
      other.appVsyncOffsetNanos = appVsyncOffsetNanos;
      other.presentationDeadlineNanos = presentationDeadlineNanos;
      other.state = state;
    }
    if (RuntimeEnvironment.getApiLevel() >= KITKAT) {
      other.ownerUid = ownerUid;
      other.ownerPackageName = ownerPackageName;
    }
    if (RuntimeEnvironment.getApiLevel() >= O) {
      other.removeMode = removeMode;
    }
  }

  // For debugging purposes
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("DisplayConfig{\"");
    sb.append(name);
    sb.append("\", uniqueId \"");
    sb.append(uniqueId);
    sb.append("\", app ");
    sb.append(appWidth);
    sb.append(" x ");
    sb.append(appHeight);
    sb.append(", real ");
    sb.append(logicalWidth);
    sb.append(" x ");
    sb.append(logicalHeight);
    if (overscanLeft != 0 || overscanTop != 0 || overscanRight != 0 || overscanBottom != 0) {
      sb.append(", overscan (");
      sb.append(overscanLeft);
      sb.append(",");
      sb.append(overscanTop);
      sb.append(",");
      sb.append(overscanRight);
      sb.append(",");
      sb.append(overscanBottom);
      sb.append(")");
    }
    sb.append(", largest app ");
    sb.append(largestNominalAppWidth);
    sb.append(" x ");
    sb.append(largestNominalAppHeight);
    sb.append(", smallest app ");
    sb.append(smallestNominalAppWidth);
    sb.append(" x ");
    sb.append(smallestNominalAppHeight);
    sb.append(", mode ");
    sb.append(modeId);
    sb.append(", defaultMode ");
    sb.append(defaultModeId);
    sb.append(", modes ");
    sb.append(Arrays.toString(supportedModes));
    sb.append(", colorMode ");
    sb.append(colorMode);
    sb.append(", supportedColorModes ");
    sb.append(Arrays.toString(supportedColorModes));
    sb.append(", hdrCapabilities ");
    sb.append(hdrCapabilities);
    sb.append(", rotation ");
    sb.append(rotation);
    sb.append(", density ");
    sb.append(logicalDensityDpi);
    sb.append(" (");
    sb.append(physicalXDpi);
    sb.append(" x ");
    sb.append(physicalYDpi);
    sb.append(") dpi, layerStack ");
    sb.append(layerStack);
    sb.append(", appVsyncOff ");
    sb.append(appVsyncOffsetNanos);
    sb.append(", presDeadline ");
    sb.append(presentationDeadlineNanos);
    sb.append(", type ");
    sb.append(Display.typeToString(type));
    // if (address != null) {
    //   sb.append(", address ").append(address);
    // }
    sb.append(", state ");
    sb.append(Display.stateToString(state));
    if (ownerUid != 0 || ownerPackageName != null) {
      sb.append(", owner ").append(ownerPackageName);
      sb.append(" (uid ").append(ownerUid).append(")");
    }
    sb.append(flagsToString(flags));
    sb.append(", removeMode ");
    sb.append(removeMode);
    sb.append("}");
    return sb.toString();
  }

  private static String flagsToString(int flags) {
    StringBuilder result = new StringBuilder();
    if ((flags & Display.FLAG_SECURE) != 0) {
      result.append(", FLAG_SECURE");
    }
    if ((flags & Display.FLAG_SUPPORTS_PROTECTED_BUFFERS) != 0) {
      result.append(", FLAG_SUPPORTS_PROTECTED_BUFFERS");
    }
    if ((flags & Display.FLAG_PRIVATE) != 0) {
      result.append(", FLAG_PRIVATE");
    }
    if ((flags & Display.FLAG_PRESENTATION) != 0) {
      result.append(", FLAG_PRESENTATION");
    }
    if ((flags & Display.FLAG_SCALING_DISABLED) != 0) {
      result.append(", FLAG_SCALING_DISABLED");
    }
    if ((flags & Display.FLAG_ROUND) != 0) {
      result.append(", FLAG_ROUND");
    }
    return result.toString();
  }
}
