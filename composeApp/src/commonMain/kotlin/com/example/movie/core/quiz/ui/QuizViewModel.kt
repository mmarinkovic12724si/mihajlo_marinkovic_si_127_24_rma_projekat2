package com.example.movie.core.quiz.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movie.cinema.domain.service.GetImageBaseService
import com.example.movie.core.quiz.domain.model.QuizResult
import com.example.movie.core.quiz.domain.repository.QuizRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.min

class QuizViewModel(
    private val quizRepository: QuizRepository,
    private val getImageBaseService: GetImageBaseService
) : ViewModel() {

    private val _state = MutableStateFlow(QuizState())
    val state: StateFlow<QuizState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<QuizEffect>()
    val effect: SharedFlow<QuizEffect> = _effect.asSharedFlow()

    private var timerJob: Job? = null

    fun onIntent(intent: QuizIntent) {
        when (intent) {
            QuizIntent.StartQuiz -> startQuiz()
            is QuizIntent.SelectAnswer -> selectAnswer(intent.answer)
            QuizIntent.Retry -> startQuiz()
            QuizIntent.ExitQuiz -> exitQuiz()
        }
    }

    private fun startQuiz() {
        timerJob?.cancel()

        viewModelScope.launch {
            _state.value = QuizState(
                isLoading = true
            )

            val imageHost = try {
                getImageBaseService()
            } catch (exception: Exception) {
                ""
            }

            val sessionResult = quizRepository.generateQuizSession()

            val questions = sessionResult.getOrNull()

            if (questions == null) {
                _state.value = QuizState(
                    isLoading = false,
                    imageHost = imageHost,
                    errorMessage = sessionResult.exceptionOrNull()?.message
                        ?: "Could not start quiz."
                )
                return@launch
            }

            _state.value = QuizState(
                isLoading = false,
                imageHost = imageHost,
                questions = questions,
                currentQuestionIndex = 0,
                timeLeftSeconds = 60
            )

            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()

        timerJob = viewModelScope.launch {
            while (_state.value.timeLeftSeconds > 0 && !_state.value.isFinished) {
                delay(1000)

                _state.update { currentState ->
                    if (currentState.isFinished) {
                        currentState
                    } else {
                        currentState.copy(
                            timeLeftSeconds = currentState.timeLeftSeconds - 1
                        )
                    }
                }
            }

            if (!_state.value.isFinished) {
                finishQuiz()
            }
        }
    }

    private fun selectAnswer(
        answer: String
    ) {
        val currentState = _state.value

        if (currentState.isFinished || currentState.selectedAnswer != null) {
            return
        }

        val currentQuestion = currentState.currentQuestion ?: return
        val isCorrect = answer == currentQuestion.correctAnswer

        _state.update {
            it.copy(
                selectedAnswer = answer,
                correctAnswers = if (isCorrect) {
                    it.correctAnswers + 1
                } else {
                    it.correctAnswers
                }
            )
        }

        viewModelScope.launch {
            delay(850)

            val latestState = _state.value
            val nextIndex = latestState.currentQuestionIndex + 1

            if (nextIndex >= latestState.questions.size) {
                finishQuiz()
            } else {
                _state.update {
                    it.copy(
                        currentQuestionIndex = nextIndex,
                        selectedAnswer = null
                    )
                }
            }
        }
    }

    private fun finishQuiz() {
        timerJob?.cancel()

        viewModelScope.launch {
            val currentState = _state.value

            if (currentState.isFinished) {
                return@launch
            }

            val totalQuestions = currentState.questions.size
            val correctAnswers = currentState.correctAnswers
            val wrongAnswers = totalQuestions - correctAnswers
            val usedSeconds = 60 - currentState.timeLeftSeconds
            val remainingSeconds = currentState.timeLeftSeconds

            val rawScore = correctAnswers * (9.0 + remainingSeconds / 60.0)
            val finalScore = min(100.0, rawScore)

            val result = QuizResult(
                score = finalScore,
                correctAnswers = correctAnswers,
                wrongAnswers = wrongAnswers,
                usedSeconds = usedSeconds
            )

            quizRepository.saveQuizResult(result)

            _state.update {
                it.copy(
                    isFinished = true,
                    result = result
                )
            }
        }
    }

    private fun exitQuiz() {
        timerJob?.cancel()

        viewModelScope.launch {
            _effect.emit(QuizEffect.GoBack)
        }
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }
}