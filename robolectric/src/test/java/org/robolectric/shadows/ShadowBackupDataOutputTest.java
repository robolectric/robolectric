package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.app.backup.BackupDataOutput;
import android.os.Build.VERSION_CODES;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.IOException;
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.util.reflector.ForType;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = VERSION_CODES.LOLLIPOP)
public final class ShadowBackupDataOutputTest {

  private static final String TEST_PREFIX = "prefix";
  private static final String TEST_KEY = "key";
  private static final byte[] TEST_DATA = {1, 2, 3, 4};

  private final BackupDataOutput backupDataOutput = BackupDataOutputFactory.newInstance();

  @Test
  public void writeEntityHeader_withUnfinishedData_throwsException() throws IOException {
    backupDataOutput.writeEntityHeader(TEST_KEY, TEST_DATA.length);

    assertThrows(IOException.class, () -> backupDataOutput.writeEntityHeader("key_2", 5));
  }

  @Test
  public void writeEntityData_withKeyPrefix_hasPrefixInEntityKey() throws IOException {
    reflector(BackupDataOutputReflector.class, backupDataOutput).setKeyPrefix(TEST_PREFIX);
    backupDataOutput.writeEntityHeader(TEST_KEY, 0);

    assertThat(shadowOf(backupDataOutput).getEntities())
        .containsExactly(
            BackupDataEntity.create(
                TEST_PREFIX + ShadowBackupDataOutput.KEY_PREFIX_JOINER + TEST_KEY, new byte[0]));
  }

  @Test
  public void writeEntityData_withNonNegativeDataSize_addsEntityOfSize() throws IOException {
    backupDataOutput.writeEntityHeader(TEST_KEY, 5);

    assertThat(shadowOf(backupDataOutput).getEntities())
        .containsExactly(BackupDataEntity.create(TEST_KEY, new byte[5]));
  }

  @Test
  public void writeEntityData_withNegativeDataSize_addsDeletedEntity() throws IOException {
    backupDataOutput.writeEntityHeader(TEST_KEY, -1);

    assertThat(shadowOf(backupDataOutput).getEntities())
        .containsExactly(BackupDataEntity.createDeletedEntity(TEST_KEY));
  }

  @Test
  public void writeEntityData_withGreaterSizeThanSource_throwsException() throws IOException {
    backupDataOutput.writeEntityHeader(TEST_KEY, 10);

    assertThrows(IOException.class, () -> backupDataOutput.writeEntityData(new byte[5], 10));
  }

  @Test
  public void writeEntityData_withGreaterSizeThanDestination_throwsException() throws IOException {
    backupDataOutput.writeEntityHeader(TEST_KEY, 2);

    assertThrows(
        IOException.class, () -> backupDataOutput.writeEntityData(TEST_DATA, TEST_DATA.length));
  }

  @Test
  public void writeEntityData_withFullDataWrite_addsCorrectEntityData() throws IOException {
    backupDataOutput.writeEntityHeader(TEST_KEY, TEST_DATA.length);

    backupDataOutput.writeEntityData(TEST_DATA, TEST_DATA.length);

    assertThat(shadowOf(backupDataOutput).getEntities())
        .containsExactly(BackupDataEntity.create(TEST_KEY, TEST_DATA));
  }

  @Test
  public void writeEntityData_withPartialDataWrites_addsCorrectEntityData() throws IOException {
    backupDataOutput.writeEntityHeader(TEST_KEY, 4);

    backupDataOutput.writeEntityData(Arrays.copyOfRange(TEST_DATA, 0, 2), 2);
    backupDataOutput.writeEntityData(Arrays.copyOfRange(TEST_DATA, 2, 4), 2);

    assertThat(shadowOf(backupDataOutput).getEntities())
        .containsExactly(BackupDataEntity.create(TEST_KEY, TEST_DATA));
  }

  @ForType(BackupDataOutput.class)
  private interface BackupDataOutputReflector {

    void setKeyPrefix(String prefix);
  }
}
