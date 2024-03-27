package io.github.devngho.kisopenapi.requests.ratelimit

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * TimeSource를 통해 최소 딜레이를 유지해 유량을 제한합니다.
 *
 * @param ratePerSecond 초당 요청 가능한 횟수
 * @param timeSource 시간 소스. 기본값은 [TimeSource.Monotonic]입니다.
 */
class ClockRateLimiter(override val ratePerSecond: Int, val timeSource: TimeSource = TimeSource.Monotonic) :
    RateLimiter {
    companion object {
        /**
         * 기본 유량 제한을 가진 [ClockRateLimiter]를 생성합니다.
         *
         * @param isDemo 모의 투자 여부입니다. 기본값은 false입니다.
         */
        fun byDefaultRate(isDemo: Boolean = false) = ClockRateLimiter(RateLimiter.getDefaultRate(isDemo))
    }

    private val minDelay = 1000 / ratePerSecond

    /**
     * 마지막 요청 시간
     */
    private var lastRequestTime: TimeMark? = null
    private val lastRequestTimeMutex = Mutex()
    private fun mark() = timeSource.markNow()

    /**
     * RatedLimiter의 rate 제한에 따라 block을 실행합니다.
     * 마지막 요청 시간과의 최소 시간 간격을 유지합니다.
     *
     * @param block 실행할 코드 블럭
     */
    override suspend fun <T> rated(block: suspend () -> T): T {
        lastRequestTimeMutex.withLock {
            // 만약 이전 요청 시간이 없다면 바로 실행합니다.
            if (lastRequestTime == null) {
                lastRequestTime = mark()
                return block()
            }

            // 최소 딜레이를 유지하기 위해 대기합니다.
            val diff = lastRequestTime!!.elapsedNow().inWholeMilliseconds
            if (diff < minDelay) delay(minDelay - diff)

            lastRequestTime = mark()
        }

        return block()
    }
}