package org.robolectric.integrationtests.kotlin.flow

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow

/** A class that invokes Android Bluetooth LE APIs. */
class BluetoothProvisioner(applicationContext: Context) {

  val context: Context

  init {
    context = applicationContext
  }

  fun startScan(): Flow<BluetoothDevice> = callbackFlow {
    val scanCallback =
      object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
          if (result?.device != null) {
            @Suppress("UnusedPrivateProperty") val unused = trySend(result.device)
          }
        }

        override fun onScanFailed(errorCode: Int) {
          cancel("BLE Scan Failed", null)
        }
      }
    val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val scanner = bluetoothManager.adapter.bluetoothLeScanner
    scanner.startScan(scanCallback)
    awaitClose { scanner.stopScan(scanCallback) }
  }

  fun connectToDevice(device: BluetoothDevice): Flow<BluetoothGatt> = callbackFlow {
    val gattCallback =
      object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
          if (newState == BluetoothProfile.STATE_CONNECTED) {
            gatt!!.discoverServices()
          } else {
            cancel("Connect Failed", null)
          }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
          if (status == BluetoothGatt.GATT_SUCCESS) {
            @Suppress("UnusedPrivateProperty") val unused = trySend(gatt!!)
          } else {
            cancel("Service discovery failed", null)
          }
        }
      }

    device.connectGatt(context, true, gattCallback)
    awaitClose {}
  }

  fun scanAndConnect() =
    flow<BluetoothGattService> {
      val device = startScan().firstOrNull()
      if (device != null) {
        val gatt = connectToDevice(device).firstOrNull()
        emit(gatt!!.services[0])
      }
    }
}
