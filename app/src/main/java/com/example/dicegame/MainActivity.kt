package com.example.dicegame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApp()
        }
    }
}

/**
 * Top-level MyApp that sets up the NavController and NavHost.
 * It tracks the total wins for human and computer and passes them
 * to the DiceGameScreen.
 */
@Composable
fun MyApp() {
    // Create a Compose Navigation controller.
    val navController = rememberNavController()

    // Track total wins for the human and computer.
    var totalHumanWins by rememberSaveable { mutableStateOf(0) }
    var totalComputerWins by rememberSaveable { mutableStateOf(0) }

    // Define the NavHost with two routes: "home" and "game/{targetScore}".
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        // Home route.
        composable("home") {
            HomeScreen(
                onStartGame = { targetScore ->
                    // Navigate to the game route with the target score as an argument.
                    navController.navigate("game/$targetScore")
                },
                onShowAbout = {
                    // Implement your About logic here if needed.
                }
            )
        }
        // Game route with targetScore as an argument.
        composable("game/{targetScore}") { backStackEntry ->
            // Parse the target score passed as an argument.
            val rawScore = backStackEntry.arguments?.getString("targetScore")
            val target = rawScore?.toIntOrNull() ?: 101

            DiceGameScreen(
                targetScore = target,
                totalHumanWins = totalHumanWins,
                totalComputerWins = totalComputerWins,
                onHumanWin = { totalHumanWins++ },
                onComputerWin = { totalComputerWins++ },
                onNavigateBack = {
                    // Navigate back to the Home screen.
                    navController.popBackStack()
                }
            )
        }
    }
}
