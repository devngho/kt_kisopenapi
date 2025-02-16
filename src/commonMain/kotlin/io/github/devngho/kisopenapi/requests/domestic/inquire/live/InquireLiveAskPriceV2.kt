package io.github.devngho.kisopenapi.requests.domestic.inquire.live

import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import com.ionspin.kotlin.bignum.integer.toBigInteger
import io.github.devngho.kisopenapi.KISApiClient
import io.github.devngho.kisopenapi.requests.LiveData
import io.github.devngho.kisopenapi.requests.LiveRequest
import io.github.devngho.kisopenapi.requests.Response
import io.github.devngho.kisopenapi.requests.data.CorporationRequest
import io.github.devngho.kisopenapi.requests.domestic.inquire.live.InquireLiveAskPrice.InquireLiveAskPriceResponse
import io.github.devngho.kisopenapi.requests.response.LiveResponse
import io.github.devngho.kisopenapi.requests.util.*
import io.github.devngho.kisopenapi.requests.util.HHMMSSSerializer.HHMMSS
import kotlinx.coroutines.Job

/**
 * 국내 주식의 실시간 호가를 가져옵니다.
 */
@Suppress("SpellCheckingInspection")
@OptIn(InternalApi::class)
class InquireLiveAskPriceV2(override val client: KISApiClient) :
    LiveRequest<InquireLiveAskPriceV2.InquireLiveAskPriceData, InquireLiveAskPriceResponse> {
    private val tradeId = "H0UNASP0"

    data class InquireLiveAskPriceData(override var corp: CorporationRequest? = null) : LiveData {
        override fun tradeKey(client: KISApiClient): String {
            return client.htsId!!
        }
    }

    private var job: Job? = null
    private var subscribed: WebSocketSubscribed? = null
    private var v1Polyfill: InquireLiveAskPrice? = null

    @Suppress("UNCHECKED_CAST")
    override suspend fun register(
        data: InquireLiveAskPriceData,
        wait: Boolean,
        force: Boolean,
        init: (suspend (Result<LiveResponse>) -> Unit)?,
        block: suspend (InquireLiveAskPriceResponse) -> Unit
    ) {
        if (client.options.useV1PolyfillForV2 && !isATSAvailable()) {
            v1Polyfill = InquireLiveAskPrice(client).also {
                it.register(
                    InquireLiveAskPrice.InquireLiveAskPriceData(data.corp),
                    wait,
                    force,
                    init,
                    block
                )
            }
            return
        }

        subscribed = WebSocketSubscribed(
            this@InquireLiveAskPriceV2, data, init,
            block as suspend (Response) -> Unit
        )

        requestStart(
            data, subscribed!!, tradeId, data.tradeKey(client),
            wait = wait,
            updateJob = { job = it },
            init = init ?: {},
            block = block,
            force = force,
            bodySize = 59
        ) {
            InquireLiveAskPriceResponse(
                it[0],
                it[1].HHMMSS,
                HourCode.fromCode(it[2]),
                (0..9).map { i ->
                    val priceIdx = 3 + i
                    val countIdx = 23 + i
                    InquireLiveAskPrice.AskPrice(
                        it[priceIdx].toBigInteger(),
                        it[countIdx].toBigInteger()
                    )
                },
                (0..9).map { i ->
                    val priceIdx = 13 + i
                    val countIdx = 33 + i
                    InquireLiveAskPrice.AskPrice(
                        it[priceIdx].toBigInteger(),
                        it[countIdx].toBigInteger()
                    )
                },
                it[43].toBigInteger(),
                it[44].toBigInteger(),
                it[45].toBigInteger(),
                it[46].toBigInteger(),
                it[47].toBigInteger(),
                it[48].toBigInteger(),
                it[49].toBigInteger(),
                it[50].toBigInteger(),
                SignPrice.fromCode(it[51].toInt()),
                it[52].toBigDecimal(),
                it[53].toBigInteger(),
                it[54].toBigInteger(),
                it[55].toBigInteger(),
                it[56].toBigInteger(),
                it[57].toBigInteger(),
//                it[58].toInt(),  // 삭제된 코드입니다.
            )
        }
    }

    override suspend fun unregister(data: InquireLiveAskPriceData, wait: Boolean) {
        if (v1Polyfill != null) {
            v1Polyfill!!.unregister(InquireLiveAskPrice.InquireLiveAskPriceData(data.corp), wait)
            return
        }

        requestEnd(data, subscribed!!, tradeId, data.tradeKey(client), wait, job)
    }
}