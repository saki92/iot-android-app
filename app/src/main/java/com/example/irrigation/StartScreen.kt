import android.content.Context
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.intPreferencesKey
import java.util.prefs.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.irrigation.DeviceData
import com.example.irrigation.DeviceListViewModel
import com.example.irrigation.IrrigationScreen
import java.util.concurrent.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartScreen(modifier: Modifier = Modifier,
                onSubmitButtonClicked: (Int) -> Unit,
                uiState: DeviceData,
                navController: NavHostController = rememberNavController(),
                onCancelButtonClicked: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.pointerInput(Unit) {
            detectTapGestures(onTap = {
                focusManager.clearFocus() //hide keyboard when tapped outside
            })
        }
    ) {
        var passcode by remember { mutableStateOf("") }
        OutlinedTextField(value = passcode,
            onValueChange = {passcode = it},
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            label = { Text("Passcode:") },
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }), // hide keyboard when done is clicked
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Button(
            onClick = {
                focusManager.clearFocus()
                onSubmitButtonClicked(passcode.toInt())
            }, // hide keyboard
            enabled = passcode.isNotEmpty(),
            modifier = Modifier.padding(20.dp)
        ) {
            Text("Get Device List")
        }

    }
    if (uiState.connectionFail == -1) {
        MinimalDialog(onDismissRequest = {},
            messageText = "Server Not Reachable",
            onCancelButtonClicked = onCancelButtonClicked
        )
    }
}

@Composable
fun MinimalDialog(onDismissRequest: () -> Unit,
                  messageText: String,
                  onCancelButtonClicked: () -> Unit
) {
    Dialog(onDismissRequest = {onDismissRequest()}) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = messageText,
                    modifier = Modifier.padding(16.dp),
                )
                TextButton(
                    onClick = { onDismissRequest()
                        onCancelButtonClicked},
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Dismiss")
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun StartScreenPreview() {
    StartScreen(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        onSubmitButtonClicked = {},
        uiState = DeviceData(connectionFail = -1),
        onCancelButtonClicked = {}
    )
}