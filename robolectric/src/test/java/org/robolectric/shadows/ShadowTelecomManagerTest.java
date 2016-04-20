package org.robolectric.shadows;

import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

@RunWith(TestRunners.MultiApiWithDefaults.class)
@Config(sdk = {
    Build.VERSION_CODES.LOLLIPOP,
    Build.VERSION_CODES.LOLLIPOP_MR1,
    Build.VERSION_CODES.M})
public class ShadowTelecomManagerTest {

  private TelecomManager telecomService;

  @Before
  public void setUp() {
    telecomService = (TelecomManager) RuntimeEnvironment.application.getSystemService(Context.TELECOM_SERVICE);
  }

  @Test
  public void getSimCallManager() {
    PhoneAccountHandle handle = createHandler("id");

    shadowOf(telecomService).setSimCallManager(handle);

    assertThat(telecomService.getConnectionManager().getId()).isEqualTo("id");
  }

  @Test
  public void registerAndUnRegister() {
    assertThat(shadowOf(telecomService).getAllPhoneAccountsCount()).isEqualTo(0);
    assertThat(shadowOf(telecomService).getAllPhoneAccounts()).hasSize(0);

    PhoneAccountHandle handler = createHandler("id");
    PhoneAccount phoneAccount = PhoneAccount.builder(handler, "main_account").build();
    telecomService.registerPhoneAccount(phoneAccount);

    assertThat(shadowOf(telecomService).getAllPhoneAccountsCount()).isEqualTo(1);
    assertThat(shadowOf(telecomService).getAllPhoneAccounts()).hasSize(1);
    assertThat(telecomService.getAllPhoneAccountHandles()).hasSize(1);
    assertThat(telecomService.getAllPhoneAccountHandles()).contains(handler);
    assertThat(telecomService.getPhoneAccount(handler).getLabel()).isEqualTo(phoneAccount.getLabel());

    telecomService.unregisterPhoneAccount(handler);

    assertThat(shadowOf(telecomService).getAllPhoneAccountsCount()).isEqualTo(0);
    assertThat(shadowOf(telecomService).getAllPhoneAccounts()).hasSize(0);
    assertThat(telecomService.getAllPhoneAccountHandles()).hasSize(0);
  }

  @Test
  public void clearAccounts() {
    PhoneAccountHandle anotherPackageHandle = createHandler("some.other.package", "id");
    telecomService.registerPhoneAccount(PhoneAccount.builder(anotherPackageHandle, "another_package")
        .build());
  }

  @Test
  @Config(sdk = {
      Build.VERSION_CODES.LOLLIPOP_MR1,
      Build.VERSION_CODES.M})
  public void clearAccountsForPackage() {
    PhoneAccountHandle accountHandle1 = createHandler("a.package", "id1");
    telecomService.registerPhoneAccount(PhoneAccount.builder(accountHandle1, "another_package")
        .build());

    PhoneAccountHandle accountHandle2 = createHandler("some.other.package", "id2");
    telecomService.registerPhoneAccount(PhoneAccount.builder(accountHandle2, "another_package")
        .build());

    telecomService.clearAccountsForPackage(accountHandle1.getComponentName().getPackageName());

    assertThat(telecomService.getPhoneAccount(accountHandle1)).isNull();
    assertThat(telecomService.getPhoneAccount(accountHandle2)).isNotNull();
  }

  @Test
  public void getPhoneAccountsSupportingScheme() {
    PhoneAccountHandle handleMatchingScheme = createHandler("id1");
    telecomService.registerPhoneAccount(PhoneAccount.builder(handleMatchingScheme, "some_scheme")
        .addSupportedUriScheme("some_scheme")
        .build());
    PhoneAccountHandle handleNotMatchingScheme = createHandler("id2");
    telecomService.registerPhoneAccount(PhoneAccount.builder(handleNotMatchingScheme, "another_scheme")
        .addSupportedUriScheme("another_scheme")
        .build());

    List<PhoneAccountHandle> actual = telecomService.getPhoneAccountsSupportingScheme("some_scheme");

    assertThat(actual).contains(handleMatchingScheme);
    assertThat(actual).doesNotContain(handleNotMatchingScheme);
  }

  @Test
  @Config(sdk = {
      Build.VERSION_CODES.M})
  public void getCallCapablePhoneAccounts() {
    PhoneAccountHandle callCapableHandle = createHandler("id1");
    telecomService.registerPhoneAccount(PhoneAccount.builder(callCapableHandle, "enabled")
        .setIsEnabled(true)
        .build());
    PhoneAccountHandle notCallCapableHandler = createHandler("id2");
    telecomService.registerPhoneAccount(PhoneAccount.builder(notCallCapableHandler, "disabled")
        .setIsEnabled(false)
        .build());

    List<PhoneAccountHandle> callCapablePhoneAccounts = telecomService.getCallCapablePhoneAccounts();
    assertThat(callCapablePhoneAccounts).contains(callCapableHandle);
    assertThat(callCapablePhoneAccounts).doesNotContain(notCallCapableHandler);
  }

  @Test
  @Config(sdk = {
      Build.VERSION_CODES.LOLLIPOP_MR1,
      Build.VERSION_CODES.M})
  public void getPhoneAccountsForPackage() {
    PhoneAccountHandle handleInThisApplicationsPackage = createHandler("id1");
    telecomService.registerPhoneAccount(PhoneAccount.builder(handleInThisApplicationsPackage, "this_package")
        .build());

    PhoneAccountHandle anotherPackageHandle = createHandler("some.other.package", "id2");
    telecomService.registerPhoneAccount(PhoneAccount.builder(anotherPackageHandle, "another_package")
        .build());

    List<PhoneAccountHandle> phoneAccountsForPackage = telecomService.getPhoneAccountsForPackage();

    assertThat(phoneAccountsForPackage).contains(handleInThisApplicationsPackage);
    assertThat(phoneAccountsForPackage).doesNotContain(anotherPackageHandle);
  }

  private static PhoneAccountHandle createHandler(String id) {
    return createHandler(RuntimeEnvironment.application.getPackageName(), id);
  }

  private static PhoneAccountHandle createHandler(String packageName, String id) {
    return new PhoneAccountHandle(new ComponentName(packageName, "component_class_name"), id);
  }
}
