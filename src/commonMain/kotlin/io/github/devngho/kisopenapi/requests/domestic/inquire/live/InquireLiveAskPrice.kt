package io.github.devngho.kisopenapi.requests.domestic.inquire.live

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
import io.github.devngho.kisopenapi.requests.util.*
import io.github.devngho.kisopenapi.requests.util.HHMMSSSerializer.HHMMSS
import kotlinx.coroutines.Job
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class InquireLiveAskPrice(override val client: KISApiClient) :
    LiveRequest<InquireLiveAskPrice.InquireLiveAskPriceData, InquireLiveAskPrice.InquireLiveAskPriceResponse> {
    private val tradeId = "H0STASP0"

    @Serializable
    data class AskPrice(@Contextual val price: BigInteger, @Contextual val count: BigInteger)

    @Serializable
    data class InquireLiveAskPriceResponse(
        @SerialName("mksc_shrn_iscd") override val ticker: String? = null,
        /** 영업 시간 */
        @SerialName("bsok_hour") val bizTime: Time? = null,
        /** 시간 구분 코드 */
        @SerialName("hour_cls_code") val hourCode: HourCode? = null,
        /** 매도 호가, 10개가 있습니다. */
        val sellAskPrices: List<AskPrice>? = null,
        /** 매수 호가, 10개가 있습니다. */
        val buyAskPrices: List<AskPrice>? = null,
        /** 시간외 총 매도 호가 잔량 */
        @SerialName("ovtm_total_askp_rsqn") @Contextual val afterHourSellCount: BigInteger? = null,
        /** 시간외 총 매수 호가 잔량 */
        @SerialName("ovtm_total_bidp_rsqn") @Contextual val afterHourBuyCount: BigInteger? = null,
        /** 예상 체결가, 동시호가 등 특정 조건에서만 가져올 수 있습니다. */
        @SerialName("antc_cnpr") @Contextual val expectedPrice: BigInteger? = null,
        /** 예상 체결량, 동시호가 등 특정 조건에서만 가져올 수 있습니다. */
        @SerialName("antc_cnqn") @Contextual val expectedConfirmCount: BigInteger? = null,
        /** 예상 거래량 */
        @SerialName("antc_vol") @Contextual val expectedTradeVolume: BigInteger? = null,
        /** 예상 체결가 대비 */
        @SerialName("antc_cntg_vrss") @Contextual val expectedConfirmCountFromBefore: BigInteger? = null,
        /** 예상 체결가 대비 부호 */
        @SerialName("antc_cntg_vrss_sign") @Contextual val expectedTradeVolumeSignFromBefore: SignPrice? = null,
        /** 예상 체결가 전잃 대비울 */
        @SerialName("antc_cntg_prdy_ctrt") @Contextual val expectedConfirmCountRateFromYesterday: BigDecimal? = null,
        /** 누적 거래량
         * 이 값은 0으로 출력되며, 가져올 수 없습니다. 대신 [InquireLivePrice]를 사용하세요.
         *  */
        @SerialName("acml_vol") @Contextual val accumulatedTradeVolume: BigInteger? = null,
        /** 총 매도 호가 잔량 증감 */
        @SerialName("total_askp_rsqn_icdc") @Contextual val totalSellAskPriceCountChange: BigInteger? = null,
        /** 총 매수 호가 잔량 증감 */
        @SerialName("total_bidp_rsqn_icdc") @Contextual val totalBuyAskPriceCountChange: BigInteger? = null,
        /** 시간외 총 매도 호가 잔량 증감 */
        @SerialName("ovtm_total_askp_icdc") @Contextual val afterHourTotalSellAskPriceCountChange: BigInteger? = null,
        /** 시간외 총 매수 호가 잔량 증감 */
        @SerialName("ovtm_total_bidp_icdc") @Contextual val afterHourTotalBuyAskPriceCountChange: BigInteger? = null,
        /** 주식 매매 구분 코드 */
        @SerialName("stck_deal_cls_code") val stockTradeDivisionCode: Int? = null,
    ) : Response, Ticker {
        @SerialName("error_description")
        override val errorDescription: String? = null

        @SerialName("error_code")
        override val errorCode: String? = null
    }

    data class InquireLiveAskPriceData(override var corp: CorporationRequest? = null) : LiveData {
        override fun tradeKey(client: KISApiClient): String {
            return client.htsId!!
        }
    }

    private var job: Job? = null
    private var subscribed: KISApiClient.WebSocketSubscribed? = null

    @Suppress("UNCHECKED_CAST")
    override suspend fun register(
        data: InquireLiveAskPriceData,
        wait: Boolean,
        force: Boolean,
        init: (suspend (Result<LiveResponse>) -> Unit)?,
        block: suspend (InquireLiveAskPriceResponse) -> Unit
    ) {
        subscribed = KISApiClient.WebSocketSubscribed(
            this@InquireLiveAskPrice, data, init,
            block as (Response) -> Unit
        )

        requestStart(
            data, subscribed!!, tradeId, data.tradeKey(client), wait,
            updateJob = { job = it },
            init = init ?: {},
            block = block,
        ) {
            InquireLiveAskPriceResponse(
                it[0],
                it[1].HHMMSS,
                HourCode.fromCode(it[2]),
                (0..9).map { i ->
                    val priceIdx = 3 + i
                    val countIdx = 23 + i
                    AskPrice(
                        it[priceIdx].toBigInteger(),
                        it[countIdx].toBigInteger()
                    )
                },
                (0..9).map { i ->
                    val priceIdx = 13 + i
                    val countIdx = 33 + i
                    AskPrice(
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
                SignPrice.fromCode(it[49].toInt()),
                it[50].toBigDecimal(),
                it[51].toBigInteger(),
                it[52].toBigInteger(),
                it[53].toBigInteger(),
                it[54].toBigInteger(),
                it[55].toBigInteger(),
                it[56].toInt(),
            )
        }
    }

    override suspend fun unregister(data: InquireLiveAskPriceData, wait: Boolean) =
        requestEnd(data, subscribed, tradeId, data.tradeKey(client), wait, job)
}