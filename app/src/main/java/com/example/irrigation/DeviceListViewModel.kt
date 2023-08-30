package com.example.irrigation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.IOException
import java.io.InputStreamReader
import java.net.Socket
import kotlin.experimental.or
import kotlin.properties.Delegates

private const val SERVER_ADDRESS = "35.153.79.3"
private const val SERVER_PORT = 9482
private const val MSG_TYPE_C0 = 2
private const val MSG_TYPE_C1 = 3
private const val MSG_TYPE_C2 = 4
private const val MSG_TYPE_D0 = 5
private const val MSG_TYPE_D1 = 6
val deviceName = mapOf<Byte, String>(1.toByte() to "Kelakadu", 2.toByte() to "Singpuram")
class DeviceListViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DeviceData())
    val uiState: StateFlow<DeviceData> = _uiState.asStateFlow()

    private var connection: Socket? = null

    suspend fun getDeviceList(passcode: Int) {
        try {
            connection = Socket(SERVER_ADDRESS, SERVER_PORT)
            val writer = connection!!.getOutputStream()
            val passcodeLSB = passcode and 0xFF
            val passcodeMSB = (passcode shr 8) and 0xFF
            val sendData =
                byteArrayOf(MSG_TYPE_C0.toByte(), passcodeLSB.toByte(), passcodeMSB.toByte())
            writer.write(sendData)
            val inputStream = InputStreamReader(connection!!.getInputStream())
            val buffer = CharArray(16)
            val bytesRead = inputStream.read(buffer)
            var tempDeviceList = mutableListOf<Byte>()
            for (i in 0 until buffer[1].code) {
                tempDeviceList.add(buffer[2 + i].code.toByte())
            }
            _uiState.update { currentState ->
                currentState.copy(
                    number_of_devices = buffer[1].code,
                    device_list = tempDeviceList,
                    connectionFail = 0
                )
            }
        } catch (e: IOException) {
            e.printStackTrace()
                _uiState.update { currentState ->
                    currentState.copy(
                        connectionFail = -1
                    )
                }
        }
    }

    suspend fun getDeviceData(deviceId: Byte) {
        try {
            val sendData = byteArrayOf(MSG_TYPE_C1.toByte(), deviceId)
            val writer = connection!!.getOutputStream()
            writer.write(sendData)
            val inputStream = InputStreamReader(connection!!.getInputStream())
            val buffer = CharArray(16)
            val bytesRead = inputStream.read(buffer)
            val adc0 = (buffer[5].code and 0xFF) or ((buffer[6].code shl 8) and 0x300)
            val adc1 = ((buffer[6].code shr 2) and 0x3F) or ((buffer[7].code shl 6) and 0x3C0)
            val adc2 = ((buffer[7].code shr 4) and 0xF) or ((buffer[8].code shl 4) and 0x3F0)
            val adc3 = ((buffer[8].code shr 6) and 0x3) or ((buffer[9].code shl 2) and 0x3FC)
            val remMins = (buffer[10].code and 0xFF) or ((buffer[11].code shl 8) and 0xFF00)
            val valve0 = (buffer[12].code shr 7) and 0x1
            val valve1 = (buffer[12].code shr 6) and 0x1
            _uiState.update { currentState ->
                currentState.copy(
                    currentDeviceId = buffer[3].code.toByte(),
                    rssi = buffer[4].code.toByte(),
                    adc0 = adc0,
                    adc1 = adc1,
                    adc2 = adc2,
                    adc3 = adc3,
                    rem_time = remMins,
                    valve0_state = if (valve0 == 1) {
                        "Open"
                    } else {
                        "Closed"
                    },
                    valve1_state = if (valve1 == 1) {
                        "Open"
                    } else {
                        "Closed"
                    },
                )
            }
        } catch (e: IOException) {
            e.printStackTrace()
            _uiState.update { currentState ->
                currentState.copy(
                    connectionFail = -1
                )
            }
        }
    }

    suspend fun sendCommand(cutOffTime: Int, valve0State: Boolean, valve1State: Boolean) {
        try {
            val motorStateByte = if (cutOffTime > 0) 0x1.toByte() else 0x0.toByte()
            val val0inByte = if (valve0State) 0x2.toByte() else 0x0.toByte()
            val val1inByte = if (valve1State) 0x4.toByte() else 0x0.toByte()
            val sendData = byteArrayOf(MSG_TYPE_C2.toByte(),
                0,
                0,
                uiState.value.currentDeviceId,
                (cutOffTime and 0xFF).toByte(),
                ((cutOffTime shr 8) and 0x3).toByte(),
                val1inByte or val0inByte or motorStateByte,
                0,
                0
                )
            val writer = connection!!.getOutputStream()
            writer.write(sendData)
            } catch (e: IOException) {
            e.printStackTrace()
            _uiState.update { currentState ->
                currentState.copy(
                    connectionFail = -1
                )
            }
        }
    }

    fun resetOrder() {
        _uiState.value = DeviceData()
    }

    fun closeConnection() {
        if (connection != null) {
            if (connection!!.isConnected) {
                connection!!.close()
            }
        }
    }
}