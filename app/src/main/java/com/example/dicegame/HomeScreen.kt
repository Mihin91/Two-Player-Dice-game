package com.example.dicegame

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(
    onStartGame: (Int) -> Unit,
    onShowAbout: () -> Unit   // Not used in this version; we handle About locally.
) {
    var targetScoreInput by rememberSaveable { mutableStateOf("") }
    // Local state to control whether the AboutDialog is shown.
    var showAboutDialog by rememberSaveable { mutableStateOf(false) }
    val dullYellow = Color(255, 204, 0)

    // Show the About dialog if the flag is true.
    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Welcome to Dice Game!",
            style = TextStyle(fontSize = 24.sp),
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = targetScoreInput,
            onValueChange = { newValue ->
                // Allow only digits
                targetScoreInput = newValue.filter { it.isDigit() }
            },
            label = { Text("Target Score (Default 101)") },
            placeholder = { Text("Enter target score", fontSize = 16.sp, color = Color.Gray) },
            modifier = Modifier
                .padding(bottom = 16.dp)
                .width(280.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = TextStyle(fontSize = 18.sp),
            shape = MaterialTheme.shapes.medium, // Rounded corners
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(255, 204, 0), // Dull yellow
                unfocusedBorderColor = Color.Gray,
                cursorColor = Color(255, 204, 0),
                focusedLabelColor = Color(255, 204, 0),
                unfocusedLabelColor = Color.Gray
            )
        )

        ElevatedButton(
            onClick = {
                val finalTarget = targetScoreInput.toIntOrNull() ?: 101
                onStartGame(finalTarget)
            },
            modifier = Modifier
                .width(250.dp)
                .padding(bottom = 16.dp)
                .height(60.dp),
            colors = ButtonDefaults.elevatedButtonColors(containerColor = dullYellow)
        ) {
            Text("New Game", color = Color.White, style = TextStyle(fontSize = 18.sp))
        }

        ElevatedButton(
            onClick = { showAboutDialog = true },
            modifier = Modifier
                .width(250.dp)
                .height(60.dp),
            colors = ButtonDefaults.elevatedButtonColors(containerColor = dullYellow)
        ) {
            Text("About", color = Color.White, style = TextStyle(fontSize = 18.sp))
        }
    }
}

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Mihin Abeywickrama")
                Text("20232656 / w2081935")
            }
        },
        text = {
            Column {
                Text(
                    "I confirm that I understand what plagiarism is and have read and understood the section on Assessment Offences in the Essential Information for Students. " +
                            "The work that I have submitted is entirely my own. Any work from other authors is duly referenced and acknowledged."
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}
