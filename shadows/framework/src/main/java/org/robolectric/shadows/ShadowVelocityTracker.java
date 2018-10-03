package org.robolectric.shadows;

import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(VelocityTracker.class)
public class ShadowVelocityTracker {
  private static final int ACTIVE_POINTER_ID = -1;
  private static final int HISTORY_SIZE = 20;
  private static final long HORIZON_MS = 200L;
  private static final long MIN_DURATION = 10L;

  private boolean initialized = false;
  private int activePointerId = -1;
  private final Movement[] movements = new Movement[HISTORY_SIZE];
  private int curIndex = 0;

  private SparseArray<Float> computedVelocityX = new SparseArray<>();
  private SparseArray<Float> computedVelocityY = new SparseArray<>();

  private void maybeInitialize() {
    if (initialized) {
      return;
    }

    for (int i = 0; i < movements.length; i++) {
      movements[i] = new Movement();
    }
    initialized = true;
  }

  @Implementation
  protected void clear() {
    maybeInitialize();
    curIndex = 0;
    computedVelocityX.clear();
    computedVelocityY.clear();
    for (Movement movement : movements) {
      movement.clear();
    }
  }

  @Implementation
  protected void addMovement(MotionEvent event) {
    maybeInitialize();
    if (event == null) {
      throw new IllegalArgumentException("event must not be null");
    }

    if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
      clear();
    } else if (event.getActionMasked() != MotionEvent.ACTION_MOVE) {
      // only listen for DOWN and MOVE events
      return;
    }

    curIndex = (curIndex + 1) % HISTORY_SIZE;
    movements[curIndex].set(event);
  }

  @Implementation
  protected void computeCurrentVelocity(int units) {
    computeCurrentVelocity(units, Float.MAX_VALUE);
  }

  @Implementation
  protected void computeCurrentVelocity(int units, float maxVelocity) {
    maybeInitialize();

    // Estimation based on AOSP's LegacyVelocityTrackerStrategy
    Movement newestMovement = movements[curIndex];
    if (!newestMovement.isSet()) {
      // no movements added, so we can assume that the current velocity is 0 (and already set that
      // way)
      return;
    }

    for (int pointerId : newestMovement.pointerIds) {
      // Find the oldest sample that is for the same pointer, but not older than HORIZON_MS
      long minTime = newestMovement.eventTime - HORIZON_MS;
      int oldestIndex = curIndex;
      int numTouches = 1;
      do {
        int nextOldestIndex = (oldestIndex == 0 ? HISTORY_SIZE : oldestIndex) - 1;
        Movement nextOldestMovement = movements[nextOldestIndex];
        if (!nextOldestMovement.hasPointer(pointerId) || nextOldestMovement.eventTime < minTime) {
          break;
        }

        oldestIndex = nextOldestIndex;
      } while (++numTouches < HISTORY_SIZE);

      float accumVx = 0f;
      float accumVy = 0f;
      int index = oldestIndex;
      Movement oldestMovement = movements[oldestIndex];
      long lastDuration = 0;

      while (numTouches-- > 1) {
        if (++index == HISTORY_SIZE) {
          index = 0;
        }

        Movement movement = movements[index];
        long duration = movement.eventTime - oldestMovement.eventTime;

        if (duration >= MIN_DURATION) {
          float scale = 1000f / duration; // one over time delta in seconds
          float vx = (movement.x.get(pointerId) - oldestMovement.x.get(pointerId)) * scale;
          float vy = (movement.y.get(pointerId) - oldestMovement.y.get(pointerId)) * scale;
          accumVx = (accumVx * lastDuration + vx * duration) / (duration + lastDuration);
          accumVy = (accumVy * lastDuration + vy * duration) / (duration + lastDuration);
          lastDuration = duration;
        }
      }

      computedVelocityX.put(pointerId, windowed(accumVx * units / 1000, maxVelocity));
      computedVelocityY.put(pointerId, windowed(accumVy * units / 1000, maxVelocity));
    }

    activePointerId = newestMovement.activePointerId;
  }

  private float windowed(float value, float max) {
    return Math.min(max, Math.max(-max, value));
  }

  @Implementation
  protected float getXVelocity() {
    return getXVelocity(ACTIVE_POINTER_ID);
  }

  @Implementation
  protected float getYVelocity() {
    return getYVelocity(ACTIVE_POINTER_ID);
  }

  @Implementation
  protected float getXVelocity(int id) {
    if (id == ACTIVE_POINTER_ID) {
      id = activePointerId;
    }

    return computedVelocityX.get(id, 0f);
  }

  @Implementation
  protected float getYVelocity(int id) {
    if (id == ACTIVE_POINTER_ID) {
      id = activePointerId;
    }

    return computedVelocityY.get(id, 0f);
  }

  private static class Movement {
    public int pointerCount = 0;
    public int[] pointerIds = new int[0];
    public int activePointerId = -1;
    public long eventTime;
    public SparseArray<Float> x = new SparseArray<>();
    public SparseArray<Float> y = new SparseArray<>();

    public void set(MotionEvent event) {
      pointerCount = event.getPointerCount();
      pointerIds = new int[pointerCount];
      x.clear();
      y.clear();
      for (int i = 0; i < pointerCount; i++) {
        pointerIds[i] = event.getPointerId(i);
        x.put(pointerIds[i], event.getX(i));
        y.put(pointerIds[i], event.getY(i));
      }
      activePointerId = event.getPointerId(0);
      eventTime = event.getEventTime();
    }

    public void clear() {
      pointerCount = 0;
      activePointerId = -1;
    }

    public boolean isSet() {
      return pointerCount != 0;
    }

    public boolean hasPointer(int pointerId) {
      return x.get(pointerId) != null;
    }
  }
}
