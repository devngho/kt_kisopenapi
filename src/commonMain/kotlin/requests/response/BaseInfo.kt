package io.github.devngho.kisopenapi.requests.response

import io.github.devngho.kisopenapi.requests.Response
import io.github.devngho.kisopenapi.requests.util.ProductTypeCode
import kotlinx.serialization.SerialName

interface BaseInfo: Response {
    @SerialName("pdno") val code: String?
    @SerialName("prdt_type_cd") val type: ProductTypeCode?
    @SerialName("prdt_name") val name: String?
    @SerialName("prdt_name120") val name120: String?
    @SerialName("prdt_abrv_name") val nameShort: String?
    @SerialName("prdt_eng_name") val nameEng: String?
    @SerialName("prdt_eng_name120") val nameEng120: String?
    @SerialName("prdt_eng_abrv_name") val nameEngShort: String?
    @SerialName("std_pdno") val codeStandard: String?
    @SerialName("shtn_pdno") val codeShort: String?
    @SerialName("prdt_sale_stat_cd") val productSaleState: String?
    @SerialName("prdt_risk_grad_cd") val productRiskGrade: String?
    @SerialName("prdt_clsf_cd") val productClassifier: String?
    @SerialName("prdt_clsf_name") val productClassifierName: String?
    @SerialName("sale_strt_dt") val saleStartDate: String?
    @SerialName("sale_end_dt") val saleEndDate: String?
    @SerialName("wrap_asst_type_cd") val wrapAssetType: String?
    @SerialName("ivst_prdt_type_cd") val productInvestmentType: String?
    @SerialName("ivst_prdt_type_cd_name") val productInvestmentTypeName: String?
    @SerialName("frst_erlm_date") val firstRegisterDate: String?
}