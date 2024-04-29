package org.robolectric.shadows;

import static java.lang.Math.min;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.app.backup.BackupDataInput;
import com.google.common.collect.ImmutableList;
import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/** Shadow for BackupDataInput. */
@Implements(value = BackupDataInput.class, looseSignatures = true)
public class ShadowBackupDataInput {

  private List<BackupDataEntity> entities = new ArrayList<>();
  private int currentEntityIndex = -1;
  private int currentBytesRead = 0;
  private int currentBytesToRead = 0;

  /**
   * Sets the entities to return when reading from this {@link BackupDataInput}. Use {@link
   * org.robolectric.shadows.BackupDataInputBuilder} to get a populated instance.
   */
  void setEntities(ImmutableList<BackupDataEntity> entities) {
    this.entities = entities;
  }

  @Implementation
  protected static long ctor(FileDescriptor fd) {
    // Return value greater than 0 to indicate successful allocation of backup reader. The real
    // implementation would return an allocated pointer address, but since the methods are not
    // static, we do not need a native object in this shadow implementation, and can return a fixed
    // value instead.
    return 1;
  }

  // Using loose signature because EntityHeader is a private nested class.
  @Implementation
  protected int readNextHeader_native(Object backupReader, Object entity) {
    if (currentBytesRead < currentBytesToRead) {
      // Return failure to read header due to unread data bytes.
      return -1;
    }

    currentEntityIndex++;

    if (currentEntityIndex >= entities.size()) {
      // Return end of backup input data.
      return 1;
    }

    BackupDataEntity shadowEntity = entities.get(currentEntityIndex);

    currentBytesRead = 0;
    currentBytesToRead = shadowEntity.dataSize();

    // Accessing using reflection because EntityHeader is a private nested class.
    reflector(EntityHeaderReflector.class, entity).setKey(shadowEntity.key());
    reflector(EntityHeaderReflector.class, entity).setDataSize(shadowEntity.dataSize());
    return 0;
  }

  @Implementation
  protected int readEntityData_native(long backupReader, byte[] data, int offset, int size) {
    if (currentEntityIndex >= entities.size() || currentBytesRead >= currentBytesToRead) {
      // Return end of data.
      return 0;
    }

    if (offset + size > data.length) {
      // Return error reading data.
      return -1;
    }

    byte[] shadowData = entities.get(currentEntityIndex).data();
    int remainingBytes = currentBytesToRead - currentBytesRead;
    int bytesToRead = min(size, remainingBytes);

    System.arraycopy(shadowData, currentBytesRead, data, offset, bytesToRead);
    currentBytesRead += bytesToRead;
    return bytesToRead;
  }

  @Implementation
  protected int skipEntityData_native(long backupReader) {
    if (currentEntityIndex < entities.size()) {
      currentBytesRead = entities.get(currentEntityIndex).dataSize();
    }
    return 0;
  }

  @ForType(className = "android.app.backup.BackupDataInput$EntityHeader")
  private interface EntityHeaderReflector {

    @Accessor("key")
    void setKey(String key);

    @Accessor("dataSize")
    void setDataSize(int dataSize);
  }
}
