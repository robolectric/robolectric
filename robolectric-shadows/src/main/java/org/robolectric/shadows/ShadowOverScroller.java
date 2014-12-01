package org.robolectric.shadows;

import android.os.Looper;
import android.widget.OverScroller;
import org.robolectric.Shadows;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.Scheduler;

@Implements(OverScroller.class)
public class ShadowOverScroller {
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
    // post a task so that the scheduler will actually run
    getScheduler().postDelayed(new Runnable() {
      @Override
      public void run() {
        // do nothing
      }
    }, duration);
  }

  @Implementation
  public void abortAnimation() {
    duration = deltaTime() - 1;
  }

  @Implementation
  public void forceFinished(boolean finished) {
    if (!finished) {
      throw new RuntimeException("Not implemented.");
    }

    finalX = getCurrX();
    finalY = getCurrY();
    duration = deltaTime() - 1;
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

  @Implementation
  public boolean isScrollingInDirection(float xvel, float yvel) {
    final int dx = finalX - startX;
    final int dy = finalY - startY;
    return !isFinished()
        && Math.signum(xvel) == Math.signum(dx)
        && Math.signum(yvel) == Math.signum(dy);
  }

  private long deltaTime() {
    return getScheduler().getCurrentTime() - startTime;
  }

  private Scheduler getScheduler() {
    return Shadows.shadowOf(Looper.getMainLooper()).getScheduler();
  }

  private int deltaX() {
    return (finalX - startX);
  }

  private int deltaY() {
    return (finalY - startY);
  }
}

