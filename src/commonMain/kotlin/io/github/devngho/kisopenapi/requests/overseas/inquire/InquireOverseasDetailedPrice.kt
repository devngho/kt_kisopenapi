package io.github.devngho.kisopenapi.requests.overseas.inquire

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.KISApiClient
import io.github.devngho.kisopenapi.requests.Data
import io.github.devngho.kisopenapi.requests.DataRequest
import io.github.devngho.kisopenapi.requests.Response
import io.github.devngho.kisopenapi.requests.data.CorporationRequest
import io.github.devngho.kisopenapi.requests.data.TradeContinuousData
import io.github.devngho.kisopenapi.requests.data.TradeContinuousResponse
import io.github.devngho.kisopenapi.requests.data.TradeIdMsg
import io.github.devngho.kisopenapi.requests.response.stock.Ticker
import io.github.devngho.kisopenapi.requests.response.stock.price.overseas.StockOverseasPriceFull
import io.github.devngho.kisopenapi.requests.util.*
import io.ktor.client.request.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 해외 주식 종목의 가격 및 상세 정보를 조회하고 반환합니다.
 */
class InquireOverseasDetailedPrice(override val client: KISApiClient) :
    DataRequest<InquireOverseasDetailedPrice.InquirePriceData, InquireOverseasDetailedPrice.InquirePriceResponse> {
    private val url = "${client.options.baseUrl}/uapi/overseas-price/v1/quotations/price-detail"

    @Serializable
    data class InquirePriceResponse(
        @SerialName("tr_id") override var tradeId: String?,
        @SerialName("tr_cont") var tradeCount: String?,
        @SerialName("gt_uid") override var globalTradeID: String?,
        @SerialName("msg_cd") override val code: String?,
        @SerialName("msg1") override val msg: String?,
        @SerialName("rt_cd") @Serializable(with = ResultCodeSerializer::class) override val isOk: Boolean?,

        var output: InquirePriceResponseOutput?, override var next: (suspend () -> Result<InquirePriceResponse>)?,
        override var tradeContinuous: String?
    ) : Response, TradeContinuousResponse<InquirePriceResponse>, TradeIdMsg {
        @SerialName("error_description")
        override val errorDescription: String? = null

        @SerialName("error_code")
        override val errorCode: String? = null
    }

    @Serializable
    @Suppress("SpellCheckingInspection")
    data class InquirePriceResponseOutput(
        @SerialName("rsym") val liveLoadCode: String?,
        @SerialName("zdiv") override val decimalPoint: Int?,
        @SerialName("base") @Contextual override val priceYesterday: BigDecimal?,
        @SerialName("pvol") @Contextual override val tradeVolumeYesterday: BigDecimal?,
        @SerialName("last") @Contextual override val price: BigDecimal?,
        @SerialName("sign") override val sign: SignPrice?,
        @SerialName("diff") @Contextual override val change: BigDecimal?,
        @SerialName("rate") @Contextual override val rate: BigDecimal?,
        @SerialName("tvol") @Contextual override val tradeVolume: BigInteger?,
        @SerialName("tamt") @Contextual override val tradePriceVolume: BigDecimal?,
        @SerialName("open") @Contextual override val openingPrice: BigDecimal?,
        @SerialName("high") @Contextual override val highestPrice: BigDecimal?,
        @SerialName("low") @Contextual override val lowestPrice: BigDecimal?,
        @SerialName("tomv") @Contextual override val marketCap: BigDecimal?,
        @SerialName("pamt") @Contextual override val tradePriceVolumeFromYesterday: BigDecimal?,
        @SerialName("uplp") @Contextual override val upperLimitPrice: BigDecimal?,
        @SerialName("dnlp") @Contextual override val lowerLimitPrice: BigDecimal?,
        @SerialName("h52p") @Contextual override val highPriceW52: BigDecimal?,
        @SerialName("h52d") @Serializable(with = YYYYMMDDSerializer::class) override val highPriceDateW52: Date?,
        @SerialName("l52p") @Contextual override val lowPriceW52: BigDecimal?,
        @SerialName("l52d") @Serializable(with = YYYYMMDDSerializer::class) override val lowPriceDateW52: Date?,
        @SerialName("perx") @Contextual override val per: BigDecimal?,
        @SerialName("pbrx") @Contextual override val pbr: BigDecimal?,
        @SerialName("epsx") @Contextual override val eps: BigDecimal?,
        @SerialName("bpsx") @Contextual override val bps: BigDecimal?,
        @SerialName("shar") @Contextual override val listedStockCount: BigInteger?,
        @SerialName("mcap") @Contextual override val capitalFinance: BigDecimal?,
        @SerialName("curr") override val currency: Currency?,
        @SerialName("vnit") @Contextual override val tradeUnit: BigInteger?,
        @SerialName("t_xprc") @Contextual override val priceKRW: BigDecimal?,
        @SerialName("t_xdif") @Contextual override val changeKRW: BigDecimal?,
        @SerialName("t_xrat") @Contextual override val rateKRW: BigDecimal?,
        @SerialName("t_xsgn") override val signKRW: SignPrice?,
        @SerialName("p_xprc") @Contextual override val priceKRWYesterday: BigDecimal?,
        @SerialName("p_xdif") @Contextual override val changeYesterdayKRW: BigDecimal?,
        @SerialName("p_xrat") @Contextual override val rateYesterdayKRW: BigDecimal?,
        @SerialName("p_xsng") override val signYesterdayKRW: SignPrice?,
        @SerialName("t_rate") @Contextual override val exchangeRate: BigDecimal?,
        @SerialName("p_rate") @Contextual override val exchangeRateYesterday: BigDecimal?,
        @SerialName("e_ordyn") override val canOrder: String?,
        @SerialName("e_hogau") @Contextual override val askingPriceUnit: BigDecimal?,
        @SerialName("e_icod") override val sectorName: String?,
        @SerialName("e_parp") @Contextual override val facePrice: BigDecimal?,
        @SerialName("etyp_nm") override val etpClassificationName: String?,
    ) : StockOverseasPriceFull {
        @SerialName("error_description")
        override val errorDescription: String? = null

        @SerialName("error_code")
        override val errorCode: String? = null
    }

    data class InquirePriceData(
        override val ticker: String,
        val market: OverseasMarket,
        override var corp: CorporationRequest? = null,
        override var tradeContinuous: String? = ""
    ) : Data, TradeContinuousData, Ticker

    @Suppress("SpellCheckingInspection")
    override suspend fun call(data: InquirePriceData) = request(data, block = {
        client.httpClient.get(url) {
            setAuth(client)
            url { _ ->
                parameters.run {
                    append("AUTH", "")
                    append("EXCD", it.market.code)
                    append("SYMB", it.ticker)
                }
            }
            setTradeId("HHDFS76200200")
            setCorporation(it.corp)
        }
    }, bodyModifier = {
        // 실제 변화량이 음수인 경우에도 changeFromYesterday 값이 양수로 반환됨
        // 편의성 위해 변동률이 음수이고 변화량은 양수인 경우 경우 changeFromYesterday 값을 음수로 변환함
        if (it.output?.rate?.isNegative == true && it.output?.change?.isPositive == true) it.copy(
            output = it.output!!.copy(change = it.output!!.change!! * -1)
        )
        else it
    })
}