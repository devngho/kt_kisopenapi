package io.github.devngho.kisopenapi.requests

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
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
    @OptIn(ExperimentalContracts::class)
    suspend fun <T> rated(block: suspend () -> T): T {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }

        lastRequestTimeMutex.withLock {
            // 만약 이전 요청 시간이 없다면 바로 실행합니다.
            if (lastRequestTime == null) {
                lastRequestTime = nano()
                return block()
            }

            // 최소 딜레이를 유지하기 위해 대기합니다.
            val diff = lastRequestTime!!.elapsedNow().inWholeMilliseconds
            if (diff < minDelay) delay(minDelay - diff)

            lastRequestTime = nano()
        }

        return block()
    }
}