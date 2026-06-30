package com.ferhatozcelik.jetpackcomposetemplate

import android.app.Activity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.delay
import kotlin.random.Random

// 1. Define App States
enum class AppMode { MENU, CHECKER, FIXER }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Edge-to-edge full screen setup
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContent {
            MaterialTheme {
                BurnInApp()
            }
        }
    }
}

@Composable
fun BurnInApp() {
    var currentMode by remember { mutableStateOf(AppMode.MENU) }

    // Simple State-Based Navigation
    when (currentMode) {
        AppMode.MENU -> MainMenuScreen(
            onNavigateChecker = { currentMode = AppMode.CHECKER },
            onNavigateFixer = { currentMode = AppMode.FIXER }
        )
        AppMode.CHECKER -> CheckerScreen(onBack = { currentMode = AppMode.MENU })
        AppMode.FIXER -> FixerScreen(onBack = { currentMode = AppMode.MENU })
    }
}

// ==========================================
// 2. MAIN MENU SCREEN
// ==========================================
@Composable
fun MainMenuScreen(onNavigateChecker: () -> Unit, onNavigateFixer: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("AMOLED Rescue", fontSize = 32.sp, color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Professional Image Retention Tool", color = Color.Gray, textAlign = TextAlign.Center)
        
        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onNavigateChecker,
            modifier = Modifier.fillMaxWidth().height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333))
        ) {
            Text("1. Run Burn-In Checker", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onNavigateFixer,
            modifier = Modifier.fillMaxWidth().height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
        ) {
            Text("2. Launch Pixel Fixer", fontSize = 18.sp)
        }
    }
}

// ==========================================
// 3. DIAGNOSTIC CHECKER SCREEN
// ==========================================
@Composable
fun CheckerScreen(onBack: () -> Unit) {
    val activity = LocalContext.current as Activity
    MaximizeDisplayCapabilities(activity.window) // Force max brightness
    BackHandler { onBack() } // Handle Android back button

    val testColors = listOf(Color.DarkGray, Color.LightGray, Color.Red, Color.Green, Color.Blue, Color.White)
    var colorIndex by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(testColors[colorIndex])
            .clickable { 
                // Cycle colors on tap, go back to menu when done
                if (colorIndex < testColors.size - 1) colorIndex++ else onBack() 
            },
        contentAlignment = Alignment.Center
    ) {
        if (colorIndex == 0) {
            Text(
                "Tap to cycle colors.\nLook for faint UI outlines.",
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.background(Color.Black.copy(alpha = 0.5f)).padding(16.dp)
            )
        }
    }
}

// ==========================================
// 4. THE FIXER SCREEN (WITH TIMER & MATRIX)
// ==========================================
@Composable
fun FixerScreen(onBack: () -> Unit) {
    val activity = LocalContext.current as Activity
    MaximizeDisplayCapabilities(activity.window)
    BackHandler { onBack() }

    val colors = listOf(Color.Red, Color.Green, Color.Blue, Color.White)
    var speedMs by remember { mutableLongStateOf(100L) }
    var redrawTrigger by remember { mutableIntStateOf(0) } 
    var timeRemainingSeconds by remember { mutableIntStateOf(300) } // Default 5 mins

    // Matrix Render Loop
    LaunchedEffect(speedMs) {
        while (true) {
            delay(speedMs)
            redrawTrigger++
        }
    }

    // Auto-Shutdown Timer Loop
    LaunchedEffect(Unit) {
        while (timeRemainingSeconds > 0) {
            delay(1000)
            timeRemainingSeconds--
        }
        onBack() // Auto-exit when timer hits 0
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top 15% Matrix Flasher (Expanded slightly for modern phone notches/status bars)
        MatrixCanvas(modifier = Modifier.fillMaxWidth().weight(0.15f), colors = colors, trigger = redrawTrigger)
        
        // Middle 70% UI & Black Space
        Box(
            modifier = Modifier.fillMaxWidth().weight(0.70f).background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                
                Text(
                    text = String.format("Time Remaining: %02d:%02d", timeRemainingSeconds / 60, timeRemainingSeconds % 60),
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text("Flash Interval: ${speedMs}ms", color = Color.LightGray)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                    Button(onClick = { speedMs = 100L }) { Text("Safe") }
                    Button(onClick = { speedMs = 30L }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57C00))) { Text("Aggressive") }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                Text("Set Timer", color = Color.LightGray)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                    Button(onClick = { timeRemainingSeconds = 300 }) { Text("5m") }
                    Button(onClick = { timeRemainingSeconds = 600 }) { Text("10m") }
                    Button(onClick = { timeRemainingSeconds = 1200 }) { Text("20m") }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "WARNING: PHOTOSENSITIVE SEIZURE RISK\nDo not look directly at screen.", 
                    color = Color.Red,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) {
                    Text("Stop & Exit")
                }
            }
        }
        
        // Bottom 15% Matrix Flasher
        MatrixCanvas(modifier = Modifier.fillMaxWidth().weight(0.15f), colors = colors, trigger = redrawTrigger)
    }
}

// ==========================================
// 5. OPTIMIZED MATRIX CANVAS
// ==========================================
@Composable
fun MatrixCanvas(modifier: Modifier, colors: List<Color>, trigger: Int) {
    Canvas(modifier = modifier) {
        trigger.hashCode() // Force recomposition on trigger change
        
        val cols = 40 
        val rows = 12  
        val cellWidth = size.width / cols
        val cellHeight = size.height / rows

        // Math happens directly inside the draw loop to avoid creating thousands 
        // of temporary objects, saving garbage collection and battery life.
        for (x in 0 until cols) {
            for (y in 0 until rows) {
                drawRect(
                    color = colors[Random.nextInt(colors.size)],
                    topLeft = Offset(x * cellWidth, y * cellHeight),
                    size = Size(cellWidth, cellHeight)
                )
            }
        }
    }
}

// ==========================================
// 6. HARDWARE CONTROL LIFECYCLE
// ==========================================
@Composable
fun MaximizeDisplayCapabilities(window: Window) {
    // DisposableEffect runs when the screen opens, and onDispose runs when it closes
    DisposableEffect(Unit) {
        val originalBrightness = window.attributes.screenBrightness
        val layoutParams = window.attributes
        
        // Force screen on and brightness to hardware maximum
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
        window.attributes = layoutParams

        onDispose {
            // Restore the user's original settings perfectly when they leave the screen
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            layoutParams.screenBrightness = originalBrightness
            window.attributes = layoutParams
        }
    }
}
