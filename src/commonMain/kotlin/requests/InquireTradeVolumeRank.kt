package io.github.devngho.kisopenapi.requests

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.KisOpenApi
import io.github.devngho.kisopenapi.requests.response.*
import io.github.devngho.kisopenapi.requests.util.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class InquireTradeVolumeRank(override val client: KisOpenApi) :
    DataRequest<InquireTradeVolumeRank.InquireTradeVolumeRankData, InquireTradeVolumeRank.InquireTradeVolumeRankResponse> {
    private val url = if (client.isDemo) throw DemoError("InquireTradeVolumeRank cannot run with demo account.")
    else "https://openapi.koreainvestment.com:9443/uapi/domestic-stock/v1/quotations/volume-rank"

    @Serializable
    data class InquireTradeVolumeRankResponse(
        @SerialName("msg_cd") override val code: String?,
        @SerialName("msg1") override val msg: String?,
        @SerialName("rt_cd") @Serializable(with = ResultCodeSerializer::class) override val isOk: Boolean?,

        var output: List<InquireTradeVolumeRankOutput>?
    ) : Response, Msg {
        override val errorDescription: String? = null
        override val errorCode: String? = null
    }

    @Serializable
    @Suppress("SpellCheckingInspection")
    data class InquireTradeVolumeRankOutput(
        @SerialName("mksc_shrn_iscd") val ticker: String?,
        @SerialName("data_rank") val rank: Int?,
        @SerialName("hts_kor_isnm") val name: String?,
        @SerialName("stck_prpr") @Contextual override val price: BigInteger?,
        @SerialName("prdy_vrss") @Contextual override val changeFromYesterday: BigInteger?,
        @SerialName("prdy_vrss_sign") @Contextual override val signFromYesterday: SignPrice?,
        @SerialName("prdy_ctrt") @Contextual override val rateFromYesterday: BigDecimal?,
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
    ) : StockPriceChange, StockPriceBase, StockTradeFull, Response {
        override val errorDescription: String? = null
        override val errorCode: String? = null
    }

    @Suppress("unused")
    enum class BelongClassifier {
        AverageTradeVolume,
        TradeChangeRate,
        AverageTradeTurnoverRate,
        TradeVolume,
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
    override suspend fun call(data: InquireTradeVolumeRankData): InquireTradeVolumeRankResponse =
        client.rateLimiter.rated {
        if (data.corp == null) data.corp = client.corp

        val res = client.httpClient.get(url) {
            auth(client)
            tradeId("FHPST01710000")
            data.corp?.let { corporation(it) }

            url {
                parameters["FID_COND_MRKT_DIV_CODE"] = "J"
                parameters["FID_COND_SCR_DIV_CODE"] = "20171"
                parameters["FID_INPUT_ISCD"] = data.ticker
                parameters["FID_DIV_CLS_CODE"] =
                    if (data.seeCommonShare && data.seePreferredShare) "0" else if (data.seeCommonShare) "1" else "2"
                parameters["FID_BLNG_CLS_CODE"] = data.belongClassifier.ordinal.toString()
                parameters["FID_TRGT_CLS_CODE"] =
                    listOf(
                        data.includeMargin30,
                        data.includeMargin40,
                        data.includeMargin50,
                        data.includeMargin60,
                        data.includeMargin100,
                        data.includeCreditDeposit30,
                        data.includeCreditDeposit40,
                        data.includeCreditDeposit50,
                        data.includeCreditDeposit60
                    ).joinToString("") { if (it) "1" else "0" }
                parameters["FID_TRGT_EXLS_CLS_CODE"] =
                    listOf(
                        data.excludeInvestmentDanger,
                        data.excludeInvestmentWarning,
                        data.excludeInvestmentAlert,
                        data.excludeForAdministration,
                        data.excludeLiquidating,
                        data.excludeUnreliableDisclosure,
                        data.excludePreferredShare,
                        data.excludeTradeSuspended
                    ).joinToString("") { if (it) "1" else "0" }
                parameters["FID_INPUT_PRICE_1"] = data.priceRange?.first?.toString() ?: ""
                parameters["FID_INPUT_PRICE_2"] = data.priceRange?.last?.toString() ?: ""
                parameters["FID_VOL_CNT"] = data.minTradeVolume?.toString() ?: ""
                parameters["FID_INPUT_DATE_1"] = ""
            }
        }

            res.body<InquireTradeVolumeRankResponse>().apply {
            if (this.errorCode != null) throw RequestError(this.errorDescription)

            processHeader(res)
        }
    }
}