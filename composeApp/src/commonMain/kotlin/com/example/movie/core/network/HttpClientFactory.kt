package com.example.movie.core.network

import io.ktor.client.HttpClient

expect object HttpClientFactory {

    fun create(): HttpClient
}