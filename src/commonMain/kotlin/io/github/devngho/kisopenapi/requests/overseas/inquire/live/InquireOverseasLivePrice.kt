package io.github.devngho.kisopenapi.requests.overseas.inquire.live

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.toBigInteger
import io.github.devngho.kisopenapi.KISApiClient
import io.github.devngho.kisopenapi.requests.LiveData
import io.github.devngho.kisopenapi.requests.LiveRequest
import io.github.devngho.kisopenapi.requests.Response
import io.github.devngho.kisopenapi.requests.data.CorporationRequest
import io.github.devngho.kisopenapi.requests.response.LiveResponse
import io.github.devngho.kisopenapi.requests.response.stock.Ticker
import io.github.devngho.kisopenapi.requests.response.stock.price.overseas.StockOverseasPrice
import io.github.devngho.kisopenapi.requests.util.*
import io.github.devngho.kisopenapi.requests.util.HHMMSSSerializer.HHMMSS
import io.github.devngho.kisopenapi.requests.util.YYYYMMDDSerializer.YYYYMMDD
import kotlinx.coroutines.Job
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 *  해외 주식의 실시간 체결가를 가져옵니다.
 */
@OptIn(InternalApi::class)
class InquireOverseasLivePrice(override val client: KISApiClient) :
    LiveRequest<InquireOverseasLivePrice.InquireOverseasLivePriceData, InquireOverseasLivePrice.InquireOverseasLivePriceResponse> {
    @Serializable
    @Suppress("SpellCheckingInspection")
    data class InquireOverseasLivePriceResponse(
        @SerialName("RSYM") val liveLoadCode: String?,
        @SerialName("SYMB") override val ticker: String?,
        @SerialName("ZDIV") override val decimalPoint: Int?,
        @SerialName("TYMD") val localMarketDate: Date?,
        @SerialName("XYMD") val localDate: Date?,
        @SerialName("XHMS") val localTime: Time?,
        @SerialName("KYMD") val koreaDate: Date?,
        @SerialName("KHMS") val koreaTime: Time?,
        @SerialName("OPEN") @Contextual val marketPrice: BigDecimal?,
        @SerialName("HIGH") @Contextual val highPrice: BigDecimal?,
        @SerialName("LOW") @Contextual val lowPrice: BigDecimal?,
        @SerialName("LAST") @Contextual override val price: BigDecimal?,
        @SerialName("SIGN") @Contextual override val sign: SignPrice?,
        @SerialName("DIFF") @Contextual override val change: BigDecimal?,
        @SerialName("RATE") @Contextual override val rate: BigDecimal?,
        @SerialName("PBID") @Contextual val bidPrice: BigDecimal?,
        @SerialName("PASK") @Contextual val askPrice: BigDecimal?,
        @SerialName("VBID") @Contextual val bidCount: BigInteger?,
        @SerialName("VASK") @Contextual val askCount: BigInteger?,
        @SerialName("EVOL") @Contextual val confirmVolume: BigInteger?,
        @SerialName("TVOL") @Contextual override val tradeVolume: BigInteger?,
        @SerialName("TAMT") @Contextual override val tradePriceVolume: BigDecimal?,
        @SerialName("BIVL") @Contextual val buyConfirmVolume: BigInteger?,
        @SerialName("ASVL") @Contextual val sellConfirmVolume: BigInteger?,
        @SerialName("STRN") @Contextual val confirmStrength: BigDecimal?,
        @SerialName("MTYP") val marketStatus: MarketStatus?,
    ) : Response, StockOverseasPrice, Ticker {
        @SerialName("error_description")
        override val errorDescription: String? = null

        @SerialName("error_code")
        override val errorCode: String? = null
    }

    data class InquireOverseasLivePriceData(
        override val ticker: String,
        val market: OverseasMarket,
        override var corp: CorporationRequest? = null
    ) : LiveData, Ticker {
        override fun tradeKey(client: KISApiClient): String = "D${market.code}$ticker"
    }

    private var job: Job? = null
    private var subscribed: WebSocketSubscribed? = null

    @Suppress("UNCHECKED_CAST")
    override suspend fun register(
        data: InquireOverseasLivePriceData,
        wait: Boolean,
        force: Boolean,
        init: (suspend (Result<LiveResponse>) -> Unit)?,
        block: suspend (InquireOverseasLivePriceResponse) -> Unit
    ) {
        subscribed = WebSocketSubscribed(
            this@InquireOverseasLivePrice, data, init,
            block as suspend (Response) -> Unit
        )

        requestStart(
            data, subscribed!!, @Suppress("SpellCheckingInspection") "HDFSCNT0", data.tradeKey(client),
            wait = wait,
            updateJob = { job = it },
            init = init ?: {},
            block = block,
            force = force
        ) {
            InquireOverseasLivePriceResponse(
                it[0],
                it[1],
                it[2].toInt(),
                it[3].YYYYMMDD,
                it[4].YYYYMMDD,
                it[5].HHMMSS,
                it[6].YYYYMMDD,
                it[7].HHMMSS,
                it[8].toBigDecimal(),
                it[9].toBigDecimal(),
                it[10].toBigDecimal(),
                it[11].toBigDecimal(),
                SignPrice.entries.firstOrNull { f -> f.value.toString() == it[12] },
                // 변화량이 음수인 경우에도 changeFromYesterday 값이 양수로 반환됨
                // 편의성 위해 변동률 음수인 경우 changeFromYesterday 값도 음수로 변환함
                if (it[14].startsWith("-")) it[13].toBigDecimal() * -1 else it[13].toBigDecimal(),
                it[14].toBigDecimal(),
                it[15].toBigDecimal(),
                it[16].toBigDecimal(),
                it[17].toBigInteger(),
                it[18].toBigInteger(),
                it[19].toBigInteger(),
                it[20].toBigInteger(),
                it[21].toBigDecimal(),
                it[22].toBigInteger(),
                it[23].toBigInteger(),
                it[24].toBigDecimal(),
                MarketStatus.entries.firstOrNull { f -> f.code == it[24] }
            )
        }
    }

    override suspend fun unregister(data: InquireOverseasLivePriceData, wait: Boolean) =
        requestEnd(data, subscribed!!, "HDFSCNT0", data.tradeKey(client), wait, job)
}