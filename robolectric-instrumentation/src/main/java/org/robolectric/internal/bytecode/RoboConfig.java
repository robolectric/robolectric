package org.robolectric.internal.bytecode;

public @interface RoboConfig {
  Class<?>[] shadows();
}
