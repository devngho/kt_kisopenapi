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

class ProductBaseInfo(override val client: KISApiClient) :
    DataRequest<ProductBaseInfo.ProductBaseInfoData, ProductBaseInfo.ProductBaseInfoResponse> {
    private val url = if (client.isDemo) throw RequestException(
        "ProductBaseInfo couldn't supports demo.",
        RequestCode.DemoUnavailable
    )
    else "https://openapi.koreainvestment.com:9443/uapi/domestic-stock/v1/quotations/search-info"

    @Serializable
    data class ProductBaseInfoResponse(
        @SerialName("tr_id") override var tradeId: String?,
        @SerialName("tr_cont") override var tradeContinuous: String?,
        @SerialName("gt_uid") override var globalTradeID: String?,
        @SerialName("msg_cd") override val code: String?,
        @SerialName("msg1") override val msg: String?,
        @SerialName("rt_cd") @Serializable(with = ResultCodeSerializer::class) override val isOk: Boolean?,

        var output: ProductBaseInfoResponseOutput?, override var next: (suspend () -> Result<ProductBaseInfoResponse>)?
    ) : Response, TradeContinuousResponse<ProductBaseInfoResponse>, TradeIdMsg {
        @SerialName("error_description")
        override val errorDescription: String? = null

        @SerialName("error_code")
        override val errorCode: String? = null
    }

    @Serializable
    @Suppress("SpellCheckingInspection")
    data class ProductBaseInfoResponseOutput(
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

    data class ProductBaseInfoData(
        /** 조회할 상품 번호 */
        override val ticker: String,
        /** 조회할 상품 종류 */
        val type: ProductTypeCode,
        override var corp: CorporationRequest? = null, override var tradeContinuous: String? = ""
    ) : Data, TradeContinuousData, Ticker

    @Suppress("SpellCheckingInspection")
    override suspend fun call(data: ProductBaseInfoData) = request(data) {
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