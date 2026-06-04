package com.example.movie.cinema.data.mapper

import com.example.movie.cinema.data.dto.GenreDto
import com.example.movie.cinema.data.dto.MovieDetailsDto
import com.example.movie.cinema.data.dto.MovieListItemDto
import com.example.movie.cinema.model.Movie
import com.example.movie.core.database.entity.MovieEntity

fun MovieListItemDto.toEntity(
    existingEntity: MovieEntity? = null
): MovieEntity {
    return MovieEntity(
        id = imdbId,
        title = movieTitle,
        overview = existingEntity?.overview.orEmpty(),
        year = releaseYear,
        runtime = existingEntity?.runtime,
        imdbRating = imdbScore ?: 0.0,
        imdbVotes = imdbVotesCount ?: 0,
        popularity = popularityScore ?: 0.0,
        posterUrl = posterImagePath,
        backdropUrl = existingEntity?.backdropUrl,
        genresText = genreItems.toGenreIdsText(),
        castText = existingEntity?.castText.orEmpty(),
        languageCode = existingEntity?.languageCode,
        productionBudget = existingEntity?.productionBudget,
        boxOfficeRevenue = existingEntity?.boxOfficeRevenue,
        tmdbScore = existingEntity?.tmdbScore,
        updatedAtMillis = System.currentTimeMillis()
    )
}

fun MovieDetailsDto.toEntity(
    castText: String,
    existingEntity: MovieEntity? = null
): MovieEntity {
    return MovieEntity(
        id = imdbId,
        title = movieTitle,
        overview = movieOverview.orEmpty(),
        year = releaseYear,
        runtime = runtimeMinutes,
        imdbRating = imdbScore ?: 0.0,
        imdbVotes = imdbVotesCount ?: 0,
        popularity = popularityScore ?: 0.0,
        posterUrl = posterImagePath,
        backdropUrl = backdropImagePath,
        genresText = genreItems.toGenreIdsText(),
        castText = castText.ifBlank {
            existingEntity?.castText.orEmpty()
        },
        languageCode = originalLanguageCode,
        productionBudget = budgetAmount,
        boxOfficeRevenue = revenueAmount,
        tmdbScore = tmdbScore,
        updatedAtMillis = System.currentTimeMillis()
    )
}

fun MovieEntity.toDomain(): Movie {
    return Movie(
        movieId = id,
        movieTitle = title,
        movieOverview = overview,
        posterUrl = posterUrl,
        backdropUrl = backdropUrl,
        releaseDateYear = year,
        imdbScore = imdbRating,
        imdbVotesCount = imdbVotes,
        trendingScore = popularity,
        categoryIds = genresText.toGenreIdsList(),
        durationMinutes = runtime,
        languageCode = languageCode,
        productionBudget = productionBudget,
        boxOfficeRevenue = boxOfficeRevenue,
        tmdbScore = tmdbScore
    )
}

fun List<MovieEntity>.toDomainMovies(): List<Movie> {
    return map { movieEntity ->
        movieEntity.toDomain()
    }
}

private fun List<GenreDto>.toGenreIdsText(): String {
    if (isEmpty()) {
        return ""
    }

    return joinToString(
        separator = "",
        prefix = "|",
        postfix = "|"
    ) { genreDto ->
        genreDto.genreId.toString()
    }
}

private fun String.toGenreIdsList(): List<Int> {
    if (isBlank()) {
        return emptyList()
    }

    return split("|")
        .mapNotNull { value ->
            value.toIntOrNull()
        }
}