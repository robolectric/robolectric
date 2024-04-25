package org.robolectric.shadows;

import android.annotation.Nullable;
import android.app.backup.BackupDataOutput;
import com.google.common.collect.ImmutableList;
import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.ReflectorObject;
import org.robolectric.res.android.NativeObjRegistry;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/** Shadow for BackupDataOutput. */
@Implements(value = BackupDataOutput.class)
public class ShadowBackupDataOutput {

  protected static final String KEY_PREFIX_JOINER = ":";

  private static final NativeObjRegistry<NativeBackupDataOutput> nativeObjectRegistry =
      new NativeObjRegistry<>(NativeBackupDataOutput.class);

  @ReflectorObject private BackupDataOutputReflector backupDataOutputReflector;

  /** Gets a list of all data written to the {@link BackupDataOutput}. */
  public ImmutableList<BackupDataEntity> getEntities() {
    return ImmutableList.copyOf(
        nativeObjectRegistry.getNativeObject(backupDataOutputReflector.getBackupWriter()).entities);
  }

  @Implementation
  protected static int writeEntityHeader_native(long mBackupWriter, String key, int dataSize) {
    NativeBackupDataOutput nativeObject = nativeObjectRegistry.getNativeObject(mBackupWriter);

    if (nativeObject.currentEntity != null
        && nativeObject.currentBytesWritten < nativeObject.currentEntity.dataSize()) {
      // Return failed due to write due to unfinished previous record.
      return -1;
    }

    String prefixedKey =
        nativeObject.keyPrefix != null ? nativeObject.keyPrefix + KEY_PREFIX_JOINER + key : key;
    if (dataSize >= 0) {
      nativeObject.currentEntity = BackupDataEntity.create(prefixedKey, new byte[dataSize]);
    } else {
      nativeObject.currentEntity = BackupDataEntity.createDeletedEntity(prefixedKey);
    }
    nativeObject.currentBytesWritten = 0;

    nativeObject.entities.add(nativeObject.currentEntity);

    // Return bytes written (1 byte per char in key plus 1 for the size int).
    return key.length() + 1;
  }

  @Implementation
  protected static int writeEntityData_native(long mBackupWriter, byte[] data, int size) {
    NativeBackupDataOutput nativeObject = nativeObjectRegistry.getNativeObject(mBackupWriter);

    if (nativeObject.currentEntity == null) {
      // Return error writing due to missing header.
      return -1;
    }

    if (size > data.length
        || nativeObject.currentBytesWritten + size > nativeObject.currentEntity.dataSize()) {
      // Return error writing due to size exceeding of one of the arrays.
      return -1;
    }

    System.arraycopy(
        data, 0, nativeObject.currentEntity.data(), nativeObject.currentBytesWritten, size);
    nativeObject.currentBytesWritten += size;

    return size;
  }

  @Implementation
  protected static void setKeyPrefix_native(long mBackupWriter, String keyPrefix) {
    nativeObjectRegistry.getNativeObject(mBackupWriter).keyPrefix = keyPrefix;
  }

  @Implementation
  protected static long ctor(FileDescriptor fd) {
    return nativeObjectRegistry.register(new NativeBackupDataOutput());
  }

  @Implementation
  protected static void dtor(long mBackupWriter) {
    nativeObjectRegistry.unregister(mBackupWriter);
  }

  @ForType(BackupDataOutput.class)
  private interface BackupDataOutputReflector {

    @Accessor("mBackupWriter")
    long getBackupWriter();
  }

  private static final class NativeBackupDataOutput {
    final List<BackupDataEntity> entities = new ArrayList<>();
    @Nullable String keyPrefix = null;
    @Nullable BackupDataEntity currentEntity = null;
    int currentBytesWritten = 0;
  }
}
