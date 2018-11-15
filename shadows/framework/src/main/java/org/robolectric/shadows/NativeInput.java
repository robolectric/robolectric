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

import android.os.Parcel;
import android.view.MotionEvent.PointerProperties;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.robolectric.res.android.Ref;

/**
 * Java representation of framework native input Transliterated from oreo-mr1 (SDK 27)
 * frameworks/native/include/input/Input.h and libs/input/Input.cpp
 *
 * @see <a href="https://android.googlesource.com/platform/frameworks/native/+/oreo-mr1-release/include/input/Input.h">include/input/Input.h</a>
 * @see <a href="https://android.googlesource.com/platform/frameworks/native/+/oreo-mr1-release/libs/input/Input.cpp>libs/input/Input.cpp</a>
 */
public class NativeInput {

  /*
   * Maximum number of pointers supported per motion event.
   * Smallest number of pointers is 1.
   * (We want at least 10 but some touch controllers obstensibly configured for 10 pointers
   * will occasionally emit 11.  There is not much harm making this ant bigger.)
   */
  private static final int MAX_POINTERS = 16;
  /*
   * Maximum number of samples supported per motion event.
   */
  private static final int MAX_SAMPLES = 2 ^ 16; /* UINT16_MAX */
  /*
   * Maximum pointer id value supported in a motion event.
   * Smallest pointer id is 0.
   * (This is limited by our use of BitSet32 to track pointer assignments.)
   */
  private static final int MAX_POINTER_ID = 31;

  /*
   * Declare a concrete type for the NDK's input event forward declaration.
   */
  static class AInputEvent {}

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

    public boolean readFromParcel(Parcel parcel) {
      bits.setValue(parcel.readLong());
      int count = bits.count();
      if (count > MAX_AXES) {
        return false;
      }
      for (int i = 0; i < count; i++) {
        values[i] = parcel.readFloat();
      }
      return true;
    }

    public boolean writeToParcel(Parcel parcel) {
      parcel.writeLong(bits.getValue());
      int count = bits.count();
      for (int i = 0; i < count; i++) {
        parcel.writeFloat(values[i]);
      }
      return true;
    }

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

    protected int mDeviceId;
    protected int mSource;

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
    @SuppressWarnings("FloatingPointLiteralPrecision")
    private static final double M_PI = 3.14159265358979323846f; /* pi */

    @SuppressWarnings("FloatingPointLiteralPrecision")
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

    @Override
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
        // mSamplePointerCoords.appendArray(other->mSamplePointerCoords.array()
        //    + (historySize * pointerCount), pointerCount);
        int currentStartIndex = historySize * pointerCount;
        mSamplePointerCoords.addAll(
            other.mSamplePointerCoords.subList(
                currentStartIndex, currentStartIndex + pointerCount));
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
      final Ref<Float> newX = new Ref<>(0f);
      final Ref<Float> newY = new Ref<>(0f);
      float rawX = getRawX(0);
      float rawY = getRawY(0);
      transformPoint(matrix, rawX + oldXOffset, rawY + oldYOffset, newX, newY);
      mXOffset = newX.get() - rawX;
      mYOffset = newY.get() - rawY;
      // Determine how the origin is transformed by the matrix so that we
      // can transform orientation vectors.
      final Ref<Float> originX = new Ref<>(0f);
      final Ref<Float> originY = new Ref<>(0f);
      transformPoint(matrix, 0, 0, originX, originY);
      // Apply the transformation to all samples.
      int numSamples = mSamplePointerCoords.size();
      for (int i = 0; i < numSamples; i++) {
        PointerCoords c = mSamplePointerCoords.get(i);
        final Ref<Float> x = new Ref<>(c.getAxisValue(AMOTION_EVENT_AXIS_X) + oldXOffset);
        final Ref<Float> y = new Ref<>(c.getAxisValue(AMOTION_EVENT_AXIS_Y) + oldYOffset);
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
      final Ref<Float> x = new Ref<>((float) Math.sin(angleRadians));
      final Ref<Float> y = new Ref<>(-(float) Math.cos(angleRadians));
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

    public boolean readFromParcel(Parcel parcel) {
      int pointerCount = parcel.readInt();
      int sampleCount = parcel.readInt();
      if (pointerCount == 0
          || pointerCount > MAX_POINTERS
          || sampleCount == 0
          || sampleCount > MAX_SAMPLES) {
        return false;
      }
      mDeviceId = parcel.readInt();
      mSource = parcel.readInt();
      mAction = parcel.readInt();
      mActionButton = parcel.readInt();
      mFlags = parcel.readInt();
      mEdgeFlags = parcel.readInt();
      mMetaState = parcel.readInt();
      mButtonState = parcel.readInt();
      mXOffset = parcel.readFloat();
      mYOffset = parcel.readFloat();
      mXPrecision = parcel.readFloat();
      mYPrecision = parcel.readFloat();
      mDownTime = parcel.readLong();
      mPointerProperties = new ArrayList<>(pointerCount);
      mSampleEventTimes = new ArrayList<>(sampleCount);
      mSamplePointerCoords = new ArrayList<>(sampleCount * pointerCount);
      for (int i = 0; i < pointerCount; i++) {
        PointerProperties properties = new PointerProperties();
        mPointerProperties.add(properties);
        properties.id = parcel.readInt();
        properties.toolType = parcel.readInt();
      }
      while (sampleCount > 0) {
        sampleCount--;
        mSampleEventTimes.add(parcel.readLong());
        for (int i = 0; i < pointerCount; i++) {
          NativeInput.PointerCoords pointerCoords = new NativeInput.PointerCoords();
          mSamplePointerCoords.add(pointerCoords);
          if (!pointerCoords.readFromParcel(parcel)) {
            return false;
          }
        }
      }
      return true;
    }

    public boolean writeToParcel(Parcel parcel) {
      int pointerCount = mPointerProperties.size();
      int sampleCount = mSampleEventTimes.size();
      parcel.writeInt(pointerCount);
      parcel.writeInt(sampleCount);
      parcel.writeInt(mDeviceId);
      parcel.writeInt(mSource);
      parcel.writeInt(mAction);
      parcel.writeInt(mActionButton);
      parcel.writeInt(mFlags);
      parcel.writeInt(mEdgeFlags);
      parcel.writeInt(mMetaState);
      parcel.writeInt(mButtonState);
      parcel.writeFloat(mXOffset);
      parcel.writeFloat(mYOffset);
      parcel.writeFloat(mXPrecision);
      parcel.writeFloat(mYPrecision);
      parcel.writeLong(mDownTime);
      for (int i = 0; i < pointerCount; i++) {
        PointerProperties properties = mPointerProperties.get(i);
        parcel.writeInt(properties.id);
        parcel.writeInt(properties.toolType);
      }
      for (int h = 0; h < sampleCount; h++) {
        parcel.writeLong(mSampleEventTimes.get(h));
        for (int i = 0; i < pointerCount; i++) {
          if (!mSamplePointerCoords.get(i).writeToParcel(parcel)) {
            return false;
          }
        }
      }
      return true;
    }

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

    // Low-level accessors.
    public List<PointerProperties> getPointerProperties() {
      return mPointerProperties;
    }

    List<Long> getSampleEventTimes() {
      return mSampleEventTimes;
    }

    List<NativeInput.PointerCoords> getSamplePointerCoords() {
      return mSamplePointerCoords;
    }
  }
}
