package com.example

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.StatusShower
import com.example.ui.screens.AutoTuneScreen
import com.example.ui.screens.ExportScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.InstrumentsScreen
import com.example.ui.screens.MixerScreen
import com.example.ui.screens.OfflineGuideScreen
import com.example.ui.screens.PianoRollScreen
import com.example.ui.screens.RecordingSessionScreen
import com.example.ui.screens.VocalFxScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.StudioScreen
import com.example.viewmodel.StudioViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable edge-to-edge with transparent status bar and dark icons for readability
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT,
                Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT,
                Color.TRANSPARENT
            )
        )
        setContent {
            MyApplicationTheme {
                StudioApp()
            }
        }
    }
}

@Composable
fun StudioApp(viewModel: StudioViewModel = viewModel()) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val statusNotification by viewModel.statusNotification.collectAsState()

    // Handle Android system back button on sub-screens
    BackHandler(enabled = currentScreen != StudioScreen.HOME) {
        if (!viewModel.navigateBack()) {
            viewModel.navigateTo(StudioScreen.HOME)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "screenTransition"
        ) { screen ->
            when (screen) {
                StudioScreen.HOME -> HomeScreen(viewModel)
                StudioScreen.PROJECTS -> HomeScreen(viewModel)
                StudioScreen.MIXER -> MixerScreen(viewModel)
                StudioScreen.PIANO_ROLL -> PianoRollScreen(viewModel)
                StudioScreen.INSTRUMENTS -> InstrumentsScreen(viewModel)
                StudioScreen.DRUM_PADS -> com.example.ui.screens.DrumPadsScreen(viewModel)
                StudioScreen.RECORDING_SESSION -> RecordingSessionScreen(viewModel)
                StudioScreen.VOCAL_FX -> VocalFxScreen(viewModel)
                StudioScreen.AUTO_TUNE -> AutoTuneScreen(viewModel)
                StudioScreen.EXPORT_SHARE -> ExportScreen(viewModel)
                StudioScreen.OFFLINE_GUIDE -> OfflineGuideScreen(viewModel)
            }
        }

        // Live Error Shower & Action Feedback Banner positioned cleanly below status bar
        StatusShower(
            notification = statusNotification,
            onDismiss = { viewModel.dismissNotification() },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 10.dp)
        )
    }
}
