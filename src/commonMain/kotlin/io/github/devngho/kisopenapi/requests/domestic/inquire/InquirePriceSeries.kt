package io.github.devngho.kisopenapi.requests.domestic.inquire

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
import io.github.devngho.kisopenapi.requests.response.stock.price.domestic.StockPriceLowHigh
import io.github.devngho.kisopenapi.requests.util.*
import io.github.devngho.kisopenapi.requests.util.YYYYMMDDSerializer.YYYYMMDD
import io.ktor.client.request.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 국내 주식 종목의 일/주/월/년 기간별 시세를 조회하고 반환합니다.
 */
class InquirePriceSeries(override val client: KISApiClient) :
    DataRequest<InquirePriceSeries.InquirePriceSeriesData, InquirePriceSeries.InquirePriceSeriesResponse> {
    private val url = "${client.options.baseUrl}/uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice"

    @Serializable
    data class InquirePriceSeriesResponse(
        @SerialName("tr_id") override var tradeId: String?,
        @SerialName("tr_cont") override var tradeContinuous: String?,
        @SerialName("gt_uid") override var globalTradeID: String?,
        @SerialName("msg_cd") override val code: String?,
        @SerialName("msg1") override val msg: String?,
        @SerialName("rt_cd") @Serializable(with = ResultCodeSerializer::class) override val isOk: Boolean?,

        var output1: InquirePrice.InquirePriceResponseOutput?,
        var output2: List<InquirePriceSeriesResponseOutput2>?,
        override var next: (suspend () -> Result<InquirePriceSeriesResponse>)?
    ) : Response, TradeContinuousResponse<InquirePriceSeriesResponse>, TradeIdMsg {
        @SerialName("error_description")
        override val errorDescription: String? = null

        @SerialName("error_code")
        override val errorCode: String? = null
    }

    @Serializable
    @Suppress("SpellCheckingInspection")
    data class InquirePriceSeriesResponseOutput2(
        @Serializable(with = YYYYMMDDSerializer::class) @SerialName("stck_bsop_date") val bizDate: Date?,
        @SerialName("stck_oprc") @Contextual override val openingPrice: BigInteger?,
        @SerialName("stck_hgpr") @Contextual override val highestPrice: BigInteger?,
        @SerialName("stck_lwpr") @Contextual override val lowestPrice: BigInteger?,
        /**
         * Close Price
         */
        @SerialName("stck_clpr") @Contextual override val price: BigInteger?,
        @SerialName("prdy_vrss") @Contextual val changeFromYesterday: BigInteger?,
        @SerialName("prdy_vrss_sign") val signFromYesterday: SignPrice?,
        @SerialName("flng_cls_code") val lockCode: LockCode?,
        @SerialName("acml_prtt_rate") @Contextual val accumulateDivisionRate: BigDecimal?,
        @SerialName("acml_vol") @Contextual val accumulateTradeVolume: BigInteger?,
        @SerialName("acml_tr_pbmn") @Contextual val accumulateTradePrice: BigInteger?,
        @SerialName("prtt_rate") @Contextual val divisionRate: BigDecimal?,
        @SerialName("mod_yn") @Serializable(with = YNSerializer::class) val isModified: Boolean?,
        @SerialName("revl_issu_reas") val reasonForRevision: String?,
    ) : StockPriceLowHigh {
        @SerialName("error_description")
        override val errorDescription: String? = null

        @SerialName("error_code")
        override val errorCode: String? = null
    }

    data class InquirePriceSeriesData(
        override val ticker: String,
        val period: PeriodDivisionCode = PeriodDivisionCode.Days,
        val useOriginalPrice: Boolean = false,
        val startDate: Date = Date(0, 0, 0),
        val endDate: Date = Date(0, 0, 0),
        override var corp: CorporationRequest? = null, override var tradeContinuous: String? = ""
    ) : Data, TradeContinuousData, Ticker

    @Suppress("SpellCheckingInspection")
    override suspend fun call(data: InquirePriceSeriesData) = request(data) {
        client.httpClient.get(url) {
            setAuth(client)
            setTradeId("FHKST03010100")
            setStock(it.ticker)
            setCorporation(it.corp)

            url { _ ->
                parameters.run {
                    append("FID_PERIOD_DIV_CODE", it.period.num)
                    append("FID_INPUT_DATE_1", it.startDate.YYYYMMDD)
                    append("FID_INPUT_DATE_2", it.endDate.YYYYMMDD)
                    append("FID_ORG_ADJ_PRC", if (it.useOriginalPrice) "1" else "0")
                }
            }
        }
    }
}