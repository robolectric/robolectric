package org.robolectric.integrationtests.kotlin.flow

import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Build.VERSION_CODES.S
import com.google.common.truth.Truth.assertThat
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.android.util.concurrent.PausedExecutorService
import org.robolectric.annotation.Config
import org.robolectric.shadow.api.Shadow
import org.robolectric.shadows.ShadowBluetoothDevice
import org.robolectric.shadows.ShadowBluetoothGatt
import org.robolectric.shadows.ShadowBluetoothLeScanner

/**
 * A test that uses a custom executor-backed coroutine dispatcher to control the execution of
 * coroutines.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [S])
class BluetoothProvisionerTest {

  val context = RuntimeEnvironment.getApplication()

  val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

  fun newScanResult(): ScanResult {
    val bluetoothDevice = bluetoothManager.adapter.getRemoteDevice(BLUETOOTH_MAC)
    return ScanResult(bluetoothDevice, null, 0, 0)
  }

  @Test
  fun testBluetoothProvisioner() {
    val executor = PausedExecutorService()
    val dispatcher = executor.asCoroutineDispatcher()
    val scope = CoroutineScope(dispatcher)

    scope.launch {
      val gattService =
        BluetoothProvisioner(RuntimeEnvironment.getApplication()).scanAndConnect().firstOrNull()
      assertThat(gattService).isNotNull()
    }

    executor.runAll()

    val scanner = bluetoothManager.adapter.bluetoothLeScanner
    val shadowScanner = Shadow.extract<ShadowBluetoothLeScanner>(scanner)

    val scanResult = newScanResult()
    val bluetoothDevice = scanResult.device
    shadowScanner.scanCallbacks.first().onScanResult(0, newScanResult())

    executor.runAll()

    val shadowDevice = Shadow.extract<ShadowBluetoothDevice>(bluetoothDevice)

    val gatt = shadowDevice.bluetoothGatts.first()
    val shadowGatt = Shadow.extract<ShadowBluetoothGatt>(gatt)

    val service =
      BluetoothGattService(
        UUID.fromString("00000000-0000-0000-0000-0000000000A1"),
        BluetoothGattService.SERVICE_TYPE_PRIMARY,
      )

    shadowGatt.addDiscoverableService(service)
    shadowGatt.notifyConnection(BLUETOOTH_MAC)

    executor.runAll()
  }

  private companion object {
    private const val BLUETOOTH_MAC = "00:11:22:33:AA:BB"
  }
}
