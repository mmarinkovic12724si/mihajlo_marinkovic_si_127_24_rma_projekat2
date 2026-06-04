package com.example.movie.core.lists.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.movie.app.common.ImageUrlBuilder
import com.example.movie.cinema.model.Movie

@Composable
fun MovieCollectionScreen(
    type: MovieCollectionType,
    state: MovieCollectionState,
    onIntent: (MovieCollectionIntent) -> Unit,
    onBackClick: () -> Unit
) {
    LaunchedEffect(type) {
        onIntent(
            MovieCollectionIntent.Load(
                type = type
            )
        )
    }

    MovieCollectionContent(
        state = state,
        onIntent = onIntent,
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MovieCollectionContent(
    state: MovieCollectionState,
    onIntent: (MovieCollectionIntent) -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = state.title)
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            onIntent(MovieCollectionIntent.Retry)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
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

            state.movies.isEmpty() -> {
                EmptyCollectionContent(
                    message = state.emptyMessage,
                    errorMessage = state.errorMessage,
                    onRetryClick = {
                        onIntent(MovieCollectionIntent.Retry)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    state.errorMessage?.let { message ->
                        item {
                            Text(
                                text = message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    items(
                        items = state.movies,
                        key = { movie ->
                            movie.movieId
                        }
                    ) { movie ->
                        MovieCollectionItem(
                            movie = movie,
                            imageHost = state.imageHost,
                            isRemoving = state.removingMovieId == movie.movieId,
                            removeLabel = when (state.type) {
                                MovieCollectionType.FAVORITES -> "Remove from Favorite"
                                MovieCollectionType.WATCHLIST -> "Remove from Watchlist"
                            },
                            onMovieClick = {
                                onIntent(
                                    MovieCollectionIntent.OpenMovie(
                                        movieId = movie.movieId
                                    )
                                )
                            },
                            onRemoveClick = {
                                onIntent(
                                    MovieCollectionIntent.RemoveMovie(
                                        movieId = movie.movieId
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyCollectionContent(
    message: String,
    errorMessage: String?,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium
        )

        errorMessage?.let { text ->
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = text,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onRetryClick
        ) {
            Text(text = "Refresh")
        }
    }
}

@Composable
private fun MovieCollectionItem(
    movie: Movie,
    imageHost: String,
    isRemoving: Boolean,
    removeLabel: String,
    onMovieClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onMovieClick()
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val posterUrl = ImageUrlBuilder.buildPosterUrl(
                imageHost = imageHost,
                imagePath = movie.posterUrl
            )

            AsyncImage(
                model = posterUrl,
                contentDescription = movie.movieTitle,
                modifier = Modifier
                    .width(76.dp)
                    .height(112.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = movie.movieTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = movie.releaseDateYear?.toString() ?: "Unknown year",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "IMDb %.1f".format(movie.imdbScore),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Card {
                IconButton(
                    onClick = onRemoveClick,
                    enabled = !isRemoving
                ) {
                    if (isRemoving) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = removeLabel
                        )
                    }
                }
            }
        }
    }
}