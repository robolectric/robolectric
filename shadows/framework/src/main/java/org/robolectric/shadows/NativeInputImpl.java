package org.robolectric.shadows;



/**
 * Java representation of framework native input implementation. Transliterated from oreo-mr1
 * frameworks/native/include/input/Input.h
 */
public class NativeInputImpl {
  //   namespace android {
  // // --- InputEvent ---
  //     void InputEvent::initialize(int deviceId, int source) {
  //       mDeviceId = deviceId;
  //       mSource = source;
  //     }
  //     void InputEvent::initialize( InputEvent& from) {
  //       mDeviceId = from.mDeviceId;
  //       mSource = from.mSource;
  //     }
  // // --- KeyEvent ---
  //  char* KeyEvent::getLabel(int keyCode) {
  //       return NativeInputEventLabel(keyCode);
  //     }
  //     int KeyEvent::getKeyCodeFromLabel( char* label) {
  //       return NativeInputEventLabel(label);
  //     }
  //     void KeyEvent::initialize(
  //         int deviceId,
  //         int source,
  //         int action,
  //         int flags,
  //         int keyCode,
  //         int scanCode,
  //         int metaState,
  //         int repeatCount,
  //         nsecs_t downTime,
  //         nsecs_t eventTime) {
  //       InputEvent::initialize(deviceId, source);
  //       mAction = action;
  //       mFlags = flags;
  //       mKeyCode = keyCode;
  //       mScanCode = scanCode;
  //       mMetaState = metaState;
  //       mRepeatCount = repeatCount;
  //       mDownTime = downTime;
  //       mEventTime = eventTime;
  //     }
  //     void KeyEvent::initialize( KeyEvent& from) {
  //       InputEvent::initialize(from);
  //       mAction = from.mAction;
  //       mFlags = from.mFlags;
  //       mKeyCode = from.mKeyCode;
  //       mScanCode = from.mScanCode;
  //       mMetaState = from.mMetaState;
  //       mRepeatCount = from.mRepeatCount;
  //       mDownTime = from.mDownTime;
  //       mEventTime = from.mEventTime;
  //     }
  // static class PointerCoordsImpl extends PointerCoords {

  //   static inline void scaleAxisValue(PointerCoords& c, int axis, float scaleFactor) {
  //     float value = c.getAxisValue(axis);
  //     if (value != 0) {
  //       c.setAxisValue(axis, value * scaleFactor);
  //     }
  //   }
  //   void PointerCoords::scale(float scaleFactor) {
  //     // No need to scale pressure or size since they are normalized.
  //     // No need to scale orientation since it is meaningless to do so.
  //     scaleAxisValue(*this, AMOTION_EVENT_AXIS_X, scaleFactor);
  //     scaleAxisValue(*this, AMOTION_EVENT_AXIS_Y, scaleFactor);
  //     scaleAxisValue(*this, AMOTION_EVENT_AXIS_TOUCH_MAJOR, scaleFactor);
  //     scaleAxisValue(*this, AMOTION_EVENT_AXIS_TOUCH_MINOR, scaleFactor);
  //     scaleAxisValue(*this, AMOTION_EVENT_AXIS_TOOL_MAJOR, scaleFactor);
  //     scaleAxisValue(*this, AMOTION_EVENT_AXIS_TOOL_MINOR, scaleFactor);
  //   }
  //   void PointerCoords::applyOffset(float xOffset, float yOffset) {
  //     setAxisValue(AMOTION_EVENT_AXIS_X, getX() + xOffset);
  //     setAxisValue(AMOTION_EVENT_AXIS_Y, getY() + yOffset);
  //   }
  // #ifdef __ANDROID__
  //   status_t PointerCoords::readFromParcel(Parcel* parcel) {
  //     bits = parcel->readInt64();
  //     uint count = BitSet64::count(bits);
  //     if (count > MAX_AXES) {
  //       return BAD_VALUE;
  //     }
  //     for (uint i = 0; i < count; i++) {
  //       values[i] = parcel->readFloat();
  //     }
  //     return OK;
  //   }
  //   status_t PointerCoords::writeToParcel(Parcel* parcel)  {
  //     parcel->writeInt64(bits);
  //     uint count = BitSet64::count(bits);
  //     for (uint i = 0; i < count; i++) {
  //       parcel->writeFloat(values[i]);
  //     }
  //     return OK;
  //   }
  // #endif
  //   void PointerCoords::tooManyAxes(int axis) {
  //     ALOGW("Could not set value for axis %d because the PointerCoords structure is full and "
  //         "cannot contain more than %d axis values.", axis, int(MAX_AXES));
  //   }
  //   bool PointerCoords::operator==( PointerCoords& other)  {
  //     if (bits != other.bits) {
  //       return false;
  //     }
  //     uint count = BitSet64::count(bits);
  //     for (uint i = 0; i < count; i++) {
  //       if (values[i] != other.values[i]) {
  //         return false;
  //       }
  //     }
  //     return true;
  //   }
  //   void PointerCoords::copyFrom( PointerCoords& other) {
  //     bits = other.bits;
  //     uint count = BitSet64::count(bits);
  //     for (uint i = 0; i < count; i++) {
  //       values[i] = other.values[i];
  //     }
  //   }
  //  } // end PointerCoordsImpl

  //   // --- PointerProperties ---
  //   bool PointerProperties::operator==( PointerProperties& other)  {
  //     return id == other.id
  //         && toolType == other.toolType;
  //   }
  //   void PointerProperties::copyFrom( PointerProperties& other) {
  //     id = other.id;
  //     toolType = other.toolType;
  //   }
  //
  // static class MotionEventImpl extends NativeInput.MotionEvent {

  //   void MotionEvent::copyFrom( MotionEvent* other, bool keepHistory) {
  //     InputEvent::initialize(other->mDeviceId, other->mSource);
  //     mAction = other->mAction;
  //     mActionButton = other->mActionButton;
  //     mFlags = other->mFlags;
  //     mEdgeFlags = other->mEdgeFlags;
  //     mMetaState = other->mMetaState;
  //     mButtonState = other->mButtonState;
  //     mXOffset = other->mXOffset;
  //     mYOffset = other->mYOffset;
  //     mXPrecision = other->mXPrecision;
  //     mYPrecision = other->mYPrecision;
  //     mDownTime = other->mDownTime;
  //     mPointerProperties = other->mPointerProperties;
  //     if (keepHistory) {
  //       mSampleEventTimes = other->mSampleEventTimes;
  //       mSamplePointerCoords = other->mSamplePointerCoords;
  //     } else {
  //       mSampleEventTimes.clear();
  //       mSampleEventTimes.push(other->getEventTime());
  //       mSamplePointerCoords.clear();
  //       int pointerCount = other->getPointerCount();
  //       int historySize = other->getHistorySize();
  //       mSamplePointerCoords.appendArray(other->mSamplePointerCoords.array()
  //           + (historySize * pointerCount), pointerCount);
  //     }
  //   }
  //   void MotionEvent::addSample(
  //       int64_t eventTime,
  //          PointerCoords* pointerCoords) {
  //     mSampleEventTimes.push(eventTime);
  //     mSamplePointerCoords.appendArray(pointerCoords, getPointerCount());
  //   }
  //  PointerCoords* MotionEvent::getRawPointerCoords(int pointerIndex)  {
  //     return &mSamplePointerCoords[getHistorySize() * getPointerCount() + pointerIndex];
  //   }
  //   float MotionEvent::getRawAxisValue(int axis, int pointerIndex)  {
  //     return getRawPointerCoords(pointerIndex)->getAxisValue(axis);
  //   }
  //   float MotionEvent::getAxisValue(int axis, int pointerIndex)  {
  //     float value = getRawPointerCoords(pointerIndex)->getAxisValue(axis);
  //     switch (axis) {
  //       case AMOTION_EVENT_AXIS_X:
  //         return value + mXOffset;
  //       case AMOTION_EVENT_AXIS_Y:
  //         return value + mYOffset;
  //     }
  //     return value;
  //   }
  //  PointerCoords* MotionEvent::getHistoricalRawPointerCoords(
  //       int pointerIndex, int historicalIndex)  {
  //     return &mSamplePointerCoords[historicalIndex * getPointerCount() + pointerIndex];
  //   }
  //   float MotionEvent::getHistoricalRawAxisValue(int axis, int pointerIndex,
  //       int historicalIndex)  {
  //     return getHistoricalRawPointerCoords(pointerIndex, historicalIndex)->getAxisValue(axis);
  //   }
  //   float MotionEvent::getHistoricalAxisValue(int axis, int pointerIndex,
  //       int historicalIndex)  {
  //     float value = getHistoricalRawPointerCoords(pointerIndex,
  // historicalIndex)->getAxisValue(axis);
  //     switch (axis) {
  //       case AMOTION_EVENT_AXIS_X:
  //         return value + mXOffset;
  //       case AMOTION_EVENT_AXIS_Y:
  //         return value + mYOffset;
  //     }
  //     return value;
  //   }
  //   sint MotionEvent::findPointerIndex(int pointerId)  {
  //     int pointerCount = mPointerProperties.size();
  //     for (int i = 0; i < pointerCount; i++) {
  //       if (mPointerProperties.itemAt(i).id == pointerId) {
  //         return i;
  //       }
  //     }
  //     return -1;
  //   }
  //   void MotionEvent::shaation(float xOffset, float yOffset) {
  //     mXOffset += xOffset;
  //     mYOffset += yOffset;
  //   }
  //   void MotionEvent::scale(float scaleFactor) {
  //     mXOffset *= scaleFactor;
  //     mYOffset *= scaleFactor;
  //     mXPrecision *= scaleFactor;
  //     mYPrecision *= scaleFactor;
  //     int numSamples = mSamplePointerCoords.size();
  //     for (int i = 0; i < numSamples; i++) {
  //       mSamplePointerCoords.editItemAt(i).scale(scaleFactor);
  //     }
  //   }
  //   static void transformPoint( float matrix[9], float x, float y, float *outX, float *outY) {
  //     // Apply perspective transform like Skia.
  //     float newX = matrix[0] * x + matrix[1] * y + matrix[2];
  //     float newY = matrix[3] * x + matrix[4] * y + matrix[5];
  //     float newZ = matrix[6] * x + matrix[7] * y + matrix[8];
  //     if (newZ) {
  //       newZ = 1.0f / newZ;
  //     }
  //     *outX = newX * newZ;
  //     *outY = newY * newZ;
  //   }
  //   static float transformAngle( float matrix[9], float angleRadians,
  //       float originX, float originY) {
  //     // ruct and transform a vector oriented at the specified clockwise angle from vertical.
  //     // Coordinate system: down is increasing Y, right is increasing X.
  //     float x = sinf(angleRadians);
  //     float y = -cosf(angleRadians);
  //     transformPoint(matrix, x, y, &x, &y);
  //     x -= originX;
  //     y -= originY;
  //     // Derive the transformed vector's clockwise angle from vertical.
  //     float result = atan2f(x, -y);
  //     if (result < - M_PI_2) {
  //       result += M_PI;
  //     } else if (result > M_PI_2) {
  //       result -= M_PI;
  //     }
  //     return result;
  //   }
  //   void MotionEvent::transform( float matrix[9]) {
  //     // The tricky part of this implementation is to preserve the value of
  //     // rawX and rawY.  So we apply the transformation to the first point
  //     // then derive an appropriate new X/Y offset that will preserve rawX
  //     // and rawY for that point.
  //     float oldXOffset = mXOffset;
  //     float oldYOffset = mYOffset;
  //     float newX, newY;
  //     float rawX = getRawX(0);
  //     float rawY = getRawY(0);
  //     transformPoint(matrix, rawX + oldXOffset, rawY + oldYOffset, &newX, &newY);
  //     mXOffset = newX - rawX;
  //     mYOffset = newY - rawY;
  //     // Determine how the origin is transformed by the matrix so that we
  //     // can transform orientation vectors.
  //     float originX, originY;
  //     transformPoint(matrix, 0, 0, &originX, &originY);
  //     // Apply the transformation to all samples.
  //     int numSamples = mSamplePointerCoords.size();
  //     for (int i = 0; i < numSamples; i++) {
  //       PointerCoords& c = mSamplePointerCoords.editItemAt(i);
  //       float x = c.getAxisValue(AMOTION_EVENT_AXIS_X) + oldXOffset;
  //       float y = c.getAxisValue(AMOTION_EVENT_AXIS_Y) + oldYOffset;
  //       transformPoint(matrix, x, y, &x, &y);
  //       c.setAxisValue(AMOTION_EVENT_AXIS_X, x - mXOffset);
  //       c.setAxisValue(AMOTION_EVENT_AXIS_Y, y - mYOffset);
  //       float orientation = c.getAxisValue(AMOTION_EVENT_AXIS_ORIENTATION);
  //       c.setAxisValue(AMOTION_EVENT_AXIS_ORIENTATION,
  //           transformAngle(matrix, orientation, originX, originY));
  //     }
  //   }
  // #ifdef __ANDROID__
  //   status_t MotionEvent::readFromParcel(Parcel* parcel) {
  //     int pointerCount = parcel->readInt32();
  //     int sampleCount = parcel->readInt32();
  //     if (pointerCount == 0 || pointerCount > MAX_POINTERS ||
  //         sampleCount == 0 || sampleCount > MAX_SAMPLES) {
  //       return BAD_VALUE;
  //     }
  //     mDeviceId = parcel->readInt32();
  //     mSource = parcel->readInt32();
  //     mAction = parcel->readInt32();
  //     mActionButton = parcel->readInt32();
  //     mFlags = parcel->readInt32();
  //     mEdgeFlags = parcel->readInt32();
  //     mMetaState = parcel->readInt32();
  //     mButtonState = parcel->readInt32();
  //     mXOffset = parcel->readFloat();
  //     mYOffset = parcel->readFloat();
  //     mXPrecision = parcel->readFloat();
  //     mYPrecision = parcel->readFloat();
  //     mDownTime = parcel->readInt64();
  //     mPointerProperties.clear();
  //     mPointerProperties.setCapacity(pointerCount);
  //     mSampleEventTimes.clear();
  //     mSampleEventTimes.setCapacity(sampleCount);
  //     mSamplePointerCoords.clear();
  //     mSamplePointerCoords.setCapacity(sampleCount * pointerCount);
  //     for (int i = 0; i < pointerCount; i++) {
  //       mPointerProperties.push();
  //       PointerProperties& properties = mPointerProperties.editTop();
  //       properties.id = parcel->readInt32();
  //       properties.toolType = parcel->readInt32();
  //     }
  //     while (sampleCount > 0) {
  //       sampleCount--;
  //       mSampleEventTimes.push(parcel->readInt64());
  //       for (int i = 0; i < pointerCount; i++) {
  //         mSamplePointerCoords.push();
  //         status_t status = mSamplePointerCoords.editTop().readFromParcel(parcel);
  //         if (status) {
  //           return status;
  //         }
  //       }
  //     }
  //     return OK;
  //   }
  //   status_t MotionEvent::writeToParcel(Parcel* parcel)  {
  //     int pointerCount = mPointerProperties.size();
  //     int sampleCount = mSampleEventTimes.size();
  //     parcel->writeInt32(pointerCount);
  //     parcel->writeInt32(sampleCount);
  //     parcel->writeInt32(mDeviceId);
  //     parcel->writeInt32(mSource);
  //     parcel->writeInt32(mAction);
  //     parcel->writeInt32(mActionButton);
  //     parcel->writeInt32(mFlags);
  //     parcel->writeInt32(mEdgeFlags);
  //     parcel->writeInt32(mMetaState);
  //     parcel->writeInt32(mButtonState);
  //     parcel->writeFloat(mXOffset);
  //     parcel->writeFloat(mYOffset);
  //     parcel->writeFloat(mXPrecision);
  //     parcel->writeFloat(mYPrecision);
  //     parcel->writeInt64(mDownTime);
  //     for (int i = 0; i < pointerCount; i++) {
  //          PointerProperties& properties = mPointerProperties.itemAt(i);
  //       parcel->writeInt32(properties.id);
  //       parcel->writeInt32(properties.toolType);
  //     }
  //      PointerCoords* pc = mSamplePointerCoords.array();
  //     for (int h = 0; h < sampleCount; h++) {
  //       parcel->writeInt64(mSampleEventTimes.itemAt(h));
  //       for (int i = 0; i < pointerCount; i++) {
  //         status_t status = (pc++)->writeToParcel(parcel);
  //         if (status) {
  //           return status;
  //         }
  //       }
  //     }
  //     return OK;
  //   }
  // #endif
  //   bool MotionEvent::isTouchEvent(int source, int action) {
  //     if (source & AINPUT_SOURCE_CLASS_POINTER) {
  //       // Specifically excludes HOVER_MOVE and SCROLL.
  //       switch (action & AMOTION_EVENT_ACTION_MASK) {
  //         case AMOTION_EVENT_ACTION_DOWN:
  //         case AMOTION_EVENT_ACTION_MOVE:
  //         case AMOTION_EVENT_ACTION_UP:
  //         case AMOTION_EVENT_ACTION_POINTER_DOWN:
  //         case AMOTION_EVENT_ACTION_POINTER_UP:
  //         case AMOTION_EVENT_ACTION_CANCEL:
  //         case AMOTION_EVENT_ACTION_OUTSIDE:
  //           return true;
  //       }
  //     }
  //     return false;
  //   }
  //  char* MotionEvent::getLabel(int axis) {
  //     return NativeInputEventLabel(axis);
  //   }
  //   int MotionEvent::getAxisFromLabel( char* label) {
  //     return NativeInputEventLabel(label);
  //   }

  // } // end MotionEventImpl

  // // --- PooledInputEventFactory ---
  //   PooledInputEventFactory::PooledInputEventFactory(int maxPoolSize) :
  //   mMaxPoolSize(maxPoolSize) {
  //   }
  //   PooledInputEventFactory::~PooledInputEventFactory() {
  //     for (int i = 0; i < mKeyEventPool.size(); i++) {
  //       delete mKeyEventPool.itemAt(i);
  //     }
  //     for (int i = 0; i < mMotionEventPool.size(); i++) {
  //       delete mMotionEventPool.itemAt(i);
  //     }
  //   }
  //   KeyEvent* PooledInputEventFactory::createKeyEvent() {
  //     if (!mKeyEventPool.isEmpty()) {
  //       KeyEvent* event = mKeyEventPool.top();
  //       mKeyEventPool.pop();
  //       return event;
  //     }
  //     return new KeyEvent();
  //   }
  //   MotionEvent* PooledInputEventFactory::createMotionEvent() {
  //     if (!mMotionEventPool.isEmpty()) {
  //       MotionEvent* event = mMotionEventPool.top();
  //       mMotionEventPool.pop();
  //       return event;
  //     }
  //     return new MotionEvent();
  //   }
  //   void PooledInputEventFactory::recycle(InputEvent* event) {
  //     switch (event->getType()) {
  //       case AINPUT_EVENT_TYPE_KEY:
  //         if (mKeyEventPool.size() < mMaxPoolSize) {
  //           mKeyEventPool.push(static_cast<KeyEvent*>(event));
  //           return;
  //         }
  //         break;
  //       case AINPUT_EVENT_TYPE_MOTION:
  //         if (mMotionEventPool.size() < mMaxPoolSize) {
  //           mMotionEventPool.push(static_cast<MotionEvent*>(event));
  //           return;
  //         }
  //         break;
  //     }
  //     delete event;
  //   }
  // } // namespace android
}
