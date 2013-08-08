package org.robolectric.shadows;

import android.os.Looper;
import android.widget.Scroller;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.Scheduler;

import static org.robolectric.Robolectric.shadowOf;

@Implements(Scroller.class)
public class ShadowScroller {
  private int startX;
  private int startY;
  private int finalX;
  private int finalY;
  private long startTime;
  private long duration;
  private boolean started;

  @Implementation
  public int getStartX() {
    return startX;
  }

  @Implementation
  public int getStartY() {
    return startY;
  }

  @Implementation
  public int getCurrX() {
    long dt = deltaTime();
    return dt >= duration ? finalX : startX + (int) ((deltaX() * dt) / duration);
  }

  @Implementation
  public int getCurrY() {
    long dt = deltaTime();
    return dt >= duration ? finalY : startY + (int) ((deltaY() * dt) / duration);
  }

  @Implementation
  public int getFinalX() {
    return finalX;
  }

  @Implementation
  public int getFinalY() {
    return finalY;
  }

  @Implementation
  public int getDuration() {
    return (int) duration;
  }

  @Implementation
  public void startScroll(int startX, int startY, int dx, int dy, int duration) {
    this.startX = startX;
    this.startY = startY;
    finalX = startX + dx;
    finalY = startY + dy;
    startTime = getScheduler().getCurrentTime();
    this.duration = duration;
    started = true;
    // enque a dummy task so that the scheduler will actually run
    getScheduler().postDelayed(new Runnable() {
      @Override
      public void run() {
        // do nothing
      }
    }, duration);
  }

  @Implementation
  public boolean computeScrollOffset() {
    if (!started) {
      return false;
    }
    started &= deltaTime() < duration;
    return true;
  }

  @Implementation
  public boolean isFinished() {
    return deltaTime() > duration;
  }

  @Implementation
  public int timePassed() {
    return (int) deltaTime();
  }

  private long deltaTime() {
    return getScheduler().getCurrentTime() - startTime;
  }

  private Scheduler getScheduler() {
    return shadowOf(Looper.getMainLooper()).getScheduler();
  }

  private int deltaX() {
    return (finalX - startX);
  }

  private int deltaY() {
    return (finalY - startY);
  }

}
