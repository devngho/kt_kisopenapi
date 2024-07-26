@file:Suppress("SpellCheckingInspection")

package io.github.devngho.kisopenapi.requests.response.stock

import io.github.devngho.kisopenapi.requests.util.ProductTypeCode
import kotlinx.serialization.SerialName

/**
 * [KIS Developers 문서](https://apiportal.koreainvestment.com/apiservice/apiservice-domestic-stock-quotations)를 참조하세요.
 * @see io.github.devngho.kisopenapi.requests.common.InquireProductBaseInfo
 */
interface ProductInfo : BaseProductInfo {
    /** 상품 번호 */
    @SerialName("pdno")
    override val ticker: String?

    /** 상품 유형 코드 */
    @SerialName("prdt_type_cd")
    override val type: ProductTypeCode?

    /** 상품명 */
    @SerialName("prdt_name")
    override val name: String?

    /** 상품명 120 */
    @SerialName("prdt_name120")
    override val name120: String?

    /** 상품명 약어 */
    @SerialName("prdt_abrv_name")
    override val nameShort: String?

    /** 상품명 영문 */
    @SerialName("prdt_eng_name")
    override val nameEng: String?

    /** 상품명 영문 120 */
    @SerialName("prdt_eng_name120")
    override val nameEng120: String?

    /** 상품명 영문 약어 */
    @SerialName("prdt_eng_abrv_name")
    override val nameEngShort: String?

    /** 표준 상품 번호 */
    @SerialName("std_pdno")
    override val codeStandard: String?

    /** 상품 판매 상태 코드 */
    @SerialName("prdt_sale_stat_cd")
    val productSaleState: String?

    /** 상품 위험 등급 코드 */
    @SerialName("prdt_risk_grad_cd")
    val productRiskGrade: String?

    /** 상품 분류 코드 */
    @SerialName("prdt_clsf_cd")
    val productClassifier: String?

    /** 상품 분류명 */
    @SerialName("prdt_clsf_name")
    val productClassifierName: String?

    /** 판매 시작 일자 */
    @SerialName("sale_strt_dt")
    val saleStartDate: String?

    /** 판매 종료 일자 */
    @SerialName("sale_end_dt")
    val saleEndDate: String?

    /** 랩 어카운트 자산 유형 코드 */
    @SerialName("wrap_asst_type_cd")
    val wrapAssetType: String?

    /** 투자상품 유형 코드 */
    @SerialName("ivst_prdt_type_cd")
    val productInvestmentType: String?

    /** 투자상품 유형 코드명 */
    @SerialName("ivst_prdt_type_cd_name")
    val productInvestmentTypeName: String?

    /** 최초 등록 일자 */
    @SerialName("frst_erlm_date")
    val firstRegisterDate: String?
}