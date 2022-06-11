package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.robolectric.Shadows.shadowOf;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.telecom.ConnectionRequest;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telecom.VideoProfile;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowTelecomManager.CallRequestMode;
import org.robolectric.shadows.testing.TestConnectionService;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = LOLLIPOP)
public class ShadowTelecomManagerTest {

  @Rule public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Mock TestConnectionService.Listener connectionServiceListener;

  private TelecomManager telecomService;
  private Context context;

  @Before
  public void setUp() {
    telecomService =
        (TelecomManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.TELECOM_SERVICE);
    TestConnectionService.setListener(connectionServiceListener);
    context = ApplicationProvider.getApplicationContext();
  }

  @Test
  public void getSimCallManager() {
    PhoneAccountHandle handle = createHandle("id");

    shadowOf(telecomService).setSimCallManager(handle);

    assertThat(telecomService.getConnectionManager().getId()).isEqualTo("id");
  }

  @Test
  public void registerAndUnRegister() {
    assertThat(shadowOf(telecomService).getAllPhoneAccountsCount()).isEqualTo(0);
    assertThat(shadowOf(telecomService).getAllPhoneAccounts()).hasSize(0);

    PhoneAccountHandle handler = createHandle("id");
    PhoneAccount phoneAccount = PhoneAccount.builder(handler, "main_account").build();
    telecomService.registerPhoneAccount(phoneAccount);

    assertThat(shadowOf(telecomService).getAllPhoneAccountsCount()).isEqualTo(1);
    assertThat(shadowOf(telecomService).getAllPhoneAccounts()).hasSize(1);
    assertThat(telecomService.getAllPhoneAccountHandles()).hasSize(1);
    assertThat(telecomService.getAllPhoneAccountHandles()).contains(handler);
    assertThat(telecomService.getPhoneAccount(handler).getLabel().toString())
        .isEqualTo(phoneAccount.getLabel().toString());

    telecomService.unregisterPhoneAccount(handler);

    assertThat(shadowOf(telecomService).getAllPhoneAccountsCount()).isEqualTo(0);
    assertThat(shadowOf(telecomService).getAllPhoneAccounts()).hasSize(0);
    assertThat(telecomService.getAllPhoneAccountHandles()).hasSize(0);
  }

  @Test
  public void clearAccounts() {
    PhoneAccountHandle anotherPackageHandle =
        createHandle("some.other.package", "OtherConnectionService", "id");
    telecomService.registerPhoneAccount(
        PhoneAccount.builder(anotherPackageHandle, "another_package").build());
  }

  @Test
  @Config(minSdk = LOLLIPOP_MR1)
  public void clearAccountsForPackage() {
    PhoneAccountHandle accountHandle1 = createHandle("a.package", "OtherConnectionService", "id1");
    telecomService.registerPhoneAccount(PhoneAccount.builder(accountHandle1, "another_package")
        .build());

    PhoneAccountHandle accountHandle2 =
        createHandle("some.other.package", "OtherConnectionService", "id2");
    telecomService.registerPhoneAccount(PhoneAccount.builder(accountHandle2, "another_package")
        .build());

    telecomService.clearAccountsForPackage(accountHandle1.getComponentName().getPackageName());

    assertThat(telecomService.getPhoneAccount(accountHandle1)).isNull();
    assertThat(telecomService.getPhoneAccount(accountHandle2)).isNotNull();
  }

  @Test
  public void getPhoneAccountsSupportingScheme() {
    PhoneAccountHandle handleMatchingScheme = createHandle("id1");
    telecomService.registerPhoneAccount(
        PhoneAccount.builder(handleMatchingScheme, "some_scheme")
            .addSupportedUriScheme("some_scheme")
            .build());
    PhoneAccountHandle handleNotMatchingScheme = createHandle("id2");
    telecomService.registerPhoneAccount(
        PhoneAccount.builder(handleNotMatchingScheme, "another_scheme")
            .addSupportedUriScheme("another_scheme")
            .build());

    List<PhoneAccountHandle> actual =
        telecomService.getPhoneAccountsSupportingScheme("some_scheme");

    assertThat(actual).contains(handleMatchingScheme);
    assertThat(actual).doesNotContain(handleNotMatchingScheme);
  }

  @Test
  @Config(minSdk = M)
  public void getCallCapablePhoneAccounts() {
    PhoneAccountHandle callCapableHandle = createHandle("id1");
    telecomService.registerPhoneAccount(PhoneAccount.builder(callCapableHandle, "enabled")
        .setIsEnabled(true)
        .build());
    PhoneAccountHandle notCallCapableHandler = createHandle("id2");
    telecomService.registerPhoneAccount(PhoneAccount.builder(notCallCapableHandler, "disabled")
        .setIsEnabled(false)
        .build());

    List<PhoneAccountHandle> callCapablePhoneAccounts = telecomService.getCallCapablePhoneAccounts();
    assertThat(callCapablePhoneAccounts).contains(callCapableHandle);
    assertThat(callCapablePhoneAccounts).doesNotContain(notCallCapableHandler);
  }

  @Test
  @Config(minSdk = O)
  public void getSelfManagedPhoneAccounts() {
    PhoneAccountHandle selfManagedPhoneAccountHandle = createHandle("id1");
    telecomService.registerPhoneAccount(
        PhoneAccount.builder(selfManagedPhoneAccountHandle, "self-managed")
            .setCapabilities(PhoneAccount.CAPABILITY_SELF_MANAGED)
            .build());
    PhoneAccountHandle nonSelfManagedPhoneAccountHandle = createHandle("id2");
    telecomService.registerPhoneAccount(
        PhoneAccount.builder(nonSelfManagedPhoneAccountHandle, "not-self-managed").build());

    List<PhoneAccountHandle> selfManagedPhoneAccounts =
        telecomService.getSelfManagedPhoneAccounts();
    assertThat(selfManagedPhoneAccounts).containsExactly(selfManagedPhoneAccountHandle);
  }

  @Test
  @Config(minSdk = LOLLIPOP_MR1)
  public void getPhoneAccountsForPackage() {
    PhoneAccountHandle handleInThisApplicationsPackage = createHandle("id1");
    telecomService.registerPhoneAccount(
        PhoneAccount.builder(handleInThisApplicationsPackage, "this_package").build());

    PhoneAccountHandle anotherPackageHandle =
        createHandle("some.other.package", "OtherConnectionService", "id2");
    telecomService.registerPhoneAccount(
        PhoneAccount.builder(anotherPackageHandle, "another_package").build());

    List<PhoneAccountHandle> phoneAccountsForPackage = telecomService.getPhoneAccountsForPackage();

    assertThat(phoneAccountsForPackage).contains(handleInThisApplicationsPackage);
    assertThat(phoneAccountsForPackage).doesNotContain(anotherPackageHandle);
  }

  @Test
  public void testAddNewIncomingCall() {
    telecomService.addNewIncomingCall(createHandle("id"), null);

    assertThat(shadowOf(telecomService).getAllIncomingCalls()).hasSize(1);
    assertThat(shadowOf(telecomService).getLastIncomingCall()).isNotNull();
    assertThat(shadowOf(telecomService).getOnlyIncomingCall()).isNotNull();
  }

  @Test
  public void testAllowNewIncomingCall() {
    shadowOf(telecomService).setCallRequestMode(CallRequestMode.ALLOW_ALL);

    Uri address = Uri.parse("tel:+1-201-555-0123");
    PhoneAccountHandle phoneAccount = createHandle("id");
    Bundle extras = new Bundle();
    extras.putParcelable(TelecomManager.EXTRA_INCOMING_CALL_ADDRESS, address);
    extras.putInt(
        TelecomManager.EXTRA_START_CALL_WITH_VIDEO_STATE, VideoProfile.STATE_BIDIRECTIONAL);
    extras.putString("TEST_EXTRA_KEY", "TEST_EXTRA_VALUE");
    telecomService.addNewIncomingCall(createHandle("id"), extras);

    ArgumentCaptor<ConnectionRequest> requestCaptor =
        ArgumentCaptor.forClass(ConnectionRequest.class);
    verify(connectionServiceListener)
        .onCreateIncomingConnection(eq(phoneAccount), requestCaptor.capture());
    verifyNoMoreInteractions(connectionServiceListener);

    ConnectionRequest request = requestCaptor.getValue();
    assertThat(request.getAccountHandle()).isEqualTo(phoneAccount);
    assertThat(request.getExtras().getString("TEST_EXTRA_KEY")).isEqualTo("TEST_EXTRA_VALUE");
    assertThat(request.getAddress()).isEqualTo(address);
    assertThat(request.getVideoState()).isEqualTo(VideoProfile.STATE_BIDIRECTIONAL);
  }

  @Test
  @Config(minSdk = O)
  public void testDenyNewIncomingCall() {
    shadowOf(telecomService).setCallRequestMode(CallRequestMode.DENY_ALL);

    Uri address = Uri.parse("tel:+1-201-555-0123");
    PhoneAccountHandle phoneAccount = createHandle("id");
    Bundle extras = new Bundle();
    extras.putParcelable(TelecomManager.EXTRA_INCOMING_CALL_ADDRESS, address);
    extras.putInt(
        TelecomManager.EXTRA_START_CALL_WITH_VIDEO_STATE, VideoProfile.STATE_BIDIRECTIONAL);
    extras.putString("TEST_EXTRA_KEY", "TEST_EXTRA_VALUE");
    telecomService.addNewIncomingCall(createHandle("id"), extras);

    ArgumentCaptor<ConnectionRequest> requestCaptor =
        ArgumentCaptor.forClass(ConnectionRequest.class);
    verify(connectionServiceListener)
        .onCreateIncomingConnectionFailed(eq(phoneAccount), requestCaptor.capture());
    verifyNoMoreInteractions(connectionServiceListener);

    ConnectionRequest request = requestCaptor.getValue();
    assertThat(request.getAccountHandle()).isEqualTo(phoneAccount);
    assertThat(request.getExtras().getString("TEST_EXTRA_KEY")).isEqualTo("TEST_EXTRA_VALUE");
    assertThat(request.getAddress()).isEqualTo(address);
    assertThat(request.getVideoState()).isEqualTo(VideoProfile.STATE_BIDIRECTIONAL);
  }

  @Test
  @Config(minSdk = M)
  public void testPlaceCall() {
    Bundle extras = new Bundle();
    extras.putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, createHandle("id"));
    telecomService.placeCall(Uri.parse("tel:+1-201-555-0123"), extras);

    assertThat(shadowOf(telecomService).getAllOutgoingCalls()).hasSize(1);
    assertThat(shadowOf(telecomService).getLastOutgoingCall()).isNotNull();
    assertThat(shadowOf(telecomService).getOnlyOutgoingCall()).isNotNull();
  }

  @Test
  @Config(minSdk = M)
  public void testAllowPlaceCall() {
    shadowOf(telecomService).setCallRequestMode(CallRequestMode.ALLOW_ALL);

    Uri address = Uri.parse("tel:+1-201-555-0123");
    PhoneAccountHandle phoneAccount = createHandle("id");
    Bundle outgoingCallExtras = new Bundle();
    outgoingCallExtras.putString("TEST_EXTRA_KEY", "TEST_EXTRA_VALUE");
    Bundle extras = new Bundle();
    extras.putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, phoneAccount);
    extras.putInt(
        TelecomManager.EXTRA_START_CALL_WITH_VIDEO_STATE, VideoProfile.STATE_BIDIRECTIONAL);
    extras.putBundle(TelecomManager.EXTRA_OUTGOING_CALL_EXTRAS, outgoingCallExtras);
    telecomService.placeCall(address, extras);

    ArgumentCaptor<ConnectionRequest> requestCaptor =
        ArgumentCaptor.forClass(ConnectionRequest.class);
    verify(connectionServiceListener)
        .onCreateOutgoingConnection(eq(phoneAccount), requestCaptor.capture());
    verifyNoMoreInteractions(connectionServiceListener);

    ConnectionRequest request = requestCaptor.getValue();
    assertThat(request.getAccountHandle()).isEqualTo(phoneAccount);
    assertThat(request.getExtras().getString("TEST_EXTRA_KEY")).isEqualTo("TEST_EXTRA_VALUE");
    assertThat(request.getAddress()).isEqualTo(address);
    assertThat(request.getVideoState()).isEqualTo(VideoProfile.STATE_BIDIRECTIONAL);
  }

  @Test
  @Config(minSdk = O)
  public void testDenyPlaceCall() {
    shadowOf(telecomService).setCallRequestMode(CallRequestMode.DENY_ALL);

    Uri address = Uri.parse("tel:+1-201-555-0123");
    PhoneAccountHandle phoneAccount = createHandle("id");
    Bundle outgoingCallExtras = new Bundle();
    outgoingCallExtras.putString("TEST_EXTRA_KEY", "TEST_EXTRA_VALUE");
    Bundle extras = new Bundle();
    extras.putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, phoneAccount);
    extras.putInt(
        TelecomManager.EXTRA_START_CALL_WITH_VIDEO_STATE, VideoProfile.STATE_BIDIRECTIONAL);
    extras.putBundle(TelecomManager.EXTRA_OUTGOING_CALL_EXTRAS, outgoingCallExtras);
    telecomService.placeCall(address, extras);

    ArgumentCaptor<ConnectionRequest> requestCaptor =
        ArgumentCaptor.forClass(ConnectionRequest.class);
    verify(connectionServiceListener)
        .onCreateOutgoingConnectionFailed(eq(phoneAccount), requestCaptor.capture());
    verifyNoMoreInteractions(connectionServiceListener);

    ConnectionRequest request = requestCaptor.getValue();
    assertThat(request.getAccountHandle()).isEqualTo(phoneAccount);
    assertThat(request.getExtras().getString("TEST_EXTRA_KEY")).isEqualTo("TEST_EXTRA_VALUE");
    assertThat(request.getAddress()).isEqualTo(address);
    assertThat(request.getVideoState()).isEqualTo(VideoProfile.STATE_BIDIRECTIONAL);
  }

  @Test
  public void testAddUnknownCall() {
    telecomService.addNewUnknownCall(createHandle("id"), null);

    assertThat(shadowOf(telecomService).getAllUnknownCalls()).hasSize(1);
    assertThat(shadowOf(telecomService).getLastUnknownCall()).isNotNull();
    assertThat(shadowOf(telecomService).getOnlyUnknownCall()).isNotNull();
  }

  @Test
  public void testIsRinging_noIncomingOrUnknownCallsAdded_shouldBeFalse() {
    assertThat(shadowOf(telecomService).isRinging()).isFalse();
  }

  @Test
  public void testIsRinging_incomingCallAdded_shouldBeTrue() {
    telecomService.addNewIncomingCall(createHandle("id"), null);

    assertThat(shadowOf(telecomService).isRinging()).isTrue();
  }

  @Test
  public void testIsRinging_unknownCallAdded_shouldBeTrue() {
    shadowOf(telecomService).addNewUnknownCall(createHandle("id"), null);

    assertThat(shadowOf(telecomService).isRinging()).isTrue();
  }

  @Test
  public void testIsRinging_incomingCallAdded_thenRingerSilenced_shouldBeFalse() {
    telecomService.addNewIncomingCall(createHandle("id"), null);
    telecomService.silenceRinger();

    assertThat(shadowOf(telecomService).isRinging()).isFalse();
  }

  @Test
  public void testIsRinging_unknownCallAdded_thenRingerSilenced_shouldBeFalse() {
    shadowOf(telecomService).addNewUnknownCall(createHandle("id"), null);
    telecomService.silenceRinger();

    assertThat(shadowOf(telecomService).isRinging()).isFalse();
  }

  @Test
  public void testIsRinging_ringerSilenced_thenIncomingCallAdded_shouldBeTrue() {
    telecomService.silenceRinger();
    telecomService.addNewIncomingCall(createHandle("id"), null);

    assertThat(shadowOf(telecomService).isRinging()).isTrue();
  }

  @Test
  public void testIsRinging_ringerSilenced_thenUnknownCallAdded_shouldBeTrue() {
    telecomService.silenceRinger();
    shadowOf(telecomService).addNewUnknownCall(createHandle("id"), null);

    assertThat(shadowOf(telecomService).isRinging()).isTrue();
  }

  @Test
  @Config(minSdk = M)
  public void setDefaultDialer() {
    assertThat(telecomService.getDefaultDialerPackage()).isNull();
    shadowOf(telecomService).setDefaultDialer("some.package");
    assertThat(telecomService.getDefaultDialerPackage()).isEqualTo("some.package");
  }

  @Test
  @Config(minSdk = M)
  public void setDefaultDialerPackage() {
    assertThat(telecomService.getDefaultDialerPackage()).isNull();
    shadowOf(telecomService).setDefaultDialerPackage("some.package");
    assertThat(telecomService.getDefaultDialerPackage()).isEqualTo("some.package");
  }

  @Test
  @Config(minSdk = Q)
  public void setSystemDefaultDialerPackage() {
    assertThat(telecomService.getSystemDialerPackage()).isNull();
    shadowOf(telecomService).setSystemDialerPackage("some.package");
    assertThat(telecomService.getSystemDialerPackage()).isEqualTo("some.package");
  }

  @Test
  public void setTtySupported() {
    assertThat(telecomService.isTtySupported()).isFalse();
    shadowOf(telecomService).setTtySupported(true);
    assertThat(telecomService.isTtySupported()).isTrue();
  }

  @Test
  public void canSetAndGetIsInCall() {
    shadowOf(telecomService).setIsInCall(true);
    assertThat(telecomService.isInCall()).isTrue();
  }

  @Test
  public void isInCall_setIsInCallNotCalled_shouldReturnFalse() {
    assertThat(telecomService.isInCall()).isFalse();
  }

  @Test
  public void getDefaultOutgoingPhoneAccount() {
    // Check initial state
    assertThat(telecomService.getDefaultOutgoingPhoneAccount("abc")).isNull();

    // After setting
    PhoneAccountHandle phoneAccountHandle = createHandle("id1");
    shadowOf(telecomService).setDefaultOutgoingPhoneAccount("abc", phoneAccountHandle);
    assertThat(telecomService.getDefaultOutgoingPhoneAccount("abc")).isEqualTo(phoneAccountHandle);

    // After removing
    shadowOf(telecomService).removeDefaultOutgoingPhoneAccount("abc");
    assertThat(telecomService.getDefaultOutgoingPhoneAccount("abc")).isNull();
  }

  @Config(minSdk = R)
  @Test
  public void createLaunchEmergencyDialerIntent_shouldReturnValidIntent() {
    Intent intent = telecomService.createLaunchEmergencyDialerIntent(/* number= */ null);
    assertThat(intent.getAction()).isEqualTo(Intent.ACTION_DIAL_EMERGENCY);
  }

  @Config(minSdk = R)
  @Test
  public void createLaunchEmergencyDialerIntent_whenPackageAvailable_shouldContainPackage()
      throws NameNotFoundException {
    ComponentName componentName = new ComponentName("com.android.phone", "EmergencyDialer");
    shadowOf(context.getPackageManager()).addActivityIfNotPresent(componentName);

    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(Intent.ACTION_DIAL_EMERGENCY);

    shadowOf(context.getPackageManager()).addIntentFilterForActivity(componentName, intentFilter);

    Intent intent = telecomService.createLaunchEmergencyDialerIntent(/* number= */ null);
    assertThat(intent.getAction()).isEqualTo(Intent.ACTION_DIAL_EMERGENCY);
    assertThat(intent.getPackage()).isEqualTo("com.android.phone");
  }

  @Config(minSdk = R)
  @Test
  public void
      createLaunchEmergencyDialerIntent_whenSetPhoneNumber_shouldReturnValidIntentWithPhoneNumber() {
    Intent intent = telecomService.createLaunchEmergencyDialerIntent("1234");
    assertThat(intent.getAction()).isEqualTo(Intent.ACTION_DIAL_EMERGENCY);
    Uri uri = intent.getData();
    assertThat(uri.toString()).isEqualTo("tel:1234");
  }

  @Test
  @Config(minSdk = Q)
  public void getUserSelectedOutgoingPhoneAccount() {
    // Check initial state
    assertThat(telecomService.getUserSelectedOutgoingPhoneAccount()).isNull();

    // Set a phone account and verify
    PhoneAccountHandle phoneAccountHandle = createHandle("id1");
    shadowOf(telecomService).setUserSelectedOutgoingPhoneAccount(phoneAccountHandle);
    assertThat(telecomService.getUserSelectedOutgoingPhoneAccount()).isEqualTo(phoneAccountHandle);
  }

  @Test
  @Config(minSdk = N)
  public void testSetManageBlockNumbersIntent() {
    // Check initial state
    Intent targetIntent = telecomService.createManageBlockedNumbersIntent();
    assertThat(targetIntent).isNull();

    // Set intent and verify
    Intent initialIntent = new Intent();
    shadowOf(telecomService).setManageBlockNumbersIntent(initialIntent);

    targetIntent = telecomService.createManageBlockedNumbersIntent();
    assertThat(initialIntent).isEqualTo(targetIntent);
  }

  @Test
  @Config(minSdk = M)
  public void isVoicemailNumber() {
    // Check initial state
    PhoneAccountHandle phoneAccountHandle = createHandle("id1");
    assertThat(telecomService.isVoiceMailNumber(phoneAccountHandle, "123")).isFalse();

    // After setting
    shadowOf(telecomService).setVoicemailNumber(phoneAccountHandle, "123");
    assertThat(telecomService.isVoiceMailNumber(phoneAccountHandle, "123")).isTrue();

    // After reset
    shadowOf(telecomService).setVoicemailNumber(phoneAccountHandle, null);
    assertThat(telecomService.isVoiceMailNumber(phoneAccountHandle, "123")).isFalse();
  }

  @Test
  @Config(minSdk = M)
  public void getVoicemailNumber() {
    // Check initial state
    PhoneAccountHandle phoneAccountHandle = createHandle("id1");
    assertThat(telecomService.getVoiceMailNumber(phoneAccountHandle)).isNull();

    // After setting
    shadowOf(telecomService).setVoicemailNumber(phoneAccountHandle, "123");
    assertThat(telecomService.getVoiceMailNumber(phoneAccountHandle)).isEqualTo("123");

    // After reset
    shadowOf(telecomService).setVoicemailNumber(phoneAccountHandle, null);
    assertThat(telecomService.getVoiceMailNumber(phoneAccountHandle)).isNull();
  }

  private static PhoneAccountHandle createHandle(String id) {
    return new PhoneAccountHandle(
        new ComponentName(ApplicationProvider.getApplicationContext(), TestConnectionService.class),
        id);
  }

  private static PhoneAccountHandle createHandle(String packageName, String className, String id) {
    return new PhoneAccountHandle(new ComponentName(packageName, className), id);
  }
}
