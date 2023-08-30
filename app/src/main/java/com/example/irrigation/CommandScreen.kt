import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.irrigation.DeviceData
import com.example.irrigation.DeviceSelect
import com.example.irrigation.deviceName

@Composable
fun CommandScreen(
    modifier: Modifier,
    deviceData: DeviceData,
    onSubmitButtonClicked: (Int, Boolean, Boolean) -> Unit,
    onCancelButtonClicked: () -> Unit,
    onRefreshButtonClicked: () -> Unit
    ) {
    val focusManager = LocalFocusManager.current
    var timerValue by remember { mutableStateOf("") }
    var valve0Set by remember { mutableStateOf(false) }
    var valve1Set by remember { mutableStateOf(false) }
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier) {
            Text("Motor State")
            Text(deviceData.motor_state)
            Divider(thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            Text("Valve 0 State")
            Text(deviceData.valve0_state)
            Divider(thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            Text("Valve 1 State")
            Text(deviceData.valve1_state)
            Divider(thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            Text("Remaining Time to Cut-off (minutes)")
            Text(deviceData.rem_time.toString())
            Divider(thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            val motorTimeMax = 300
            OutlinedTextField(value = timerValue,
                onValueChange = {
                    val intIt = it.toIntOrNull()?:0
                    if (intIt <= motorTimeMax) {
                        timerValue = it
                    }
                                },
                singleLine = true,
                label = { Text("Cut-off Timer:") },
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }), // hide keyboard when done is clicked
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.height(10.dp))

            Text("Set Valve 0")
            Row(modifier = Modifier.selectable(
                selected = valve0Set,
                onClick = {
                    valve0Set = true
                }
            ),
                verticalAlignment = Alignment.CenterVertically
            ){
                RadioButton(
                    selected = valve0Set,
                    onClick = {
                        valve0Set = true
                    }
                )
                Text("ON")}

            Row(modifier = Modifier.selectable(
                selected = !valve0Set,
                onClick = {
                    valve0Set = false
                }
            ),
                verticalAlignment = Alignment.CenterVertically
            ){
                RadioButton(
                    selected = !valve0Set,
                    onClick = {
                        valve0Set = false
                    }
                )
                Text("OFF")}

            Divider(
                thickness = 1.dp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Spacer(modifier = Modifier.height(0.dp))
            Text("Set Valve 1")
            Row(modifier = Modifier.selectable(
                selected = valve1Set,
                onClick = {
                    valve1Set = true
                }
            ),
                verticalAlignment = Alignment.CenterVertically
            ){
                RadioButton(
                    selected = valve1Set,
                    onClick = {
                        valve1Set = true
                    }
                )
                Text("ON")}

            Row(modifier = Modifier.selectable(
                selected = !valve1Set,
                onClick = {
                    valve1Set = false
                }
            ),
                verticalAlignment = Alignment.CenterVertically
            ){
                RadioButton(
                    selected = !valve1Set,
                    onClick = {
                        valve1Set = false
                    }
                )
                Text("OFF")}

            Divider(
                thickness = 1.dp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .weight(1f, false),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                OutlinedButton(modifier = Modifier.weight(1f), onClick = onCancelButtonClicked) {
                    Text("Cancel")
                }

                Button(
                    modifier = Modifier.weight(1f),
                    // the button is enabled when the user makes a selection
                    enabled = timerValue.isNotEmpty(),
                    onClick = { onSubmitButtonClicked(timerValue.toInt(), valve0Set, valve1Set) }
                ) {
                    Text("Next")
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .weight(1f, false),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    // the button is enabled when the user makes a selection
                    enabled = true,
                    onClick = { onRefreshButtonClicked() }
                ) {
                    Text("Refresh")
                }
            }
        }
    }
    if (deviceData.connectionFail == -1) {
        MinimalDialog(onDismissRequest = {},
            messageText = "Server Not Reachable",
            onCancelButtonClicked = onCancelButtonClicked
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CommandScreenPreview() {
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
        valve1_state = "Closed",
        rem_time = 0
    )
    CommandScreen(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        onSubmitButtonClicked = { _: Int, _: Boolean, _: Boolean ->},
        deviceData = data,
        onCancelButtonClicked = {},
        onRefreshButtonClicked = {}
    )
}
