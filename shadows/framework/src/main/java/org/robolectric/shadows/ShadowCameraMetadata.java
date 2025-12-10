package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.N_MR1;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

/** Shadow for {@link CameraMetadata}. */
@Implements(CameraMetadata.class)
public class ShadowCameraMetadata<TKey> {

  /**
   * The Android framework implementation of this method has a bug that caused it to include the
   * __robo_data__ field in the list of keys. This bug is fixed starting in SDK 37.
   *
   * <p>TODO(hoisie): Remove this shadow when PREINSTRUMENTED_VERSION is updated to 8 or above.
   */
  @Implementation(minSdk = Q)
  protected ArrayList<TKey> getKeys(
      Class<?> type,
      Class<TKey> keyClass,
      CameraMetadata<TKey> instance,
      int[] filterTags,
      boolean includeSynthetic) {

    return doGetKeys(type, keyClass, instance, filterTags, includeSynthetic);
  }

  @Implementation(minSdk = O, maxSdk = P)
  protected ArrayList<TKey> getKeys(
      Class<?> type, Class<TKey> keyClass, CameraMetadata<TKey> instance, int[] filterTags) {
    return doGetKeys(type, keyClass, instance, filterTags, /* includeSynthetic= */ false);
  }

  @Implementation(maxSdk = N_MR1)
  protected static <TKey> ArrayList<TKey> getKeysStatic(
      Class<?> type, Class<TKey> keyClass, CameraMetadata<TKey> instance, int[] filterTags) {
    return doGetKeys(type, keyClass, instance, filterTags, /* includeSynthetic= */ false);
  }

  private static <TKey> ArrayList<TKey> doGetKeys(
      Class<?> type,
      Class<TKey> keyClass,
      CameraMetadata<TKey> instance,
      int[] filterTags,
      boolean includeSynthetic) {

    if (type.equals(TotalCaptureResult.class)) {
      type = CaptureResult.class;
    }

    if (filterTags != null) {
      Arrays.sort(filterTags);
    }

    ArrayList<TKey> keyList = new ArrayList<TKey>();

    Field[] fields = type.getDeclaredFields();
    for (Field field : fields) {
      // Filter for Keys that are public and are of type TKey or a subclass of TKey.
      if (keyClass.isAssignableFrom(field.getType())
          && Modifier.isPublic(field.getModifiers())
          && !field.isSynthetic()) {

        TKey key;
        try {
          key = (TKey) field.get(instance);
        } catch (IllegalAccessException e) {
          throw new AssertionError("Can't get IllegalAccessException", e);
        } catch (IllegalArgumentException e) {
          throw new AssertionError("Can't get IllegalArgumentException", e);
        }

        if (instance == null
            || reflector(CameraMetadataReflector.class, instance).getProtected(key) != null) {
          if (shouldKeyBeAddedImpl(key, field, filterTags, includeSynthetic)) {
            keyList.add(key);
          }
        }
      }
    }
    return keyList;
  }

  private static <TKey> boolean shouldKeyBeAddedImpl(
      TKey key, Field field, int[] filterTags, boolean includeSynthetic) {
    if (RuntimeEnvironment.getApiLevel() >= Q) {
      return reflector(CameraMetadataReflector.class)
          .shouldKeyBeAdded(key, field, filterTags, includeSynthetic);
    } else {
      return reflector(CameraMetadataReflector.class).shouldKeyBeAdded(key, field, filterTags);
    }
  }

  @ForType(CameraMetadata.class)
  interface CameraMetadataReflector<TKey> {
    // SDK >= Q
    @Static
    boolean shouldKeyBeAdded(TKey key, Field field, int[] filterTags, boolean includeSynthetic);

    // SDK < Q
    @Static
    boolean shouldKeyBeAdded(TKey key, Field field, int[] filterTags);

    Object getProtected(TKey key);
  }
}
