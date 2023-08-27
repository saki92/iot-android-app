package com.example.irrigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.irrigation.DeviceData

@Composable
fun DeviceSelect(modifier: Modifier = Modifier,
                 onNextButtonClicked: (Byte) -> Unit = {},
                 deviceData: DeviceData,
                 onSelectionChanged: (Byte) -> Unit,
                 onCancelButtonClicked: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    var selectedValue by remember { mutableStateOf("") }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier) {
            deviceData.device_list.forEach{ dev ->
                Row(
                    modifier = Modifier.selectable(
                        selected = selectedValue == dev.toString(),
                        onClick = {
                            selectedValue = dev.toString()
                            onSelectionChanged(dev)
                        }
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ){
                    RadioButton(
                        selected = selectedValue == dev.toString(),
                        onClick = {
                            selectedValue = dev.toString()
                            onSelectionChanged(dev)
                        }
                    )
                    deviceName[dev]?.let { Text(it) }
                }
            }
            Divider(
                thickness = 1.dp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .weight(1f, false),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Bottom
        ){
            OutlinedButton(modifier = Modifier.weight(1f), onClick = onCancelButtonClicked) {
                Text("Cancel")
            }
            Button(
                modifier = Modifier.weight(1f),
                // the button is enabled when the user makes a selection
                enabled = selectedValue.isNotEmpty(),
                onClick = { onNextButtonClicked(selectedValue.toByte()) }
            ) {
                Text("Next")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DeviceSelectPreview() {
    val tmpDeviceList = mutableListOf<Byte>()
    val numberOfDevices = 2
    var tmpDevId = 1
    repeat(numberOfDevices) {
        tmpDeviceList.add(tmpDevId.toByte())
        tmpDevId += 1
    }
    val data = DeviceData(number_of_devices = 2,
        device_list = tmpDeviceList,
        motor_state = "OFF",
        valve0_state = "Open",
        valve1_state = "Closed"
    )
    DeviceSelect(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        onNextButtonClicked = {},
        deviceData = data,
        onSelectionChanged = {},
        onCancelButtonClicked = {}
    )
}
