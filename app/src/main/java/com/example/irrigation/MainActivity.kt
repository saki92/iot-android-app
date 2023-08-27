package com.example.irrigation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.irrigation.ui.theme.IrrigationTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IrrigationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    IrrigationApp(viewModel = DeviceListViewModel())
                }
            }
        }
    }
}

enum class IrrigationScreen() {
    Start,
    DeviceSelect,
    Command
}

@Composable
fun IrrigationApp(
    viewModel: DeviceListViewModel,
    navController: NavHostController = rememberNavController()
) {
    val uiState by viewModel.uiState.collectAsState()
    NavHost(navController = navController,
        startDestination = IrrigationScreen.Start.name,
        modifier = Modifier)
    {
        composable(route = IrrigationScreen.Start.name) {
            StartScreen(modifier = Modifier,
                onSubmitButtonClicked = {
                    CoroutineScope(IO).launch{
                        viewModel.getDeviceList(it)
                    }
                    navController.navigate(IrrigationScreen.DeviceSelect.name)
                })
        }

        composable(route = IrrigationScreen.DeviceSelect.name) {
            BackHandler(true) {
                cancelOrderAndNavigateToStart(
                    viewModel, navController
                )
            }
            DeviceSelect(deviceData = uiState,
                onSelectionChanged = {},
                onCancelButtonClicked = {
                    cancelOrderAndNavigateToStart(
                        viewModel,
                        navController
                    )
                }
            )
        }
    }
}

private fun cancelOrderAndNavigateToStart(
    viewModel: DeviceListViewModel,
    navController: NavHostController
) {
    viewModel.resetOrder()
    viewModel.closeConnection()
    navController.popBackStack(IrrigationScreen.Start.name, inclusive = false)
}

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
        Text(
            text = "Enter Passcode",
            modifier = Modifier.padding(16.dp)
        )
        var passcode by remember { mutableStateOf("") }
        OutlinedTextField(value = passcode,
            onValueChange = {passcode = it},
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            label = { Text("Passcode:") },
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }), // hide keyboard when done is clicked
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
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