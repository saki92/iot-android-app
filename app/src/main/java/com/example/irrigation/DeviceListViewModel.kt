package com.example.irrigation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.InputStreamReader
import java.net.Socket

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

    private lateinit var connection: Socket

    suspend fun getDeviceList(passcode: Int) {
        connection = Socket(SERVER_ADDRESS, SERVER_PORT)
        val writer = connection.getOutputStream()
        val passcodeLSB = passcode and 0xFF
        val passcodeMSB = (passcode shr 8) and 0xFF
        val sendData = byteArrayOf(MSG_TYPE_C0.toByte(), passcodeLSB.toByte(), passcodeMSB.toByte())
        writer.write(sendData)
        val inputStream = InputStreamReader(connection.getInputStream())
        val buffer = CharArray(16)
        val bytesRead = inputStream.read(buffer)
        var tempDeviceList = mutableListOf<Byte>()
        for (i in 0 until buffer[1].code) {
            tempDeviceList.add(buffer[2+i].code.toByte())
        }
        _uiState.update { currentState -> currentState.copy(
            number_of_devices = buffer[1].code,
            device_list = tempDeviceList
        ) }
    }

    fun resetOrder() {
        _uiState.value = DeviceData()
    }

    fun closeConnection() {
        connection.close()
    }
}