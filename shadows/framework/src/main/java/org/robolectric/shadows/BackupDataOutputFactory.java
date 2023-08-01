package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.app.backup.BackupDataOutput;
import android.os.Build.VERSION_CODES;
import androidx.annotation.RequiresApi;
import java.io.FileDescriptor;
import org.robolectric.util.reflector.Constructor;
import org.robolectric.util.reflector.ForType;

/** Factory for instances of {@link BackupDataOutput}. */
public class BackupDataOutputFactory {

  private BackupDataOutputFactory() {}

  /** Returns a new instance of {@link BackupDataOutput}. */
  public static BackupDataOutput newInstance() {
    return reflector(BackupDataOutputReflector.class).newBackupDataOutput(new FileDescriptor());
  }

  /** Returns a new instance of {@link BackupDataOutput}. */
  @RequiresApi(VERSION_CODES.O)
  public static BackupDataOutput newInstance(long quota) {
    return reflector(BackupDataOutputReflector.class)
        .newBackupDataOutput(new FileDescriptor(), quota);
  }

  /** Returns a new instance of {@link BackupDataOutput}. */
  @RequiresApi(VERSION_CODES.P)
  public static BackupDataOutput newInstance(long quota, int transportFlags) {
    return reflector(BackupDataOutputReflector.class)
        .newBackupDataOutput(new FileDescriptor(), quota, transportFlags);
  }

  @ForType(BackupDataOutput.class)
  private interface BackupDataOutputReflector {

    @Constructor
    BackupDataOutput newBackupDataOutput(FileDescriptor fd);

    @Constructor
    BackupDataOutput newBackupDataOutput(FileDescriptor fd, long quota);

    @Constructor
    BackupDataOutput newBackupDataOutput(FileDescriptor fd, long quota, int transportFlags);
  }
}
