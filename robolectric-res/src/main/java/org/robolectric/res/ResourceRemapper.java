package org.robolectric.res;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ResourceRemapper {
  private Set<Class<?>> seenClasses = new HashSet<Class<?>>();
  private int nextInt = 0x70000000;

  public void remapRClass(Class<?> rClass) {
    if (seenClasses.add(rClass)) {
      Map<Integer, Integer> mappings = new HashMap<Integer, Integer>();

      for (Class<?> innerClass : rClass.getClasses()) {
        try {
          String section = innerClass.getSimpleName();

          if (!section.equals("styleable")) {
            // first renumber ints

            for (Field field : innerClass.getFields()) {
              int modifiers = field.getModifiers();
              if (!Modifier.isFinal(modifiers) && Modifier.isStatic(modifiers)) {
                if (field.getType() == int.class) {
                  int newValue = nextInt++;
                  mappings.put(field.getInt(null), newValue);
                  field.setAccessible(true);
                  field.set(null, newValue);
                }
              }
            }
          }

          if (section.equals("styleable")) {
            // then change styleable int[]'s
            for (Field field : innerClass.getFields()) {
              int modifiers = field.getModifiers();
              if (!Modifier.isFinal(modifiers) && Modifier.isStatic(modifiers)) {
                if (field.getType() == int[].class) {
                  field.setAccessible(true);
                  int[] intArray = (int[]) field.get(null);
                  for (int i = 0; i < intArray.length; i++) {
                    int oldValue = intArray[i];
                    intArray[i] = mappings.get(oldValue);
                  }
                }
              }
            }
          }
        } catch (IllegalAccessException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

}
