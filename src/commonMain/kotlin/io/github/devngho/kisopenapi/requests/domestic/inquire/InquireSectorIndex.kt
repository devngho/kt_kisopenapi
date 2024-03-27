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
import io.github.devngho.kisopenapi.requests.util.*
import io.ktor.client.request.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 국내 업종의 지수를 조회하고 반환합니다.
 */
@DemoNotSupported
class InquireSectorIndex(override val client: KISApiClient) :
    DataRequest<InquireSectorIndex.InquireSectorIndexData, InquireSectorIndex.InquireSectorIndexResponse> {
    private val url = "${client.options.baseUrl}/uapi/domestic-stock/v1/quotations/inquire-index-price"

    @Serializable
    data class InquireSectorIndexResponse(
        @SerialName("tr_id") override var tradeId: String?,
        @SerialName("tr_cont") var tradeCount: String?,
        @SerialName("gt_uid") override var globalTradeID: String?,
        @SerialName("msg_cd") override val code: String?,
        @SerialName("msg1") override val msg: String?,
        @SerialName("rt_cd") @Serializable(with = ResultCodeSerializer::class) override val isOk: Boolean?,

        var output: InquireSectorIndexResponseOutput?,
        override var next: (suspend () -> Result<InquireSectorIndexResponse>)?,
        override var tradeContinuous: String?
    ) : Response, TradeContinuousResponse<InquireSectorIndexResponse>, TradeIdMsg {
        @SerialName("error_description")
        override val errorDescription: String? = null

        @SerialName("error_code")
        override val errorCode: String? = null
    }

    @Serializable
    @Suppress("SpellCheckingInspection")
    data class InquireSectorIndexResponseOutput(
        @SerialName("bstp_nmix_prpr") @Contextual val price: BigDecimal?,
        @SerialName("bstp_nmix_prdy_vrss") @Contextual val change: BigDecimal?,
        @SerialName("prdy_vrss_sign") val sign: SignPrice?,
        @SerialName("bstp_nmix_prdy_ctrt") @Contextual val rate: BigDecimal?,
        @SerialName("acml_vol") @Contextual val accumulateTradeVolume: BigInteger?,
        @SerialName("prdy_vol") @Contextual val tradeVolumeFromYesterday: BigInteger?,
        /** 업종 지수 시가 */
        @SerialName("bstp_nmix_oprc") @Contextual val openingPrice: BigDecimal?,
        /** 전일 지수 대비 지수 시가 */
        @SerialName("prdy_nmix_vrss_nmix_oprc") @Contextual val openingPriceChange: BigDecimal?,
        /** 시가 대비 현재가 부호 */
        @SerialName("oprc_vrss_prpr_sign") val openingPriceSign: SignPrice?,
        /** 지수 시가 전일 대비율 */
        @SerialName("bstp_nmix_oprc_prdy_ctrt") @Contextual val openingPriceRate: BigDecimal?,
        @SerialName("bstp_nmix_hgpr") @Contextual val highestPrice: BigDecimal?,
        /** 전일 지수 대비 지수 고가 */
        @SerialName("prdy_nmix_vrss_nmix_hgpr") @Contextual val highestPriceChangeFromYesterday: BigDecimal?,
        /** 고가 대비 현재가 부호 */
        @SerialName("hgpr_vrss_prpr_sign") val highestPriceSign: SignPrice?,
        /** 지수 고가 전일 대비율 */
        @SerialName("bstp_nmix_hgpr_prdy_ctrt") @Contextual val highestPriceRate: BigDecimal?,
        @SerialName("bstp_nmix_lwpr") @Contextual val lowestPrice: BigDecimal?,
        /** 전일 종가 대비 지수 저가 */
        @SerialName("prdy_clpr_vrss_lwpr") @Contextual val lowestPriceChangeFromYesterday: BigDecimal?,
        /** 저가 대비 현재가 부호 */
        @SerialName("lwpr_vrss_prpr_sign") val lowestPriceSign: SignPrice?,
        /** 지수 저가, 전일 종가 대비율 */
        @SerialName("prdy_clpr_vrss_lwpr_rate") @Contextual val lowestPriceRate: BigDecimal?,
        @SerialName("ascn_issu_cnt") @Contextual val ascendingStockCount: BigInteger?,
        @SerialName("uplm_issu_cnt") @Contextual val upperLimitStockCount: BigInteger?,
        @SerialName("stnr_issu_cnt") @Contextual val complementStockCount: BigInteger?,
        @SerialName("down_issu_cnt") @Contextual val descendingStockCount: BigInteger?,
        @SerialName("lslm_issu_cnt") @Contextual val lowerLimitStockCount: BigInteger?,
        @SerialName("dryy_bstp_nmix_hgpr") @Contextual val highestPriceInYear: BigDecimal?,
        @Serializable(with = YYYYMMDDSerializer::class) @SerialName("dryy_bstp_nmix_hgpr_date") val highestPriceDateInYear: Date?,
        @SerialName("dryy_hgpr_vrss_prpr_rate") @Contextual val highestPriceRateInYear: BigDecimal?,
        @SerialName("dryy_bstp_nmix_lwpr") @Contextual val lowestPriceInYear: BigDecimal?,
        @Serializable(with = YYYYMMDDSerializer::class) @SerialName("dryy_bstp_nmix_lwpr_date") val lowestPriceDateInYear: Date?,
        @SerialName("dryy_lwpr_vrss_prpr_rate") @Contextual val lowestPriceRateInYear: BigDecimal?,
        @SerialName("acml_tr_pbmn") @Contextual val accumulateTradePrice: BigInteger?,
        @SerialName("prdy_tr_pbmn") @Contextual val tradePriceFromYesterday: BigInteger?,
        @SerialName("total_askp_rsqn") @Contextual val totalAskCount: BigInteger?,
        @SerialName("total_bidp_rsqn") @Contextual val totalBidCount: BigInteger?,
        @SerialName("seln_rsqn_rate") @Contextual val sellCountRate: BigDecimal?,
        @SerialName("shnu_rsqn_rate") @Contextual val buyCountRate: BigDecimal?,
        @SerialName("ntby_rsqn") @Contextual val netBuyCount: BigInteger?,
    ) : Response {
        @SerialName("error_description")
        override val errorDescription: String? = null

        @SerialName("error_code")
        override val errorCode: String? = null
    }

    data class InquireSectorIndexData(
        /** 업종(FAQ : 종목정보 다운로드(국내) - 업종코드 참조) */
        val sector: String,
        override var corp: CorporationRequest? = null,
        override var tradeContinuous: String? = ""
    ) : Data, TradeContinuousData

    @Suppress("SpellCheckingInspection")
    override suspend fun call(data: InquireSectorIndexData) = request(data) {
        if (client.isDemo) throw RequestException(
            "모의투자에서는 사용할 수 없는 API InquireSectorIndex를 호출했습니다.",
            RequestCode.DemoUnavailable
        )

        client.httpClient.get(url) {
            setAuth(client)
            setSector(it.sector)
            setTradeId("FHPUP02100000")
            setCorporation(it.corp)
        }
    }
}