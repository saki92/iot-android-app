package com.example.irrigation

data class DeviceData(
    val number_of_devices: Int = 0,
    val device_list: List<Byte> = listOf(),
    val motor_state: String = "OFF",
    val rem_time: Int = 0,
    val valve0_state: String = "Closed",
    val valve1_state: String = "Open",
    val rssi: Byte = 0,
    val adc0: Int = 0,
    val adc1: Int = 0,
    val adc2: Int = 0,
    val adc3: Int = 0,
    val currentDeviceId: Byte = 0,

    val connectionFail: Int = 0,
)