package io.github.devngho.kisopenapi.requests

import io.github.devngho.kisopenapi.KisOpenApi
import io.github.devngho.kisopenapi.requests.response.*
import io.github.devngho.kisopenapi.requests.util.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class ProductBaseInfo(override val client: KisOpenApi):
    DataRequest<ProductBaseInfo.ProductBaseInfoData, ProductBaseInfo.ProductBaseInfoResponse> {
    private val url = if (client.isDemo) throw DemoError("ProductBaseInfo couldn't supports demo.")
    else                                 "https://openapi.koreainvestment.com:9443/uapi/domestic-stock/v1/quotations/search-info"

    @Serializable
    data class ProductBaseInfoResponse(
        @SerialName("tr_id") override var tradeId: String?,
        @SerialName("tr_cont") override var tradeContinuous: String?,
        @SerialName("gt_uid") override var globalTradeID: String?,
        @SerialName("msg_cd") override val code: String?,
        @SerialName("msg1") override val msg: String?,

        var output: ProductBaseInfoResponseOutput?, override var next: (suspend () -> Response)?
    ): Response, TradeContinuousResponse, Msg {
        override val error_description: String? = null
        override val error_code: String? = null
    }

    @Serializable
    data class ProductBaseInfoResponseOutput(
        @SerialName("pdno") override val code: String?,
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
    ): BaseInfo {
        override val error_description: String? = null
        override val error_code: String? = null
    }

    data class ProductBaseInfoData(val code: String, val type: ProductTypeCode,
                                   override var corp: CorporationRequest? = null, override val tradeContinuous: String? = ""): Data,
        TradeContinuousData

    override suspend fun call(data: ProductBaseInfoData): ProductBaseInfoResponse {
        if (data.corp == null) data.corp = client.corp

        val res = client.httpClient.get(url) {
            auth(client)
            tradeId("CTPF1604R")
            data.corp?.let { corporation(it) }
            url {
                parameters.run {
                    set("PDNO", data.code)
                    set("PRDT_TYPE_CD", data.type.num)
                }
            }
        }
        return res.body<ProductBaseInfoResponse>().apply {
            if (this.error_code != null) throw RequestError(this.error_description)

            res.headers.forEach { s, strings ->
                when(s) {
                    "tr_id" -> this.tradeId = strings[0]
                    "tr_cont" -> this.tradeContinuous = strings[0]
                    "gt_uid" -> this.globalTradeID = strings[0]
                }
            }

            if (this.tradeContinuous == "F" || this.tradeContinuous == "M") {
                this.next = {
                    call(data.copy(tradeContinuous = "N"))
                }
            }
        }
    }
}