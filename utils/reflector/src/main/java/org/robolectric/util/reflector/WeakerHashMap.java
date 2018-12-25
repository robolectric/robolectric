package org.robolectric.util.reflector;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Values are held via {@link WeakReference}, so if it becomes otherwise unreachable it can be
 * garbage collected.
 */
class WeakerHashMap<K, V> implements Map<K, V> {

  public final Map<K, WeakReference<V>> map = new WeakHashMap<>();

  @Override
  public int size() {
    return map.size();
  }

  @Override
  public boolean isEmpty() {
    return map.isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    return map.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public V get(Object key) {
    WeakReference<V> ref = map.get(key);
    if (ref != null) {
      V v = ref.get();
      if (v == null) {
        map.remove(key);
      }
      return v;
    }
    return null;
  }

  @Override
  public V put(K key, V value) {
    WeakReference<V> oldV = map.put(key, new WeakReference<>(value));
    return oldV == null ? null : oldV.get();
  }

  @Override
  public V remove(Object key) {
    WeakReference<V> oldV = map.remove(key);
    return oldV == null ? null : oldV.get();
  }

  @Override
  public void putAll(Map<? extends K, ? extends V> m) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clear() {
    map.clear();
  }

  @Override
  public Set<K> keySet() {
    return map.keySet();
  }

  @Override
  public Collection<V> values() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Set<Entry<K, V>> entrySet() {
    throw new UnsupportedOperationException();
  }
}
