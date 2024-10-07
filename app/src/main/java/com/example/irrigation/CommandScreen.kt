import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
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
    deviceData: DeviceData,
    onSubmitButtonClicked: (Int, Boolean, Boolean, Boolean) -> Unit,
    onCancelButtonClicked: () -> Unit,
    onRefreshButtonClicked: () -> Unit
    ) {
    val focusManager = LocalFocusManager.current
    var timerValue by remember { mutableStateOf("") }
    var valve0Set by remember { mutableStateOf(false) }
    var valve1Set by remember { mutableStateOf(false) }
    var motorState by remember { mutableStateOf(false) }

    // LaunchedEffect to update states whenever deviceData changes
    LaunchedEffect(deviceData) {
        valve0Set = deviceData.valve0_state == "Open"
        valve1Set = deviceData.valve1_state == "Open"
        motorState = deviceData.motor_state == "ON"
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus() //hide keyboard when tapped outside
                })
            }
    ) {
        Column(modifier = Modifier
            .verticalScroll(rememberScrollState())
            ) {
            Text("RSSI")
            Text(deviceData.rssi.toString())
            Divider(thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            Text("Motor State")
            Text(deviceData.motor_state)
            Divider(thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            Text("Starter Coil Current")
            Text(starterCoilCurrent(adcVal = deviceData.adc0).toString())
            Divider(thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            Text("Phase A Voltage")
            Text(getACVoltage(adcVal = deviceData.adc1).toString())
            Divider(thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            Text("Phase B Voltage")
            Text(getACVoltage(adcVal = deviceData.adc2).toString())
            Divider(thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            Text("Phase C Voltage")
            Text(getACVoltage(adcVal = deviceData.adc3).toString())
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
            
            Text("Set Motor State")
            Row(modifier = Modifier.selectable(
                selected = motorState,
                onClick = {
                    motorState = true
                }
            ),
                verticalAlignment = Alignment.CenterVertically
            ){
                RadioButton(
                    selected = motorState,
                    onClick = {
                        motorState = true
                    }
                )
                Text("ON")}

            Row(modifier = Modifier.selectable(
                selected = !motorState,
                onClick = {
                    motorState = false
                }
            ),
                verticalAlignment = Alignment.CenterVertically
            ){
                RadioButton(
                    selected = !motorState,
                    onClick = {
                        motorState = false
                    }
                )
                Text("OFF")}

            Divider(
                thickness = 1.dp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Spacer(modifier = Modifier.height(0.dp))

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
                Text("Open")}

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
                Text("Close")}

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
                Text("Open")}

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
                Text("Close")}

            Divider(
                thickness = 1.dp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(modifier = Modifier.weight(1f), onClick = onCancelButtonClicked) {
                    Text("Cancel")
                }

                Button(
                    modifier = Modifier.weight(1f),
                    // the button is enabled when the user makes a selection
                    enabled = timerValue.isNotEmpty(),
                    onClick = { onSubmitButtonClicked(timerValue.toInt(), valve0Set, valve1Set, motorState) }
                ) {
                    Text("Send")
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
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

@Composable
fun getACVoltage(adcVal: Int): Double {
    val vref = 3.3
    val res = 1024
    val volt = adcVal * vref / res

    val r1 = 2e6
    val r2 = 20e3

    val acVolt = volt * (r1 + r2) / r2

    return String.format("%.1f", acVolt).toDouble()
}

@Composable
fun starterCoilCurrent(adcVal: Int): Double {
    val vref = 3.3
    val res = 1024
    val resolution = 0.05
    val zeroCurVal = 1.65
    val adcVoltage = adcVal * vref / res
    val curVal = (adcVoltage - zeroCurVal) / resolution

    return String.format("%.1f", curVal).toDouble()
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
        onSubmitButtonClicked = { _: Int, _: Boolean, _: Boolean, _: Boolean ->},
        deviceData = data,
        onCancelButtonClicked = {},
        onRefreshButtonClicked = {}
    )
}
