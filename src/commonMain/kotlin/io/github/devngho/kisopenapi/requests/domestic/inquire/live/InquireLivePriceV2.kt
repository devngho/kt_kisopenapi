package io.github.devngho.kisopenapi.requests.domestic.inquire.live

import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import com.ionspin.kotlin.bignum.integer.toBigInteger
import io.github.devngho.kisopenapi.KISApiClient
import io.github.devngho.kisopenapi.requests.LiveData
import io.github.devngho.kisopenapi.requests.LiveRequest
import io.github.devngho.kisopenapi.requests.Response
import io.github.devngho.kisopenapi.requests.data.CorporationRequest
import io.github.devngho.kisopenapi.requests.domestic.inquire.live.InquireLivePrice.InquireLivePriceResponse
import io.github.devngho.kisopenapi.requests.response.LiveResponse
import io.github.devngho.kisopenapi.requests.response.stock.Ticker
import io.github.devngho.kisopenapi.requests.util.*
import io.github.devngho.kisopenapi.requests.util.HHMMSSSerializer.HHMMSS
import io.github.devngho.kisopenapi.requests.util.YNSerializer.YN
import kotlinx.coroutines.Job

/**
 * 국내 주식의 실시간 체결가를 가져옵니다.
 */
@OptIn(InternalApi::class)
class InquireLivePriceV2(override val client: KISApiClient) :
    LiveRequest<InquireLivePriceV2.InquireLivePriceData, InquireLivePriceResponse> {
    data class InquireLivePriceData(override val ticker: String, override var corp: CorporationRequest? = null) :
        LiveData, Ticker {
        override fun tradeKey(client: KISApiClient): String = ticker
    }

    private var job: Job? = null
    private var subscribed: WebSocketSubscribed? = null
    private var v1Polyfill: InquireLivePrice? = null

    @Suppress("UNCHECKED_CAST")
    override suspend fun register(
        data: InquireLivePriceData,
        wait: Boolean,
        force: Boolean,
        init: (suspend (Result<LiveResponse>) -> Unit)?,
        block: suspend (InquireLivePriceResponse) -> Unit
    ) {
        if (client.options.useV1PolyfillForV2 && !isATSAvailable()) {
            v1Polyfill = InquireLivePrice(client).also {
                v1Polyfill!!.register(InquireLivePrice.InquireLivePriceData(data.ticker), wait, force, init, block)
            }

            return
        }

        subscribed = WebSocketSubscribed(
            this@InquireLivePriceV2, data, init,
            block as suspend (Response) -> Unit
        )

        requestStart(
            data, subscribed!!, "H0UNCNT0", data.tradeKey(client),
            wait = wait,
            updateJob = { job = it },
            init = init ?: {},
            block = block,
            force = force,
            bodySize = 46
        ) {
            InquireLivePriceResponse(
                it[0],
                it[1].HHMMSS,
                it[2].toBigInteger(),
                SignPrice.entries.find { f -> f.value.toString() == it[3] },
                it[4].toBigInteger(),
                it[5].toBigDecimal(),
                it[6].toBigDecimal(),
                it[7].toBigInteger(),
                it[8].toBigInteger(),
                it[9].toBigInteger(),
                it[10].toBigInteger(),
                it[11].toBigInteger(),
                it[12].toBigInteger(),
                it[13].toBigInteger(),
                it[14].toBigInteger(),
                it[15].toBigInteger(),
                it[16].toBigInteger(),
                it[17].toBigInteger(),
                it[18].toBigDecimal(),
                it[19].toBigInteger(),
                it[20].toBigInteger(),
                it[21],
                it[22].toBigDecimal(),
                it[23].toBigDecimal(),
                it[24],
                SignPrice.entries.find { f -> f.value.toString() == it[25] },
                it[26].toBigInteger(),
                it[27],
                SignPrice.entries.find { f -> f.value.toString() == it[28] },
                it[29].toBigInteger(),
                it[30],
                SignPrice.entries.find { f -> f.value.toString() == it[31] },
                it[32].toBigInteger(),
                it[33],
                it[34],
                it[35].YN,
                it[36].toBigInteger(),
                it[37].toBigInteger(),
                it[38].toBigInteger(),
                it[39].toBigInteger(),
                it[40].toBigDecimal(),
                it[41].toBigInteger(),
                it[42].toBigDecimal(),
                HourCode.entries.find { f -> f.num == it[43] },
                it[44],
                it[45].toBigInteger()
            )
        }
    }

    override suspend fun unregister(data: InquireLivePriceData, wait: Boolean) {
        if (v1Polyfill != null) {
            v1Polyfill!!.unregister(InquireLivePrice.InquireLivePriceData(data.ticker), wait)
            return
        }

        requestEnd(
            data,
            subscribed!!,
            @Suppress("SpellCheckingInspection") "H0UNCNT0",
            data.tradeKey(client),
            wait,
            job
        )
    }
}