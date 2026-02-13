package org.robolectric.shadows;

import static android.Manifest.permission.ASSOCIATE_COMPANION_DEVICES;
import static android.os.Build.VERSION_CODES.BAKLAVA;
import static android.os.Build.VERSION_CODES.O;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.google.common.truth.Truth.assertThat;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.Assert.assertThrows;
import static org.robolectric.Shadows.shadowOf;

import android.app.Application;
import android.companion.AssociationInfo;
import android.companion.AssociationRequest;
import android.companion.CompanionDeviceManager;
import android.companion.DeviceNotAssociatedException;
import android.companion.ObservingDevicePresenceRequest;
import android.content.ComponentName;
import android.content.IntentSender;
import android.net.MacAddress;
import android.os.Build.VERSION_CODES;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import javax.annotation.Nonnull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/** Unit test for ShadowCompanionDeviceManager. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = O)
public class ShadowCompanionDeviceManagerTest {

  private static final String MAC_ADDRESS = "AA:BB:CC:DD:FF:EE";
  private static final String PACKAGE_NAME = "org.robolectric";

  private final Application application = getApplicationContext();
  private final ExecutorService executorService = Executors.newSingleThreadExecutor();

  private ComponentName componentName;
  private CompanionDeviceManager companionDeviceManager;
  private ShadowCompanionDeviceManager shadowCompanionDeviceManager;

  @Before
  public void setUp() throws Exception {
    companionDeviceManager = application.getSystemService(CompanionDeviceManager.class);
    shadowCompanionDeviceManager = shadowOf(companionDeviceManager);
    componentName = new ComponentName(application, Application.class);
  }

  @After
  public void tearDown() throws Exception {
    executorService.shutdownNow();
  }

  @Test
  public void testAddAssociation() {
    assertThat(companionDeviceManager.getAssociations()).isEmpty();
    shadowCompanionDeviceManager.addAssociation(MAC_ADDRESS);
    assertThat(companionDeviceManager.getAssociations()).contains(MAC_ADDRESS);
  }

  @Test
  public void testDisassociate() {
    shadowCompanionDeviceManager.addAssociation(MAC_ADDRESS);
    companionDeviceManager.disassociate(MAC_ADDRESS);
    assertThat(companionDeviceManager.getAssociations()).isEmpty();
  }

  @Config(minSdk = VERSION_CODES.TIRAMISU)
  @Test
  public void testAddAssociation_withAssociationInfo() {
    AssociationInfo associationInfo =
        AssociationInfoBuilder.newBuilder().setId(100).setDeviceMacAddress(MAC_ADDRESS).build();

    assertThat(companionDeviceManager.getAssociations()).isEmpty();
    shadowOf(companionDeviceManager).addAssociation(associationInfo);
    assertThat(companionDeviceManager.getAssociations())
        .contains(MAC_ADDRESS.toLowerCase(Locale.ROOT));
  }

  @Test
  public void testDisassociate_throwsIfNotFound() {
    assertThrows(Exception.class, () -> companionDeviceManager.disassociate(MAC_ADDRESS));
  }

  @Test
  public void testHasNotificationAccess() {
    shadowCompanionDeviceManager.addAssociation(MAC_ADDRESS);

    assertThat(companionDeviceManager.hasNotificationAccess(componentName)).isFalse();
    shadowCompanionDeviceManager.setNotificationAccess(componentName, true);
    assertThat(companionDeviceManager.hasNotificationAccess(componentName)).isTrue();

    shadowCompanionDeviceManager.setNotificationAccess(componentName, false);
    assertThat(companionDeviceManager.hasNotificationAccess(componentName)).isFalse();
  }

  @Test
  public void testHasNotificationAccess_throwsIfNotAssociated() {
    assertThrows(
        Exception.class, () -> companionDeviceManager.hasNotificationAccess(componentName));
  }

  @Test
  public void testRequestNotificationAccess() {
    shadowCompanionDeviceManager.addAssociation(MAC_ADDRESS);

    companionDeviceManager.requestNotificationAccess(componentName);
    assertThat(shadowCompanionDeviceManager.getLastRequestedNotificationAccess())
        .isEqualTo(componentName);
  }

  @Test
  public void testRequestNotificationAccess_throwsIfNotAssociated() {
    assertThrows(
        Exception.class, () -> companionDeviceManager.requestNotificationAccess(componentName));
  }

  @Test
  public void testAssociate() {
    AssociationRequest request = new AssociationRequest.Builder().build();
    CompanionDeviceManager.Callback callback =
        new CompanionDeviceManager.Callback() {
          @Override
          public void onDeviceFound(@Nonnull IntentSender chooserLauncher) {}

          @Override
          public void onFailure(CharSequence error) {}
        };
    companionDeviceManager.associate(request, callback, null);

    assertThat(shadowCompanionDeviceManager.getLastAssociationRequest()).isSameInstanceAs(request);
    assertThat(shadowCompanionDeviceManager.getLastAssociationCallback())
        .isSameInstanceAs(callback);
  }

  @Test
  public void testAddAssociation_byMacAddress() {
    assertThat(companionDeviceManager.getAssociations()).isEmpty();
    shadowCompanionDeviceManager.addAssociation(MAC_ADDRESS);
    assertThat(companionDeviceManager.getAssociations()).contains(MAC_ADDRESS);
  }

  @Test
  @Config(minSdk = VERSION_CODES.TIRAMISU)
  public void testAddAssociation_byAssociationInfo_defaultValue() {
    AssociationInfoBuilder infoBuilder =
        AssociationInfoBuilder.newBuilder()
            .setId(1)
            .setUserId(1)
            .setDeviceMacAddress(MAC_ADDRESS)
            .setDisplayName("displayName")
            .setSystemDataSyncFlags(-1);
    AssociationInfo info = infoBuilder.build();

    AssociationInfoBuilder expectedInfoBuilder =
        AssociationInfoBuilder.newBuilder()
            .setId(1)
            .setUserId(1)
            .setDeviceMacAddress(MAC_ADDRESS)
            .setDisplayName("displayName")
            .setSelfManaged(false)
            .setNotifyOnDeviceNearby(false)
            .setRevoked(false)
            .setApprovedMs(0)
            .setLastTimeConnectedMs(0)
            .setSystemDataSyncFlags(-1);
    AssociationInfo expectedInfo = expectedInfoBuilder.build();
    assertThat(companionDeviceManager.getAssociations()).isEmpty();
    shadowCompanionDeviceManager.addAssociation(info);
    assertThat(companionDeviceManager.getMyAssociations()).contains(expectedInfo);
    assertThat(companionDeviceManager.getAllAssociations()).contains(expectedInfo);
  }

  @Test
  @Config(minSdk = VERSION_CODES.TIRAMISU)
  public void testAddAssociation_byAssociationInfo() {
    AssociationInfoBuilder infoBuilder =
        AssociationInfoBuilder.newBuilder()
            .setId(1)
            .setUserId(1)
            .setPackageName("packageName")
            .setDeviceMacAddress(MAC_ADDRESS)
            .setDisplayName("displayName")
            .setSelfManaged(false)
            .setNotifyOnDeviceNearby(false)
            .setApprovedMs(0)
            .setLastTimeConnectedMs(0);
    Object associatedDeviceValue;
    if (ReflectionHelpers.hasField(AssociationInfo.class, "mAssociatedDevice")) {
      try {
        Class<?> associatedDeviceClazz = Class.forName("android.companion.AssociatedDevice");
        associatedDeviceValue = ReflectionHelpers.newInstance(associatedDeviceClazz);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
      infoBuilder = infoBuilder.setAssociatedDevice(associatedDeviceValue);
    }
    int systemDataSyncFlagsValue = 1;
    String newTagValue = "newTag";
    boolean revokedValue = true;
    if (ReflectionHelpers.hasField(AssociationInfo.class, "mSystemDataSyncFlags")) {
      infoBuilder = infoBuilder.setSystemDataSyncFlags(systemDataSyncFlagsValue);
    }
    if (ReflectionHelpers.hasField(AssociationInfo.class, "mTag")) {
      infoBuilder = infoBuilder.setTag(newTagValue);
    }
    if (ReflectionHelpers.hasField(AssociationInfo.class, "mRevoked")) {
      infoBuilder = infoBuilder.setRevoked(revokedValue);
    }

    AssociationInfo info = infoBuilder.build();
    if (ReflectionHelpers.hasField(AssociationInfo.class, "mSystemDataSyncFlags")) {
      int systemDataSyncFlags =
          ReflectionHelpers.callInstanceMethod(info, "getSystemDataSyncFlags");
      assertThat(systemDataSyncFlags).isEqualTo(systemDataSyncFlagsValue);
    }
    if (ReflectionHelpers.hasField(AssociationInfo.class, "mTag")) {
      String tag = ReflectionHelpers.callInstanceMethod(info, "getTag");
      assertThat(tag).isEqualTo(newTagValue);
    }
    if (ReflectionHelpers.hasField(AssociationInfo.class, "mRevoked")) {
      boolean revoked = ReflectionHelpers.callInstanceMethod(info, "isRevoked");
      assertThat(revoked).isEqualTo(revokedValue);
    }

    assertThat(companionDeviceManager.getAssociations()).isEmpty();
    shadowCompanionDeviceManager.addAssociation(info);
    assertThat(companionDeviceManager.getMyAssociations()).contains(info);
    assertThat(companionDeviceManager.getAllAssociations()).contains(info);
  }

  @Test
  public void testDisassociate_byMacAddress() {
    shadowCompanionDeviceManager.addAssociation(MAC_ADDRESS);
    companionDeviceManager.disassociate(MAC_ADDRESS);
    assertThat(companionDeviceManager.getAssociations()).isEmpty();
  }

  @Test
  @Config(minSdk = VERSION_CODES.TIRAMISU)
  public void testDisassociate_byId() {
    shadowCompanionDeviceManager.addAssociation(MAC_ADDRESS);
    // default ID is 1
    companionDeviceManager.disassociate(1);
    assertThat(companionDeviceManager.getAssociations()).isEmpty();
    assertThat(companionDeviceManager.getMyAssociations()).isEmpty();
    assertThat(companionDeviceManager.getAllAssociations()).isEmpty();
  }

  @Test
  public void testDisassociate_byMacAddress_throwsIfNotFound() {
    assertThrows(Exception.class, () -> companionDeviceManager.disassociate(MAC_ADDRESS));
  }

  @Test
  @Config(minSdk = VERSION_CODES.TIRAMISU)
  public void testDisassociate_byId_throwsIfNotFound() {
    assertThrows(Exception.class, () -> companionDeviceManager.disassociate(0));
  }

  @Test
  public void testAssociate_handlerVariant_updatesShadow() {
    AssociationRequest request = new AssociationRequest.Builder().build();
    CompanionDeviceManager.Callback callback = createCallback();

    companionDeviceManager.associate(request, callback, null);

    assertThat(shadowCompanionDeviceManager.getLastAssociationRequest()).isSameInstanceAs(request);
    assertThat(shadowCompanionDeviceManager.getLastAssociationCallback())
        .isSameInstanceAs(callback);
  }

  @Test
  @Config(minSdk = VERSION_CODES.TIRAMISU)
  public void testAssociate_executorVariant_updatesShadow() {
    AssociationRequest request = new AssociationRequest.Builder().build();
    CompanionDeviceManager.Callback callback = createCallback();
    companionDeviceManager.associate(request, executorService, callback);

    assertThat(shadowCompanionDeviceManager.getLastAssociationRequest()).isSameInstanceAs(request);
    assertThat(shadowCompanionDeviceManager.getLastAssociationCallback())
        .isSameInstanceAs(callback);
  }

  @Test
  @Config(minSdk = VERSION_CODES.TIRAMISU)
  public void notifyDeviceAppeared() {
    ReflectionHelpers.callInstanceMethod(
        companionDeviceManager, "notifyDeviceAppeared", ClassParameter.from(int.class, 1));
  }

  @Test
  @Config(minSdk = VERSION_CODES.TIRAMISU)
  public void testStartObservingDevicePresence_deviceNotAssociated_throwsException() {
    assertThrows(
        DeviceNotAssociatedException.class,
        () -> companionDeviceManager.startObservingDevicePresence(MAC_ADDRESS));
    assertThat(shadowCompanionDeviceManager.getLastObservingDevicePresenceDeviceAddress())
        .isEqualTo(MAC_ADDRESS);
  }

  @Test
  @Config(minSdk = VERSION_CODES.TIRAMISU)
  public void testStartObservingDevicePresence_deviceAssociated_presenceObserved() {
    shadowCompanionDeviceManager.addAssociation(MAC_ADDRESS);

    companionDeviceManager.startObservingDevicePresence(MAC_ADDRESS);
    assertThat(shadowCompanionDeviceManager.getLastObservingDevicePresenceDeviceAddress())
        .isEqualTo(MAC_ADDRESS);
  }

  @Test
  @Config(minSdk = VERSION_CODES.BAKLAVA)
  public void testStartObservingDevicePresence_withRequest_deviceAssociated_presenceObserved() {
    int associationId = 1;
    AssociationInfo info =
        AssociationInfoBuilder.newBuilder()
            .setId(associationId)
            .setDeviceMacAddress(MAC_ADDRESS)
            .build();
    shadowCompanionDeviceManager.addAssociation(info);

    ObservingDevicePresenceRequest request =
        new ObservingDevicePresenceRequest.Builder().setAssociationId(associationId).build();
    shadowCompanionDeviceManager.startObservingDevicePresence(request);

    assertThat(shadowCompanionDeviceManager.getLastObservingDevicePresenceRequestAssociationId())
        .isEqualTo(associationId);
    assertThat(shadowCompanionDeviceManager.isObservingDevicePresence(associationId)).isTrue();
  }

  @Test
  @Config(minSdk = VERSION_CODES.BAKLAVA)
  public void
      testStartObservingDevicePresence_withRequest_deviceNotAssociated_presenceNotObserved() {
    int nonExistentAssociationId = 999;
    ObservingDevicePresenceRequest request =
        new ObservingDevicePresenceRequest.Builder()
            .setAssociationId(nonExistentAssociationId)
            .build();

    shadowCompanionDeviceManager.startObservingDevicePresence(request);

    assertThat(shadowCompanionDeviceManager.getLastObservingDevicePresenceRequestAssociationId())
        .isEqualTo(nonExistentAssociationId);
    assertThat(shadowCompanionDeviceManager.isObservingDevicePresence(nonExistentAssociationId))
        .isFalse();
  }

  @Test
  @Config(minSdk = VERSION_CODES.TIRAMISU)
  public void
      testGetLastObservingDevicePresenceDeviceAddress_startObservingDevicePresenceNotCalled_returnsNull() {
    assertThat(shadowCompanionDeviceManager.getLastObservingDevicePresenceDeviceAddress()).isNull();
  }

  @Test
  @Config(minSdk = VERSION_CODES.BAKLAVA)
  public void
      testGetLastObservingDevicePresenceRequestAssociationId_startObservingDevicePresenceNotCalled_returnsNegative() {
    assertThat(shadowCompanionDeviceManager.getLastObservingDevicePresenceRequestAssociationId())
        .isEqualTo(-1);
  }

  @Test
  @Config(minSdk = VERSION_CODES.TIRAMISU)
  public void testAssociate_systemApi_deviceAssociated() {
    MacAddress macAddress = MacAddress.fromString(MAC_ADDRESS);
    shadowOf(application).grantPermissions(ASSOCIATE_COMPANION_DEVICES);

    companionDeviceManager.associate(PACKAGE_NAME, macAddress, new byte[] {0x01});
    assertThat(companionDeviceManager.getAssociations()).contains(macAddress.toString());
    assertThat(shadowCompanionDeviceManager.getLastSystemApiAssociationMacAddress())
        .isEqualTo(macAddress);
  }

  @Test
  @Config(minSdk = VERSION_CODES.TIRAMISU)
  public void testGetLastSystemApiAssociationMacAddress_associateCalled_returnsLastMacAddress() {
    MacAddress macAddress = MacAddress.fromString(MAC_ADDRESS);
    shadowOf(application).grantPermissions(ASSOCIATE_COMPANION_DEVICES);

    companionDeviceManager.associate(PACKAGE_NAME, macAddress, new byte[] {0x01});
    assertThat(companionDeviceManager.getAssociations()).contains(macAddress.toString());
    assertThat(shadowCompanionDeviceManager.getLastSystemApiAssociationMacAddress())
        .isEqualTo(macAddress);
  }

  @Test
  @Config(minSdk = VERSION_CODES.TIRAMISU)
  public void testGetLastSystemApiAssociationMacAddress_associateNotCalled_returnsNull() {
    assertThat(shadowCompanionDeviceManager.getLastSystemApiAssociationMacAddress()).isNull();
  }

  @Test
  @Config(minSdk = VERSION_CODES.TIRAMISU)
  public void testAssociate_systemApi_permissionDeniedDeviceNotAssociated() {
    MacAddress macAddress = MacAddress.fromString(MAC_ADDRESS);
    shadowOf(application).denyPermissions(ASSOCIATE_COMPANION_DEVICES);

    assertThrows(
        SecurityException.class,
        () -> companionDeviceManager.associate(PACKAGE_NAME, macAddress, new byte[] {0x01}));
    assertThat(shadowCompanionDeviceManager.getLastSystemApiAssociationMacAddress())
        .isEqualTo(macAddress);
  }

  @Test
  @Config(minSdk = VERSION_CODES.TIRAMISU)
  public void testAssociate_systemApi_badPackageNameDeviceNotAssociated() {
    MacAddress macAddress = MacAddress.fromString(MAC_ADDRESS);
    shadowOf(application).grantPermissions(ASSOCIATE_COMPANION_DEVICES);

    assertThrows(
        SecurityException.class,
        () -> companionDeviceManager.associate("some.package", macAddress, new byte[] {0x01}));
    assertThat(shadowCompanionDeviceManager.getLastSystemApiAssociationMacAddress())
        .isEqualTo(macAddress);
  }

  @Test
  @Config(minSdk = VERSION_CODES.TIRAMISU)
  public void testAssociate_systemApi_badCertificateDeviceNotAssociated() {
    MacAddress macAddress = MacAddress.fromString(MAC_ADDRESS);
    shadowOf(application).grantPermissions(ASSOCIATE_COMPANION_DEVICES);

    assertThrows(
        SecurityException.class,
        () -> companionDeviceManager.associate(PACKAGE_NAME, macAddress, null));
    assertThat(shadowCompanionDeviceManager.getLastSystemApiAssociationMacAddress())
        .isEqualTo(macAddress);
  }

  @Test
  @Config(minSdk = BAKLAVA)
  public void testRemoveBond_returnsTrueWhenPreSpecified() {
    shadowCompanionDeviceManager.addAssociation(MAC_ADDRESS);
    int id = shadowCompanionDeviceManager.getMyAssociations().get(0).getId();

    shadowCompanionDeviceManager.markAssociationBondRemovable(id);
    boolean result = shadowCompanionDeviceManager.removeBond(id);

    assertThat(result).isTrue();
    assertThat(shadowCompanionDeviceManager.getLastRemoveBondAssociationId()).isEqualTo(id);
  }

  @Test
  @Config(minSdk = BAKLAVA)
  public void testRemoveBond_returnsFalseWhenPreSpecified() {
    shadowCompanionDeviceManager.addAssociation(MAC_ADDRESS);
    int id = shadowCompanionDeviceManager.getMyAssociations().get(0).getId();

    shadowCompanionDeviceManager.markAssociationBondNotRemovable(id);
    boolean result = shadowCompanionDeviceManager.removeBond(id);

    assertThat(result).isFalse();
    assertThat(shadowCompanionDeviceManager.getLastRemoveBondAssociationId()).isEqualTo(id);
  }

  @Test
  @Config(minSdk = BAKLAVA)
  public void testRemoveBond_returnsFalseIfNoAssociation() {
    shadowCompanionDeviceManager.markAssociationBondRemovable(1);

    boolean result = shadowCompanionDeviceManager.removeBond(1);

    assertThat(result).isFalse();
    assertThat(shadowCompanionDeviceManager.getLastRemoveBondAssociationId()).isEqualTo(1);
  }

  @Test
  @Config(minSdk = BAKLAVA)
  public void testGetLastRemoveBondAssociationBeforeCalled() {
    assertThat(shadowCompanionDeviceManager.getLastRemoveBondAssociationId()).isEqualTo(-1);
  }

  @Test
  @Config(minSdk = BAKLAVA)
  public void testRemoveBond_returnsFalseIfNoValuePreSpecified() {
    shadowCompanionDeviceManager.addAssociation(MAC_ADDRESS);
    int id = shadowCompanionDeviceManager.getMyAssociations().get(0).getId();

    boolean result = shadowCompanionDeviceManager.removeBond(id);

    assertThat(result).isFalse();
    assertThat(shadowCompanionDeviceManager.getLastRemoveBondAssociationId()).isEqualTo(id);
  }

  @Test
  @Config(minSdk = VERSION_CODES.UPSIDE_DOWN_CAKE)
  public void testAttachSystemDataTransport_updatesShadow() {
    shadowCompanionDeviceManager.addAssociation(MAC_ADDRESS);
    int id = shadowCompanionDeviceManager.getMyAssociations().get(0).getId();
    InputStream inputStream = new ByteArrayInputStream(new byte[] {0x01});
    OutputStream outputStream = new ByteArrayOutputStream();

    companionDeviceManager.attachSystemDataTransport(id, inputStream, outputStream);

    assertThat(shadowCompanionDeviceManager.getAttachedInputStream(id))
        .isSameInstanceAs(inputStream);
    assertThat(shadowCompanionDeviceManager.getAttachedOutputStream(id))
        .isSameInstanceAs(outputStream);

    companionDeviceManager.detachSystemDataTransport(id);
    assertThat(shadowCompanionDeviceManager.getAttachedInputStream(id)).isNull();
    assertThat(shadowCompanionDeviceManager.getAttachedOutputStream(id)).isNull();
  }

  @Test
  public void testGetAttachedInputStream_returnsNullIfNoTransportAttached() {
    assertThat(shadowCompanionDeviceManager.getAttachedInputStream(1)).isNull();
  }

  @Test
  public void testGetAttachedOutputStream_returnsNullIfNoTransportAttached() {
    assertThat(shadowCompanionDeviceManager.getAttachedOutputStream(1)).isNull();
  }

  @Test
  @Config(minSdk = VERSION_CODES.VANILLA_ICE_CREAM)
  public void testAddOnMessageReceivedListener_addsListener() {
    TestCdmListener testListener = new TestCdmListener();
    int messageType = 100;

    companionDeviceManager.addOnMessageReceivedListener(executorService, messageType, testListener);

    assertThat(shadowCompanionDeviceManager.getMessageReceivedListeners(messageType))
        .containsExactly(testListener);
  }

  @Test
  @Config(minSdk = VERSION_CODES.VANILLA_ICE_CREAM)
  public void testGetLastMessageListener_returnsNullIfNoListenerAdded() {
    assertThat(shadowCompanionDeviceManager.getMessageReceivedListeners(100)).isEmpty();
  }

  @Test
  @Config(minSdk = VERSION_CODES.VANILLA_ICE_CREAM)
  public void testRemoveOnMessageReceivedListener_removesListener() {
    TestCdmListener testListener = new TestCdmListener();
    int messageType = 100;
    companionDeviceManager.addOnMessageReceivedListener(executorService, messageType, testListener);

    companionDeviceManager.removeOnMessageReceivedListener(messageType, testListener);

    assertThat(shadowCompanionDeviceManager.getMessageReceivedListeners(messageType)).isEmpty();
  }

  @Test
  @Config(minSdk = VERSION_CODES.VANILLA_ICE_CREAM)
  public void testSendMessage_sendsMessageToAddedListeners() throws Exception {
    TestCdmListener listener = new TestCdmListener();
    byte[] data = new byte[] {0x01};
    int messageType = 100;
    int receiverId = 10;
    shadowCompanionDeviceManager.setSelfAssociationInfo(
        AssociationInfoBuilder.newBuilder().setDisplayName("self").setId(receiverId).build());
    companionDeviceManager.addOnMessageReceivedListener(executorService, messageType, listener);

    int senderId = 20;
    shadowCompanionDeviceManager.setSelfAssociationInfo(
        AssociationInfoBuilder.newBuilder()
            .setDeviceMacAddress(MAC_ADDRESS)
            .setId(senderId)
            .build());

    companionDeviceManager.sendMessage(messageType, data, new int[] {0, receiverId});

    assertThat(listener.awaitInvocation()).isTrue();
    assertThat(listener.receivedId).isEqualTo(senderId);
    assertThat(listener.receivedData).isEqualTo(data);
  }

  @Test
  @Config(minSdk = VERSION_CODES.VANILLA_ICE_CREAM)
  public void testRemoveOnMessageReceivedListener_noLongerReceivesMessages() throws Exception {
    TestCdmListener listener1 = new TestCdmListener();
    TestCdmListener listener2 = new TestCdmListener();
    byte[] data = new byte[] {0x01};
    int messageType = 100;
    int listenerId = 10;
    shadowCompanionDeviceManager.setSelfAssociationInfo(
        AssociationInfoBuilder.newBuilder().setDisplayName("self").setId(listenerId).build());
    companionDeviceManager.addOnMessageReceivedListener(executorService, messageType, listener1);
    companionDeviceManager.addOnMessageReceivedListener(executorService, messageType, listener2);
    companionDeviceManager.removeOnMessageReceivedListener(messageType, listener1);

    int senderId = 20;
    shadowCompanionDeviceManager.setSelfAssociationInfo(
        AssociationInfoBuilder.newBuilder()
            .setDeviceMacAddress(MAC_ADDRESS)
            .setId(senderId)
            .build());

    companionDeviceManager.sendMessage(messageType, data, new int[] {0, listenerId});

    executorService.shutdown();
    assertThat(executorService.awaitTermination(100, MILLISECONDS)).isTrue();
    assertThat(listener1.hasInvocation()).isFalse();
    assertThat(listener2.hasInvocation()).isTrue();
  }

  @Test
  @Config(minSdk = VERSION_CODES.UPSIDE_DOWN_CAKE)
  public void testEnableSystemDataSyncForTypes_updatesShadow() {
    shadowCompanionDeviceManager.addAssociation(MAC_ADDRESS);
    int id = shadowCompanionDeviceManager.getMyAssociations().get(0).getId();
    int flag = 1 << 0;

    companionDeviceManager.enableSystemDataSyncForTypes(id, flag);

    assertThat(shadowCompanionDeviceManager.getSystemDataSyncFlags(id)).isEqualTo(flag);
  }

  @Test
  @Config(minSdk = VERSION_CODES.UPSIDE_DOWN_CAKE)
  public void testEnableSystemDataSyncForTypes_multipleFlags_updatesShadow() {
    shadowCompanionDeviceManager.addAssociation(MAC_ADDRESS);
    int id = shadowCompanionDeviceManager.getMyAssociations().get(0).getId();
    int flagOne = 1 << 0;
    int flagTwo = 1 << 1;

    companionDeviceManager.enableSystemDataSyncForTypes(id, flagOne);
    assertThat(shadowCompanionDeviceManager.getSystemDataSyncFlags(id)).isEqualTo(flagOne);
    companionDeviceManager.enableSystemDataSyncForTypes(id, flagTwo);
    assertThat(shadowCompanionDeviceManager.getSystemDataSyncFlags(id))
        .isEqualTo(flagOne | flagTwo);
  }

  @Test
  public void testGetSystemDataSyncFlags_noAssociation_returnsZero() {
    assertThat(shadowCompanionDeviceManager.getSystemDataSyncFlags(1)).isEqualTo(0);
  }

  private CompanionDeviceManager.Callback createCallback() {
    return new CompanionDeviceManager.Callback() {
      @Override
      public void onDeviceFound(@Nonnull IntentSender chooserLauncher) {}

      @Override
      public void onFailure(CharSequence error) {}
    };
  }

  /** A test listener for receiving messages from the CompanionDeviceManager. */
  private static class TestCdmListener implements BiConsumer<Integer, byte[]> {
    private static final Duration WAIT_TIMEOUT = Duration.ofMillis(100);
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    int receivedId;
    byte[] receivedData;

    @Override
    public void accept(Integer id, byte[] data) {
      receivedId = id;
      receivedData = data;
      countDownLatch.countDown();
    }

    private boolean awaitInvocation() throws InterruptedException {
      return countDownLatch.await(WAIT_TIMEOUT.toMillis(), MILLISECONDS);
    }

    private boolean hasInvocation() {
      return countDownLatch.getCount() == 0;
    }
  }
}
