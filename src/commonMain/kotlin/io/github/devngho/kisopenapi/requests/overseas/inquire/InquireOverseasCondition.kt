package io.github.devngho.kisopenapi.requests.overseas.inquire

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.KISApiClient
import io.github.devngho.kisopenapi.requests.Data
import io.github.devngho.kisopenapi.requests.DataRequest
import io.github.devngho.kisopenapi.requests.Response
import io.github.devngho.kisopenapi.requests.data.CorporationRequest
import io.github.devngho.kisopenapi.requests.data.TradeContinuousResponse
import io.github.devngho.kisopenapi.requests.data.TradeIdMsg
import io.github.devngho.kisopenapi.requests.response.stock.Ticker
import io.github.devngho.kisopenapi.requests.response.stock.price.overseas.StockOverseasPrice
import io.github.devngho.kisopenapi.requests.util.*
import io.ktor.client.request.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 해외 주식 종목을 조건 검색해 반환합니다.
 */
class InquireOverseasCondition(override val client: KISApiClient) :
    DataRequest<InquireOverseasCondition.ConditionData, InquireOverseasCondition.ConditionResponse> {
    private val url = "${client.options.baseUrl}/uapi/overseas-price/v1/quotations/inquire-search"

    @Serializable
    data class ConditionResponse(
        @SerialName("tr_cont") override var tradeContinuous: String?,
        @SerialName("tr_id") override var tradeId: String?,
        @SerialName("gt_uid") override var globalTradeID: String?,
        @SerialName("msg_cd") override val code: String?,
        @SerialName("msg1") override val msg: String?,
        @SerialName("rt_cd") @Serializable(with = ResultCodeSerializer::class) override val isOk: Boolean?,

        @SerialName("output2") var output: List<ConditionResponseOutput>?,
        override var next: (suspend () -> Result<ConditionResponse>)?
    ) : Response, TradeContinuousResponse<ConditionResponse>, TradeIdMsg {
        @SerialName("error_description")
        override val errorDescription: String? = null

        @SerialName("error_code")
        override val errorCode: String? = null
    }

    @Serializable
    @Suppress("SpellCheckingInspection")
    data class ConditionResponseOutput @OptIn(ExperimentalSerializationApi::class) constructor(
        @SerialName("symb") override val ticker: String?,
        /** 실시간 조회 심볼 */
        @SerialName("rsym") val liveLoadCode: String?,
        /** 해외 거래소 */
        @SerialName("excd") @Serializable(with = OverseasMarket.OverseasMarketSerializer::class) val exchange: OverseasMarket?,
        @SerialName("zdiv") override val decimalPoint: Int? = null,
        /** 전일 종가 */
        @SerialName("base") @Contextual val priceYesterday: BigDecimal?,
        /** 전일 거래량 */
        @SerialName("pvol") @Contextual val tradeVolumeYesterday: BigInteger?,
        @SerialName("last") @Contextual override val price: BigDecimal?,
        /** 저가 */
        @SerialName("plow") @Contextual val minPrice: BigDecimal?,
        /** 고가 */
        @SerialName("phigh") @Contextual val maxPrice: BigDecimal?,
        /** 시가 */
        @SerialName("popen") @Contextual val openingPrice: BigDecimal?,
        /** 부호 */
        @SerialName("sign") override val sign: SignPrice?,
        @SerialName("diff") @Contextual override val changeFromYesterday: BigDecimal?,
        @SerialName("rate") @Contextual override val rateFromYesterday: BigDecimal?,
        @SerialName("tvol") @Contextual override val tradeVolume: BigInteger?,
        @SerialName("avol") @Contextual override val tradePriceVolume: BigDecimal?,
        /** 발행 주식 */
        @SerialName("shar") @Contextual val share: BigInteger?,
        /** 시가총액 */
        @SerialName("valx") @Contextual val marketCap: BigDecimal?,
        /** 종목명(한글) */
        @SerialName("name") val name: String?,
        /** 종목명(영문) */
        @SerialName("enmae") val nameEnglish: String?,
        /** 거래 가능 여부 */
        @SerialName("e_ordyn") @Contextual val canBuy: String?,
        /** 순위 */
        @SerialName("rank") val rank: Int?,
    ) : StockOverseasPrice, Ticker {
        @SerialName("error_description")
        override val errorDescription: String? = null

        @SerialName("error_code")
        override val errorCode: String? = null
    }

    data class ConditionData(
        val exchange: OverseasMarket,
        val priceRange: ClosedRange<BigDecimal>? = null,
        val rateFromYesterdayRange: ClosedRange<BigDecimal>? = null,
        val tradeVolumeRange: BigInteger.BigIntegerRange? = null,
        val perRange: ClosedRange<BigDecimal>? = null,
        val epsRange: ClosedRange<BigDecimal>? = null,
        val tradePriceVolumeRange: ClosedRange<BigDecimal>? = null,
        val shareRange: BigInteger.BigIntegerRange? = null,
        val marketCapRange: ClosedRange<BigDecimal>? = null,
        override var corp: CorporationRequest? = null,
    ): Data

    @Suppress("SpellCheckingInspection")
    override suspend fun call(data: ConditionData) = request(data) {
        client.httpClient.get(url) {
            setAuth(client)
            setTradeId("HHDFS76410000")
            setCorporation(it.corp)

            url { _ ->
                parameters.run {
                    set("EXCD", it.exchange.code)
                    set("AUTH", "")
                    it.priceRange?.let { p ->
                        set("CO_YN_PRICECUR", "1")
                        set("CO_ST_PRICECUR", p.start.toStringExpanded())
                        set("CO_EN_PRICECUR", p.endInclusive.toStringExpanded())
                    }
                    it.rateFromYesterdayRange?.let { r ->
                        set("CO_YN_RATE", "1")
                        set("CO_ST_RATE", r.start.toStringExpanded())
                        set("CO_EN_RATE", r.endInclusive.toStringExpanded())
                    }
                    it.tradeVolumeRange?.let { v ->
                        set("CO_YN_VOLUME", "1")
                        set("CO_ST_VOLUME", v.start.toString())
                        set("CO_EN_VOLUME", v.endInclusive.toString())
                    }
                    it.perRange?.let { p ->
                        set("CO_YN_PER", "1")
                        set("CO_ST_PER", p.start.toStringExpanded())
                        set("CO_EN_PER", p.endInclusive.toStringExpanded())
                    }
                    it.epsRange?.let { e ->
                        set("CO_YN_EPS", "1")
                        set("CO_ST_EPS", e.start.toStringExpanded())
                        set("CO_EN_EPS", e.endInclusive.toStringExpanded())
                    }
                    it.tradePriceVolumeRange?.let { a ->
                        set("CO_YN_AMT", "1")
                        set("CO_ST_AMT", a.start.toStringExpanded())
                        set("CO_EN_AMT", a.endInclusive.toStringExpanded())
                    }
                    it.shareRange?.let { s ->
                        set("CO_YN_SHAR", "1")
                        set("CO_ST_SHAR", s.start.toString())
                        set("CO_EN_SHAR", s.endInclusive.toString())
                    }
                    it.marketCapRange?.let { v ->
                        set("CO_YN_VALX", "1")
                        set("CO_ST_VALX", v.start.toStringExpanded())
                        set("CO_EN_VALX", v.endInclusive.toStringExpanded())
                    }
                }
            }
        }
    }
}