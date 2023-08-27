import android.content.Context
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.intPreferencesKey
import java.util.prefs.Preferences
import androidx.datastore.preferences.preferencesDataStore
import java.util.concurrent.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartScreen(modifier: Modifier = Modifier,
                onSubmitButtonClicked: (Int) -> Unit) {
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

}

@Preview(showBackground = true)
@Composable
fun StartScreenPreview() {
    StartScreen(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        onSubmitButtonClicked = {}
    )
}