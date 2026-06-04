package com.example.movie.core.profile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movie.core.auth.domain.repository.AuthRepository
import com.example.movie.core.auth.domain.repository.MovieListsRepository
import com.example.movie.core.database.dao.FavoriteDao
import com.example.movie.core.database.dao.QuizStatsDao
import com.example.movie.core.database.dao.WatchlistDao
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val favoriteDao: FavoriteDao,
    private val watchlistDao: WatchlistDao,
    private val quizStatsDao: QuizStatsDao,
    private val movieListsRepository: MovieListsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<ProfileEffect>()
    val effect: SharedFlow<ProfileEffect> = _effect.asSharedFlow()

    init {
        observeLocalStats()
    }

    fun onIntent(intent: ProfileIntent) {
        when (intent) {
            ProfileIntent.LoadProfile -> {
                loadProfile()
            }

            ProfileIntent.Logout -> {
                logout()
            }
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    fullName = "",
                    username = "",
                    errorMessage = null
                )
            }

            val user = authRepository.getMe()

            if (user == null) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        fullName = "",
                        username = "",
                        errorMessage = "Sesija je istekla. Prijavi se ponovo."
                    )
                }

                _effect.emit(ProfileEffect.OpenAuth)
                return@launch
            }

            _state.update {
                it.copy(
                    fullName = user.fullName,
                    username = user.username,
                    errorMessage = null
                )
            }

            syncUserMovieLists()

            _state.update {
                it.copy(
                    isLoading = false
                )
            }
        }
    }

    private suspend fun syncUserMovieLists() {
        val favoriteResult = movieListsRepository.syncFavorites()
        val watchlistResult = movieListsRepository.syncWatchlist()

        val errorMessage = favoriteResult.exceptionOrNull()?.message
            ?: watchlistResult.exceptionOrNull()?.message

        if (errorMessage != null) {
            _state.update {
                it.copy(
                    errorMessage = errorMessage
                )
            }
        }
    }

    private fun observeLocalStats() {
        viewModelScope.launch {
            combine(
                favoriteDao.observeFavoriteCount(),
                watchlistDao.observeWatchlistCount(),
                quizStatsDao.observeQuizStats()
            ) { favoriteCount, watchlistCount, quizStats ->
                ProfileLocalStats(
                    favoriteCount = favoriteCount,
                    watchlistCount = watchlistCount,
                    bestScore = quizStats?.bestScore ?: 0.0,
                    gamesPlayed = quizStats?.gamesPlayed ?: 0
                )
            }.collect { localStats ->
                _state.update {
                    it.copy(
                        favoriteCount = localStats.favoriteCount,
                        watchlistCount = localStats.watchlistCount,
                        bestScore = localStats.bestScore,
                        gamesPlayed = localStats.gamesPlayed
                    )
                }
            }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            authRepository.logout()

            _state.value = ProfileState()

            _effect.emit(ProfileEffect.OpenAuth)
        }
    }

    private data class ProfileLocalStats(
        val favoriteCount: Int,
        val watchlistCount: Int,
        val bestScore: Double,
        val gamesPlayed: Int
    )
}