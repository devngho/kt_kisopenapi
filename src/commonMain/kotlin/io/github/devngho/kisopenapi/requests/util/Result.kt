package io.github.devngho.kisopenapi.requests.util

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * API 요청 결과를 나타냅니다. [value] 또는 [error]를 통해 결과를 확인할 수 있습니다.
 *
 * @param T API 요청 결과의 타입
 * @property value API 요청 결과
 * @property error API 요청 중 발생한 에러
 */
@Serializable
class Result<out T>(val value: T?, @Transient val error: RequestException? = null) {
    override fun toString(): String {
        return if (isOk) "Ok($value)"
        else "Error($error)"
    }

    val isOk: Boolean = error == null && value != null

    fun getOrThrow(): T =
        if (isOk) value!!
        else throw error!!

    fun getOrNull(): T? =
        if (isOk) value!!
        else null
}