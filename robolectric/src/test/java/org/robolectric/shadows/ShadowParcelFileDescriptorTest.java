package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.Assume.assumeThat;
import static org.robolectric.Shadows.shadowOf;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.ParcelFileDescriptor;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

@RunWith(AndroidJUnit4.class)
public class ShadowParcelFileDescriptorTest {

  private static final int READ_ONLY_FILE_CONTENTS = 42;

  private File file;
  private File readOnlyFile;
  private ParcelFileDescriptor pfd;

  @Before
  public void setUp() throws Exception {
    file = new File(ApplicationProvider.getApplicationContext().getFilesDir(), "test");
    FileOutputStream os = new FileOutputStream(file);
    os.close();
    readOnlyFile =
        new File(ApplicationProvider.getApplicationContext().getFilesDir(), "test_readonly");
    os = new FileOutputStream(readOnlyFile);
    os.write(READ_ONLY_FILE_CONTENTS);
    os.close();
    assertThat(readOnlyFile.setReadOnly()).isTrue();
  }

  @After
  public void tearDown() throws Exception {
    if (pfd != null) {
      pfd.close();
    }
  }

  @Test
  public void testOpens() throws Exception {
    pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_WRITE);
    assertThat(pfd).isNotNull();
    assertThat(pfd.getFileDescriptor().valid()).isTrue();
  }

  @Test
  public void testOpens_canReadReadOnlyFile() throws Exception {
    pfd = ParcelFileDescriptor.open(readOnlyFile, ParcelFileDescriptor.MODE_READ_ONLY);
    assertThat(pfd).isNotNull();
    assertThat(pfd.getFileDescriptor().valid()).isTrue();
    FileInputStream is = new FileInputStream(pfd.getFileDescriptor());
    assertThat(is.read()).isEqualTo(READ_ONLY_FILE_CONTENTS);
  }

  @Test
  public void testOpens_canWriteWritableFile() throws Exception {
    pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_WRITE);
    assertThat(pfd).isNotNull();
    assertThat(pfd.getFileDescriptor().valid()).isTrue();
    FileOutputStream os = new FileOutputStream(pfd.getFileDescriptor());
    os.write(5);
    os.close();
  }

  @Test
  @Config(minSdk = KITKAT)
  public void testOpenWithOnCloseListener_nullHandler() throws Exception {
    final AtomicBoolean onCloseCalled = new AtomicBoolean(false);
    ParcelFileDescriptor.OnCloseListener onCloseListener =
        new ParcelFileDescriptor.OnCloseListener() {
          @Override
          public void onClose(IOException e) {
            onCloseCalled.set(true);
          }
        };
    assertThrows(
        IllegalArgumentException.class,
        () ->
            ParcelFileDescriptor.open(
                file, ParcelFileDescriptor.MODE_READ_WRITE, null, onCloseListener));
  }

  @Test
  @Config(minSdk = KITKAT)
  public void testOpenWithOnCloseListener_nullOnCloseListener() throws Exception {
    HandlerThread handlerThread = new HandlerThread("test");
    handlerThread.start();
    Handler handler = new Handler(handlerThread.getLooper());
    assertThrows(
        IllegalArgumentException.class,
        () -> ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_WRITE, handler, null));
    handlerThread.quit();
  }

  @Test
  @Config(minSdk = KITKAT)
  public void testOpenWithOnCloseListener_callsListenerOnClose() throws Exception {
    HandlerThread handlerThread = new HandlerThread("test");
    handlerThread.start();
    Handler handler = new Handler(handlerThread.getLooper());
    final AtomicBoolean onCloseCalled = new AtomicBoolean(false);
    ParcelFileDescriptor.OnCloseListener onCloseListener =
        new ParcelFileDescriptor.OnCloseListener() {
          @Override
          public void onClose(IOException e) {
            onCloseCalled.set(true);
          }
        };
    pfd =
        ParcelFileDescriptor.open(
            file, ParcelFileDescriptor.MODE_READ_WRITE, handler, onCloseListener);
    assertThat(pfd).isNotNull();
    assertThat(pfd.getFileDescriptor().valid()).isTrue();
    pfd.close();
    shadowOf(handlerThread.getLooper()).idle();
    assertThat(onCloseCalled.get()).isTrue();
    handlerThread.quit();
  }

  @Test
  public void testStatSize_emptyFile() throws Exception {
    pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_WRITE);
    assertThat(pfd).isNotNull();
    assertThat(pfd.getFileDescriptor().valid()).isTrue();
    assertThat(pfd.getStatSize()).isEqualTo(0);
  }

  @Test
  public void testStatSize_writtenFile() throws Exception {
    pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_WRITE);
    assertThat(pfd).isNotNull();
    assertThat(pfd.getFileDescriptor().valid()).isTrue();
    FileOutputStream os = new FileOutputStream(pfd.getFileDescriptor());
    os.write(5);
    assertThat(pfd.getStatSize()).isEqualTo(1); // One byte.
    os.close();
  }

  @Test
  public void testAppend() throws Exception {
    pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_WRITE);
    assertThat(pfd).isNotNull();
    assertThat(pfd.getFileDescriptor().valid()).isTrue();
    FileOutputStream os = new FileOutputStream(pfd.getFileDescriptor());
    os.write(5);
    assertThat(pfd.getStatSize()).isEqualTo(1); // One byte.
    os.close();
    pfd.close();

    pfd =
        ParcelFileDescriptor.open(
            file, ParcelFileDescriptor.MODE_READ_WRITE | ParcelFileDescriptor.MODE_APPEND);
    assertThat(pfd).isNotNull();
    assertThat(pfd.getFileDescriptor().valid()).isTrue();
    os = new FileOutputStream(pfd.getFileDescriptor());
    os.write(5);
    assertThat(pfd.getStatSize()).isEqualTo(2); // Two bytes.
    os.close();
  }

  @Test
  public void testTruncate() throws Exception {
    pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_WRITE);
    assertThat(pfd).isNotNull();
    assertThat(pfd.getFileDescriptor().valid()).isTrue();
    FileOutputStream os = new FileOutputStream(pfd.getFileDescriptor());
    os.write(1);
    os.write(2);
    os.write(3);
    assertThat(pfd.getStatSize()).isEqualTo(3); // Three bytes.
    os.close();

    try (FileInputStream in = new FileInputStream(file)) {
      byte[] buffer = new byte[3];
      assertThat(in.read(buffer)).isEqualTo(3);
      assertThat(buffer).isEqualTo(new byte[] {1, 2, 3});
      assertThat(in.available()).isEqualTo(0);
    }

    pfd.close();
    pfd =
        ParcelFileDescriptor.open(
            file, ParcelFileDescriptor.MODE_READ_WRITE | ParcelFileDescriptor.MODE_TRUNCATE);
    assertThat(pfd).isNotNull();
    assertThat(pfd.getFileDescriptor().valid()).isTrue();
    os = new FileOutputStream(pfd.getFileDescriptor());
    os.write(4);
    assertThat(pfd.getStatSize()).isEqualTo(1); // One byte.
    os.close();

    try (FileInputStream in = new FileInputStream(file)) {
      assertThat(in.read()).isEqualTo(4);
      assertThat(in.available()).isEqualTo(0);
    }
  }

  @Test
  public void testWriteTwiceNoTruncate() throws Exception {
    pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_WRITE);
    assertThat(pfd).isNotNull();
    assertThat(pfd.getFileDescriptor().valid()).isTrue();
    FileOutputStream os = new FileOutputStream(pfd.getFileDescriptor());
    os.write(1);
    os.write(2);
    os.write(3);
    assertThat(pfd.getStatSize()).isEqualTo(3); // Three bytes.
    os.close();

    try (FileInputStream in = new FileInputStream(file)) {
      byte[] buffer = new byte[3];
      assertThat(in.read(buffer)).isEqualTo(3);
      assertThat(buffer).isEqualTo(new byte[] {1, 2, 3});
      assertThat(in.available()).isEqualTo(0);
    }
    pfd.close();

    pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_WRITE);
    assertThat(pfd).isNotNull();
    assertThat(pfd.getFileDescriptor().valid()).isTrue();
    os = new FileOutputStream(pfd.getFileDescriptor());
    os.write(4);
    assertThat(pfd.getStatSize()).isEqualTo(3); // One byte.
    os.close();

    try (FileInputStream in = new FileInputStream(file)) {
      byte[] buffer = new byte[3];
      assertThat(in.read(buffer)).isEqualTo(3);
      assertThat(buffer).isEqualTo(new byte[] {4, 2, 3});
      assertThat(in.available()).isEqualTo(0);
    }
  }

  @Test
  public void testCloses() throws Exception {
    pfd = ParcelFileDescriptor.open(file, -1);
    pfd.close();
    assertThat(pfd.getFileDescriptor().valid()).isFalse();
  }

  @Test
  public void testClose_twice() throws Exception {
    pfd = ParcelFileDescriptor.open(file, -1);
    pfd.close();
    assertThat(pfd.getFileDescriptor().valid()).isFalse();

    pfd.close();
    assertThat(pfd.getFileDescriptor().valid()).isFalse();
  }

  @Test
  public void testCloses_getStatSize_returnsInvalidLength() throws Exception {
    pfd = ParcelFileDescriptor.open(file, -1);
    pfd.close();
    assertThat(pfd.getStatSize()).isEqualTo(-1);
  }

  @Test
  public void testAutoCloseInputStream() throws Exception {
    pfd = ParcelFileDescriptor.open(file, -1);
    ParcelFileDescriptor.AutoCloseInputStream is =
        new ParcelFileDescriptor.AutoCloseInputStream(pfd);
    is.close();
    assertThat(pfd.getFileDescriptor().valid()).isFalse();
  }

  @Test
  public void testAutoCloseOutputStream() throws Exception {
    File f = new File(ApplicationProvider.getApplicationContext().getFilesDir(), "outfile");
    ParcelFileDescriptor pfd = ParcelFileDescriptor.open(f, -1);
    ParcelFileDescriptor.AutoCloseOutputStream os =
        new ParcelFileDescriptor.AutoCloseOutputStream(pfd);
    os.close();
    assertThat(pfd.getFileDescriptor().valid()).isFalse();
  }

  @Test
  public void testCreatePipe() throws IOException {
    ParcelFileDescriptor[] pipe = ParcelFileDescriptor.createPipe();
    ParcelFileDescriptor readSide = pipe[0];
    ParcelFileDescriptor writeSide = pipe[1];
    byte[] dataToWrite = new byte[] {0, 1, 2, 3, 4};
    ParcelFileDescriptor.AutoCloseInputStream inputStream =
        new ParcelFileDescriptor.AutoCloseInputStream(readSide);
    ParcelFileDescriptor.AutoCloseOutputStream outputStream =
        new ParcelFileDescriptor.AutoCloseOutputStream(writeSide);
    outputStream.write(dataToWrite);
    byte[] read = new byte[dataToWrite.length];
    int byteCount = inputStream.read(read);
    inputStream.close();
    outputStream.close();
    assertThat(byteCount).isEqualTo(dataToWrite.length);
    assertThat(read).isEqualTo(dataToWrite);
  }

  @Test
  public void testCreatePipeTwice() throws IOException {
    testCreatePipe();
    testCreatePipe();
  }

  @Test
  public void testGetFd_canRead() throws IOException {
    assumeThat("Windows is an affront to decency.",
        File.separator, Matchers.equalTo("/"));

    pfd = ParcelFileDescriptor.open(readOnlyFile, ParcelFileDescriptor.MODE_READ_ONLY);
    int fd = pfd.getFd();
    assertThat(fd).isGreaterThan(0);

    final FileDescriptor fileDescriptor = pfd.getFileDescriptor();
    assertThat(fileDescriptor.valid()).isTrue();
    assertThat(fd).isEqualTo(ReflectionHelpers.getField(fileDescriptor, "fd"));

    FileInputStream is = new FileInputStream(fileDescriptor);
    assertThat(is.read()).isEqualTo(READ_ONLY_FILE_CONTENTS);
    is.close();
  }

  @Test
  public void testGetFd_alreadyClosed() throws Exception {
    pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_WRITE);
    assertThat(pfd).isNotNull();
    assertThat(pfd.getFileDescriptor().valid()).isTrue();

    pfd.close();

    assertThrows(IllegalStateException.class, () -> pfd.getFd());
  }
}
