package io.github.devngho.kisopenapi.requests.ratelimit

import kotlin.jvm.JvmStatic

/**
 * 유량 제한을 위해 사용할 기능을 제공하는 인터페이스입니다.
 *
 * @property ratePerSecond 초당 요청 가능한 횟수
 */
interface RateLimiter {
    val ratePerSecond: Int
    companion object {
        @JvmStatic
        fun defaultRate(isDemo: Boolean = false) = if (isDemo) 5 else 20
    }

    /**
     * RatedLimiter의 rate 제한에 따라 block을 실행합니다.
     *
     * @param block 실행할 코드 블럭. 1회 실행을 보장합니다.
     */
    suspend fun <T> rated(block: suspend () -> T): T
}