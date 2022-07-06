package org.robolectric.res;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * This class rewrites application R class resource values from multiple input R classes to all have unique values
 * existing within the same ID space, i.e: no resource collisions. This replicates the behaviour of AAPT when building
 * the final APK.
 *
 * IDs are in the format:-
 *
 * 0x PPTTEEEE
 *
 * where:
 *
 * P is unique for the package
 * T is unique for the type
 * E is the entry within that type.
 */
class ResourceRemapper {

  private BiMap<String, Integer> resIds = HashBiMap.create();
  private ResourceIdGenerator resourceIdGenerator = new ResourceIdGenerator(0x7F);

  /**
   * @param primaryRClass - An R class (usually the applications) that can be assumed to have a complete set of IDs. If
   *                      this is provided then use the values from this class for re-writting all values in follow up
   *                      calls to {@link #remapRClass(Class)}. If it is not provided the ResourceRemapper will generate
   *                      its own unique non-conflicting IDs.
   */
  ResourceRemapper(Class<?> primaryRClass) {
    if (primaryRClass != null) {
      remapRClass(true, primaryRClass);
    }
  }

  void remapRClass(Class<?> rClass) {
    remapRClass(false, rClass);
  }

  /**
   * @param isPrimary - Only one R class can allow final values and that is the final R class for the application
   *                  that has had its resource id values generated to include all libraries in its dependency graph
   *                  and therefore will be the only R file with the complete set of IDs in a unique ID space so we
   *                  can assume to use the values from this class only. All other R files are partial R files for each
   *                  library and on non-Android aware build systems like Maven where library R files are not re-written
   *                  with the final R values we need to rewrite them ourselves.
   */
  private void remapRClass(boolean isPrimary, Class<?> rClass) {
    // Collect all the local attribute id -> name mappings. These are used when processing the stylables to look up
    // the reassigned values.
    Map<Integer, String> localAttributeIds = new HashMap<>();
    for (Class<?> aClass : rClass.getClasses()) {
      if (aClass.getSimpleName().equals("attr")) {
        for (Field field : aClass.getFields()) {
          try {
            localAttributeIds.put(field.getInt(null), field.getName());
          } catch (IllegalAccessException e) {
            throw new RuntimeException("Could not read attr value for " + field.getName(), e);
          }
        }
      }
    }

    for (Class<?> innerClass : rClass.getClasses()) {
      String resourceType = innerClass.getSimpleName();
      if (!resourceType.startsWith("styleable")) {
        for (Field field : innerClass.getFields()) {
          try {
            if (!isPrimary && Modifier.isFinal(field.getModifiers())) {
              throw new IllegalArgumentException(rClass + " contains final fields, these will be inlined by the compiler and cannot be remapped.");
            }

            String resourceName = resourceType + "/" + field.getName();
            Integer value = resIds.get(resourceName);
            if (value != null) {
              field.setAccessible(true);
              field.setInt(null, value);
              resourceIdGenerator.record(field.getInt(null), resourceType, field.getName());
            } else if (resIds.containsValue(field.getInt(null))) {
              int remappedValue = resourceIdGenerator.generate(resourceType, field.getName());
              field.setInt(null, remappedValue);
              resIds.put(resourceName, remappedValue);
            } else {
              if (isPrimary) {
                resourceIdGenerator.record(field.getInt(null), resourceType, field.getName());
                resIds.put(resourceName, field.getInt(null));
              } else {
                int remappedValue = resourceIdGenerator.generate(resourceType, field.getName());
                field.setInt(null, remappedValue);
                resIds.put(resourceName, remappedValue);
              }
            }
          } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
          }
        }
      }
    }

    // Reassign the ids in the style arrays accordingly.
    for (Class<?> innerClass : rClass.getClasses()) {
      String resourceType = innerClass.getSimpleName();
      if (resourceType.startsWith("styleable")) {
        for (Field field : innerClass.getFields()) {
          if (field.getType().equals(int[].class)) {
            try {
              int[] styleableArray = (int[]) (field.get(null));
              for (int k = 0; k < styleableArray.length; k++) {
                Integer value = resIds.get("attr/" + localAttributeIds.get(styleableArray[k]));
                if (value != null) {
                  styleableArray[k] = value;
                }
              }
            } catch (IllegalAccessException e) {
              throw new IllegalStateException(e);
            }
          }
        }
      }
    }
  }
}
