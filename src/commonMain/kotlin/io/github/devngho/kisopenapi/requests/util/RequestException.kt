package io.github.devngho.kisopenapi.requests.util

/**
 * API 요청 중 발생한 에러를 나타냅니다.
 */
class RequestException(
    /**
     * 에러 메시지
     */
    message: String?,
    /**
    에러 코드
     */
    type: RequestCode?,
    /**
     * 원 에러
     */
    cause: Throwable? = null
) : Exception("${type?.code ?: "(Unknown code)"} $message", cause)