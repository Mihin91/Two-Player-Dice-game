package com.example.dicegame

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

@Composable
fun DiceGameScreen(
    targetScore: Int,
    totalHumanWins: Int,
    totalComputerWins: Int,
    onHumanWin: () -> Unit,
    onComputerWin: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val dullYellow = Color(255, 204, 0)
    val darkYellow = Color(204, 153, 0) // Darker variant for held dice

    // Advanced strategy toggle state.
    var advancedStrategyEnabled by rememberSaveable { mutableStateOf(false) }

    // HUMAN state
    var humanDice by rememberSaveable { mutableStateOf(List(5) { 1 }) }
    var humanScore by rememberSaveable { mutableStateOf(0) }
    var humanAttempts by rememberSaveable { mutableStateOf(0) }
    var rollCount by rememberSaveable { mutableStateOf(0) }
    // selectedDice represents which dice the human holds (true means held)
    var selectedDice by rememberSaveable { mutableStateOf(List(5) { false }) }

    // COMPUTER state
    var computerDice by rememberSaveable { mutableStateOf(List(5) { 1 }) }
    var computerScore by rememberSaveable { mutableStateOf(0) }
    var computerAttempts by rememberSaveable { mutableStateOf(0) }
    var computerRollCount by rememberSaveable { mutableStateOf(0) }

    // currentPlayer (0 = human, 1 = computer) – kept for potential expansion.
    var currentPlayer by rememberSaveable { mutableStateOf(0) }

    // Tie-break / endgame state
    var isTieBreak by rememberSaveable { mutableStateOf(false) }
    var gameOver by rememberSaveable { mutableStateOf(false) }
    var showWinnerDialog by rememberSaveable { mutableStateOf(false) }
    var humanIsWinner by rememberSaveable { mutableStateOf(false) }

    fun resetGameState() {
        humanDice = List(5) { 1 }
        computerDice = List(5) { 1 }
        selectedDice = List(5) { false }
        rollCount = 0
        computerRollCount = 0
        currentPlayer = 0
    }

    if (gameOver && isTieBreak) {
        TieBreakDialog(onTieBreakRoll = {
            val hRoll = List(5) { Random.nextInt(1, 7) }
            val cRoll = List(5) { Random.nextInt(1, 7) }
            val hSum = hRoll.sum()
            val cSum = cRoll.sum()
            // Update both players’ scores with their respective tie-break roll sums.
            humanScore += hSum
            computerScore += cSum
            when {
                hSum > cSum -> {
                    onHumanWin()
                    humanIsWinner = true
                    showWinnerDialog = true
                    isTieBreak = false
                    resetGameState() // Reset dice and held state.
                }
                cSum > hSum -> {
                    onComputerWin()
                    humanIsWinner = false
                    showWinnerDialog = true
                    isTieBreak = false
                    resetGameState() // Reset dice and held state.
                }
                else -> {
                    // Remain in tie-break if still tied.
                }
            }
        })
    }

    // Winner dialog: when a final result is reached.
    if (showWinnerDialog) {
        WinnerDialog(
            humanIsWinner = humanIsWinner,
            onDismiss = {
                // Game is over; user should use the system back button to return to Home.
                showWinnerDialog = false
            }
        )
    }

    // Wrap the entire game UI in a Box to center the content.
    Box(modifier = Modifier.fillMaxSize()) {
        // Fixed total wins scoreboard at top-left.
        Text(
            text = "H:$totalHumanWins / C:$totalComputerWins",
            style = TextStyle(fontSize = 16.sp),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        )

        // Main game content centered and scrollable.
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Display target score so the player knows what they're playing for.
            Text(
                text = "Target Score: $targetScore",
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium),
                modifier = Modifier.padding(top = 8.dp)
            )

            // Current game scores.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text("Your Score: $humanScore\nAttempts: $humanAttempts", style = TextStyle(fontSize = 16.sp))
                Text("Computer Score: $computerScore\nAttempts: $computerAttempts", style = TextStyle(fontSize = 16.sp))
            }

            // Human dice row.
            Text("Your Dice", style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold))
            Row(modifier = Modifier.padding(8.dp)) {
                humanDice.forEachIndexed { index, value ->
                    // If a die is held (selected), display its border in dark yellow with increased width.
                    val borderModifier = if (selectedDice[index]) {
                        Modifier
                            .border(BorderStroke(4.dp, darkYellow), RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
                    } else Modifier

                    Image(
                        painter = painterResource(id = getDiceImage(value)),
                        contentDescription = "Human Dice $index",
                        modifier = Modifier
                            .size(60.dp)
                            .padding(4.dp)
                            .then(borderModifier)
                            .clickable {
                                if (!gameOver && currentPlayer == 0 && rollCount in 1..2) {
                                    selectedDice = selectedDice.toMutableList().also {
                                        it[index] = !it[index]
                                    }
                                }
                            }
                    )
                }
            }

            // Computer dice row.
            Text("Computer's Dice", style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold))
            Row(modifier = Modifier.padding(8.dp)) {
                computerDice.forEach { diceVal ->
                    Image(
                        painter = painterResource(id = getDiceImage(diceVal)),
                        contentDescription = "Computer Dice",
                        modifier = Modifier
                            .size(60.dp)
                            .padding(4.dp)
                    )
                }
            }

            // Human controls (only visible if game is not over and it's the human's turn).
            if (!gameOver && currentPlayer == 0) {
                Text("Roll count in this turn: $rollCount / 3", style = TextStyle(fontSize = 16.sp))
                // Disable the "Throw/Re-Roll" button if all dice are held.
                val canReRoll = rollCount == 0 || !selectedDice.all { it }
                ElevatedButton(
                    onClick = {
                        rollCount++
                        if (rollCount == 1) {
                            // First roll: roll all human dice and reset selection.
                            humanDice = List(5) { Random.nextInt(1, 7) }
                            selectedDice = List(5) { false }
                            // Computer does its first roll.
                            computerDice = List(5) { Random.nextInt(1, 7) }
                            computerRollCount = 1
                        } else if (rollCount <= 3) {
                            // Re-roll only unselected dice.
                            humanDice = humanDice.mapIndexed { i, oldVal ->
                                if (selectedDice[i]) oldVal else Random.nextInt(1, 7)
                            }
                        }
                        // If 3 rolls are used, auto-score.
                        if (rollCount == 3) {
                            endHumanTurn(
                                humanDice,
                                onScore = {
                                    humanScore += humanDice.sum()
                                    humanAttempts++
                                },
                                onNext = {
                                    val result = checkWinCondition(
                                        humanScore, computerScore,
                                        humanAttempts, computerAttempts,
                                        targetScore
                                    )
                                    handleWinCheckResult(
                                        result,
                                        onHumanWins = {
                                            onHumanWin()
                                            humanIsWinner = true
                                            showWinnerDialog = true
                                            gameOver = true
                                            resetGameState()
                                        },
                                        onComputerWins = {
                                            onComputerWin()
                                            humanIsWinner = false
                                            showWinnerDialog = true
                                            gameOver = true
                                            resetGameState()
                                        },
                                        onTieBreakNeeded = {
                                            gameOver = true
                                            isTieBreak = true
                                        }
                                    )
                                    if (!gameOver) {
                                        finishComputerTurnIfNeeded(
                                            computerDice,
                                            computerRollCount,
                                            advancedStrategy = advancedStrategyEnabled,
                                            humanScore = humanScore,
                                            computerScore = computerScore,
                                            onDiceUpdate = { computerDice = it },
                                            onScoreUpdate = { computerScore += it },
                                            onAttemptsUpdate = { computerAttempts++ }
                                        )
                                        val compResult = checkWinCondition(
                                            humanScore, computerScore,
                                            humanAttempts, computerAttempts,
                                            targetScore
                                        )
                                        handleWinCheckResult(
                                            compResult,
                                            onHumanWins = {
                                                onHumanWin()
                                                humanIsWinner = true
                                                showWinnerDialog = true
                                                gameOver = true
                                            },
                                            onComputerWins = {
                                                onComputerWin()
                                                humanIsWinner = false
                                                showWinnerDialog = true
                                                gameOver = true
                                            },
                                            onTieBreakNeeded = {
                                                gameOver = true
                                                isTieBreak = true
                                            }
                                        )
                                        if (!gameOver) {
                                            // Reset held dice for new turn.
                                            selectedDice = List(5) { false }
                                            // Start new turn.
                                            currentPlayer = 0
                                            rollCount = 0
                                            computerRollCount = 0
                                        }
                                    }
                                }
                            )
                        }
                    },
                    enabled = rollCount < 3 && canReRoll,
                    modifier = Modifier
                        .width(220.dp)
                        .height(50.dp),
                    colors = ButtonDefaults.elevatedButtonColors(containerColor = dullYellow)
                ) {
                    Text(
                        text = if (rollCount == 0) "Throw" else "Re-Roll",
                        color = Color.White,
                        style = TextStyle(fontSize = 16.sp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                ElevatedButton(
                    onClick = {
                        endHumanTurn(
                            humanDice,
                            onScore = {
                                humanScore += humanDice.sum()
                                humanAttempts++
                            },
                            onNext = {
                                val result = checkWinCondition(
                                    humanScore, computerScore,
                                    humanAttempts, computerAttempts,
                                    targetScore
                                )
                                handleWinCheckResult(
                                    result,
                                    onHumanWins = {
                                        onHumanWin()
                                        humanIsWinner = true
                                        showWinnerDialog = true
                                        gameOver = true
                                        resetGameState()
                                    },
                                    onComputerWins = {
                                        onComputerWin()
                                        humanIsWinner = false
                                        showWinnerDialog = true
                                        gameOver = true
                                        resetGameState()
                                    },
                                    onTieBreakNeeded = {
                                        gameOver = true
                                        isTieBreak = true
                                    }
                                )
                                if (!gameOver) {
                                    finishComputerTurnIfNeeded(
                                        computerDice,
                                        computerRollCount,
                                        advancedStrategy = advancedStrategyEnabled,
                                        humanScore = humanScore,
                                        computerScore = computerScore,
                                        onDiceUpdate = { computerDice = it },
                                        onScoreUpdate = { computerScore += it },
                                        onAttemptsUpdate = { computerAttempts++ }
                                    )
                                    val compResult = checkWinCondition(
                                        humanScore, computerScore,
                                        humanAttempts, computerAttempts,
                                        targetScore
                                    )
                                    handleWinCheckResult(
                                        compResult,
                                        onHumanWins = {
                                            onHumanWin()
                                            humanIsWinner = true
                                            showWinnerDialog = true
                                            gameOver = true
                                        },
                                        onComputerWins = {
                                            onComputerWin()
                                            humanIsWinner = false
                                            showWinnerDialog = true
                                            gameOver = true
                                        },
                                        onTieBreakNeeded = {
                                            gameOver = true
                                            isTieBreak = true
                                        }
                                    )
                                    if (!gameOver) {
                                        currentPlayer = 0
                                        rollCount = 0
                                        computerRollCount = 0
                                        // Reset held dice for new turn.
                                        selectedDice = List(5) { false }
                                    }
                                }
                            }
                        )
                    },
                    enabled = rollCount > 0,
                    modifier = Modifier
                        .width(220.dp)
                        .height(50.dp),
                    colors = ButtonDefaults.elevatedButtonColors(containerColor = dullYellow)
                ) {
                    Text("Score", color = Color.White, style = TextStyle(fontSize = 16.sp))
                }
            }

            // Advanced strategy toggle at the bottom.
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text("Advanced Strategy", style = TextStyle(fontSize = 16.sp))
                Spacer(modifier = Modifier.width(8.dp))
                // Disable toggle if game is over.
                Switch(
                    checked = advancedStrategyEnabled,
                    onCheckedChange = { advancedStrategyEnabled = it },
                    enabled = !gameOver,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = dullYellow,
                        uncheckedThumbColor = Color.Gray
                    )
                )
            }
        }
    }
}

// Called when the human's turn ends.
private fun endHumanTurn(
    humanDice: List<Int>,
    onScore: () -> Unit,
    onNext: () -> Unit
) {
    onScore()
    onNext()
}

/**
 * Computer's random re-rolls function, with an additional parameter for advanced strategy.
 *
 * If advancedStrategy is enabled, the computer uses a more intelligent approach:
 * - It calculates the difference (humanScore - computerScore).
 * - If the computer is behind (difference > 0), it sets a higher threshold (e.g., 4 or 5)
 *   and re-rolls any dice below that threshold.
 * - If the computer is ahead or tied, it re-rolls only dice below a lower threshold (e.g., 3).
 *
 * Otherwise, it uses the default random 50/50 re-roll chance.
 */
private fun finishComputerTurnIfNeeded(
    currentDice: List<Int>,
    currentRollCount: Int,
    advancedStrategy: Boolean,
    humanScore: Int,
    computerScore: Int,
    onDiceUpdate: (List<Int>) -> Unit,
    onScoreUpdate: (Int) -> Unit,
    onAttemptsUpdate: () -> Unit
) {
    var dice = currentDice
    var rollUsed = currentRollCount
    while (rollUsed < 3) {
        if (advancedStrategy) {
            // Compute the difference and set a threshold.
            val difference = humanScore - computerScore
            val threshold = when {
                difference > 10 -> 5  // If far behind, aim for high numbers.
                difference > 0 -> 4   // If moderately behind, aim for moderately high numbers.
                else -> 3             // If tied or ahead, only re-roll very low dice.
            }
            // Re-roll dice with value below the threshold.
            dice = dice.map { die ->
                if (die < threshold) Random.nextInt(1, 7) else die
            }
        } else {
            // Default random strategy: re-roll each die with a 50% chance.
            dice = dice.map { oldVal ->
                if (Random.nextBoolean()) oldVal else Random.nextInt(1, 7)
            }
        }
        onDiceUpdate(dice)
        rollUsed++
    }
    onScoreUpdate(dice.sum())
    onAttemptsUpdate()
}

// Checks for final or partial results.
private fun checkWinCondition(
    humanScore: Int,
    computerScore: Int,
    humanAttempts: Int,
    computerAttempts: Int,
    target: Int
): WinCheckResult {
    val humanReached = humanScore >= target
    val compReached = computerScore >= target
    if (!humanReached && !compReached) {
        return WinCheckResult(false, false, "")
    }
    if (humanReached && !compReached) {
        return if (humanAttempts == computerAttempts) {
            WinCheckResult(true, false, "You reached $target! You win!")
        } else {
            WinCheckResult(false, false, "")
        }
    }
    if (!humanReached && compReached) {
        return if (computerAttempts == humanAttempts) {
            WinCheckResult(true, false, "Computer reached $target! Computer wins!")
        } else {
            WinCheckResult(false, false, "")
        }
    }
    return when {
        humanAttempts < computerAttempts ->
            WinCheckResult(true, false, "You reached $target first! You win!")
        computerAttempts < humanAttempts ->
            WinCheckResult(true, false, "Computer reached $target first! Computer wins!")
        else -> {
            if (humanScore > computerScore) {
                WinCheckResult(true, false, "Both reached $target, but you have a higher score! You win!")
            } else if (computerScore > humanScore) {
                WinCheckResult(true, false, "Both reached $target, but Computer has a higher score! Computer wins!")
            } else {
                WinCheckResult(true, true, "")
            }
        }
    }
}

data class WinCheckResult(
    val isGameOver: Boolean,
    val isTie: Boolean,
    val message: String
)

// Handles final outcome or tie-break.
private fun handleWinCheckResult(
    result: WinCheckResult,
    onHumanWins: () -> Unit,
    onComputerWins: () -> Unit,
    onTieBreakNeeded: () -> Unit
) {
    if (result.isGameOver) {
        if (result.isTie) {
            onTieBreakNeeded()
        } else {
            if (result.message.contains("You win", ignoreCase = true)) {
                onHumanWins()
            } else if (result.message.contains("Computer wins", ignoreCase = true)) {
                onComputerWins()
            }
        }
    }
}

/** FULL code for WinnerDialog below: */
@Composable
fun WinnerDialog(
    humanIsWinner: Boolean,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            val text = if (humanIsWinner) "You win!" else "You lose!"
            val color = if (humanIsWinner) Color.Green else Color.Red
            Text(text, color = color, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            // Additional explanation can be provided here if desired.
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

/** FULL code for TieBreakDialog below: */
@Composable
fun TieBreakDialog(
    onTieBreakRoll: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* must continue tie-break until resolved */ },
        title = { Text("Tie-Break!") },
        text = {
            Text(
                "Both players reached the same score on the same attempt.\n" +
                        "Time for a single-roll shootout!\nPress 'Roll Now' to roll again."
            )
        },
        confirmButton = {
            Button(onClick = { onTieBreakRoll() }) {
                Text("Roll Now")
            }
        }
    )
}

/** Return the resource ID for the dice face in [1..6]. */
fun getDiceImage(diceValue: Int): Int {
    return when (diceValue) {
        1 -> R.drawable.dice1
        2 -> R.drawable.dice2
        3 -> R.drawable.dice3
        4 -> R.drawable.dice4
        5 -> R.drawable.dice5
        6 -> R.drawable.dice6
        else -> R.drawable.dice1
    }
}
