package org.robolectric.shadows;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.support.ArrayMap;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

@Implements(android.util.ArrayMap.class)
public class ShadowArrayMap<K, V> extends ArrayMap<K, V> {
  private final ArrayMap<K, V> values = new ArrayMap<>();

  @Implementation
  public boolean containsAll(Collection<?> collection) {
    return values.containsAll(collection);
  }

  @Implementation
  public void putAll(Map<? extends K, ? extends V> map) {
    values.putAll(map);
  }

  @Implementation
  public boolean removeAll(Collection<?> collection) {
    return values.removeAll(collection);
  }

  @Implementation
  public boolean retainAll(Collection<?> collection) {
    return values.retainAll(collection);
  }

  @Implementation
  public Set<Entry<K, V>> entrySet() {
    return values.entrySet();
  }

  @Implementation
  public Set<K> keySet() {
    return values.keySet();
  }

  @Implementation
  public Collection<V> values() {
    return values.values();
  }

  @Implementation
  public void clear() {
    values.clear();
  }

  @Implementation
  public void ensureCapacity(int minimumCapacity) {
    values.ensureCapacity(minimumCapacity);
  }

  @Implementation
  public boolean containsKey(Object key) {
    return values.containsKey(key);
  }

  @Implementation
  public int indexOfKey(Object key) {
    return values.indexOfKey(key);
  }

  @Implementation
  public boolean containsValue(Object value) {
    return values.containsValue(value);
  }

  @Implementation
  public V get(Object key) {
    return values.get(key);
  }

  @Implementation
  public K keyAt(int index) {
    return values.keyAt(index);
  }

  @Implementation
  public V valueAt(int index) {
    return values.valueAt(index);
  }

  @Implementation
  public V setValueAt(int index, V value) {
    return values.setValueAt(index, value);
  }

  @Implementation
  public boolean isEmpty() {
    return values.isEmpty();
  }

  @Implementation
  public V put(K key, V value) {
    return values.put(key, value);
  }

  @Implementation
  public void putAll(org.robolectric.support.SimpleArrayMap<? extends K, ? extends V> array) {
    values.putAll(array);
  }

  @Implementation
  public V remove(Object key) {
    return values.remove(key);
  }

  @Implementation
  public V removeAt(int index) {
    return values.removeAt(index);
  }

  @Implementation
  public int size() {
    return values.size();
  }

  @Implementation
  public boolean equals(Object object) {
    return values.equals(object);
  }

  @Implementation
  public int hashCode() {
    return values.hashCode();
  }

  @Implementation
  public String toString() {
    return values.toString();
  }
}
