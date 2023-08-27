package com.example.irrigation

data class DeviceData(
    val number_of_devices: Int = 0,
    val device_list: List<Byte> = listOf(),
    val motor_state: String = "OFF",
    val rem_time: Int = 0,
    val valve0_state: String = "Closed",
    val valve1_state: String = "Open"
)