package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

import android.app.Application;
import android.app.backup.BackupManager;
import android.app.backup.RestoreObserver;
import android.app.backup.RestoreSession;
import android.app.backup.RestoreSet;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.truth.Correspondence;
import java.util.Arrays;
import java.util.Objects;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

/** Unit tests for {@link ShadowBackupManager}. */
@RunWith(AndroidJUnit4.class)
public class ShadowBackupManagerTest {
  private BackupManager backupManager;
  @Mock private TestRestoreObserver restoreObserver;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);

    ShadowLooper.pauseMainLooper();

    shadowOf((Application) ApplicationProvider.getApplicationContext())
        .grantPermissions(android.Manifest.permission.BACKUP);
    backupManager = new BackupManager(ApplicationProvider.getApplicationContext());

    shadowOf(backupManager).addAvailableRestoreSets(123L, Arrays.asList("foo.bar", "bar.baz"));
    shadowOf(backupManager).addAvailableRestoreSets(456L, Arrays.asList("hello.world"));
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void setBackupEnabled_setToTrue_shouldEnableBackup() {
    backupManager.setBackupEnabled(true);
    assertThat(backupManager.isBackupEnabled()).isTrue();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void setBackupEnabled_multipleInstances_shouldBeEnabled() {
    // BackupManager is used by creating new instances, but all of them talk to the same
    // BackupManagerService in Android, so methods that route through the service will share states.
    backupManager.setBackupEnabled(true);
    assertThat(new BackupManager(ApplicationProvider.getApplicationContext()).isBackupEnabled())
        .isTrue();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void setBackupEnabled_setToFalse_shouldDisableBackup() {
    backupManager.setBackupEnabled(false);
    assertThat(backupManager.isBackupEnabled()).isFalse();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void isBackupEnabled_noPermission_shouldThrowSecurityException() {
    shadowOf((Application) ApplicationProvider.getApplicationContext())
        .denyPermissions(android.Manifest.permission.BACKUP);
    try {
      backupManager.isBackupEnabled();
      fail("SecurityException should be thrown");
    } catch (SecurityException e) {
      // pass
    }
  }

  @Test
  public void getAvailableRestoreSets_shouldCallbackToRestoreSetsAvailable() {
    RestoreSession restoreSession = backupManager.beginRestoreSession();
    int result = restoreSession.getAvailableRestoreSets(restoreObserver);

    assertThat(result).isEqualTo(BackupManager.SUCCESS);
    Robolectric.flushForegroundThreadScheduler();
    ArgumentCaptor<RestoreSet[]> restoreSetArg = ArgumentCaptor.forClass(RestoreSet[].class);
    verify(restoreObserver).restoreSetsAvailable(restoreSetArg.capture());

    RestoreSet[] restoreSets = restoreSetArg.getValue();
    assertThat(restoreSets).hasLength(2);
    assertThat(restoreSets)
        .asList()
        .comparingElementsUsing(fieldCorrespondence("token"))
        .containsExactly(123L, 456L);
  }

  @Test
  public void restoreAll_shouldRestoreData() {
    RestoreSession restoreSession = backupManager.beginRestoreSession();
    int result = restoreSession.restoreAll(123L, restoreObserver);

    assertThat(result).isEqualTo(BackupManager.SUCCESS);
    Robolectric.flushForegroundThreadScheduler();

    verify(restoreObserver).restoreStarting(eq(2));
    verify(restoreObserver).restoreFinished(eq(BackupManager.SUCCESS));

    assertThat(shadowOf(backupManager).getPackageRestoreToken("foo.bar")).isEqualTo(123L);
    assertThat(shadowOf(backupManager).getPackageRestoreToken("bar.baz")).isEqualTo(123L);
    assertThat(shadowOf(backupManager).getPackageRestoreToken("hello.world")).isEqualTo(0L);
  }

  @Test
  public void restoreSome_shouldRestoreSpecifiedPackages() {
    RestoreSession restoreSession = backupManager.beginRestoreSession();
    int result = restoreSession.restoreSome(123L, restoreObserver, new String[] {"bar.baz"});

    assertThat(result).isEqualTo(BackupManager.SUCCESS);

    Robolectric.flushForegroundThreadScheduler();
    verify(restoreObserver).restoreStarting(eq(1));
    verify(restoreObserver).restoreFinished(eq(BackupManager.SUCCESS));

    assertThat(shadowOf(backupManager).getPackageRestoreToken("foo.bar")).isEqualTo(0L);
    assertThat(shadowOf(backupManager).getPackageRestoreToken("bar.baz")).isEqualTo(123L);
  }

  @Test
  public void restorePackage_shouldRestoreSpecifiedPackage() {
    RestoreSession restoreSession = backupManager.beginRestoreSession();

    restoreSession.restoreSome(123L, restoreObserver, new String[0]);
    assertThat(shadowOf(backupManager).getPackageRestoreToken("bar.baz")).isEqualTo(0L);
    restoreSession.endRestoreSession();
    Robolectric.flushForegroundThreadScheduler();
    Mockito.reset(restoreObserver);

    restoreSession = backupManager.beginRestoreSession();
    int result = restoreSession.restorePackage("bar.baz", restoreObserver);

    assertThat(result).isEqualTo(BackupManager.SUCCESS);
    Robolectric.flushForegroundThreadScheduler();

    verify(restoreObserver).restoreStarting(eq(1));
    verify(restoreObserver).restoreFinished(eq(BackupManager.SUCCESS));
    assertThat(shadowOf(backupManager).getPackageRestoreToken("bar.baz")).isEqualTo(123L);
  }

  @Test
  public void restorePackage_noRestoreToken_shouldReturnFailure() {
    RestoreSession restoreSession = backupManager.beginRestoreSession();
    int result = restoreSession.restorePackage("bar.baz", restoreObserver);
    assertThat(result).isEqualTo(-1);
  }

  private static <T, F> Correspondence<T, F> fieldCorrespondence(String fieldName) {
    return new Correspondence<T, F>() {
      @Override
      public boolean compare(T actual, F expected) {
        return Objects.equals(ReflectionHelpers.getField(actual, fieldName), expected);
      }

      @Override
      public String toString() {
        return "field \"" + fieldName + "\" matches";
      }
    };
  }

  private static class TestRestoreObserver extends RestoreObserver {}
}
