package org.robolectric.shadows;

import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.ShadowAnimationBridge;
import android.view.animation.Transformation;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

/**
 * Shadow implementation of {@code Animation} that provides support for invoking listener callbacks.
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(Animation.class)
public class ShadowAnimation {

  private Animation.AnimationListener listener;
  private Interpolator interpolator;
  private boolean startFlag = false;
  private boolean endFlag = false;
  private long durationMillis = 0;
  private int repeatCount;
  private int repeatMode;
  private long startOffset;
  private int loadedFromResourceId = -1;

  private boolean fillAfter;
  @RealObject
  private Animation realAnimation;

  @Implementation
  public void setAnimationListener(Animation.AnimationListener l) {
    listener = l;
  }

  @Implementation
  public void start() {
    startFlag = true;
    if (listener != null) {
      listener.onAnimationStart(realAnimation);
    }
  }

  @Implementation
  public void cancel() {
    startFlag = false;
    endAnimation();
  }

  @Implementation
  public boolean hasStarted() {
    return startFlag;
  }

  @Implementation
  public boolean hasEnded() {
    return endFlag;
  }

  @Implementation
  public void setDuration(long durationMillis) {
    this.durationMillis = durationMillis;
  }

  @Implementation
  public long getDuration() {
    return durationMillis;
  }

  @Implementation
  public void setInterpolator(Interpolator interpolator) {
    this.interpolator = interpolator;
  }

  @Implementation
  public Interpolator getInterpolator() {
    return interpolator;
  }

  @Implementation
  public void setRepeatCount(int repeatCount) {
    this.repeatCount = repeatCount;
  }

  @Implementation
  public int getRepeatCount() {
    return repeatCount;
  }

  @Implementation
  public void setRepeatMode(int repeatMode) {
    this.repeatMode = repeatMode;
  }

  @Implementation
  public int getRepeatMode() {
    return repeatMode;
  }

  @Implementation
  public void setStartOffset(long startOffset) {
    this.startOffset = startOffset;
  }

  @Implementation
  public long getStartOffset() {
    return startOffset;
  }

  @Implementation
  public void setFillAfter(boolean fillAfter) {
    this.fillAfter = fillAfter;
  }

  @Implementation
  public boolean getFillAfter() {
    return fillAfter;
  }

  /**
   * Non-Android accessor.  Returns most recently set animation listener.
   *
   * @return
   */
  public Animation.AnimationListener getAnimationListener() {
    return listener;
  }

  /**
   * Non-Android accessor.  Use to simulate repeat loops of animation.
   */
  public void invokeRepeat() {
    if (listener != null) {
      listener.onAnimationRepeat(realAnimation);
    }
  }

  /**
   * Non-Android accessor.  Use to simulate end of animation.
   */
  public void invokeEnd() {
    endAnimation();
    new ShadowAnimationBridge(realAnimation).applyTransformation(1.0f, new Transformation());
  }

  public void setLoadedFromResourceId(int loadedFromResourceId) {
    this.loadedFromResourceId = loadedFromResourceId;
  }

  public int getLoadedFromResourceId() {
    if (loadedFromResourceId == -1) {
      throw new IllegalStateException("not loaded from a resource");
    }
    return loadedFromResourceId;
  }

  private void endAnimation() {
    if (listener != null) {
      listener.onAnimationEnd(realAnimation);
    }

    endFlag = true;
  }

}
