package io.github.devngho.kisopenapi.requests.util

/**
 * 닫을 수 있는 객체를 나타냅니다. 웹소켓 연결 등에 사용됩니다.
 */
interface Closeable {
    suspend fun close()
}