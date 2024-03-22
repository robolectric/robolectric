package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.shadows.ShadowLooper.shadowMainLooper;

import android.app.Application;
import android.app.backup.BackupManager;
import android.app.backup.BackupTransport;
import android.app.backup.RestoreObserver;
import android.app.backup.RestoreSession;
import android.app.backup.RestoreSet;
import androidx.annotation.Nullable;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.truth.Correspondence;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

/** Unit tests for {@link ShadowBackupManager}. */
@RunWith(AndroidJUnit4.class)
public class ShadowBackupManagerTest {
  private BackupManager backupManager;
  private TestRestoreObserver restoreObserver;

  @Before
  public void setUp() {
    shadowMainLooper().pause();

    shadowOf((Application) ApplicationProvider.getApplicationContext())
        .grantPermissions(android.Manifest.permission.BACKUP);
    backupManager = new BackupManager(ApplicationProvider.getApplicationContext());
    restoreObserver = new TestRestoreObserver();

    shadowOf(backupManager).addAvailableRestoreSets(123L, ImmutableList.of("foo.bar", "bar.baz"));
    shadowOf(backupManager).addAvailableRestoreSets(456L, ImmutableList.of("hello.world"));
  }

  @Test
  public void dataChanged() {
    assertThat(shadowOf(backupManager).isDataChanged()).isFalse();
    assertThat(shadowOf(backupManager).getDataChangedCount()).isEqualTo(0);

    for (int i = 1; i <= 3; i++) {
      backupManager.dataChanged();

      assertThat(shadowOf(backupManager).isDataChanged()).isTrue();
      assertThat(shadowOf(backupManager).getDataChangedCount()).isEqualTo(i);
    }

    ShadowBackupManager.reset();
    assertThat(shadowOf(backupManager).isDataChanged()).isFalse();
    assertThat(shadowOf(backupManager).getDataChangedCount()).isEqualTo(0);
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

    assertThrows(SecurityException.class, () -> backupManager.isBackupEnabled());
  }

  @Test
  public void getAvailableRestoreSets_shouldCallbackToRestoreSetsAvailable() {
    RestoreSession restoreSession = backupManager.beginRestoreSession();
    int result = restoreSession.getAvailableRestoreSets(restoreObserver);

    assertThat(result).isEqualTo(BackupManager.SUCCESS);
    shadowMainLooper().idle();

    RestoreSet[] restoreSets = restoreObserver.getRestoreSets();
    assertThat(restoreSets).hasLength(2);
    assertThat(restoreSets)
        .asList()
        .comparingElementsUsing(fieldCorrespondence("token"))
        .containsExactly(123L, 456L);
  }

  @Test
  public void getAvailableRestoreToken_setNullAvailableRestoreSets_returnsNull() {
    shadowOf(backupManager).setNullAvailableRestoreSets(true);
    RestoreSession restoreSession = backupManager.beginRestoreSession();

    int result = restoreSession.getAvailableRestoreSets(restoreObserver);
    shadowMainLooper().idle();

    assertThat(result).isEqualTo(BackupManager.SUCCESS);
    assertThat(restoreObserver.restoreSetsAvailableCalled).isTrue();
    assertThat(restoreObserver.getRestoreSets()).isNull();
  }

  @Test
  public void restoreAll_shouldRestoreData() {
    RestoreSession restoreSession = backupManager.beginRestoreSession();
    int result = restoreSession.restoreAll(123L, restoreObserver);

    assertThat(result).isEqualTo(BackupManager.SUCCESS);
    shadowMainLooper().idle();

    assertThat(restoreObserver.getRestoreStartingNumPackages()).isEqualTo(2);
    assertThat(restoreObserver.getRestoreFinishedResult()).isEqualTo(BackupManager.SUCCESS);
    assertThat(shadowOf(backupManager).getPackageRestoreToken("foo.bar")).isEqualTo(123L);
    assertThat(shadowOf(backupManager).getPackageRestoreCount("foo.bar")).isEqualTo(1);
    assertThat(shadowOf(backupManager).getPackageRestoreToken("bar.baz")).isEqualTo(123L);
    assertThat(shadowOf(backupManager).getPackageRestoreCount("bar.baz")).isEqualTo(1);
    assertThat(shadowOf(backupManager).getPackageRestoreToken("hello.world")).isEqualTo(0L);
    assertThat(shadowOf(backupManager).getPackageRestoreCount("hello.world")).isEqualTo(0);
  }

  @Test
  public void restoreSome_shouldRestoreSpecifiedPackages() {
    RestoreSession restoreSession = backupManager.beginRestoreSession();
    int result = restoreSession.restoreSome(123L, restoreObserver, new String[] {"bar.baz"});

    assertThat(result).isEqualTo(BackupManager.SUCCESS);

    shadowMainLooper().idle();

    assertThat(restoreObserver.getRestoreStartingNumPackages()).isEqualTo(1);
    assertThat(restoreObserver.getRestoreFinishedResult()).isEqualTo(BackupManager.SUCCESS);
    assertThat(shadowOf(backupManager).getPackageRestoreToken("foo.bar")).isEqualTo(0L);
    assertThat(shadowOf(backupManager).getPackageRestoreCount("foo.bar")).isEqualTo(0);
    assertThat(shadowOf(backupManager).getPackageRestoreToken("bar.baz")).isEqualTo(123L);
    assertThat(shadowOf(backupManager).getPackageRestoreCount("bar.baz")).isEqualTo(1);
  }

  @Test
  @Config(minSdk = Q)
  public void restorePackages_multipleRestores_countsRestores() {
    RestoreSession restoreSession = backupManager.beginRestoreSession();

    int result = restoreSession.restorePackages(123L, restoreObserver, ImmutableSet.of("foo.bar"));
    assertThat(result).isEqualTo(BackupManager.SUCCESS);
    restoreSession.endRestoreSession();

    restoreSession = backupManager.beginRestoreSession();
    result = restoreSession.restorePackages(123L, restoreObserver, ImmutableSet.of("foo.bar"));
    assertThat(result).isEqualTo(BackupManager.SUCCESS);

    assertThat(shadowOf(backupManager).getPackageRestoreToken("foo.bar")).isEqualTo(123L);
    assertThat(shadowOf(backupManager).getPackageRestoreCount("foo.bar")).isEqualTo(2);
  }

  @Test
  @Config(minSdk = Q)
  public void restorePackages_transportError_returnsErrorInRestoreFinishedCallback() {
    shadowOf(backupManager)
        .addAvailableRestoreSets(
            789L, ImmutableList.of("foo.bar"), BackupTransport.TRANSPORT_ERROR);
    RestoreSession restoreSession = backupManager.beginRestoreSession();

    int result = restoreSession.restorePackages(789L, restoreObserver, ImmutableSet.of("foo.bar"));
    assertThat(result).isEqualTo(BackupManager.SUCCESS);
    shadowMainLooper().idle();

    assertThat(restoreObserver.getRestoreFinishedResult())
        .isEqualTo(BackupTransport.TRANSPORT_ERROR);
  }

  @Test
  @Config(minSdk = Q)
  public void restorePackages_multipleRestoreSets_returnsEachResult() {
    // Add two restore set for token 789
    shadowOf(backupManager)
        .addAvailableRestoreSets(
            789L, ImmutableList.of("foo.bar"), BackupTransport.TRANSPORT_ERROR);
    shadowOf(backupManager)
        .addAvailableRestoreSets(789L, ImmutableList.of("foo.bar"), BackupManager.SUCCESS);
    RestoreSession restoreSession = backupManager.beginRestoreSession();

    List<Integer> restoreResults = new ArrayList<>();
    for (int attempt = 1; attempt <= 3; attempt++) {
      if (restoreSession != null) {
        restoreSession.endRestoreSession();
      }
      restoreObserver = new TestRestoreObserver();
      restoreSession = backupManager.beginRestoreSession();
      int result =
          restoreSession.restorePackages(789L, restoreObserver, ImmutableSet.of("foo.bar"));
      assertThat(result).isEqualTo(BackupManager.SUCCESS);
      shadowMainLooper().idle();
      restoreResults.add(restoreObserver.getRestoreFinishedResult());
    }

    // The first attempt is expected to fail, any subsequent attempt is expected to succeed
    assertThat(restoreResults)
        .containsExactly(
            BackupTransport.TRANSPORT_ERROR, BackupManager.SUCCESS, BackupManager.SUCCESS);
  }

  @Test
  @Config(minSdk = Q)
  public void restorePackages_unknownToken_returnsError() {
    RestoreSession restoreSession = backupManager.beginRestoreSession();

    int result = restoreSession.restorePackages(1L, restoreObserver, ImmutableSet.of("foo.bar"));
    assertThat(result).isEqualTo(BackupManager.SUCCESS);
    shadowMainLooper().idle();

    assertThat(restoreObserver.getRestoreStartingNumPackages()).isEqualTo(0);
    assertThat(restoreObserver.getRestoreFinishedResult()).isEqualTo(-1);
  }

  @Test
  public void restorePackage_shouldRestoreSpecifiedPackage() {
    RestoreSession restoreSession = backupManager.beginRestoreSession();

    restoreSession.restoreSome(123L, restoreObserver, new String[0]);
    assertThat(shadowOf(backupManager).getPackageRestoreToken("bar.baz")).isEqualTo(0L);
    assertThat(shadowOf(backupManager).getPackageRestoreCount("bar.baz")).isEqualTo(0);
    restoreSession.endRestoreSession();
    shadowMainLooper().idle();
    restoreObserver = new TestRestoreObserver();

    restoreSession = backupManager.beginRestoreSession();
    int result = restoreSession.restorePackage("bar.baz", restoreObserver);

    assertThat(result).isEqualTo(BackupManager.SUCCESS);
    shadowMainLooper().idle();

    assertThat(restoreObserver.getRestoreStartingNumPackages()).isEqualTo(1);
    assertThat(restoreObserver.getRestoreFinishedResult()).isEqualTo(BackupManager.SUCCESS);
    assertThat(shadowOf(backupManager).getPackageRestoreToken("bar.baz")).isEqualTo(123L);
    assertThat(shadowOf(backupManager).getPackageRestoreCount("bar.baz")).isEqualTo(1);
  }

  @Test
  public void restorePackage_noRestoreToken_shouldReturnFailure() {
    RestoreSession restoreSession = backupManager.beginRestoreSession();
    int result = restoreSession.restorePackage("bar.baz", restoreObserver);
    assertThat(result).isEqualTo(-1);
  }

  @Test
  @Config(minSdk = M)
  public void getAvailableRestoreToken_noRestoreToken_returnsDefaultValue() {
    long defaultValue = 0L;
    assertThat(shadowOf(backupManager).getPackageRestoreToken("foo.bar")).isEqualTo(defaultValue);
    assertThat(backupManager.getAvailableRestoreToken("foo.bar")).isEqualTo(defaultValue);
  }

  @Test
  @Config(minSdk = M)
  public void getAvailableRestoreToken_restoreTokenAvailableForSomePackages_returnsCorrectValues() {
    long defaultVal = 0L;
    long restoreToken = 123L;
    RestoreSession restoreSession = backupManager.beginRestoreSession();

    int result =
        restoreSession.restoreSome(restoreToken, restoreObserver, new String[] {"bar.baz"});
    assertThat(result).isEqualTo(BackupManager.SUCCESS);
    shadowMainLooper().idle();

    assertThat(restoreObserver.getRestoreStartingNumPackages()).isEqualTo(1);
    assertThat(restoreObserver.getRestoreFinishedResult()).isEqualTo(BackupManager.SUCCESS);
    assertThat(backupManager.getAvailableRestoreToken("foo.bar")).isEqualTo(defaultVal);
    assertThat(backupManager.getAvailableRestoreToken("bar.baz")).isEqualTo(restoreToken);
  }

  private static <T, F> Correspondence<T, F> fieldCorrespondence(String fieldName) {
    return Correspondence.from(
        (actual, expected) ->
            Objects.equals(ReflectionHelpers.getField(actual, fieldName), expected),
        "field \"" + fieldName + "\" matches");
  }

  private static class TestRestoreObserver extends RestoreObserver {
    @Nullable private RestoreSet[] restoreSets;
    private boolean restoreSetsAvailableCalled;
    @Nullable private Integer restoreStartingNumPackages;
    @Nullable private Integer restoreFinishedResult;

    @Override
    public void restoreSetsAvailable(RestoreSet[] restoreSets) {
      this.restoreSets = restoreSets;
      this.restoreSetsAvailableCalled = true;
    }

    @Override
    public void restoreStarting(int numPackages) {
      this.restoreStartingNumPackages = numPackages;
    }

    @Override
    public void restoreFinished(int result) {
      this.restoreFinishedResult = result;
    }

    @Nullable
    public RestoreSet[] getRestoreSets() {
      return restoreSets;
    }

    @Nullable
    public Integer getRestoreStartingNumPackages() {
      return restoreStartingNumPackages;
    }

    @Nullable
    public Integer getRestoreFinishedResult() {
      return restoreFinishedResult;
    }
  }
}
