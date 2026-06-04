package com.example.movie.core.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movie.core.auth.domain.model.AuthResult
import com.example.movie.core.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<AuthEffect>()
    val effect: SharedFlow<AuthEffect> = _effect.asSharedFlow()

    init {
        checkExistingSession()
    }

    fun onIntent(intent: AuthIntent) {
        when (intent) {
            AuthIntent.SwitchToLogin -> {
                _state.update {
                    it.copy(
                        mode = AuthMode.LOGIN,
                        errorMessage = null
                    )
                }
            }

            AuthIntent.SwitchToSignup -> {
                _state.update {
                    it.copy(
                        mode = AuthMode.SIGNUP,
                        errorMessage = null
                    )
                }
            }

            is AuthIntent.FullNameChanged -> {
                _state.update {
                    it.copy(
                        fullName = intent.value,
                        errorMessage = null
                    )
                }
            }

            is AuthIntent.UsernameChanged -> {
                _state.update {
                    it.copy(
                        username = intent.value,
                        errorMessage = null
                    )
                }
            }

            is AuthIntent.PasswordChanged -> {
                _state.update {
                    it.copy(
                        password = intent.value,
                        errorMessage = null
                    )
                }
            }

            AuthIntent.Submit -> {
                submit()
            }
        }
    }

    private fun checkExistingSession() {
        viewModelScope.launch {
            val token = authRepository.tokenFlow.first()

            if (token.isNullOrBlank()) {
                _state.update {
                    it.copy(
                        isCheckingSession = false
                    )
                }
                return@launch
            }

            val user = authRepository.getMe()

            if (user != null) {
                _effect.emit(AuthEffect.OpenMovies)
            } else {
                _state.update {
                    it.copy(
                        isCheckingSession = false
                    )
                }
            }
        }
    }

    private fun submit() {
        val currentState = state.value

        val username = currentState.username.trim()
        val password = currentState.password
        val fullName = currentState.fullName.trim()

        if (username.isBlank() || password.isBlank()) {
            _state.update {
                it.copy(
                    errorMessage = "Username i password su obavezni."
                )
            }
            return
        }

        if (currentState.mode == AuthMode.SIGNUP && fullName.isBlank()) {
            _state.update {
                it.copy(
                    errorMessage = "Full name je obavezan za registraciju."
                )
            }
            return
        }

        if (username.length < 3) {
            _state.update {
                it.copy(
                    errorMessage = "Username mora imati najmanje 3 karaktera."
                )
            }
            return
        }

        if (password.length < 8) {
            _state.update {
                it.copy(
                    errorMessage = "Password mora imati najmanje 8 karaktera."
                )
            }
            return
        }

        val usernameRegex = Regex("^[A-Za-z0-9_]+$")

        if (!usernameRegex.matches(username)) {
            _state.update {
                it.copy(
                    errorMessage = "Username sme da sadrži samo slova, cifre i donju crtu."
                )
            }
            return
        }

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            val result = when (currentState.mode) {
                AuthMode.LOGIN -> {
                    authRepository.login(
                        username = username,
                        password = password
                    )
                }

                AuthMode.SIGNUP -> {
                    authRepository.signup(
                        fullName = fullName,
                        username = username,
                        password = password
                    )
                }
            }

            when (result) {
                is AuthResult.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false
                        )
                    }

                    _effect.emit(AuthEffect.OpenMovies)
                }

                is AuthResult.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }
}