package io.github.devngho.kisopenapi.requests

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.jvm.JvmStatic
import kotlin.time.TimeSource

/**
 * 유량 제한을 위한 클래스입니다.
 */
class RateLimiter(ratePerSecond: Int) {
    companion object {

        @JvmStatic
        fun defaultRate(isDemo: Boolean = false) = RateLimiter(if (isDemo) 5 else 20)
    }

    val minDelay = 1000 / ratePerSecond

    /**
     * 마지막 요청 시간
     */
    private var lastRequestTime: TimeSource.Monotonic.ValueTimeMark? = null
    private val lastRequestTimeMutex = Mutex()
    private fun nano() = TimeSource.Monotonic.markNow()

    /**
     * RatedLimiter의 rate 제한에 따라 block을 실행합니다.
     * 마지막 요청 시간과의 최소 시간 간격을 유지합니다.
     * @param block 실행할 코드 블럭
     */
    suspend fun <T> rated(block: suspend () -> T): T {
        lastRequestTimeMutex.withLock {
            if (lastRequestTime == null) {
                lastRequestTime = nano()
                return block()
            }

            val diff = lastRequestTime!!.elapsedNow().inWholeMilliseconds
            if (diff < minDelay) delay(minDelay - diff)

            lastRequestTime = nano()
        }

        return block()
    }
}