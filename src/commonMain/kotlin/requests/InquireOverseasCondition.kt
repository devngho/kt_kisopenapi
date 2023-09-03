package io.github.devngho.kisopenapi.requests

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.KisOpenApi
import io.github.devngho.kisopenapi.requests.response.*
import io.github.devngho.kisopenapi.requests.util.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class InquireOverseasCondition(override val client: KisOpenApi): DataRequest<InquireOverseasCondition.ConditionData, InquireOverseasCondition.ConditionResponse> {
    private val url = if (client.isDemo) "https://openapivts.koreainvestment.com:29443/uapi/overseas-price/v1/quotations/inquire-search"
    else               "https://openapi.koreainvestment.com:9443/uapi/overseas-price/v1/quotations/inquire-search"

    @Serializable
    data class ConditionResponse(
        @SerialName("tr_cont") override var tradeContinuous: String?,
        @SerialName("tr_id") override var tradeId: String?,
        @SerialName("gt_uid") override var globalTradeID: String?,
        @SerialName("msg_cd") override val code: String?,
        @SerialName("msg1") override val msg: String?,
        @SerialName("rt_cd") @Serializable(with = ResultCodeSerializer::class) override val isOk: Boolean?,

        @SerialName("output2") var output: List<ConditionResponseOutput>?, override var next: (suspend () -> Response)?
    ): Response, TradeContinuousResponse, TradeIdMsg {
        override val errorDescription: String? = null
        override val errorCode: String? = null
    }

    @Serializable
    data class ConditionResponseOutput @OptIn(ExperimentalSerializationApi::class) constructor(
        @SerialName("symb") val code: String?,
        @SerialName("rsym") val liveLoadCode: String?,
        @SerialName("excd") @Serializable(with = OverseasMarket.OverseasMarketSerializer::class) val exchange: OverseasMarket?,
        @SerialName("zdiv") override val decimalPoint: Int? = null,
        @SerialName("base") @Contextual val priceYesterday: BigDecimal?,
        @SerialName("pvol") @Contextual val tradeVolumeYesterday: BigInteger?,
        @SerialName("last") @Contextual override val price: BigDecimal?,
        @SerialName("plow") @Contextual val priceLow: BigDecimal?,
        @SerialName("phigh") @Contextual val priceHigh: BigDecimal?,
        @SerialName("popen") @Contextual val priceMarket: BigDecimal?,
        @SerialName("sign") override val sign: SignPrice?,
        @SerialName("diff") @Contextual override val changeFromYesterday: BigDecimal?,
        @SerialName("rate") @Contextual override val rateFromYesterday: BigDecimal?,
        @SerialName("tvol") @Contextual override val tradeVolume: BigInteger?,
        @SerialName("avol") @Contextual override val tradePriceVolume: BigDecimal?,
        @SerialName("shar") @Contextual val share: BigInteger?,
        @SerialName("valx") @Contextual val marketCap: BigDecimal?,
        @SerialName("name") val name: String?,
        @SerialName("enmae") val nameEnglish: String?,
        @SerialName("e_ordyn") @Contextual val canBuy: String?,
        @SerialName("rank") val rank: Int?,
    ): StockOverseasPrice {
        override val errorDescription: String? = null
        override val errorCode: String? = null
    }

    data class ConditionData(
        val exchange: OverseasMarket,
        val priceRange: BigDecimalRange? = null,
        val rateFromYesterdayRange: BigDecimalRange? = null,
        val tradeVolumeRange: BigInteger.BigIntegerRange? = null,
        val perRange: BigDecimalRange? = null,
        val epsRange: BigDecimalRange? = null,
        val tradePriceVolumeRange: BigDecimalRange? = null,
        val shareRange: BigInteger.BigIntegerRange? = null,
        val marketCapRange: BigDecimalRange? = null,
        override var corp: CorporationRequest? = null,
    ): Data

    @Suppress("duplicate")
    override suspend fun call(data: ConditionData): ConditionResponse {
        if (data.corp == null) data.corp = client.corp

        val res = client.httpClient.get(url) {
            auth(client)
            tradeId("HHDFS76410000")
            data.corp?.let { corporation(it) }
            url {
                parameters.run {
                    set("EXCD", data.exchange.code)
                    set("AUTH", "")
                    data.priceRange?.let { p ->
                        set("CO_YN_PRICECUR", "1")
                        set("CO_ST_PRICECUR", p.start.toStringExpanded())
                        set("CO_EN_PRICECUR", p.endInclusive.toStringExpanded())
                    }
                    data.rateFromYesterdayRange?.let { r ->
                        set("CO_YN_RATE", "1")
                        set("CO_ST_RATE", r.start.toStringExpanded())
                        set("CO_EN_RATE", r.endInclusive.toStringExpanded())
                    }
                    data.tradeVolumeRange?.let { v ->
                        set("CO_YN_VOLUME", "1")
                        set("CO_ST_VOLUME", v.start.toString())
                        set("CO_EN_VOLUME", v.endInclusive.toString())
                    }
                    data.perRange?.let { p ->
                        set("CO_YN_PER", "1")
                        set("CO_ST_PER", p.start.toStringExpanded())
                        set("CO_EN_PER", p.endInclusive.toStringExpanded())
                    }
                    data.epsRange?.let { e ->
                        set("CO_YN_EPS", "1")
                        set("CO_ST_EPS", e.start.toStringExpanded())
                        set("CO_EN_EPS", e.endInclusive.toStringExpanded())
                    }
                    data.tradePriceVolumeRange?.let { a ->
                        set("CO_YN_AMT", "1")
                        set("CO_ST_AMT", a.start.toStringExpanded())
                        set("CO_EN_AMT", a.endInclusive.toStringExpanded())
                    }
                    data.shareRange?.let { s ->
                        set("CO_YN_SHAR", "1")
                        set("CO_ST_SHAR", s.start.toString())
                        set("CO_EN_SHAR", s.endInclusive.toString())
                    }
                    data.marketCapRange?.let { v ->
                        set("CO_YN_VALX", "1")
                        set("CO_ST_VALX", v.start.toStringExpanded())
                        set("CO_EN_VALX", v.endInclusive.toStringExpanded())
                    }
                }
            }
        }
        return res.body<ConditionResponse>().apply {
            if (this.errorCode != null) throw RequestError(this.errorDescription)

            processHeader(res)
        }
    }
}