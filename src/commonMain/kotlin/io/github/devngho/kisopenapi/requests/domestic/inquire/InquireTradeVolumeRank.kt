package io.github.devngho.kisopenapi.requests.domestic.inquire

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.KISApiClient
import io.github.devngho.kisopenapi.requests.Data
import io.github.devngho.kisopenapi.requests.DataRequest
import io.github.devngho.kisopenapi.requests.Response
import io.github.devngho.kisopenapi.requests.data.CorporationRequest
import io.github.devngho.kisopenapi.requests.data.Msg
import io.github.devngho.kisopenapi.requests.response.stock.Ticker
import io.github.devngho.kisopenapi.requests.response.stock.price.domestic.StockPriceBase
import io.github.devngho.kisopenapi.requests.response.stock.price.domestic.StockPriceChange
import io.github.devngho.kisopenapi.requests.response.stock.trade.StockTradeFull
import io.github.devngho.kisopenapi.requests.util.*
import io.github.devngho.kisopenapi.requests.util.RequestException.Companion.throwIfClientIsDemo
import io.ktor.client.request.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 국내 주식 종목의 거래량 순위를 조회하고 반환합니다.
 */
@DemoNotSupported
class InquireTradeVolumeRank(override val client: KISApiClient) :
    DataRequest<InquireTradeVolumeRank.InquireTradeVolumeRankData, InquireTradeVolumeRank.InquireTradeVolumeRankResponse> {
    private val url = "${client.options.baseUrl}/uapi/domestic-stock/v1/quotations/volume-rank"

    @Serializable
    data class InquireTradeVolumeRankResponse(
        @SerialName("msg_cd") override val code: String?,
        @SerialName("msg1") override val msg: String?,
        @SerialName("rt_cd") @Serializable(with = ResultCodeSerializer::class) override val isOk: Boolean?,

        var output: List<InquireTradeVolumeRankOutput>?
    ) : Response, Msg {
        @SerialName("error_description")
        override val errorDescription: String? = null

        @SerialName("error_code")
        override val errorCode: String? = null
    }

    @Serializable
    @Suppress("SpellCheckingInspection")
    data class InquireTradeVolumeRankOutput(
        @SerialName("mksc_shrn_iscd") override val ticker: String?,
        @SerialName("data_rank") val rank: Int?,
        @SerialName("hts_kor_isnm") val name: String?,
        @SerialName("stck_prpr") @Contextual override val price: BigInteger?,
        @SerialName("prdy_vrss") @Contextual override val change: BigInteger?,
        @SerialName("prdy_vrss_sign") @Contextual override val sign: SignPrice?,
        @SerialName("prdy_ctrt") @Contextual override val rate: BigDecimal?,
        @SerialName("vol_inrt") @Contextual override val rateTradeVolumeFromYesterday: BigDecimal?,
        @SerialName("acml_vol") @Contextual override val accumulateTradeVolume: BigInteger?,
        @SerialName("vol_tnrt") @Contextual override val tradeVolumeTurningRate: BigDecimal?,
        @SerialName("acml_tr_pbmn") @Contextual override val accumulateTradePrice: BigInteger?,
        @SerialName("lstn_stcn") @Contextual val listedStockCount: BigInteger?,
        @SerialName("avrg_vol") @Contextual val averageTradeVolume: BigInteger?,
        @SerialName("n_befr_clpr_vrss_prpr_rate") @Contextual val rateFromNDayAgo: BigDecimal?,
        @SerialName("nday_vol_tnrt") @Contextual val tradeVolumeTurningRateNDay: BigDecimal?,
        @SerialName("avrg_tr_pbmn") @Contextual val averageTradePrice: BigInteger?,
        @SerialName("tr_pbmn_tnrt") @Contextual val tradePriceTurningRate: BigDecimal?,
        @SerialName("nday_tr_pbmn_tnrt") @Contextual val tradePriceTurningRateNDay: BigDecimal?,
    ) : StockPriceChange, StockPriceBase, StockTradeFull, Response, Ticker {
        @SerialName("error_description")
        override val errorDescription: String? = null

        @SerialName("error_code")
        override val errorCode: String? = null
    }

    @Suppress("unused")
    enum class BelongClassifier {
        /** 평균 거래량 */
        AverageTradeVolume,

        /** 거래 증가율 */
        TradeChangeRate,

        /** 평균 거래 회전율 */
        AverageTradeTurnoverRate,

        /** 거래 대금 */
        TradeVolume,

        /** 평균 거래 금액 회전율 */
        AverageTradeVolumeTurnoverRate
    }

    data class InquireTradeVolumeRankData(
        var belongClassifier: BelongClassifier,
        var includeMargin30: Boolean = true,
        var includeMargin40: Boolean = true,
        var includeMargin50: Boolean = true,
        var includeMargin60: Boolean = true,
        var includeMargin100: Boolean = true,
        var includeCreditDeposit30: Boolean = true,
        var includeCreditDeposit40: Boolean = true,
        var includeCreditDeposit50: Boolean = true,
        var includeCreditDeposit60: Boolean = true,
        var excludeInvestmentDanger: Boolean = false,
        var excludeInvestmentWarning: Boolean = false,
        var excludeInvestmentAlert: Boolean = false,
        var excludeLiquidating: Boolean = false,
        var excludeForAdministration: Boolean = false,
        var excludeUnreliableDisclosure: Boolean = false,
        var excludePreferredShare: Boolean = false,
        var excludeTradeSuspended: Boolean = false,
        val priceRange: IntRange? = null,
        val minTradeVolume: Int? = null,
        /**
         * 0000 : 전체
         * 기타 : 업종 코드
         */
        val ticker: String = "0000",
        var seeCommonShare: Boolean = true,
        var seePreferredShare: Boolean = true,
        override var corp: CorporationRequest? = null
    ) : Data

    @Suppress("SpellCheckingInspection")
    override suspend fun call(data: InquireTradeVolumeRankData) = request(data) {
        throwIfClientIsDemo()

        client.httpClient.get(url) {
            setAuth(client)
            setTradeId("FHPST01710000")
            setCorporation(it.corp)

            url { _ ->
                parameters["FID_COND_MRKT_DIV_CODE"] = "J"
                parameters["FID_COND_SCR_DIV_CODE"] = "20171"
                parameters["FID_INPUT_ISCD"] = it.ticker
                parameters["FID_DIV_CLS_CODE"] =
                    if (it.seeCommonShare && it.seePreferredShare) "0" else if (it.seeCommonShare) "1" else "2"
                parameters["FID_BLNG_CLS_CODE"] = it.belongClassifier.ordinal.toString()
                parameters["FID_TRGT_CLS_CODE"] =
                    listOf(
                        it.includeMargin30,
                        it.includeMargin40,
                        it.includeMargin50,
                        it.includeMargin60,
                        it.includeMargin100,
                        it.includeCreditDeposit30,
                        it.includeCreditDeposit40,
                        it.includeCreditDeposit50,
                        it.includeCreditDeposit60
                    ).joinToString("") { if (it) "1" else "0" }
                parameters["FID_TRGT_EXLS_CLS_CODE"] =
                    listOf(
                        it.excludeInvestmentDanger,
                        it.excludeInvestmentWarning,
                        it.excludeInvestmentAlert,
                        it.excludeForAdministration,
                        it.excludeLiquidating,
                        it.excludeUnreliableDisclosure,
                        it.excludePreferredShare,
                        it.excludeTradeSuspended
                    ).joinToString("") { if (it) "1" else "0" }
                parameters["FID_INPUT_PRICE_1"] = it.priceRange?.first?.toString() ?: ""
                parameters["FID_INPUT_PRICE_2"] = it.priceRange?.last?.toString() ?: ""
                parameters["FID_VOL_CNT"] = it.minTradeVolume?.toString() ?: ""
                parameters["FID_INPUT_DATE_1"] = ""
            }
        }
    }
}