package org.robolectric.shadows;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.auto.value.AutoValue;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Represents a key value pair in {@link ShadowBackupDataInput} and {@link ShadowBackupDataOutput}.
 */
@AutoValue
public abstract class BackupDataEntity {

  /** The header key for a backup entity. */
  public abstract String key();

  /** The size of data in a backup entity. */
  public abstract int dataSize();

  /** The byte array of data in a backup entity. */
  @SuppressWarnings("mutable")
  public abstract byte[] data();

  /**
   * Constructs a new entity with the given key but a negative size. This represents a deleted pair.
   */
  public static BackupDataEntity createDeletedEntity(String key) {
    return new AutoValue_BackupDataEntity(
        checkNotNull(key), /* dataSize= */ -1, /* data= */ new byte[0]);
  }

  /**
   * Constructs a pair with a string value. The value will be converted to a byte array in {@link
   * StandardCharsets#UTF_8}.
   */
  public static BackupDataEntity create(String key, String data) {
    return create(key, data.getBytes(UTF_8));
  }

  /** Constructs a new entity where the size of the value is the entire array. */
  public static BackupDataEntity create(String key, byte[] data) {
    return create(key, data, data.length);
  }

  /**
   * Constructs a new entity.
   *
   * @param key the key of the pair
   * @param data the value to associate with the key
   * @param dataSize the length of the value in bytes
   */
  public static BackupDataEntity create(String key, byte[] data, int dataSize) {
    return new AutoValue_BackupDataEntity(
        checkNotNull(key), dataSize, Arrays.copyOf(data, dataSize));
  }
}
