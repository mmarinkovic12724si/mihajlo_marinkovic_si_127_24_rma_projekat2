package com.example.movie.core.quiz.data.repository

import com.example.movie.cinema.data.dto.MovieListItemDto
import com.example.movie.cinema.data.mapper.toCastDomainList
import com.example.movie.cinema.data.mapper.toEntity
import com.example.movie.core.database.dao.MovieDao
import com.example.movie.core.database.dao.QuizStatsDao
import com.example.movie.core.database.entity.MovieEntity
import com.example.movie.core.database.entity.QuizStatsEntity
import com.example.movie.core.network.MoviesApi
import com.example.movie.core.quiz.domain.model.QuizQuestion
import com.example.movie.core.quiz.domain.model.QuizQuestionType
import com.example.movie.core.quiz.domain.model.QuizResult
import com.example.movie.core.quiz.domain.repository.QuizRepository
import kotlin.math.max

class QuizRepositoryImpl(
    private val moviesApi: MoviesApi,
    private val movieDao: MovieDao,
    private val quizStatsDao: QuizStatsDao
) : QuizRepository {

    companion object {
        private const val MOVIE_KNOWLEDGE_CATEGORY_ID = 1
    }

    override suspend fun generateQuizSession(): Result<List<QuizQuestion>> {
        return try {
            bootstrapQuizPoolIfNeeded()

            val pool = movieDao.getQuizPool(limit = 100)

            if (pool.size < 10) {
                return Result.failure(
                    Exception("Browse the catalog first to populate your quiz pool.")
                )
            }

            val moviesWithImage = pool.filter { movie ->
                !movie.posterUrl.isNullOrBlank() || !movie.backdropUrl.isNullOrBlank()
            }

            if (moviesWithImage.size < 10) {
                return Result.failure(
                    Exception("Browse the catalog first to populate your quiz pool.")
                )
            }

            val preparedPool = enrichSomeMoviesWithCast(
                movies = moviesWithImage
            )

            val questions = buildQuestions(
                pool = preparedPool
            )

            if (questions.size < 10) {
                Result.failure(
                    Exception("Could not generate enough quiz questions.")
                )
            } else {
                Result.success(
                    questions.take(10).mapIndexed { index, question ->
                        question.copy(id = index + 1)
                    }
                )
            }
        } catch (exception: Exception) {
            Result.failure(
                Exception("Could not start quiz. Try browsing the catalog first.")
            )
        }
    }

    override suspend fun saveQuizResult(
        result: QuizResult
    ) {
        val currentStats = quizStatsDao.getQuizStats()

        val newBestScore = max(
            currentStats?.bestScore ?: 0.0,
            result.score
        )

        val newGamesPlayed = (currentStats?.gamesPlayed ?: 0) + 1

        quizStatsDao.upsertQuizStats(
            QuizStatsEntity(
                id = 1,
                bestScore = newBestScore,
                gamesPlayed = newGamesPlayed
            )
        )
    }

    private suspend fun bootstrapQuizPoolIfNeeded() {
        val currentPool = movieDao.getQuizPool(limit = 100)

        if (currentPool.size >= 30) {
            return
        }

        val firstPage = moviesApi.getMovies(
            pageNumber = 1,
            itemsPerPage = 50,
            searchText = null,
            pickedGenreId = null,
            yearFrom = null,
            yearTo = null,
            ratingFrom = null,
            sortField = "imdb_rating",
            sortDirection = "desc"
        )

        val secondPage = moviesApi.getMovies(
            pageNumber = 2,
            itemsPerPage = 50,
            searchText = null,
            pickedGenreId = null,
            yearFrom = null,
            yearTo = null,
            ratingFrom = null,
            sortField = "imdb_rating",
            sortDirection = "desc"
        )

        val remoteMovies = firstPage.results + secondPage.results

        val entities = remoteMovies.map { movieDto: MovieListItemDto ->
            val existingEntity = movieDao.getMovieById(movieDto.imdbId)

            movieDto.toEntity(
                existingEntity = existingEntity
            )
        }

        movieDao.upsertMovies(entities)
    }

    private suspend fun enrichSomeMoviesWithCast(
        movies: List<MovieEntity>
    ): List<MovieEntity> {
        val moviesNeedingCast = movies
            .filter { movie ->
                movie.castText.isBlank()
            }
            .shuffled()
            .take(20)

        moviesNeedingCast.forEach { movie ->
            try {
                val detailsResponse = moviesApi.getMovieDetails(movie.id)

                val castResponse = moviesApi.getMovieCast(
                    targetMovieId = movie.id,
                    itemsPerPage = 10
                )

                val castText = castResponse.results
                    .toCastDomainList()
                    .take(10)
                    .joinToString(separator = "|") { castMember ->
                        castMember.fullName
                    }

                val updatedEntity = detailsResponse.toEntity(
                    castText = castText,
                    existingEntity = movie
                )

                movieDao.upsertMovie(updatedEntity)
            } catch (exception: Exception) {
                // Ako jedan film nema details/cast ili pukne mreža, samo nastavljamo dalje.
            }
        }

        return movieDao.getQuizPool(limit = 100)
    }

    private fun buildQuestions(
        pool: List<MovieEntity>
    ): List<QuizQuestion> {
        val moviesWithImage = pool
            .filter { movie ->
                !movie.posterUrl.isNullOrBlank() || !movie.backdropUrl.isNullOrBlank()
            }
            .shuffled()
            .distinctBy { movie ->
                movie.id
            }

        val usedImages = mutableSetOf<String>()
        val questions = mutableListOf<QuizQuestion>()

        val preferredTypes = listOf(
            QuizQuestionType.GUESS_MOVIE,
            QuizQuestionType.GUESS_YEAR,
            QuizQuestionType.GUESS_LEAD_ACTOR,
            QuizQuestionType.GUESS_MOVIE,
            QuizQuestionType.GUESS_YEAR,
            QuizQuestionType.GUESS_LEAD_ACTOR,
            QuizQuestionType.GUESS_MOVIE,
            QuizQuestionType.GUESS_YEAR,
            QuizQuestionType.GUESS_LEAD_ACTOR,
            QuizQuestionType.GUESS_MOVIE
        ).shuffled()

        moviesWithImage.forEach { movie ->
            if (questions.size >= 10) {
                return@forEach
            }

            val desiredType = preferredTypes.getOrElse(questions.size) {
                QuizQuestionType.GUESS_MOVIE
            }

            val question = createQuestionByType(
                type = desiredType,
                questionId = questions.size + 1,
                movie = movie,
                pool = pool,
                usedImages = usedImages
            ) ?: createFallbackQuestion(
                questionId = questions.size + 1,
                movie = movie,
                pool = pool,
                usedImages = usedImages
            )

            if (question != null) {
                questions.add(question)
            }
        }

        return questions
    }

    private fun createQuestionByType(
        type: QuizQuestionType,
        questionId: Int,
        movie: MovieEntity,
        pool: List<MovieEntity>,
        usedImages: MutableSet<String>
    ): QuizQuestion? {
        return when (type) {
            QuizQuestionType.GUESS_MOVIE -> {
                createGuessMovieQuestion(
                    questionId = questionId,
                    movie = movie,
                    pool = pool,
                    usedImages = usedImages
                )
            }

            QuizQuestionType.GUESS_YEAR -> {
                createGuessYearQuestion(
                    questionId = questionId,
                    movie = movie,
                    usedImages = usedImages
                )
            }

            QuizQuestionType.GUESS_LEAD_ACTOR -> {
                createGuessLeadActorQuestion(
                    questionId = questionId,
                    movie = movie,
                    pool = pool,
                    usedImages = usedImages
                )
            }
        }
    }

    private fun createFallbackQuestion(
        questionId: Int,
        movie: MovieEntity,
        pool: List<MovieEntity>,
        usedImages: MutableSet<String>
    ): QuizQuestion? {
        return createGuessMovieQuestion(
            questionId = questionId,
            movie = movie,
            pool = pool,
            usedImages = usedImages
        ) ?: createGuessYearQuestion(
            questionId = questionId,
            movie = movie,
            usedImages = usedImages
        )
    }

    private fun createGuessMovieQuestion(
        questionId: Int,
        movie: MovieEntity,
        pool: List<MovieEntity>,
        usedImages: MutableSet<String>
    ): QuizQuestion? {
        val imagePath = pickAnyImage(movie, usedImages) ?: return null

        val wrongAnswers = pool
            .filter { otherMovie ->
                otherMovie.id != movie.id
            }
            .map { otherMovie ->
                otherMovie.title
            }
            .distinct()
            .shuffled()
            .take(3)

        if (wrongAnswers.size < 3) {
            return null
        }

        val options = (wrongAnswers + movie.title).shuffled()

        return QuizQuestion(
            id = questionId,
            type = QuizQuestionType.GUESS_MOVIE,
            title = "Guess the Movie",
            subtitle = "Which movie is shown in the image?",
            movieId = movie.id,
            movieTitle = movie.title,
            imagePath = imagePath,
            options = options,
            correctAnswer = movie.title,
            categoryId = MOVIE_KNOWLEDGE_CATEGORY_ID
        )
    }

    private fun createGuessYearQuestion(
        questionId: Int,
        movie: MovieEntity,
        usedImages: MutableSet<String>
    ): QuizQuestion? {
        val year = movie.year ?: return null

        // Spec traži poster + naslov filma za Guess the Movie Year.
        val imagePath = pickPosterImage(movie, usedImages) ?: return null

        val offsets = listOf(
            -10, -9, -8, -7, -6, -5, -4, -3, -2, -1,
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10
        ).shuffled()

        val wrongYears = offsets
            .map { offset ->
                year + offset
            }
            .filter { candidate ->
                candidate > 1880
            }
            .distinct()
            .take(3)

        if (wrongYears.size < 3) {
            return null
        }

        val options = (wrongYears.map { it.toString() } + year.toString()).shuffled()

        return QuizQuestion(
            id = questionId,
            type = QuizQuestionType.GUESS_YEAR,
            title = "Guess the Movie Year",
            subtitle = "When was ${movie.title} released?",
            movieId = movie.id,
            movieTitle = movie.title,
            imagePath = imagePath,
            options = options,
            correctAnswer = year.toString(),
            categoryId = MOVIE_KNOWLEDGE_CATEGORY_ID
        )
    }

    private fun createGuessLeadActorQuestion(
        questionId: Int,
        movie: MovieEntity,
        pool: List<MovieEntity>,
        usedImages: MutableSet<String>
    ): QuizQuestion? {
        val movieActors = movie.castText
            .split("|")
            .map { actor ->
                actor.trim()
            }
            .filter { actor ->
                actor.isNotBlank()
            }
            .distinct()

        if (movieActors.isEmpty()) {
            return null
        }

        val correctActor = movieActors
            .take(3)
            .random()

        val wrongActors = collectAllActors(pool)
            .filter { actor ->
                actor != correctActor && actor !in movieActors
            }
            .distinct()
            .shuffled()
            .take(3)

        if (wrongActors.size < 3) {
            return null
        }

        // Spec traži poster + naslov filma za Guess the Lead Actor.
        val imagePath = pickPosterImage(movie, usedImages) ?: return null

        val options = (wrongActors + correctActor).shuffled()

        return QuizQuestion(
            id = questionId,
            type = QuizQuestionType.GUESS_LEAD_ACTOR,
            title = "Guess the Lead Actor",
            subtitle = "Who appears in ${movie.title}?",
            movieId = movie.id,
            movieTitle = movie.title,
            imagePath = imagePath,
            options = options,
            correctAnswer = correctActor,
            categoryId = MOVIE_KNOWLEDGE_CATEGORY_ID
        )
    }

    private fun collectAllActors(
        pool: List<MovieEntity>
    ): List<String> {
        return pool.flatMap { movie ->
            movie.castText
                .split("|")
                .map { actor ->
                    actor.trim()
                }
                .filter { actor ->
                    actor.isNotBlank()
                }
        }.distinct()
    }

    private fun pickAnyImage(
        movie: MovieEntity,
        usedImages: MutableSet<String>
    ): String? {
        val freshCandidates = listOfNotNull(
            movie.backdropUrl,
            movie.posterUrl
        ).filter { imagePath ->
            imagePath.isNotBlank() && imagePath !in usedImages
        }

        val selectedFreshImage = freshCandidates.firstOrNull()

        if (selectedFreshImage != null) {
            usedImages.add(selectedFreshImage)
            return selectedFreshImage
        }

        val fallbackImage = listOfNotNull(
            movie.backdropUrl,
            movie.posterUrl
        ).filter { imagePath ->
            imagePath.isNotBlank()
        }.randomOrNull()

        if (fallbackImage != null) {
            usedImages.add(fallbackImage)
        }

        return fallbackImage
    }

    private fun pickPosterImage(
        movie: MovieEntity,
        usedImages: MutableSet<String>
    ): String? {
        val posterPath = movie.posterUrl
            ?.takeIf { imagePath ->
                imagePath.isNotBlank() && imagePath !in usedImages
            }

        if (posterPath != null) {
            usedImages.add(posterPath)
            return posterPath
        }

        val fallbackPosterPath = movie.posterUrl
            ?.takeIf { imagePath ->
                imagePath.isNotBlank()
            }

        if (fallbackPosterPath != null) {
            usedImages.add(fallbackPosterPath)
        }

        return fallbackPosterPath
    }
}