package com.app.explorella

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Enable edge-to-edge (fullscreen) display
        enableEdgeToEdge()

        super.onCreate(savedInstanceState)

        setContent {
            App(DriverFactory(
                context = LocalContext.current
            ).createDriver())
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App(DriverFactory(
        context = LocalContext.current
    ).createDriver())
}