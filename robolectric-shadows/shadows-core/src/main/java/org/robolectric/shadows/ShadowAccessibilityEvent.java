package org.robolectric.shadows;

import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.internal.ShadowExtractor;
import org.robolectric.util.ReflectionHelpers;

import java.util.HashMap;
import java.util.Map;

/**
 * Shadow of {@link android.view.accessibility.AccessibilityEvent}.
 */
@Implements(AccessibilityEvent.class)
public class ShadowAccessibilityEvent {
  // Map of obtained instances of the class along with stack traces of how they were obtained
  private static final Map<StrictEqualityEventWrapper, StackTraceElement[]> obtainedInstances =
      new HashMap<>();

  private int eventType;
  private CharSequence contentDescription;
  private CharSequence packageName;
  private CharSequence className;
  private boolean enabled;

  @RealObject
  private AccessibilityEvent realAccessibilityEvent;

  @Implementation
  public static AccessibilityEvent obtain(AccessibilityEvent event) {
    ShadowAccessibilityEvent shadowEvent =
        ((ShadowAccessibilityEvent) ShadowExtractor.extract(event));
    AccessibilityEvent obtainedInstance = shadowEvent.getClone();

    obtainedInstances.put(
        new StrictEqualityEventWrapper(obtainedInstance), Thread.currentThread().getStackTrace());
    return obtainedInstance;
  }

  @Implementation
  public static AccessibilityEvent obtain(int eventType) {
    // We explicitly avoid allocating the AccessibilityEvent from the actual pool by using
    // the private constructor. Not doing so affects test suites which use both shadow and
    // non-shadow objects.
    final AccessibilityEvent obtainedInstance =
        ReflectionHelpers.callConstructor(AccessibilityEvent.class);
    final ShadowAccessibilityEvent shadowObtained =
        ((ShadowAccessibilityEvent) ShadowExtractor.extract(obtainedInstance));

    obtainedInstances.put(
        new StrictEqualityEventWrapper(obtainedInstance), Thread.currentThread().getStackTrace());
    shadowObtained.eventType = eventType;
    return obtainedInstance;
  }

  @Implementation
  public static AccessibilityEvent obtain() {
    return obtain(0);
  }

  /**
   * Check for leaked objects that were {@code obtain}ed but never {@code recycle}d.
   * @param printUnrecycledEventsToSystemErr - if true, stack traces of calls to {@code obtain}
   * that lack matching calls to {@code recycle} are dumped to System.err.
   * @return {@code true} if there are unrecycled events
   */
  public static boolean areThereUnrecycledEvents(boolean printUnrecycledEventsToSystemErr) {
    if (printUnrecycledEventsToSystemErr) {
      for (final StrictEqualityEventWrapper wrapper : obtainedInstances.keySet()) {
        final ShadowAccessibilityNodeInfo shadow =
            ((ShadowAccessibilityNodeInfo) ShadowExtractor.extract(wrapper.mEvent));

        System.err.println(String.format(
            "Leaked AccessibilityEvent. Stack trace of allocation:",
            shadow.getContentDescription()));
        for (final StackTraceElement stackTraceElement : obtainedInstances.get(wrapper)) {
          System.err.println(stackTraceElement.toString());
        }
      }
    }

    return (obtainedInstances.size() != 0);
  }

  /**
   * Clear list of obtained instance objects. {@code areThereUnrecycledNodes} will always
   * return false if called immediately afterwards.
   */
  public static void resetObtainedInstances() {
    obtainedInstances.clear();
  }

  @Implementation
  public void recycle() {
    final StrictEqualityEventWrapper wrapper =
        new StrictEqualityEventWrapper(realAccessibilityEvent);
    if (!obtainedInstances.containsKey(wrapper)) {
      throw new IllegalStateException();
    }

    obtainedInstances.remove(wrapper);
  }

  @Implementation
  public void setEventType(int type) {
    eventType = type;
  }

  @Implementation
  public int getEventType() {
    return eventType;
  }

  @Implementation
  public void setContentDescription(CharSequence description) {
    contentDescription = description;
  }

  @Implementation
  public CharSequence getContentDescription() {
    return contentDescription;
  }

  @Implementation
  public void setPackageName(CharSequence name) {
    packageName = name;
  }

  @Implementation
  public CharSequence getPackageName() {
    return packageName;
  }

  @Implementation
  public void setClassName(CharSequence name) {
    className = name;
  }

  @Implementation
  public CharSequence getClassName() {
    return className;
  }

  @Implementation
  public void setEnabled(boolean value) {
    enabled = value;
  }

  @Implementation
  public boolean isEnabled() {
    return enabled;
  }

  @Override
  @Implementation
  public boolean equals(Object object) {
    if (!(object instanceof AccessibilityEvent)) {
      return false;
    }

    final AccessibilityEvent event = (AccessibilityEvent) object;
    final ShadowAccessibilityEvent otherShadow =
        (ShadowAccessibilityEvent) ShadowExtractor.extract(event);

    boolean areEqual = (eventType == otherShadow.eventType);
    areEqual &= (enabled == otherShadow.enabled);
    areEqual &= TextUtils.equals(contentDescription, otherShadow.contentDescription);
    areEqual &= TextUtils.equals(packageName, otherShadow.packageName);
    areEqual &= TextUtils.equals(className, otherShadow.className);

    return areEqual;
  }

  @Override
  public int hashCode() {
    // This is 0 for a reason. If you change it, you will break the obtained instances map in
    // a manner that is remarkably difficult to debug. Having a dynamic hash code keeps this
    // object from being located in the map if it was mutated after being obtained.
    return 0;
  }

  /**
   * @return A shallow copy.
   */
  private AccessibilityEvent getClone() {
    // We explicitly avoid allocating the AccessibilityEvent from the actual pool by using
    // the private constructor. Not doing so affects test suites which use both shadow and
    // non-shadow objects.
    final AccessibilityEvent newEvent = ReflectionHelpers.callConstructor(AccessibilityEvent.class);
    final ShadowAccessibilityEvent newShadow =
        (ShadowAccessibilityEvent) ShadowExtractor.extract(newEvent);

    newShadow.eventType = eventType;
    newShadow.contentDescription = contentDescription;
    newShadow.packageName = packageName;
    newShadow.className = className;
    newShadow.enabled = enabled;

    return newEvent;
  }

  /**
   * Private class to keep different events straight in the mObtainedInstances map.
   */
  private static class StrictEqualityEventWrapper {
    public final AccessibilityEvent mEvent;

    public StrictEqualityEventWrapper(AccessibilityEvent event) {
      mEvent = event;
    }

    @Override
    public boolean equals(Object object) {
      if (object == null) {
        return false;
      }

      final StrictEqualityEventWrapper wrapper = (StrictEqualityEventWrapper) object;
      return mEvent == wrapper.mEvent;
    }

    @Override
    public int hashCode() {
      return mEvent.hashCode();
    }
  }
}
