package org.robolectric.res.android;

public class Ref<T> {

  private T t;

  public Ref(T t) {
    this.t = t;
  }

  public T get() {
    return t;
  }

  public void set(T t) {
    this.t = t;
  }

  @Override
  public String toString() {
    return "Ref<" + t + '>';
  }
}
