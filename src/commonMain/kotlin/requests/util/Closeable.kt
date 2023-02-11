package io.github.devngho.kisopenapi.requests.util

interface Closeable {
    suspend fun close()
}