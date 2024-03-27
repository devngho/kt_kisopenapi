package io.github.devngho.kisopenapi.requests.common

import io.github.devngho.kisopenapi.KISApiClient
import io.github.devngho.kisopenapi.requests.Data
import io.github.devngho.kisopenapi.requests.DataRequest
import io.github.devngho.kisopenapi.requests.Response
import io.github.devngho.kisopenapi.requests.data.CorporationRequest
import io.github.devngho.kisopenapi.requests.data.TradeContinuousData
import io.github.devngho.kisopenapi.requests.data.TradeContinuousResponse
import io.github.devngho.kisopenapi.requests.data.TradeIdMsg
import io.github.devngho.kisopenapi.requests.response.stock.BaseInfo
import io.github.devngho.kisopenapi.requests.response.stock.Ticker
import io.github.devngho.kisopenapi.requests.util.*
import io.ktor.client.request.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 상품의 기본 정보를 조회하고 반환합니다.
 */
@DemoNotSupported
class InquireProductBaseInfo(override val client: KISApiClient) :
    DataRequest<InquireProductBaseInfo.InquireProductBaseInfoData, InquireProductBaseInfo.InquireProductBaseInfoResponse> {
    private val url = "${client.options.baseUrl}/uapi/domestic-stock/v1/quotations/search-info"

    @Serializable
    data class InquireProductBaseInfoResponse(
        @SerialName("tr_id") override var tradeId: String?,
        @SerialName("tr_cont") override var tradeContinuous: String?,
        @SerialName("gt_uid") override var globalTradeID: String?,
        @SerialName("msg_cd") override val code: String?,
        @SerialName("msg1") override val msg: String?,
        @SerialName("rt_cd") @Serializable(with = ResultCodeSerializer::class) override val isOk: Boolean?,

        var output: InquireProductBaseInfoResponseOutput?,
        override var next: (suspend () -> Result<InquireProductBaseInfoResponse>)?
    ) : Response, TradeContinuousResponse<InquireProductBaseInfoResponse>, TradeIdMsg {
        @SerialName("error_description")
        override val errorDescription: String? = null

        @SerialName("error_code")
        override val errorCode: String? = null
    }

    @Serializable
    @Suppress("SpellCheckingInspection")
    data class InquireProductBaseInfoResponseOutput(
        @SerialName("pdno") override val ticker: String?,
        @SerialName("prdt_type_cd") override val type: ProductTypeCode?,
        @SerialName("prdt_name") override val name: String?,
        @SerialName("prdt_name120") override val name120: String?,
        @SerialName("prdt_abrv_name") override val nameShort: String?,
        @SerialName("prdt_eng_name") override val nameEng: String?,
        @SerialName("prdt_eng_name120") override val nameEng120: String?,
        @SerialName("prdt_eng_abrv_name") override val nameEngShort: String?,
        @SerialName("std_pdno") override val codeStandard: String?,
        @SerialName("shtn_pdno") override val codeShort: String?,
        @SerialName("prdt_sale_stat_cd") override val productSaleState: String?,
        @SerialName("prdt_risk_grad_cd") override val productRiskGrade: String?,
        @SerialName("prdt_clsf_cd") override val productClassifier: String?,
        @SerialName("prdt_clsf_name") override val productClassifierName: String?,
        @SerialName("sale_strt_dt") override val saleStartDate: String?,
        @SerialName("sale_end_dt") override val saleEndDate: String?,
        @SerialName("wrap_asst_type_cd") override val wrapAssetType: String?,
        @SerialName("ivst_prdt_type_cd") override val productInvestmentType: String?,
        @SerialName("ivst_prdt_type_cd_name") override val productInvestmentTypeName: String?,
        @SerialName("frst_erlm_date") override val firstRegisterDate: String?,
    ) : BaseInfo {
        override val errorDescription: String? = null
        override val errorCode: String? = null
    }

    data class InquireProductBaseInfoData(
        /** 조회할 상품 번호 */
        override val ticker: String,
        /** 조회할 상품 종류 */
        val type: ProductTypeCode,
        override var corp: CorporationRequest? = null, override var tradeContinuous: String? = ""
    ) : Data, TradeContinuousData, Ticker

    @Suppress("SpellCheckingInspection")
    override suspend fun call(data: InquireProductBaseInfoData) = request(data) {
        if (client.isDemo) throw RequestException(
            "모의투자에서는 사용할 수 없는 API ProductBaseInfo를 호출했습니다.",
            RequestCode.DemoUnavailable
        )

        client.httpClient.get(url) {
            setAuth(client)
            setTradeId("CTPF1604R")
            setCorporation(it.corp)

            url { _ ->
                parameters.run {
                    set("PDNO", it.ticker)
                    set("PRDT_TYPE_CD", it.type.num)
                }
            }
        }
    }
}