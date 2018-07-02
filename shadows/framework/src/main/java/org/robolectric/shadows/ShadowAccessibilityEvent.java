package org.robolectric.shadows;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.accessibility.AccessibilityEvent;
import java.util.HashMap;
import java.util.Map;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;

/**
 * Shadow of {@link android.view.accessibility.AccessibilityEvent}.
 */
@Implements(AccessibilityEvent.class)
public class ShadowAccessibilityEvent extends ShadowAccessibilityRecord {
  // Map of obtained instances of the class along with stack traces of how they were obtained
  private static final Map<StrictEqualityEventWrapper, StackTraceElement[]> obtainedInstances =
      new HashMap<>();

  private static final SparseArray<StrictEqualityEventWrapper> orderedInstances = new SparseArray<>();

  private static int sAllocationCount = 0;
  private int eventType;
  private CharSequence packageName;

  @RealObject
  private AccessibilityEvent realAccessibilityEvent;

  @Implementation
  public void __constructor__() {
    ReflectionHelpers.setStaticField(AccessibilityEvent.class, "CREATOR", ShadowAccessibilityEvent.CREATOR);
  }

  public static final Parcelable.Creator<AccessibilityEvent> CREATOR =
      new Parcelable.Creator<AccessibilityEvent>() {

    @Override
    public AccessibilityEvent createFromParcel(Parcel source) {
      return obtain(orderedInstances.get(source.readInt()).mEvent);
    }

    @Override
    public AccessibilityEvent[] newArray(int size) {
      return new AccessibilityEvent[size];
    }};

  @Implementation
  public static AccessibilityEvent obtain(AccessibilityEvent event) {
    ShadowAccessibilityEvent shadowEvent = Shadow.extract(event);
    AccessibilityEvent obtainedInstance = shadowEvent.getClone();

    sAllocationCount++;
    StrictEqualityEventWrapper wrapper = new StrictEqualityEventWrapper(obtainedInstance);
    obtainedInstances.put(wrapper, Thread.currentThread().getStackTrace());
    orderedInstances.put(sAllocationCount, wrapper);
    return obtainedInstance;
  }

  @Implementation
  public static AccessibilityEvent obtain(int eventType) {
    // We explicitly avoid allocating the AccessibilityEvent from the actual pool by using
    // the private constructor. Not doing so affects test suites which use both shadow and
    // non-shadow objects.
    final AccessibilityEvent obtainedInstance =
        ReflectionHelpers.callConstructor(AccessibilityEvent.class);
    final ShadowAccessibilityEvent shadowObtained = Shadow.extract(obtainedInstance);

    sAllocationCount++;
    StrictEqualityEventWrapper wrapper = new StrictEqualityEventWrapper(obtainedInstance);
    obtainedInstances.put(wrapper, Thread.currentThread().getStackTrace());
    orderedInstances.put(sAllocationCount, wrapper);
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
        final ShadowAccessibilityEvent shadow = Shadow.extract(wrapper.mEvent);

        System.err.println(String.format(
            "Leaked AccessibilityEvent type: %d. Stack trace of allocation:",
            shadow.getEventType()));
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
    orderedInstances.clear();
  }

  @Implementation
  public void recycle() {
    final StrictEqualityEventWrapper wrapper =
        new StrictEqualityEventWrapper(realAccessibilityEvent);
    if (!obtainedInstances.containsKey(wrapper)) {
      throw new IllegalStateException();
    }

    obtainedInstances.remove(wrapper);
    int keyOfWrapper = -1;
    for (int i = 0; i < orderedInstances.size(); i++) {
      int key = orderedInstances.keyAt(i);
      if (orderedInstances.get(key).equals(wrapper)) {
        keyOfWrapper = key;
        break;
      }
    }
    orderedInstances.remove(keyOfWrapper);
    setParcelableData(null);
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
  public void setPackageName(CharSequence name) {
    packageName = name;
  }

  @Implementation
  public CharSequence getPackageName() {
    return packageName;
  }

  @Override
  @Implementation
  public boolean equals(Object object) {
    if (!(object instanceof AccessibilityEvent)) {
      return false;
    }

    final AccessibilityEvent event = (AccessibilityEvent) object;
    final ShadowAccessibilityEvent otherShadow = Shadow.extract(event);

    boolean areEqual = (eventType == otherShadow.eventType);
    areEqual &= (enabled == otherShadow.enabled);
    areEqual &= TextUtils.equals(contentDescription, otherShadow.contentDescription);
    areEqual &= TextUtils.equals(packageName, otherShadow.packageName);
    areEqual &= TextUtils.equals(className, otherShadow.className);
    boolean parcelableDataEqual = false;
    if (getParcelableData() == null && otherShadow.getParcelableData() == null){
      parcelableDataEqual = true;
    } else if (getParcelableData().equals(otherShadow.getParcelableData())) {
      parcelableDataEqual = true;
    }
    areEqual &= parcelableDataEqual;

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
    final ShadowAccessibilityEvent newShadow = Shadow.extract(newEvent);

    newShadow.eventType = eventType;
    newShadow.contentDescription = contentDescription;
    newShadow.packageName = packageName;
    newShadow.className = className;
    newShadow.enabled = enabled;
    newShadow.setParcelableData(getParcelableData());

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

  @Implementation
  public int describeContents() {
    return 0;
  }

  @Implementation
  public void writeToParcel(Parcel dest, int flags) {
    StrictEqualityEventWrapper wrapper = new StrictEqualityEventWrapper(realAccessibilityEvent);
    int keyOfWrapper = -1;
    for (int i = 0; i < orderedInstances.size(); i++) {
      if (orderedInstances.valueAt(i).equals(wrapper)) {
        keyOfWrapper = orderedInstances.keyAt(i);
        break;
      }
    }
    dest.writeInt(keyOfWrapper);
  }
}
