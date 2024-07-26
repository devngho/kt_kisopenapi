package io.github.devngho.kisopenapi.requests.util

import io.github.devngho.kisopenapi.requests.Request

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
) : Exception("${type?.code ?: "(Unknown code)"} $message", cause) {
    companion object {
        internal fun Request<*>.throwIfClientIsDemo() {
            if (client.isDemo) throw RequestException(
                "모의투자에서는 사용할 수 없는 API ${this::class.simpleName}를 호출했습니다.",
                RequestCode.DemoUnavailable
            )
        }
    }
}