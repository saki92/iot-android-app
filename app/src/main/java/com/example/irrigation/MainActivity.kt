package com.example.irrigation

import StartScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.irrigation.ui.theme.IrrigationTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import androidx.compose.ui.window.Dialog

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
    NavHost(
            navController = navController,
            startDestination = IrrigationScreen.Start.name,
            modifier = Modifier
        )
        {
            composable(route = IrrigationScreen.Start.name) {
                StartScreen(modifier = Modifier,
                    onSubmitButtonClicked = { passcode: Int ->
                        CoroutineScope(IO).launch {
                            viewModel.getDeviceList(passcode = passcode)
                        }
                    },
                    uiState = uiState,
                    navController = navController,
                    onCancelButtonClicked = { cancelOrderAndNavigateToStart(
                        viewModel, navController
                    ) }
                )
            }

            composable(route = IrrigationScreen.DeviceSelect.name) {
                BackHandler(true) {
                    cancelOrderAndNavigateToStart(
                        viewModel, navController
                    )
                }
                DeviceSelect(deviceData = uiState,
                    onSelectionChanged = {},
                    onNextButtonClicked = {},
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