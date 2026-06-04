package com.example.movie.core.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun AuthScreen(
    state: AuthState,
    onIntent: (AuthIntent) -> Unit
) {
    if (state.isCheckingSession) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Proveravam sesiju...")
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Showtime",
                    style = MaterialTheme.typography.headlineMedium
                )

                Text(
                    text = if (state.mode == AuthMode.LOGIN) {
                        "Prijavi se da nastaviš"
                    } else {
                        "Napravi novi nalog"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (state.mode == AuthMode.LOGIN) {
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                onIntent(AuthIntent.SwitchToLogin)
                            }
                        ) {
                            Text(text = "Login")
                        }

                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                onIntent(AuthIntent.SwitchToSignup)
                            }
                        ) {
                            Text(text = "Signup")
                        }
                    } else {
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                onIntent(AuthIntent.SwitchToLogin)
                            }
                        ) {
                            Text(text = "Login")
                        }

                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                onIntent(AuthIntent.SwitchToSignup)
                            }
                        ) {
                            Text(text = "Signup")
                        }
                    }
                }

                if (state.mode == AuthMode.SIGNUP) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = state.fullName,
                        onValueChange = {
                            onIntent(AuthIntent.FullNameChanged(it))
                        },
                        label = {
                            Text(text = "Full name")
                        },
                        singleLine = true,
                        enabled = !state.isLoading
                    )
                }

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.username,
                    onValueChange = {
                        onIntent(AuthIntent.UsernameChanged(it))
                    },
                    label = {
                        Text(text = "Username")
                    },
                    singleLine = true,
                    enabled = !state.isLoading
                )

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.password,
                    onValueChange = {
                        onIntent(AuthIntent.PasswordChanged(it))
                    },
                    label = {
                        Text(text = "Password")
                    },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password
                    ),
                    enabled = !state.isLoading
                )

                state.errorMessage?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        onIntent(AuthIntent.Submit)
                    },
                    enabled = !state.isLoading
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator()
                    } else {
                        Text(
                            text = if (state.mode == AuthMode.LOGIN) {
                                "Login"
                            } else {
                                "Create account"
                            }
                        )
                    }
                }

                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        if (state.mode == AuthMode.LOGIN) {
                            onIntent(AuthIntent.SwitchToSignup)
                        } else {
                            onIntent(AuthIntent.SwitchToLogin)
                        }
                    },
                    enabled = !state.isLoading
                ) {
                    Text(
                        text = if (state.mode == AuthMode.LOGIN) {
                            "Nemaš nalog? Registruj se"
                        } else {
                            "Imaš nalog? Prijavi se"
                        }
                    )
                }
            }
        }
    }
}