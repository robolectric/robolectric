package org.robolectric.shadows;

import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;

import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.UserHandle;
import android.os.storage.StorageVolume;
import java.io.File;
import org.robolectric.util.ReflectionHelpers;

/** Class to build {@link StorageVolume} */
public final class StorageVolumeBuilder {

  private String id;
  private int storageId = 0;
  private File path;
  private String description;
  private boolean primary = true;
  private boolean removable = false;
  private boolean emulated = false;
  private long mtpReserveSize = 00L;
  private boolean allowMassStorage = false;
  private long maxFileSize = 100L;
  private UserHandle owner;
  private String fsUuid = "4213-3435:";
  private String state;

  public StorageVolumeBuilder(
      String id, File path, String description, UserHandle owner, String state) {
    this.id = id;
    this.path = path;
    this.description = description;
    this.owner = owner;
    this.state = state;
  }

  public void setStorageId(int storageId) {
    this.storageId = storageId;
  }

  public void setIsPrimary(boolean isPrimary) {
    this.primary = isPrimary;
  }

  public void setIsRemovable(boolean isRemovable) {
    this.removable = isRemovable;
  }

  public void setIsEmulated(boolean isEmulated) {
    this.emulated = isEmulated;
  }

  public void setMtpReserveSize(long mtpReserveSize) {
    this.mtpReserveSize = mtpReserveSize;
  }

  public void setAllowMassStorage(boolean allowMassStorage) {
    this.allowMassStorage = allowMassStorage;
  }

  public void setMaxFileSize(long maxFileSize) {
    this.maxFileSize = maxFileSize;
  }

  public StorageVolume build() throws IllegalStateException {
    if (Build.VERSION.SDK_INT >= VERSION_CODES.N && Build.VERSION.SDK_INT < VERSION_CODES.P) {
      return ReflectionHelpers.callConstructor(
          StorageVolume.class,
          from(String.class, id), // String id,
          from(int.class, storageId), // int storageId
          from(File.class, path), // File path,
          from(String.class, description), // String description
          from(boolean.class, primary), // boolean primary,
          from(boolean.class, removable), // boolean removable,
          from(boolean.class, emulated), // boolean emulated,
          from(long.class, mtpReserveSize), //  long mtpReserveSize,
          from(boolean.class, allowMassStorage),
          from(long.class, maxFileSize), // long maxFileSize,
          from(UserHandle.class, owner), // UserHandle owner,
          from(String.class, fsUuid), //  String fsUuid,
          from(String.class, state)); // String state
    } else if (Build.VERSION.SDK_INT >= VERSION_CODES.P) {
      return ReflectionHelpers.callConstructor(
          StorageVolume.class,
          from(String.class, id), // String id,
          from(File.class, path), // File path,
          from(File.class, path), // File internalPath
          from(String.class, description), // String description
          from(boolean.class, primary), // boolean primary,
          from(boolean.class, removable), // boolean removable,
          from(boolean.class, emulated), // boolean emulated,
          from(boolean.class, allowMassStorage), //  boolean allowMassStorage,
          from(long.class, maxFileSize), // long maxFileSize,
          from(UserHandle.class, owner), // UserHandle owner,
          from(String.class, fsUuid), //  String fsUuid,
          from(String.class, state)); // String state
    }
    throw new IllegalStateException("StorageVolume hidden constructor not found");
  }
}
