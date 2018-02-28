package org.robolectric.shadows;

import static com.google.common.base.Preconditions.checkState;
import static org.robolectric.shadows.NativeAndroidInput.AINPUT_EVENT_TYPE_MOTION;
import static org.robolectric.shadows.NativeAndroidInput.AINPUT_SOURCE_CLASS_POINTER;
import static org.robolectric.shadows.NativeAndroidInput.AMOTION_EVENT_ACTION_CANCEL;
import static org.robolectric.shadows.NativeAndroidInput.AMOTION_EVENT_ACTION_DOWN;
import static org.robolectric.shadows.NativeAndroidInput.AMOTION_EVENT_ACTION_MASK;
import static org.robolectric.shadows.NativeAndroidInput.AMOTION_EVENT_ACTION_MOVE;
import static org.robolectric.shadows.NativeAndroidInput.AMOTION_EVENT_ACTION_OUTSIDE;
import static org.robolectric.shadows.NativeAndroidInput.AMOTION_EVENT_ACTION_POINTER_DOWN;
import static org.robolectric.shadows.NativeAndroidInput.AMOTION_EVENT_ACTION_POINTER_INDEX_MASK;
import static org.robolectric.shadows.NativeAndroidInput.AMOTION_EVENT_ACTION_POINTER_INDEX_SHIFT;
import static org.robolectric.shadows.NativeAndroidInput.AMOTION_EVENT_ACTION_POINTER_UP;
import static org.robolectric.shadows.NativeAndroidInput.AMOTION_EVENT_ACTION_UP;
import static org.robolectric.shadows.NativeAndroidInput.AMOTION_EVENT_AXIS_ORIENTATION;
import static org.robolectric.shadows.NativeAndroidInput.AMOTION_EVENT_AXIS_PRESSURE;
import static org.robolectric.shadows.NativeAndroidInput.AMOTION_EVENT_AXIS_SIZE;
import static org.robolectric.shadows.NativeAndroidInput.AMOTION_EVENT_AXIS_TOOL_MAJOR;
import static org.robolectric.shadows.NativeAndroidInput.AMOTION_EVENT_AXIS_TOOL_MINOR;
import static org.robolectric.shadows.NativeAndroidInput.AMOTION_EVENT_AXIS_TOUCH_MAJOR;
import static org.robolectric.shadows.NativeAndroidInput.AMOTION_EVENT_AXIS_TOUCH_MINOR;
import static org.robolectric.shadows.NativeAndroidInput.AMOTION_EVENT_AXIS_X;
import static org.robolectric.shadows.NativeAndroidInput.AMOTION_EVENT_AXIS_Y;

import android.view.MotionEvent.PointerProperties;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.robolectric.res.android.Ref;

/**
 * Java representation of framework native input Transliterated from oreo-mr1
 * frameworks/native/include/input/Input.h and libs/input/Input.cpp
 */
public class NativeInput {

  /** Native input event structures. */
  // #include <android/input.h>
  // #include <utils/BitSet.h>
  // #include <utils/KeyedVector.h>
  // #include <utils/RefBase.h>
  // #include <utils/String8.h>
  // #include <utils/Timers.h>
  // #include <utils/Vector.h>
  // #include <stdint.h>
  // /*
  //  * Additional private ants not defined in ndk/ui/input.h.
  //  */
  //   enum {
  //     /* Signifies that the key is being predispatched */
  //     AKEY_EVENT_FLAG_PREDISPATCH = 0x20000000,
  //     /* Private control to determine when an app is tracking a key sequence. */
  //         AKEY_EVENT_FLAG_START_TRACKING = 0x40000000,
  //     /* Key event is inconsistent with previously sent key events. */
  //         AKEY_EVENT_FLAG_TAINTED = 0x80000000,
  //   };
  //   enum {
  //     /**
  //      * This flag indicates that the window that received this motion event is partly
  //      * or wholly obscured by another visible window above it.  This flag is set to true
  //      * even if the event did not directly pass through the obscured area.
  //      * A security sensitive application can check this flag to identify situations in which
  //      * a malicious application may have covered up part of its content for the purpose
  //      * of misleading the user or hijacking touches.  An appropriate response might be
  //      * to drop the suspect touches or to take additional precautions to confirm the user's
  //      * actual intent.
  //      */
  //     AMOTION_EVENT_FLAG_WINDOW_IS_PARTIALLY_OBSCURED = 0x2,
  //     /* Motion event is inconsistent with previously sent motion events. */
  //         AMOTION_EVENT_FLAG_TAINTED = 0x80000000,
  //   };
  //   enum {
  //     /* Used when a motion event is not associated with any display.
  //      * Typically used for non-pointer events. */
  //     ADISPLAY_ID_NONE = -1,
  //     /* The default display id. */
  //         ADISPLAY_ID_DEFAULT = 0,
  //   };
  //   enum {
  //     /*
  //      * Indicates that an input device has switches.
  //      * This input source flag is hidden from the API because switches are only used by the
  // system
  //      * and applications have no way to interact with them.
  //      */
  //     AINPUT_SOURCE_SWITCH = 0x80000000,
  //   };
  //   enum {
  //     /**
  //      * ants for LEDs. Hidden from the API since we don't actually expose a way to interact
  //      * with LEDs to developers
  //      *
  //      * NOTE: If you add LEDs here, you must also add them to InputEventLabels.h
  //      */
  //     ALED_NUM_LOCK = 0x00,
  //         ALED_CAPS_LOCK = 0x01,
  //         ALED_SCROLL_LOCK = 0x02,
  //         ALED_COMPOSE = 0x03,
  //         ALED_KANA = 0x04,
  //         ALED_SLEEP = 0x05,
  //         ALED_SUSPEND = 0x06,
  //         ALED_MUTE = 0x07,
  //         ALED_MISC = 0x08,
  //         ALED_MAIL = 0x09,
  //         ALED_CHARGING = 0x0a,
  //         ALED_CONTROLLER_1 = 0x10,
  //         ALED_CONTROLLER_2 = 0x11,
  //         ALED_CONTROLLER_3 = 0x12,
  //         ALED_CONTROLLER_4 = 0x13,
  //   };
  // /* Maximum number of controller LEDs we support */
  // #define MAX_CONTROLLER_LEDS 4
  // /*
  //  * SystemUiVisibility ants from View.
  //  */
  //   enum {
  //     ASYSTEM_UI_VISIBILITY_STATUS_BAR_VISIBLE = 0,
  //         ASYSTEM_UI_VISIBILITY_STATUS_BAR_HIDDEN = 0x00000001,
  //   };
  // /*
  //  * Maximum number of pointers supported per motion event.
  //  * Smallest number of pointers is 1.
  //  * (We want at least 10 but some touch controllers obstensibly configured for 10 pointers
  //  * will occasionally emit 11.  There is not much harm making this ant bigger.)
  //  */
  // #define MAX_POINTERS 16
  // /*
  //  * Maximum number of samples supported per motion event.
  //  */
  //       #define MAX_SAMPLES UINT16_MAX
  // /*
  //  * Maximum pointer id value supported in a motion event.
  //  * Smallest pointer id is 0.
  //  * (This is limited by our use of BitSet32 to track pointer assignments.)
  //  */
  // #define MAX_POINTER_ID 31
  //   /*
  //    * Declare a concrete type for the NDK's input event forward declaration.
  //    */
  static class AInputEvent {
    // virtual ~AInputEvent() { }
  }
  //   /*
  //    * Declare a concrete type for the NDK's input device forward declaration.
  //    */
  //   struct AInputDevice {
  //     virtual ~AInputDevice() { }
  //   };
  //   namespace android {
  // #ifdef __ANDROID__
  //     class Parcel;
  // #endif
  // /*
  //  * Flags that flow alongside events in the input dispatch system to help with certain
  //  * policy decisions such as waking from device sleep.
  //  *
  //  * These flags are also defined in
  // frameworks/base/core/java/android/view/WindowManagerPolicy.java.
  //  */
  //     enum {
  //     /* These flags originate in RawEvents and are generally set in the key map.
  //      * NOTE: If you want a flag to be able to set in a keylayout file, then you must add it to
  //      * InputEventLabels.h as well. */
  //       // Indicates that the event should wake the device.
  //       POLICY_FLAG_WAKE = 0x00000001,
  //           // Indicates that the key is virtual, such as a capacitive button, and should
  //           // generate haptic feedback.  Virtual keys may be suppressed for some time
  //           // after a recent touch to prevent accidental activation of virtual keys adjacent
  //           // to the touch screen during an edge swipe.
  //           POLICY_FLAG_VIRTUAL = 0x00000002,
  //           // Indicates that the key is the special function modifier.
  //           POLICY_FLAG_FUNCTION = 0x00000004,
  //           // Indicates that the key represents a special gesture that has been detected by
  //           // the touch firmware or driver.  Causes touch events from the same device to be
  // canceled.
  //           POLICY_FLAG_GESTURE = 0x00000008,
  //           POLICY_FLAG_RAW_MASK = 0x0000ffff,
  //     /* These flags are set by the input dispatcher. */
  //           // Indicates that the input event was injected.
  //           POLICY_FLAG_INJECTED = 0x01000000,
  //           // Indicates that the input event is from a trusted source such as a directly
  // attached
  //           // input device or an application with system-wide event injection permission.
  //           POLICY_FLAG_TRUSTED = 0x02000000,
  //           // Indicates that the input event has passed through an input filter.
  //           POLICY_FLAG_FILTERED = 0x04000000,
  //           // Disables automatic key repeating behavior.
  //           POLICY_FLAG_DISABLE_KEY_REPEAT = 0x08000000,
  //     /* These flags are set by the input reader policy as it intercepts each event. */
  //           // Indicates that the device was in an interactive state when the
  //           // event was intercepted.
  //           POLICY_FLAG_INTERACTIVE = 0x20000000,
  //           // Indicates that the event should be dispatched to applications.
  //           // The input event should still be sent to the InputDispatcher so that it can see all
  //           // input events received include those that it will not deliver.
  //           POLICY_FLAG_PASS_TO_USER = 0x40000000,
  //     };

  /*
   * Pointer coordinate data.
   *
   * Deviates from original platform implementation to store axises in simple SparseArray as opposed
   * to complicated bitset + array arrangement.
   */
  static class PointerCoords {
    private static final int MAX_AXES = 30;

    // Bitfield of axes that are present in this structure.
    private NativeBitSet64 bits = new NativeBitSet64();

    NativeBitSet64 getBits() {
      return bits;
    }

    // Values of axes that are stored in this structure
    private float[] values = new float[MAX_AXES];

    public void clear() {
      bits.clear();
    }

    public boolean isEmpty() {
      return bits.isEmpty();
    }

    public float getAxisValue(int axis) {
      if (axis < 0 || axis > 63 || !bits.hasBit(axis)) {
        return 0;
      }
      return values[bits.getIndexOfBit(axis)];
    }

    public boolean setAxisValue(int axis, float value) {
      checkState(axis >= 0 && axis <= 63, "axis out of range");
      int index = bits.getIndexOfBit(axis);
      if (!bits.hasBit(axis)) {
        if (value == 0) {
          return true; // axes with value 0 do not need to be stored
        }

        int count = bits.count();
        if (count >= MAX_AXES) {
          tooManyAxes(axis);
          return false;
        }
        bits.markBit(axis);
        for (int i = count; i > index; i--) {
          values[i] = values[i - 1];
        }
      }
      values[index] = value;
      return true;
    }

    static void scaleAxisValue(PointerCoords c, int axis, float scaleFactor) {
      float value = c.getAxisValue(axis);
      if (value != 0) {
        c.setAxisValue(axis, value * scaleFactor);
      }
    }

    public void scale(float scaleFactor) {
      // No need to scale pressure or size since they are normalized.
      // No need to scale orientation since it is meaningless to do so.
      scaleAxisValue(this, AMOTION_EVENT_AXIS_X, scaleFactor);
      scaleAxisValue(this, AMOTION_EVENT_AXIS_Y, scaleFactor);
      scaleAxisValue(this, AMOTION_EVENT_AXIS_TOUCH_MAJOR, scaleFactor);
      scaleAxisValue(this, AMOTION_EVENT_AXIS_TOUCH_MINOR, scaleFactor);
      scaleAxisValue(this, AMOTION_EVENT_AXIS_TOOL_MAJOR, scaleFactor);
      scaleAxisValue(this, AMOTION_EVENT_AXIS_TOOL_MINOR, scaleFactor);
    }

    public void applyOffset(float xOffset, float yOffset) {
      setAxisValue(AMOTION_EVENT_AXIS_X, getX() + xOffset);
      setAxisValue(AMOTION_EVENT_AXIS_Y, getY() + yOffset);
    }

    public float getX() {
      return getAxisValue(AMOTION_EVENT_AXIS_X);
    }

    public float getY() {
      return getAxisValue(AMOTION_EVENT_AXIS_Y);
    }

    // #ifdef __ANDROID__
    //     status_t readFromParcel(Parcel* parcel);
    //     status_t writeToParcel(Parcel* parcel) ;
    // #endif
    //     bool operator==( PointerCoords& other) ;
    //      bool operator!=( PointerCoords& other)  {
    //       return !(*this == other);
    //     }
    public void copyFrom(PointerCoords other) {
      bits = new NativeBitSet64(other.bits);
      int count = bits.count();
      for (int i = 0; i < count; i++) {
        values[i] = other.values[i];
      }
    }

    private static void tooManyAxes(int axis) {
      // native code just logs this as warning. Be a bit more defensive for now and throw
      throw new IllegalStateException(
          String.format(
              "Could not set value for axis %d because the PointerCoords structure is full and "
                  + "cannot contain more than %d axis values.",
              axis, MAX_AXES));
    }
  }

  /*
   * Input events.
   */
  static class InputEvent extends AInputEvent {

    private int mDeviceId;
    private int mSource;

    public int getType() {
      return 0;
    }

    public int getDeviceId() {
      return mDeviceId;
    }

    public int getSource() {
      return mSource;
    }

    public void setSource(int source) {
      mSource = source;
    }

    protected void initialize(int deviceId, int source) {
      this.mDeviceId = deviceId;
      this.mSource = source;
    }

    protected void initialize(NativeInput.InputEvent from) {
      initialize(from.getDeviceId(), from.getSource());
    }
  }
  /*
   * Key events.
   */
  static class KeyEvent extends InputEvent {
    //       public:
    //       virtual ~KeyEvent() { }
    //       virtual int getType()  { return AINPUT_EVENT_TYPE_KEY; }
    //        int getAction()  { return mAction; }
    //        int getFlags()  { return mFlags; }
    //        void setFlags(int flags) { mFlags = flags; }
    //        int getKeyCode()  { return mKeyCode; }
    //        int getScanCode()  { return mScanCode; }
    //        int getMetaState()  { return mMetaState; }
    //        int getRepeatCount()  { return mRepeatCount; }
    //        nsecs_t getDownTime()  { return mDownTime; }
    //        nsecs_t getEventTime()  { return mEventTime; }
    //       static  char* getLabel(int keyCode);
    //     static int getKeyCodeFromLabel( char* label);
    //
    //     void initialize(
    //         int deviceId,
    //         int source,
    //         int action,
    //         int flags,
    //         int keyCode,
    //         int scanCode,
    //         int metaState,
    //         int repeatCount,
    //         nsecs_t downTime,
    //         nsecs_t eventTime);
    //     void initialize( KeyEvent& from);
    //     protected:
    //     int mAction;
    //     int mFlags;
    //     int mKeyCode;
    //     int mScanCode;
    //     int mMetaState;
    //     int mRepeatCount;
    //     nsecs_t mDownTime;
    //     nsecs_t mEventTime;
  }

  /*
   * Motion events.
   */
  static class MotionEvent extends InputEvent {

    // constants copied from android bionic/libc/include/math.h
    private static final double M_PI = 3.14159265358979323846f; /* pi */
    private static final double M_PI_2 = 1.57079632679489661923f; /* pi/2 */

    private int mAction;
    private int mActionButton;
    private int mFlags;
    private int mEdgeFlags;
    private int mMetaState;
    private int mButtonState;
    private float mXOffset;
    private float mYOffset;
    private float mXPrecision;
    private float mYPrecision;
    private long mDownTime;
    private List<PointerProperties> mPointerProperties = new ArrayList<>();
    private List<Long> mSampleEventTimes = new ArrayList<>();
    private List<NativeInput.PointerCoords> mSamplePointerCoords = new ArrayList<>();

    public int getType() {
      return AINPUT_EVENT_TYPE_MOTION;
    }

    public int getAction() {
      return mAction;
    }

    public int getActionMasked() {
      return mAction & AMOTION_EVENT_ACTION_MASK;
    }

    public int getActionIndex() {
      return (mAction & AMOTION_EVENT_ACTION_POINTER_INDEX_MASK)
          >> AMOTION_EVENT_ACTION_POINTER_INDEX_SHIFT;
    }

    public void setAction(int action) {
      mAction = action;
    }

    public int getFlags() {
      return mFlags;
    }

    public void setFlags(int flags) {
      mFlags = flags;
    }

    public int getEdgeFlags() {
      return mEdgeFlags;
    }

    public void setEdgeFlags(int edgeFlags) {
      mEdgeFlags = edgeFlags;
    }

    public int getMetaState() {
      return mMetaState;
    }

    public void setMetaState(int metaState) {
      mMetaState = metaState;
    }

    public int getButtonState() {
      return mButtonState;
    }

    public void setButtonState(int buttonState) {
      mButtonState = buttonState;
    }

    public int getActionButton() {
      return mActionButton;
    }

    public void setActionButton(int button) {
      mActionButton = button;
    }

    public float getXOffset() {
      return mXOffset;
    }

    public float getYOffset() {
      return mYOffset;
    }

    public float getXPrecision() {
      return mXPrecision;
    }

    public float getYPrecision() {
      return mYPrecision;
    }

    public long getDownTime() {
      return mDownTime;
    }

    public void setDownTime(long downTime) {
      mDownTime = downTime;
    }

    public int getPointerCount() {
      return mPointerProperties.size();
    }

    public PointerProperties getPointerProperties(int pointerIndex) {
      return mPointerProperties.get(pointerIndex);
    }

    public int getPointerId(int pointerIndex) {
      return mPointerProperties.get(pointerIndex).id;
    }

    public int getToolType(int pointerIndex) {
      return mPointerProperties.get(pointerIndex).toolType;
    }

    public long getEventTime() {
      return mSampleEventTimes.get(getHistorySize());
    }

    public PointerCoords getRawPointerCoords(int pointerIndex) {

      return mSamplePointerCoords.get(getHistorySize() * getPointerCount() + pointerIndex);
    }

    public float getRawAxisValue(int axis, int pointerIndex) {
      return getRawPointerCoords(pointerIndex).getAxisValue(axis);
    }

    public float getRawX(int pointerIndex) {
      return getRawAxisValue(AMOTION_EVENT_AXIS_X, pointerIndex);
    }

    public float getRawY(int pointerIndex) {
      return getRawAxisValue(AMOTION_EVENT_AXIS_Y, pointerIndex);
    }

    public float getAxisValue(int axis, int pointerIndex) {
      float value = getRawPointerCoords(pointerIndex).getAxisValue(axis);
      switch (axis) {
        case AMOTION_EVENT_AXIS_X:
          return value + mXOffset;
        case AMOTION_EVENT_AXIS_Y:
          return value + mYOffset;
      }
      return value;
    }

    public float getX(int pointerIndex) {
      return getAxisValue(AMOTION_EVENT_AXIS_X, pointerIndex);
    }

    public float getY(int pointerIndex) {
      return getAxisValue(AMOTION_EVENT_AXIS_Y, pointerIndex);
    }

    public float getPressure(int pointerIndex) {
      return getAxisValue(AMOTION_EVENT_AXIS_PRESSURE, pointerIndex);
    }

    public float getSize(int pointerIndex) {
      return getAxisValue(AMOTION_EVENT_AXIS_SIZE, pointerIndex);
    }

    public float getTouchMajor(int pointerIndex) {
      return getAxisValue(AMOTION_EVENT_AXIS_TOUCH_MAJOR, pointerIndex);
    }

    public float getTouchMinor(int pointerIndex) {
      return getAxisValue(AMOTION_EVENT_AXIS_TOUCH_MINOR, pointerIndex);
    }

    public float getToolMajor(int pointerIndex) {
      return getAxisValue(AMOTION_EVENT_AXIS_TOOL_MAJOR, pointerIndex);
    }

    public float getToolMinor(int pointerIndex) {
      return getAxisValue(AMOTION_EVENT_AXIS_TOOL_MINOR, pointerIndex);
    }

    public float getOrientation(int pointerIndex) {
      return getAxisValue(AMOTION_EVENT_AXIS_ORIENTATION, pointerIndex);
    }

    public int getHistorySize() {
      return mSampleEventTimes.size() - 1;
    }

    public long getHistoricalEventTime(int historicalIndex) {
      return mSampleEventTimes.get(historicalIndex);
    }

    public PointerCoords getHistoricalRawPointerCoords(int pointerIndex, int historicalIndex) {
      return mSamplePointerCoords.get(historicalIndex * getPointerCount() + pointerIndex);
    }

    public float getHistoricalRawAxisValue(int axis, int pointerIndex, int historicalIndex) {
      return getHistoricalRawPointerCoords(pointerIndex, historicalIndex).getAxisValue(axis);
    }

    public float getHistoricalRawX(int pointerIndex, int historicalIndex) {
      return getHistoricalRawAxisValue(AMOTION_EVENT_AXIS_X, pointerIndex, historicalIndex);
    }

    public float getHistoricalRawY(int pointerIndex, int historicalIndex) {
      return getHistoricalRawAxisValue(AMOTION_EVENT_AXIS_Y, pointerIndex, historicalIndex);
    }

    public float getHistoricalAxisValue(int axis, int pointerIndex, int historicalIndex) {
      float value = getHistoricalRawPointerCoords(pointerIndex, historicalIndex).getAxisValue(axis);
      switch (axis) {
        case AMOTION_EVENT_AXIS_X:
          return value + mXOffset;
        case AMOTION_EVENT_AXIS_Y:
          return value + mYOffset;
      }
      return value;
    }

    public float getHistoricalX(int pointerIndex, int historicalIndex) {
      return getHistoricalAxisValue(AMOTION_EVENT_AXIS_X, pointerIndex, historicalIndex);
    }

    public float getHistoricalY(int pointerIndex, int historicalIndex) {
      return getHistoricalAxisValue(AMOTION_EVENT_AXIS_Y, pointerIndex, historicalIndex);
    }

    public float getHistoricalPressure(int pointerIndex, int historicalIndex) {
      return getHistoricalAxisValue(AMOTION_EVENT_AXIS_PRESSURE, pointerIndex, historicalIndex);
    }

    public float getHistoricalSize(int pointerIndex, int historicalIndex) {
      return getHistoricalAxisValue(AMOTION_EVENT_AXIS_SIZE, pointerIndex, historicalIndex);
    }

    public float getHistoricalTouchMajor(int pointerIndex, int historicalIndex) {
      return getHistoricalAxisValue(AMOTION_EVENT_AXIS_TOUCH_MAJOR, pointerIndex, historicalIndex);
    }

    public float getHistoricalTouchMinor(int pointerIndex, int historicalIndex) {
      return getHistoricalAxisValue(AMOTION_EVENT_AXIS_TOUCH_MINOR, pointerIndex, historicalIndex);
    }

    public float getHistoricalToolMajor(int pointerIndex, int historicalIndex) {
      return getHistoricalAxisValue(AMOTION_EVENT_AXIS_TOOL_MAJOR, pointerIndex, historicalIndex);
    }

    public float getHistoricalToolMinor(int pointerIndex, int historicalIndex) {
      return getHistoricalAxisValue(AMOTION_EVENT_AXIS_TOOL_MINOR, pointerIndex, historicalIndex);
    }

    public float getHistoricalOrientation(int pointerIndex, int historicalIndex) {
      return getHistoricalAxisValue(AMOTION_EVENT_AXIS_ORIENTATION, pointerIndex, historicalIndex);
    }

    public int findPointerIndex(int pointerId) {
      int pointerCount = mPointerProperties.size();
      for (int i = 0; i < pointerCount; i++) {
        if (mPointerProperties.get(i).id == pointerId) {
          return i;
        }
      }
      return -1;
    }

    public void initialize(
        int deviceId,
        int source,
        int action,
        int actionButton,
        int flags,
        int edgeFlags,
        int metaState,
        int buttonState,
        float xOffset,
        float yOffset,
        float xPrecision,
        float yPrecision,
        long downTime,
        long eventTime,
        int pointerCount,
        PointerProperties[] pointerProperties,
        NativeInput.PointerCoords[] pointerCoords) {
      super.initialize(deviceId, source);
      mAction = action;
      mActionButton = actionButton;
      mFlags = flags;
      mEdgeFlags = edgeFlags;
      mMetaState = metaState;
      mButtonState = buttonState;
      mXOffset = xOffset;
      mYOffset = yOffset;
      mXPrecision = xPrecision;
      mYPrecision = yPrecision;
      mDownTime = downTime;
      mPointerProperties.clear();
      mPointerProperties.addAll(Arrays.asList(pointerProperties).subList(0, pointerCount));
      mSampleEventTimes.clear();
      mSamplePointerCoords.clear();
      addSample(eventTime, Arrays.asList(pointerCoords).subList(0, pointerCount));
    }

    public void copyFrom(MotionEvent other, boolean keepHistory) {
      super.initialize(other.getDeviceId(), other.getSource());
      mAction = other.mAction;
      mActionButton = other.mActionButton;
      mFlags = other.mFlags;
      mEdgeFlags = other.mEdgeFlags;
      mMetaState = other.mMetaState;
      mButtonState = other.mButtonState;
      mXOffset = other.mXOffset;
      mYOffset = other.mYOffset;
      mXPrecision = other.mXPrecision;
      mYPrecision = other.mYPrecision;
      mDownTime = other.mDownTime;
      mPointerProperties = other.mPointerProperties;
      mSampleEventTimes.clear();
      mSamplePointerCoords.clear();
      if (keepHistory) {
        mSampleEventTimes.addAll(other.mSampleEventTimes);
        mSamplePointerCoords.addAll(other.mSamplePointerCoords);
      } else {
        mSampleEventTimes.add(other.getEventTime());
        int pointerCount = other.getPointerCount();
        int historySize = other.getHistorySize();
        mSamplePointerCoords.addAll(other.mSamplePointerCoords.subList(historySize * pointerCount, pointerCount));
      }
    }

    public void addSample(long eventTime, PointerCoords[] pointerCoords) {
      addSample(eventTime, Arrays.asList(pointerCoords));
    }

    public void addSample(long eventTime, List<PointerCoords> pointerCoords) {
      mSampleEventTimes.add(eventTime);
      mSamplePointerCoords.addAll(pointerCoords);
    }

    public void offsetLocation(float xOffset, float yOffset) {
      mXOffset += xOffset;
      mYOffset += yOffset;
    }

    public void scale(float scaleFactor) {
      mXOffset *= scaleFactor;
      mYOffset *= scaleFactor;
      mXPrecision *= scaleFactor;
      mYPrecision *= scaleFactor;
      int numSamples = mSamplePointerCoords.size();
      for (int i = 0; i < numSamples; i++) {
        mSamplePointerCoords.get(i).scale(scaleFactor);
      }
    }
    // Apply 3x3 perspective matrix transformation.
    // Matrix is in row-major form and compatible with SkMatrix.
    public void transform(float[] matrix) {
      checkState(matrix.length == 9);
      // The tricky part of this implementation is to preserve the value of
      // rawX and rawY.  So we apply the transformation to the first point
      // then derive an appropriate new X/Y offset that will preserve rawX
      // and rawY for that point.
      float oldXOffset = mXOffset;
      float oldYOffset = mYOffset;
      Ref<Float> newX = new Ref<>(0f);
      Ref<Float> newY = new Ref<>(0f);
      float rawX = getRawX(0);
      float rawY = getRawY(0);
      transformPoint(matrix, rawX + oldXOffset, rawY + oldYOffset, newX, newY);
      mXOffset = newX.get() - rawX;
      mYOffset = newY.get() - rawY;
      // Determine how the origin is transformed by the matrix so that we
      // can transform orientation vectors.
      Ref<Float> originX = new Ref<>(0f);
      Ref<Float> originY = new Ref<>(0f);
      transformPoint(matrix, 0, 0, originX, originY);
      // Apply the transformation to all samples.
      int numSamples = mSamplePointerCoords.size();
      for (int i = 0; i < numSamples; i++) {
        PointerCoords c = mSamplePointerCoords.get(i);
        Ref<Float> x = new Ref<>(c.getAxisValue(AMOTION_EVENT_AXIS_X) + oldXOffset);
        Ref<Float> y = new Ref<>(c.getAxisValue(AMOTION_EVENT_AXIS_Y) + oldYOffset);
        transformPoint(matrix, x.get(), y.get(), x, y);
        c.setAxisValue(AMOTION_EVENT_AXIS_X, x.get() - mXOffset);
        c.setAxisValue(AMOTION_EVENT_AXIS_Y, y.get() - mYOffset);
        float orientation = c.getAxisValue(AMOTION_EVENT_AXIS_ORIENTATION);
        c.setAxisValue(
            AMOTION_EVENT_AXIS_ORIENTATION,
            transformAngle(matrix, orientation, originX.get(), originY.get()));
      }
    }

    private static void transformPoint(
        float[] matrix, float x, float y, Ref<Float> outX, Ref<Float> outY) {
      checkState(matrix.length == 9);
      // Apply perspective transform like Skia.
      float newX = matrix[0] * x + matrix[1] * y + matrix[2];
      float newY = matrix[3] * x + matrix[4] * y + matrix[5];
      float newZ = matrix[6] * x + matrix[7] * y + matrix[8];
      if (newZ != 0) {
        newZ = 1.0f / newZ;
      }
      outX.set(newX * newZ);
      outY.set(newY * newZ);
    }

    static float transformAngle(float[] matrix, float angleRadians, float originX, float originY) {
      checkState(matrix.length == 9);
      // ruct and transform a vector oriented at the specified clockwise angle from vertical.
      // Coordinate system: down is increasing Y, right is increasing X.
      Ref<Float> x = new Ref<>((float) Math.sin(angleRadians));
      Ref<Float> y = new Ref<>(-(float) Math.cos(angleRadians));
      transformPoint(matrix, x.get(), y.get(), x, y);
      x.set(x.get() - originX);
      y.set(y.get() - originY);
      // Derive the transformed vector's clockwise angle from vertical.
      double result = Math.atan2(x.get(), -y.get());
      if (result < -M_PI_2) {
        result += M_PI;
      } else if (result > M_PI_2) {
        result -= M_PI;
      }
      return (float) result;
    }

    // #ifdef __ANDROID__
    //     status_t readFromParcel(Parcel* parcel);
    //     status_t writeToParcel(Parcel* parcel) ;
    // #endif
    public static boolean isTouchEvent(int source, int action) {
      if ((source & AINPUT_SOURCE_CLASS_POINTER) != 0) {
        // Specifically excludes HOVER_MOVE and SCROLL.
        switch (action & AMOTION_EVENT_ACTION_MASK) {
          case AMOTION_EVENT_ACTION_DOWN:
          case AMOTION_EVENT_ACTION_MOVE:
          case AMOTION_EVENT_ACTION_UP:
          case AMOTION_EVENT_ACTION_POINTER_DOWN:
          case AMOTION_EVENT_ACTION_POINTER_UP:
          case AMOTION_EVENT_ACTION_CANCEL:
          case AMOTION_EVENT_ACTION_OUTSIDE:
            return true;
        }
      }
      return false;
    }

    public boolean isTouchEvent() {
      return isTouchEvent(getSource(), mAction);
    }

    //     // Low-level accessors.
    public List<PointerProperties> getPointerProperties() {
      return mPointerProperties;
    }

    List<Long> getSampleEventTimes() {
      return mSampleEventTimes;
    }

    List<NativeInput.PointerCoords> getSamplePointerCoords() {
      return mSamplePointerCoords;
    }
    // static  String getLabel(int axis) {
    //   return getAxisLabel(axis);
    // }
    // static int getAxisFromLabel( String label) {
    //   return getAxisByLabel(label);
    // }

  }
  // /*
  //  * Input event factory.
  //  */
  //     class InputEventFactoryInterface {
  //       protected:
  //       virtual ~InputEventFactoryInterface() { }
  //       public:
  //       InputEventFactoryInterface() { }
  //       virtual KeyEvent* createKeyEvent() = 0;
  //       virtual MotionEvent* createMotionEvent() = 0;
  //     };
  // /*
  //  * A simple input event factory implementation that uses a single preallocated instance
  //  * of each type of input event that are reused for each request.
  //  */
  //     class PreallocatedInputEventFactory : public InputEventFactoryInterface {
  //       public:
  //       PreallocatedInputEventFactory() { }
  //       virtual ~PreallocatedInputEventFactory() { }
  //       virtual KeyEvent* createKeyEvent() { return & mKeyEvent; }
  //       virtual MotionEvent* createMotionEvent() { return & mMotionEvent; }
  //       private:
  //       KeyEvent mKeyEvent;
  //       MotionEvent mMotionEvent;
  //     };
  // /*
  //  * An input event factory implementation that maintains a pool of input events.
  //  */
  //     class PooledInputEventFactory : public InputEventFactoryInterface {
  //       public:
  //       PooledInputEventFactory(int maxPoolSize = 20);
  //       virtual ~PooledInputEventFactory();
  //       virtual KeyEvent* createKeyEvent();
  //       virtual MotionEvent* createMotionEvent();
  //     void recycle(InputEvent* event);
  //     private:
  //      int mMaxPoolSize;
  //     Vector<KeyEvent*> mKeyEventPool;
  //     Vector<MotionEvent*> mMotionEventPool;
  // };
  //   } // namespace android
  // #endif // _LIBINPUT_INPUT_H
}
