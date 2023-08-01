package org.robolectric.shadows;

import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.app.backup.BackupDataInput;
import com.google.common.collect.ImmutableList;
import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.util.reflector.Constructor;
import org.robolectric.util.reflector.ForType;

/** Builder for a {@link BackupDataInput} object. */
public class BackupDataInputBuilder {

  private final List<BackupDataEntity> entities = new ArrayList<>();

  private BackupDataInputBuilder() {}

  /** Creates a new builder for {@link BackupDataInput}. */
  public static BackupDataInputBuilder newBuilder() {
    return new BackupDataInputBuilder();
  }

  /** Adds the given entity to the input. */
  public BackupDataInputBuilder addEntity(BackupDataEntity entity) {
    entities.add(entity);
    return this;
  }

  /** Builds the {@link BackupDataInput} instance with the added entities. */
  public BackupDataInput build() {
    BackupDataInput data =
        reflector(BackupDataInputReflector.class).newBackupDataInput(new FileDescriptor());
    shadowOf(data).setEntities(ImmutableList.copyOf(entities));
    return data;
  }

  @ForType(BackupDataInput.class)
  private interface BackupDataInputReflector {

    @Constructor
    BackupDataInput newBackupDataInput(FileDescriptor fd);
  }
}
