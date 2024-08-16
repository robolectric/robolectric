package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import android.hardware.Sensor;
import android.hardware.SensorDirectChannel;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorEventListener2;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.os.MemoryFile;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.robolectric.annotation.ClassName;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

@Implements(value = SensorManager.class)
public class ShadowSensorManager {
  public static boolean forceListenersToFail = false;
  private static final Multimap<Integer, Sensor> sensorMap =
      Multimaps.synchronizedMultimap(HashMultimap.create());
  private static final Multimap<SensorEventListener, Sensor> listeners =
      Multimaps.synchronizedMultimap(HashMultimap.create());

  @RealObject private SensorManager realObject;

  @Resetter
  public static void reset() {
    sensorMap.clear();
    listeners.clear();
    forceListenersToFail = false;
  }

  /**
   * Provide a Sensor for the indicated sensor type.
   *
   * @param sensorType from Sensor constants
   * @param sensor Sensor instance
   * @deprecated Use {@link ShadowSensor#newInstance(int)} to construct your {@link Sensor} and add
   *     to the {@link SensorManager} using {@link #addSensor(Sensor)} instead. This method will be
   *     removed at some point allowing us to use more of the real {@link SensorManager} code.
   */
  @Deprecated
  public void addSensor(int sensorType, Sensor sensor) {
    checkNotNull(sensor);
    sensorMap.put(sensorType, sensor);
  }

  /** Adds a {@link Sensor} to the {@link SensorManager} */
  public void addSensor(Sensor sensor) {
    checkNotNull(sensor);
    sensorMap.put(sensor.getType(), sensor);
  }

  public void removeSensor(Sensor sensor) {
    checkNotNull(sensor);
    sensorMap.get(sensor.getType()).remove(sensor);
  }

  @Implementation
  protected Sensor getDefaultSensor(int type) {
    Collection<Sensor> sensorsForType = sensorMap.get(type);
    if (sensorsForType.isEmpty()) {
      return null;
    }

    return ((Sensor) sensorsForType.toArray()[0]);
  }

  @Implementation
  public List<Sensor> getSensorList(int type) {
    if (type == Sensor.TYPE_ALL) {
      return ImmutableList.copyOf(sensorMap.values());
    }

    return ImmutableList.copyOf(sensorMap.get(type));
  }

  /**
   * @param handler is ignored.
   */
  @Implementation
  protected boolean registerListener(
      SensorEventListener listener, Sensor sensor, int rate, Handler handler) {
    return registerListener(listener, sensor, rate);
  }

  /**
   * @param maxLatency is ignored.
   */
  @Implementation
  protected boolean registerListener(
      SensorEventListener listener, Sensor sensor, int rate, int maxLatency) {
    return registerListener(listener, sensor, rate);
  }

  /**
   * @param maxLatency is ignored.
   * @param handler is ignored
   */
  @Implementation
  protected boolean registerListener(
      SensorEventListener listener, Sensor sensor, int rate, int maxLatency, Handler handler) {
    return registerListener(listener, sensor, rate);
  }

  @Implementation
  protected boolean registerListener(SensorEventListener listener, Sensor sensor, int rate) {
    if (forceListenersToFail) {
      return false;
    }
    listeners.put(listener, sensor);
    return true;
  }

  @Implementation
  protected void unregisterListener(SensorEventListener listener, Sensor sensor) {
    listeners.remove(listener, sensor);
  }

  @Implementation
  protected void unregisterListener(SensorEventListener listener) {
    listeners.removeAll(listener);
  }

  /** Tests if the sensor manager has a registration for the given listener. */
  public boolean hasListener(SensorEventListener listener) {
    return listeners.containsKey(listener);
  }

  /** Tests if the sensor manager has a registration for the given listener for the given sensor. */
  public boolean hasListener(SensorEventListener listener, Sensor sensor) {
    return listeners.containsEntry(listener, sensor);
  }

  /**
   * Returns the list of {@link SensorEventListener}s registered on this SensorManager. Note that
   * the list is unmodifiable, any attempt to modify it will throw an exception.
   */
  public List<SensorEventListener> getListeners() {
    return ImmutableList.copyOf(listeners.keySet());
  }

  /** Propagates the {@code event} to all registered listeners. */
  public void sendSensorEventToListeners(SensorEvent event) {
    for (SensorEventListener listener : getListeners()) {
      listener.onSensorChanged(event);
    }
  }

  /** Propagates the {@code event} to only registered listeners of the given sensor. */
  @SuppressWarnings("JdkCollectors") // toImmutableList is only supported in Java 8+.
  public void sendSensorEventToListeners(SensorEvent event, Sensor sensor) {
    List<SensorEventListener> listenersRegisteredToSensor =
        listeners.entries().stream()
            .filter(entry -> entry.getValue() == sensor)
            .map(Entry::getKey)
            .collect(Collectors.toList());

    for (SensorEventListener listener : listenersRegisteredToSensor) {
      listener.onSensorChanged(event);
    }
  }

  @Implementation
  protected boolean flush(SensorEventListener listener) {
    // ShadowSensorManager doesn't queue up any sensor events, so nothing actually needs to be
    // flushed. Just call onFlushCompleted for each sensor that would have been flushed.
    new Handler(Looper.getMainLooper())
        .post(
            () -> {
              // Go through each sensor that the listener is registered for, and call
              // onFlushCompleted on each listener registered for that sensor.
              for (Sensor sensor : listeners.get(listener)) {
                for (SensorEventListener registeredListener : getListeners()) {
                  if ((registeredListener instanceof SensorEventListener2)
                      && listeners.containsEntry(registeredListener, sensor)) {
                    ((SensorEventListener2) registeredListener).onFlushCompleted(sensor);
                  }
                }
              }
            });
    return listeners.containsKey(listener);
  }

  /**
   * @deprecated Use {@code {@link SensorEventBuilder#newBuilder()}} instead.
   */
  @Deprecated
  public SensorEvent createSensorEvent() {
    return ReflectionHelpers.callConstructor(SensorEvent.class);
  }

  /**
   * Creates a {@link SensorEvent} with the given value array size, which the caller should set
   * based on the type of {@link Sensor} which is being emulated.
   *
   * <p>Callers can then specify individual values for the event. For example, for a proximity event
   * a caller may wish to specify the distance value:
   *
   * <pre>{@code
   * event.values[0] = distance;
   * }</pre>
   *
   * <p>See {@link SensorEvent#values} for more information about values.
   *
   * @deprecated Use {@code {@link SensorEventBuilder#newBuilder()}} instead.
   */
  @Deprecated
  public static SensorEvent createSensorEvent(int valueArraySize) {
    return createSensorEvent(valueArraySize, Sensor.TYPE_GRAVITY);
  }

  /**
   * Creates a {@link SensorEvent} for the given {@link Sensor} type with the given value array
   * size, which the caller should set based on the type of sensor which is being emulated.
   *
   * <p>Callers can then specify individual values for the event. For example, for a proximity event
   * a caller may wish to specify the distance value:
   *
   * <pre>{@code
   * event.values[0] = distance;
   * }</pre>
   *
   * <p>See {@link SensorEvent#values} for more information about values.
   *
   * @deprecated Use {@code {@link SensorEventBuilder#newBuilder()}} instead.
   */
  @Deprecated
  public static SensorEvent createSensorEvent(int valueArraySize, int sensorType) {
    checkArgument(valueArraySize > 0);
    ClassParameter<Integer> valueArraySizeParam = new ClassParameter<>(int.class, valueArraySize);
    SensorEvent sensorEvent =
        ReflectionHelpers.callConstructor(SensorEvent.class, valueArraySizeParam);
    sensorEvent.sensor = ShadowSensor.newInstance(sensorType);
    return sensorEvent;
  }

  @Implementation(minSdk = O)
  protected @ClassName("android.hardware.SensorDirectChannel") Object createDirectChannel(
      MemoryFile mem) {
    return ReflectionHelpers.callConstructor(
        SensorDirectChannel.class,
        ClassParameter.from(SensorManager.class, realObject),
        ClassParameter.from(int.class, 0),
        ClassParameter.from(int.class, SensorDirectChannel.TYPE_MEMORY_FILE),
        ClassParameter.from(long.class, mem.length()));
  }
}
