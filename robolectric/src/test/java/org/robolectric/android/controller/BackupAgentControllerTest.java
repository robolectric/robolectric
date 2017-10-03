package org.robolectric.android.controller;

import static org.assertj.core.api.Assertions.assertThat;

import android.app.backup.BackupAgent;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.os.ParcelFileDescriptor;
import java.io.IOException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class BackupAgentControllerTest {
  private final BackupAgentController<MyBackupAgent> backupAgentController = Robolectric.buildBackupAgent(MyBackupAgent.class);

  @Test
  public void shouldSetBaseContext() throws Exception {
    MyBackupAgent myBackupAgent = backupAgentController.get();
    assertThat(myBackupAgent.getBaseContext()).isEqualTo(RuntimeEnvironment.application.getBaseContext());
  }

  public static class MyBackupAgent extends BackupAgent {
    @Override
    public void onBackup(ParcelFileDescriptor parcelFileDescriptor, BackupDataOutput backupDataOutput, ParcelFileDescriptor parcelFileDescriptor1) throws IOException {
      // no op
    }

    @Override
    public void onRestore(BackupDataInput backupDataInput, int i, ParcelFileDescriptor parcelFileDescriptor) throws IOException {
      // no op
    }
  }
}
