package com.example.movie.cinema.ui.start_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movie.app.common.BrowseFilters
import com.example.movie.app.common.SortParametar
import com.example.movie.app.common.UiState
import com.example.movie.cinema.domain.service.GetGenresService
import com.example.movie.cinema.domain.service.GetImageBaseService
import com.example.movie.cinema.domain.service.GetMoviesService
import com.example.movie.cinema.model.Genre
import com.example.movie.cinema.model.MoviesPage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StartScreenViewModel(
    private val getMoviesService: GetMoviesService,
    private val getImageBaseService: GetImageBaseService,
    private val getGenresService: GetGenresService
) : ViewModel() {

    private val _state = MutableStateFlow(StartScreenState())
    val state: StateFlow<StartScreenState> = _state

    private val _effect = Channel<StartScreenEffect>()
    val effect = _effect.receiveAsFlow()

    private var isFilterDraftPrepared = false

    private var moviesObserverJob: Job? = null

    fun onIntent(intent: StartScreenIntent) {
        when (intent) {
            StartScreenIntent.FetchMovies -> {
                if (_state.value.sourceMovies.isEmpty() && _state.value.screenState !is UiState.Success) {
                    fetchMovies()
                }
            }

            StartScreenIntent.RetryLoad -> fetchMovies()

            is StartScreenIntent.ChangeSort -> updateSort(intent.selectedSort)

            is StartScreenIntent.SubmitFilters -> submitFilters(intent.selectedFilters)

            is StartScreenIntent.OpenMovie -> sendOpenMovieEffect(intent.targetMovieId)

            StartScreenIntent.OpenFilterScreen -> sendOpenFiltersEffect()

            StartScreenIntent.FilterScreenOpened -> {
                if (!isFilterDraftPrepared) {
                    _state.value = _state.value.copy(
                        pendingFilters = _state.value.activeFilters
                    )
                    isFilterDraftPrepared = true
                }

                fetchGenresIfNeeded()
            }

            is StartScreenIntent.DraftQueryChanged -> {
                _state.value = _state.value.copy(
                    pendingFilters = _state.value.pendingFilters.copy(
                        searchText = intent.typedText
                    )
                )
            }

            is StartScreenIntent.DraftGenreSelected -> {
                _state.value = _state.value.copy(
                    pendingFilters = _state.value.pendingFilters.copy(
                        pickedGenreId = intent.pickedGenreId
                    )
                )
            }

            is StartScreenIntent.DraftMinYearChanged -> {
                _state.value = _state.value.copy(
                    pendingFilters = _state.value.pendingFilters.copy(
                        yearFrom = intent.yearFrom
                    )
                )
            }

            is StartScreenIntent.DraftMaxYearChanged -> {
                _state.value = _state.value.copy(
                    pendingFilters = _state.value.pendingFilters.copy(
                        yearTo = intent.yearTo
                    )
                )
            }

            is StartScreenIntent.DraftMinRatingChanged -> {
                _state.value = _state.value.copy(
                    pendingFilters = _state.value.pendingFilters.copy(
                        ratingFrom = intent.ratingFrom
                    )
                )
            }

            StartScreenIntent.DraftClearAll -> {
                _state.value = _state.value.copy(
                    pendingFilters = BrowseFilters()
                )
            }

            StartScreenIntent.ApplyDraftFilters -> {
                _state.value = _state.value.copy(
                    activeFilters = _state.value.pendingFilters,
                    activePage = 1
                )

                isFilterDraftPrepared = false

                fetchMovies(pageNumber = 1)
            }

            StartScreenIntent.LoadNextPage -> {
                if (_state.value.activePage < _state.value.pagesCount) {
                    fetchMovies(pageNumber = _state.value.activePage + 1)
                }
            }

            StartScreenIntent.LoadPreviousPage -> {
                if (_state.value.activePage > 1) {
                    fetchMovies(pageNumber = _state.value.activePage - 1)
                }
            }
        }
    }

    private fun fetchMovies(pageNumber: Int = _state.value.activePage) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                screenState = UiState.Loading
            )

            try {
                val currentState = _state.value

                val resolvedImageHost = withContext(Dispatchers.IO) {
                    if (currentState.imageHost.isBlank()) {
                        getImageBaseService()
                    } else {
                        currentState.imageHost
                    }
                }

                val availableGenres = withContext(Dispatchers.IO) {
                    if (currentState.genreItems.isEmpty()) {
                        getGenresService()
                    } else {
                        currentState.genreItems
                    }
                }

                val resolvedSortField = mapSortToApi(currentState.activeSort)

                val resolvedSortDirection = if (currentState.activeSort == SortParametar.TITLE) {
                    "asc"
                } else {
                    "desc"
                }

                val syncedPage = withContext(Dispatchers.IO) {
                    getMoviesService(
                        pageNumber = pageNumber,
                        itemsPerPage = 30,
                        selectedFilters = currentState.activeFilters,
                        sortField = resolvedSortField,
                        sortDirection = resolvedSortDirection
                    )
                }

                updateStateFromMoviesPage(
                    moviesPage = syncedPage,
                    imageHost = resolvedImageHost,
                    genres = availableGenres
                )

                observeCurrentPageMoviesFromRoom(
                    movieIds = syncedPage.movieItems.map { movie ->
                        movie.movieId
                    },
                    imageHost = resolvedImageHost,
                    genres = availableGenres,
                    fallbackPageMetadata = syncedPage
                )
            } catch (exception: Exception) {
                _state.value = _state.value.copy(
                    screenState = UiState.Error(
                        description = exception.message ?: "Unknown error"
                    )
                )
            }
        }
    }

    private fun observeCurrentPageMoviesFromRoom(
        movieIds: List<String>,
        imageHost: String,
        genres: List<Genre>,
        fallbackPageMetadata: MoviesPage
    ) {
        moviesObserverJob?.cancel()

        moviesObserverJob = viewModelScope.launch {
            getMoviesService.observeMoviesByIds(
                movieIds = movieIds,
                fallbackPage = fallbackPageMetadata
            ).collectLatest { roomPage ->

                updateStateFromMoviesPage(
                    moviesPage = roomPage,
                    imageHost = imageHost,
                    genres = genres
                )
            }
        }
    }

    private fun updateStateFromMoviesPage(
        moviesPage: MoviesPage,
        imageHost: String,
        genres: List<Genre>
    ) {
        if (moviesPage.movieItems.isEmpty()) {
            _state.value = _state.value.copy(
                sourceMovies = emptyList(),
                visibleMovies = emptyList(),
                itemsCount = moviesPage.itemsCount,
                pagesCount = moviesPage.pagesCount,
                activePage = moviesPage.activePage,
                limitPerPage = moviesPage.limitPerPage,
                imageHost = imageHost,
                genreItems = genres,
                genreState = if (genres.isEmpty()) {
                    UiState.Empty
                } else {
                    UiState.Success(genres)
                },
                screenState = UiState.Empty
            )
            return
        }

        _state.value = _state.value.copy(
            sourceMovies = moviesPage.movieItems,
            visibleMovies = moviesPage.movieItems,
            itemsCount = moviesPage.itemsCount,
            pagesCount = moviesPage.pagesCount,
            activePage = moviesPage.activePage,
            limitPerPage = moviesPage.limitPerPage,
            imageHost = imageHost,
            genreItems = genres,
            genreState = if (genres.isEmpty()) {
                UiState.Empty
            } else {
                UiState.Success(genres)
            },
            screenState = UiState.Success(moviesPage.movieItems)
        )
    }

    private fun fetchGenresIfNeeded() {
        if (_state.value.genreItems.isNotEmpty()) return
        if (_state.value.genreState is UiState.Loading) return

        viewModelScope.launch {
            _state.value = _state.value.copy(
                genreState = UiState.Loading
            )

            try {
                val availableGenres = withContext(Dispatchers.IO) {
                    getGenresService()
                }

                _state.value = _state.value.copy(
                    genreItems = availableGenres,
                    genreState = if (availableGenres.isEmpty()) {
                        UiState.Empty
                    } else {
                        UiState.Success(availableGenres)
                    }
                )
            } catch (exception: Exception) {
                _state.value = _state.value.copy(
                    genreState = UiState.Error(
                        description = exception.message ?: "Failed to load genres"
                    )
                )
            }
        }
    }

    private fun updateSort(selectedSort: SortParametar) {
        _state.value = _state.value.copy(
            activeSort = selectedSort,
            activePage = 1
        )

        fetchMovies(pageNumber = 1)
    }

    private fun submitFilters(selectedFilters: BrowseFilters) {
        _state.value = _state.value.copy(
            activeFilters = selectedFilters,
            activePage = 1
        )

        fetchMovies(pageNumber = 1)
    }

    private fun sendOpenMovieEffect(targetMovieId: String) {
        viewModelScope.launch {
            _effect.send(
                StartScreenEffect.OpenMovieDetails(targetMovieId)
            )
        }
    }

    private fun sendOpenFiltersEffect() {
        viewModelScope.launch {
            _effect.send(
                StartScreenEffect.OpenFilters
            )
        }
    }

    private fun mapSortToApi(selectedSort: SortParametar): String {
        return when (selectedSort) {
            SortParametar.RATING -> "imdb_rating"
            SortParametar.YEAR -> "year"
            SortParametar.TITLE -> "title"
            SortParametar.POPULARITY -> "popularity"
        }
    }
}