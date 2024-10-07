package com.example.irrigation

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStreamReader
import java.net.Socket
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.or

private const val SERVER_ADDRESS = "192.168.193.106"
private const val SERVER_PORT = 9482
private const val MSG_TYPE_C0 = 2
private const val MSG_TYPE_C1 = 3
private const val MSG_TYPE_C2 = 4
private const val MSG_TYPE_D0 = 5
private const val MSG_TYPE_D1 = 6
private const val DEVICE_PASSCODE_HI = 42
private const val DEVICE_PASSCODE_LO = 90
private const val AES_KEY = "SdP5X5BVhwyT50rvAE2qK6sy2UL66KAi"
val deviceName = mapOf(1.toByte() to "Kelakadu", 2.toByte() to "Singpuram")
class DeviceListViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DeviceData())
    val uiState: StateFlow<DeviceData> = _uiState.asStateFlow()

    private var connection: Socket? = null

    suspend fun getDeviceList(passcode: Int) {
        try {
            connection = withContext(Dispatchers.IO) {
                Socket(SERVER_ADDRESS, SERVER_PORT)
            }
            val writer = withContext(Dispatchers.IO) {
                connection!!.getOutputStream()
            }
            val passcodeLSB = passcode and 0xFF
            val passcodeMSB = (passcode shr 8) and 0xFF
            val sendData =
                byteArrayOf(MSG_TYPE_C0.toByte(), passcodeLSB.toByte(), passcodeMSB.toByte())
            withContext(Dispatchers.IO) {
                writer.write(sendData)
            }
            val inputStream = InputStreamReader(withContext(Dispatchers.IO) {
                connection!!.getInputStream()
            })
            val buffer = CharArray(16)
            val bytesRead = withContext(Dispatchers.IO) {
                inputStream.read(buffer)
            }
            val tempDeviceList = mutableListOf<Byte>()
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
            val writer = withContext(Dispatchers.IO) {
                connection!!.getOutputStream()
            }
            withContext(Dispatchers.IO) {
                writer.write(sendData)
            }
            val inputStream = withContext(Dispatchers.IO) {
                connection!!.getInputStream()
            }
            val buffer = ByteArray(128)
            val bytesRead = withContext(Dispatchers.IO) {
                inputStream.read(buffer)
            }
            val adc0 = (buffer[5].toInt() and 0xFF) or ((buffer[6].toInt() shl 8) and 0x300)
            val adc1 = (buffer[7].toInt() and 0xFF) or ((buffer[8].toInt() shl 8) and 0x300)
            val adc2 = (buffer[9].toInt() and 0xFF) or ((buffer[10].toInt() shl 8) and 0x300)
            val adc3 = (buffer[11].toInt() and 0xFF) or ((buffer[12].toInt() shl 8) and 0x300)
            val remMins = (buffer[13].toInt() and 0xFF) or ((buffer[14].toInt() shl 8) and 0xFF00)
            val valve0 = (buffer[15].toInt() shr 2) and 0x1
            val valve1 = (buffer[15].toInt() shr 3) and 0x1
            val motorState = (buffer[15].toInt() shr 4) and 0x1
            _uiState.update { currentState ->
                currentState.copy(
                    currentDeviceId = buffer[3],
                    rssi = buffer[4],
                    adc0 = adc0,
                    adc1 = adc1,
                    adc2 = adc2,
                    adc3 = adc3,
                    rem_time = remMins,
                    motor_state = if (motorState == 0) "ON" else "OFF",
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

    private fun encryptAES(input: ByteArray, key: ByteArray): ByteArray {
        try {
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val secretKey = SecretKeySpec(key, "AES")
            val iv = ByteArray(16) // Generate a random IV (Initialization Vector)
            SecureRandom().nextBytes(iv)
            val ivParams = IvParameterSpec(iv)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParams)
            val encryptedBytes = cipher.doFinal(input)
            Log.i("encBytes", encryptedBytes.toString())

            // Combine the IV and the encrypted data
            val combined = ByteArray(iv.size + encryptedBytes.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(encryptedBytes, 0, combined, iv.size, encryptedBytes.size)

            return combined
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return byteArrayOf(0)
    }

    suspend fun sendCommand(cutOffTime: Int, valve0State: Boolean, valve1State: Boolean, motorState: Boolean) {
        try {
            val motorStateByte = if (motorState) 0x1.toByte() else 0x0.toByte()
            val val0inByte = if (valve0State) 0x2.toByte() else 0x0.toByte()
            val val1inByte = if (valve1State) 0x4.toByte() else 0x0.toByte()
            val sendData = byteArrayOf(MSG_TYPE_C2.toByte(),
                DEVICE_PASSCODE_HI.toByte(),
                DEVICE_PASSCODE_LO.toByte(),
                uiState.value.currentDeviceId,
                (cutOffTime and 0xFF).toByte(),
                ((cutOffTime shr 8) and 0xFF).toByte(),
                val1inByte or val0inByte or motorStateByte,
                0,
                0
                )
            val encData = encryptAES(sendData, AES_KEY.toByteArray())
            val outArray = ByteArray(encData.size + 1)
            outArray[0] = MSG_TYPE_C2.toByte()
            System.arraycopy(encData, 0, outArray, 1, encData.size)
            val writer = withContext(Dispatchers.IO) {
                connection!!.getOutputStream()
            }
            withContext(Dispatchers.IO) {
                writer.write(outArray)
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