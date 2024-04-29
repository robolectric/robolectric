package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataInputStream;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.util.reflector.Constructor;
import org.robolectric.util.reflector.ForType;

@RunWith(AndroidJUnit4.class)
public final class ShadowBackupDataInputTest {

  private static final String TEST_KEY_1 = "key_1";
  private static final byte[] TEST_DATA_1 = {1, 2, 3, 4};
  private static final String TEST_KEY_2 = "key_2";
  private static final byte[] TEST_DATA_2 = {5, 6, 7, 8};

  private final BackupDataInput backupDataInput =
      BackupDataInputBuilder.newBuilder()
          .addEntity(BackupDataEntity.create(TEST_KEY_1, TEST_DATA_1))
          .addEntity(BackupDataEntity.create(TEST_KEY_2, TEST_DATA_2))
          .build();

  @Test
  public void readNextHeader_onFirstItem_returnsTrue() throws IOException {
    boolean result = backupDataInput.readNextHeader();

    assertThat(result).isTrue();
  }

  @Test
  public void readNextHeader_afterReadData_returnsTrue() throws IOException {
    backupDataInput.readNextHeader();
    backupDataInput.readEntityData(new byte[TEST_DATA_1.length], 0, TEST_DATA_1.length);

    boolean result = backupDataInput.readNextHeader();

    assertThat(result).isTrue();
  }

  @Test
  public void readNextHeader_afterSkipData_returnsTrue() throws IOException {
    backupDataInput.readNextHeader();
    backupDataInput.skipEntityData();

    boolean result = backupDataInput.readNextHeader();

    assertThat(result).isTrue();
  }

  @Test
  public void readNextHeader_afterLastItem_returnsFalse() throws IOException {
    backupDataInput.readNextHeader();
    backupDataInput.skipEntityData();
    backupDataInput.readNextHeader();
    backupDataInput.skipEntityData();

    boolean result = backupDataInput.readNextHeader();

    assertThat(result).isFalse();
  }

  @Test
  public void readNextHeader_withoutReadOrSkipData_throwsException() throws IOException {
    backupDataInput.readNextHeader();

    assertThrows(IOException.class, backupDataInput::readNextHeader);
  }

  @Test
  public void getKey_beforeReadNextHeader_throwsException() {
    assertThrows(IllegalStateException.class, backupDataInput::getKey);
  }

  @Test
  public void getKey_afterReadNextHeader_returnsKey() throws IOException {
    backupDataInput.readNextHeader();

    String key = backupDataInput.getKey();

    assertThat(key).isEqualTo(TEST_KEY_1);
  }

  @Test
  public void getDataSize_beforeReadNextHeader_throwsException() {
    assertThrows(IllegalStateException.class, backupDataInput::getDataSize);
  }

  @Test
  public void getDataSize_afterReadNextHeader_returnsArrayLength() throws IOException {
    backupDataInput.readNextHeader();

    int dataSize = backupDataInput.getDataSize();

    assertThat(dataSize).isEqualTo(TEST_DATA_1.length);
  }

  @Test
  public void readEntityData_afterLastItem_throwsException() throws IOException {
    backupDataInput.readNextHeader();
    backupDataInput.skipEntityData();
    backupDataInput.readNextHeader();
    backupDataInput.skipEntityData();
    backupDataInput.readNextHeader();

    assertThrows(
        IllegalStateException.class,
        () -> backupDataInput.readEntityData(TEST_DATA_1, 0, TEST_DATA_1.length));
  }

  @Test
  public void readEntityData_afterAllBytesRead_returns0() throws IOException {
    backupDataInput.readNextHeader();
    backupDataInput.readEntityData(new byte[TEST_DATA_1.length], 0, TEST_DATA_1.length);

    int result =
        backupDataInput.readEntityData(new byte[TEST_DATA_1.length], 0, TEST_DATA_1.length);

    assertThat(result).isEqualTo(0);
  }

  @Test
  public void readEntityData_withSizeGreaterThanDestination_throwsException() throws IOException {
    backupDataInput.readNextHeader();

    assertThrows(
        IOException.class,
        () -> backupDataInput.readEntityData(new byte[2], 0, TEST_DATA_1.length));
  }

  @Test
  public void readEntityData_withFullDataRead_copiesSourceData() throws IOException {
    backupDataInput.readNextHeader();
    byte[] data = new byte[TEST_DATA_1.length];

    int result = backupDataInput.readEntityData(data, 0, data.length);

    assertThat(result).isEqualTo(TEST_DATA_1.length);
    assertThat(data).isEqualTo(TEST_DATA_1);
  }

  @Test
  public void readEntityData_withPartialDataRead_copiesSourceData() throws IOException {
    backupDataInput.readNextHeader();
    byte[] data1 = new byte[2];
    byte[] data2 = new byte[2];

    int result1 = backupDataInput.readEntityData(data1, 0, 2);
    int result2 = backupDataInput.readEntityData(data2, 0, 2);

    assertThat(result1).isEqualTo(2);
    assertThat(result2).isEqualTo(2);
    assertThat(data1).isEqualTo(new byte[] {1, 2});
    assertThat(data2).isEqualTo(new byte[] {3, 4});
  }

  @Test
  public void readEntityData_usingBackupDataInputStream_matchesSourceData() throws IOException {
    backupDataInput.readNextHeader();
    InputStream inputStream =
        new BufferedInputStream(
            reflector(BackupDataInputStreamReflector.class).newInstance(backupDataInput), 1);
    byte[] data = new byte[TEST_DATA_1.length];

    int result = inputStream.read(data);

    assertThat(result).isEqualTo(TEST_DATA_1.length);
    assertThat(data).isEqualTo(TEST_DATA_1);
  }

  @ForType(BackupDataInputStream.class)
  private interface BackupDataInputStreamReflector {

    @Constructor
    BackupDataInputStream newInstance(BackupDataInput backupDataInput);
  }
}
