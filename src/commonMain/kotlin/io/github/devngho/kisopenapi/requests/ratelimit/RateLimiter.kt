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
        /**
         * 유량 제한 값을 반환합니다.
         * 2024.3.29 기준 실전 투자 초당 20회, 모의 투자 초당 2회입니다.
         *
         * [API 호출 유량 안내](https://apiportal.koreainvestment.com/community/10000000-0000-0011-0000-000000000002)
         *
         * @param isDemo 모의 투자 여부입니다. 기본값은 false입니다.
         */
        @JvmStatic
        fun getDefaultRate(isDemo: Boolean = false) = if (isDemo) 2 else 20
    }

    /**
     * RatedLimiter의 rate 제한에 따라 block을 실행합니다.
     *
     * @param block 실행할 코드 블럭. 1회 실행을 보장합니다.
     */
    suspend fun <T> rated(block: suspend () -> T): T
}