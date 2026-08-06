package org.robolectric.android.controller

import android.app.Application
import android.app.backup.BackupAgent
import android.app.backup.BackupDataInput
import android.app.backup.BackupDataOutput
import android.os.ParcelFileDescriptor
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import java.io.IOException
import org.junit.Test
import org.robolectric.Robolectric

class BackupAgentControllerTest {

  @Test
  fun get_setsBaseContext() {
    val backupAgent = Robolectric.buildBackupAgent(MyBackupAgent::class.java).get()
    val expectedBaseContext = (ApplicationProvider.getApplicationContext<Application>()).baseContext
    assertThat(backupAgent.baseContext).isEqualTo(expectedBaseContext)
  }

  class MyBackupAgent : BackupAgent() {
    @Throws(IOException::class)
    override fun onBackup(
      oldState: ParcelFileDescriptor?,
      data: BackupDataOutput?,
      newState: ParcelFileDescriptor?,
    ) = Unit

    @Throws(IOException::class)
    override fun onRestore(
      data: BackupDataInput?,
      appVersionCode: Int,
      newState: ParcelFileDescriptor?,
    ) = Unit
  }
}
