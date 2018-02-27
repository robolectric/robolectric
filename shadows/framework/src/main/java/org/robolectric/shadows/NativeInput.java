package org.robolectric.shadows;

import static com.google.common.base.Preconditions.checkState;
import static org.robolectric.shadows.NativeAndroidInput.AINPUT_SOURCE_CLASS_POINTER;
import static org.robolectric.shadows.NativeAndroidInput.AMOTION_EVENT_ACTION_CANCEL;
import static org.robolectric.shadows.NativeAndroidInput.AMOTION_EVENT_ACTION_DOWN;
import static org.robolectric.shadows.NativeAndroidInput.AMOTION_EVENT_ACTION_MASK;
import static org.robolectric.shadows.NativeAndroidInput.AMOTION_EVENT_ACTION_MOVE;
import static org.robolectric.shadows.NativeAndroidInput.AMOTION_EVENT_ACTION_OUTSIDE;
import static org.robolectric.shadows.NativeAndroidInput.AMOTION_EVENT_ACTION_POINTER_DOWN;
import static org.robolectric.shadows.NativeAndroidInput.AMOTION_EVENT_ACTION_POINTER_UP;
import static org.robolectric.shadows.NativeAndroidInput.AMOTION_EVENT_ACTION_UP;
import static org.robolectric.shadows.NativeAndroidInput.AMOTION_EVENT_AXIS_X;
import static org.robolectric.shadows.NativeAndroidInput.AMOTION_EVENT_AXIS_Y;

import android.util.SparseArray;
import android.view.MotionEvent.PointerProperties;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Java representation of framework native input
 * Transliterated from oreo-mr1 frameworks/native/include/input/Input.h and libs/input/Input.cpp
 */
public class NativeInput {

  /**
   * Native input event structures.
   */
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
//      * This input source flag is hidden from the API because switches are only used by the system
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
//  * These flags are also defined in frameworks/base/core/java/android/view/WindowManagerPolicy.java.
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
//           // the touch firmware or driver.  Causes touch events from the same device to be canceled.
//           POLICY_FLAG_GESTURE = 0x00000008,
//           POLICY_FLAG_RAW_MASK = 0x0000ffff,
//     /* These flags are set by the input dispatcher. */
//           // Indicates that the input event was injected.
//           POLICY_FLAG_INJECTED = 0x01000000,
//           // Indicates that the input event is from a trusted source such as a directly attached
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
      // Values of axes that are stored in this structure
      protected SparseArray<Float> values = new SparseArray<>();

      void clear() {
        values.clear();
      }

      boolean isEmpty() {
        return values.size() == 0;
      }

  float getAxisValue(int axis)  {
    return values.get(axis, 0f);
  }

  boolean setAxisValue(int axis, float value) {
        values.put(axis, value);
    return true;
  }
      //abstract void scale(float scale);
      //abstract void applyOffset(float xOffset, float yOffset);

      float getX()  {
        return getAxisValue(AMOTION_EVENT_AXIS_X);
      }

      float getY() {
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
//     void copyFrom( PointerCoords& other);
//     private:
//     void tooManyAxes(int axis);
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

//       public:
//       virtual ~MotionEvent() { }
       //int getType()  { return AINPUT_EVENT_TYPE_MOTION; }
        int getAction()  { return mAction; }
      //  int getActionMasked()  { return mAction & AMOTION_EVENT_ACTION_MASK; }
      //  int getActionIndex()  {
      //   return (mAction & AMOTION_EVENT_ACTION_POINTER_INDEX_MASK)
      //       >> AMOTION_EVENT_ACTION_POINTER_INDEX_SHIFT;
      // }
       void setAction(int action) { mAction = action; }
       int getFlags()  { return mFlags; }
       void setFlags(int flags) { mFlags = flags; }
       int getEdgeFlags()  { return mEdgeFlags; }
       void setEdgeFlags(int edgeFlags) { mEdgeFlags = edgeFlags; }
       int getMetaState()  { return mMetaState; }
       void setMetaState(int metaState) { mMetaState = metaState; }
       int getButtonState()  { return mButtonState; }
       void setButtonState(int buttonState) { mButtonState = buttonState; }
       int getActionButton()  { return mActionButton; }
       void setActionButton(int button) { mActionButton = button; }
       float getXOffset()  { return mXOffset; }
       float getYOffset()  { return mYOffset; }
       float getXPrecision()  { return mXPrecision; }
       float getYPrecision()  { return mYPrecision; }
         long getDownTime() { return mDownTime; }
//        void setDownTime(nsecs_t downTime) { mDownTime = downTime; }
        int getPointerCount()  { return mPointerProperties.size(); }
    PointerProperties getPointerProperties(int pointerIndex)  {
        return mPointerProperties.get(pointerIndex);
      }
//        int getPointerId(size_t pointerIndex)  {
//         return mPointerProperties[pointerIndex].id;
//       }
//        int getToolType(size_t pointerIndex)  {
//         return mPointerProperties[pointerIndex].toolType;
//       }
        long getEventTime()  { return mSampleEventTimes.get(getHistorySize()); }

//      PointerCoords* getRawPointerCoords(size_t pointerIndex) ;
//     float getRawAxisValue(int axis, size_t pointerIndex) ;
//      float getRawX(size_t pointerIndex)  {
//       return getRawAxisValue(AMOTION_EVENT_AXIS_X, pointerIndex);
//     }
//      float getRawY(size_t pointerIndex)  {
//       return getRawAxisValue(AMOTION_EVENT_AXIS_Y, pointerIndex);
//     }
//     float getAxisValue(int axis, size_t pointerIndex) ;
//      float getX(size_t pointerIndex)  {
//       return getAxisValue(AMOTION_EVENT_AXIS_X, pointerIndex);
//     }
//      float getY(size_t pointerIndex)  {
//       return getAxisValue(AMOTION_EVENT_AXIS_Y, pointerIndex);
//     }
//      float getPressure(size_t pointerIndex)  {
//       return getAxisValue(AMOTION_EVENT_AXIS_PRESSURE, pointerIndex);
//     }
//      float getSize(size_t pointerIndex)  {
//       return getAxisValue(AMOTION_EVENT_AXIS_SIZE, pointerIndex);
//     }
//      float getTouchMajor(size_t pointerIndex)  {
//       return getAxisValue(AMOTION_EVENT_AXIS_TOUCH_MAJOR, pointerIndex);
//     }
//      float getTouchMinor(size_t pointerIndex)  {
//       return getAxisValue(AMOTION_EVENT_AXIS_TOUCH_MINOR, pointerIndex);
//     }
//      float getToolMajor(size_t pointerIndex)  {
//       return getAxisValue(AMOTION_EVENT_AXIS_TOOL_MAJOR, pointerIndex);
//     }
//      float getToolMinor(size_t pointerIndex)  {
//       return getAxisValue(AMOTION_EVENT_AXIS_TOOL_MINOR, pointerIndex);
//     }
//      float getOrientation(size_t pointerIndex)  {
//       return getAxisValue(AMOTION_EVENT_AXIS_ORIENTATION, pointerIndex);
//     }
      int getHistorySize()  { return mSampleEventTimes.size() - 1; }

     long getHistoricalEventTime(int historicalIndex)  {
      return mSampleEventTimes.get(historicalIndex);
    }

//      PointerCoords* getHistoricalRawPointerCoords(
//         size_t pointerIndex, size_t historicalIndex) ;
//     float getHistoricalRawAxisValue(int axis, size_t pointerIndex,
//         size_t historicalIndex) ;
//      float getHistoricalRawX(size_t pointerIndex, size_t historicalIndex)  {
//       return getHistoricalRawAxisValue(
//           AMOTION_EVENT_AXIS_X, pointerIndex, historicalIndex);
//     }
//      float getHistoricalRawY(size_t pointerIndex, size_t historicalIndex)  {
//       return getHistoricalRawAxisValue(
//           AMOTION_EVENT_AXIS_Y, pointerIndex, historicalIndex);
//     }
//     float getHistoricalAxisValue(int axis, size_t pointerIndex, size_t historicalIndex) ;
//      float getHistoricalX(size_t pointerIndex, size_t historicalIndex)  {
//       return getHistoricalAxisValue(
//           AMOTION_EVENT_AXIS_X, pointerIndex, historicalIndex);
//     }
//      float getHistoricalY(size_t pointerIndex, size_t historicalIndex)  {
//       return getHistoricalAxisValue(
//           AMOTION_EVENT_AXIS_Y, pointerIndex, historicalIndex);
//     }
//      float getHistoricalPressure(size_t pointerIndex, size_t historicalIndex)  {
//       return getHistoricalAxisValue(
//           AMOTION_EVENT_AXIS_PRESSURE, pointerIndex, historicalIndex);
//     }
//      float getHistoricalSize(size_t pointerIndex, size_t historicalIndex)  {
//       return getHistoricalAxisValue(
//           AMOTION_EVENT_AXIS_SIZE, pointerIndex, historicalIndex);
//     }
//      float getHistoricalTouchMajor(size_t pointerIndex, size_t historicalIndex)  {
//       return getHistoricalAxisValue(
//           AMOTION_EVENT_AXIS_TOUCH_MAJOR, pointerIndex, historicalIndex);
//     }
//      float getHistoricalTouchMinor(size_t pointerIndex, size_t historicalIndex)  {
//       return getHistoricalAxisValue(
//           AMOTION_EVENT_AXIS_TOUCH_MINOR, pointerIndex, historicalIndex);
//     }
//      float getHistoricalToolMajor(size_t pointerIndex, size_t historicalIndex)  {
//       return getHistoricalAxisValue(
//           AMOTION_EVENT_AXIS_TOOL_MAJOR, pointerIndex, historicalIndex);
//     }
//      float getHistoricalToolMinor(size_t pointerIndex, size_t historicalIndex)  {
//       return getHistoricalAxisValue(
//           AMOTION_EVENT_AXIS_TOOL_MINOR, pointerIndex, historicalIndex);
//     }
//      float getHistoricalOrientation(size_t pointerIndex, size_t historicalIndex)  {
//       return getHistoricalAxisValue(
//           AMOTION_EVENT_AXIS_ORIENTATION, pointerIndex, historicalIndex);
//     }
//     ssize_t findPointerIndex(int pointerId) ;

    void initialize(
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
      mPointerProperties.addAll(Arrays.asList(pointerProperties));
      mSampleEventTimes.clear();
      mSamplePointerCoords.clear();
      addSample(eventTime, pointerCoords);
    }

//     void copyFrom( MotionEvent* other, bool keepHistory);
  void addSample(
      long eventTime,
        PointerCoords[] pointerCoords) {
    mSampleEventTimes.add(eventTime);
    //mSamplePointerCoords.addAll(Arrays.asList(pointerCoords, getPointerCount());
    mSamplePointerCoords.addAll(Arrays.asList(pointerCoords));
   }

  void offsetLocation(float xOffset, float yOffset) {
    mXOffset += xOffset;
    mYOffset += yOffset;
  }
//     void scale(float scaleFactor);
//     // Apply 3x3 perspective matrix transformation.
//     // Matrix is in row-major form and compatible with SkMatrix.
//     void transform( float matrix[9]);
// #ifdef __ANDROID__
//     status_t readFromParcel(Parcel* parcel);
//     status_t writeToParcel(Parcel* parcel) ;
// #endif
  private static boolean isTouchEvent(int source, int action) {
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

      boolean isTouchEvent()  {
       return isTouchEvent(getSource(), mAction);
     }

//     // Low-level accessors.
      List<PointerProperties> getPointerProperties()  {
      return mPointerProperties;
    }
//       nsecs_t* getSampleEventTimes()  { return mSampleEventTimes.array(); }
//       PointerCoords* getSamplePointerCoords()  {
//       return mSamplePointerCoords.array();
//     }
//     static  char* getLabel(int axis);
//     static int getAxisFromLabel( char* label);
//     protected:
    protected int mAction;
    protected int mActionButton;
    protected int mFlags;
    protected int mEdgeFlags;
    protected int mMetaState;
    protected int mButtonState;
    protected float mXOffset;
    protected float mYOffset;
    protected float mXPrecision;
    protected float mYPrecision;
    protected long mDownTime;
    protected List<PointerProperties> mPointerProperties = new ArrayList<>();
    protected List<Long> mSampleEventTimes = new ArrayList<>();
    protected List<NativeInput.PointerCoords> mSamplePointerCoords = new ArrayList<>();
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
//       PooledInputEventFactory(size_t maxPoolSize = 20);
//       virtual ~PooledInputEventFactory();
//       virtual KeyEvent* createKeyEvent();
//       virtual MotionEvent* createMotionEvent();
//     void recycle(InputEvent* event);
//     private:
//      size_t mMaxPoolSize;
//     Vector<KeyEvent*> mKeyEventPool;
//     Vector<MotionEvent*> mMotionEventPool;
// };
//   } // namespace android
// #endif // _LIBINPUT_INPUT_H
}
