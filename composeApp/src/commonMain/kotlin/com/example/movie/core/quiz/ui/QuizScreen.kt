package com.example.movie.core.quiz.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.movie.core.platform.PlatformBackHandler
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.movie.app.common.ImageUrlBuilder
import com.example.movie.core.quiz.domain.model.QuizQuestion
import com.example.movie.core.quiz.domain.model.QuizQuestionType
import com.example.movie.core.quiz.domain.model.QuizResult

@Composable
fun QuizScreen(
    state: QuizState,
    onIntent: (QuizIntent) -> Unit,
    onBackClick: () -> Unit
) {
    var showExitDialog by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        onIntent(QuizIntent.StartQuiz)
    }

    PlatformBackHandler(
        enabled = !state.isFinished
    ) {
        showExitDialog = true
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = {
                showExitDialog = false
            },
            title = {
                Text(text = "Abandon quiz?")
            },
            text = {
                Text(text = "Your progress will be lost.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        onIntent(QuizIntent.ExitQuiz)
                    }
                ) {
                    Text(text = "Exit")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                    }
                ) {
                    Text(text = "Continue")
                }
            }
        )
    }

    QuizContent(
        state = state,
        onIntent = onIntent,
        onBackClick = onBackClick,
        onAskExit = {
            if (state.isFinished) {
                onBackClick()
            } else {
                showExitDialog = true
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuizContent(
    state: QuizState,
    onIntent: (QuizIntent) -> Unit,
    onBackClick: () -> Unit,
    onAskExit: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Quiz")
                },
                navigationIcon = {
                    TextButton(
                        onClick = onAskExit
                    ) {
                        Text(text = "Close")
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.errorMessage != null -> {
                QuizErrorContent(
                    message = state.errorMessage,
                    onRetryClick = {
                        onIntent(QuizIntent.Retry)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }

            state.isFinished && state.result != null -> {
                QuizResultContent(
                    result = state.result,
                    onBackClick = onBackClick,
                    onRetryClick = {
                        onIntent(QuizIntent.Retry)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }

            else -> {
                val question = state.currentQuestion

                if (question != null) {
                    QuizQuestionContent(
                        question = question,
                        imageHost = state.imageHost,
                        questionNumber = state.questionNumber,
                        totalQuestions = state.totalQuestions,
                        timeLeftSeconds = state.timeLeftSeconds,
                        selectedAnswer = state.selectedAnswer,
                        onAnswerClick = { answer ->
                            onIntent(
                                QuizIntent.SelectAnswer(
                                    answer = answer
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuizQuestionContent(
    question: QuizQuestion,
    imageHost: String,
    questionNumber: Int,
    totalQuestions: Int,
    timeLeftSeconds: Int,
    selectedAnswer: String?,
    onAnswerClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val imageUrl = when (question.type) {
        QuizQuestionType.GUESS_MOVIE -> {
            ImageUrlBuilder.buildBackdropUrl(
                imageHost = imageHost,
                imagePath = question.imagePath
            )
        }

        QuizQuestionType.GUESS_YEAR,
        QuizQuestionType.GUESS_LEAD_ACTOR -> {
            ImageUrlBuilder.buildPosterUrl(
                imageHost = imageHost,
                imagePath = question.imagePath
            )
        }
    }

    Column(
        modifier = modifier
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        RowHeader(
            questionNumber = questionNumber,
            totalQuestions = totalQuestions,
            timeLeftSeconds = timeLeftSeconds
        )

        LinearProgressIndicator(
            progress = {
                questionNumber.toFloat() / totalQuestions.toFloat()
            },
            modifier = Modifier.fillMaxWidth()
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = question.movieTitle,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .clip(RoundedCornerShape(20.dp)),
                contentScale = ContentScale.Crop
            )
        }

        Text(
            text = question.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = question.subtitle,
            style = MaterialTheme.typography.bodyLarge
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            question.options.forEach { option ->
                val isSelected = selectedAnswer == option
                val isCorrect = option == question.correctAnswer
                val showResult = selectedAnswer != null

                val label = when {
                    showResult && isCorrect -> "✓ $option"
                    showResult && isSelected && !isCorrect -> "✕ $option"
                    else -> option
                }

                ElevatedButton(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedAnswer == null,
                    onClick = {
                        onAnswerClick(option)
                    },
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Text(text = label)
                }
            }
        }
    }
}

@Composable
private fun RowHeader(
    questionNumber: Int,
    totalQuestions: Int,
    timeLeftSeconds: Int
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Question $questionNumber / $totalQuestions",
                fontWeight = FontWeight.Bold
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = "Timer"
                )

                Text(
                    text = "${timeLeftSeconds}s",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun QuizResultContent(
    result: QuizResult,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Quiz Result",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "%.2f / 100".format(result.score),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )

                ResultRow(
                    label = "Correct answers",
                    value = result.correctAnswers.toString(),
                    isGood = true
                )

                ResultRow(
                    label = "Wrong answers",
                    value = result.wrongAnswers.toString(),
                    isGood = false
                )

                ResultRow(
                    label = "Used time",
                    value = "${result.usedSeconds}s",
                    isGood = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onRetryClick
                ) {
                    Text(text = "Play again")
                }

                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onBackClick
                ) {
                    Text(text = "Back to profile")
                }
            }
        }
    }
}

@Composable
private fun ResultRow(
    label: String,
    value: String,
    isGood: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isGood) {
                    Icons.Default.CheckCircle
                } else {
                    Icons.Default.Close
                },
                contentDescription = label
            )

            Text(text = label)
        }

        Text(
            text = value,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun QuizErrorContent(
    message: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onRetryClick
        ) {
            Text(text = "Retry")
        }
    }
}