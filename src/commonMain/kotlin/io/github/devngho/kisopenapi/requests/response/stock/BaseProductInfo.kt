@file:Suppress("SpellCheckingInspection")

package io.github.devngho.kisopenapi.requests.response.stock

import io.github.devngho.kisopenapi.requests.Response
import io.github.devngho.kisopenapi.requests.util.ProductTypeCode
import kotlinx.serialization.SerialName

/**
 * [KIS Developers 문서](https://apiportal.koreainvestment.com/apiservice/apiservice-domestic-stock-quotations)를 참조하세요.
 * @see io.github.devngho.kisopenapi.requests.common.InquireProductBaseInfo
 */
interface BaseProductInfo : Response, Ticker {
    /** 상품 번호 */
    @SerialName("pdno")
    override val ticker: String?

    /** 상품 유형 코드 */
    @SerialName("prdt_type_cd")
    val type: ProductTypeCode?

    /** 상품명 */
    @SerialName("prdt_name")
    val name: String?

    /** 상품명 120 */
    @SerialName("prdt_name120")
    val name120: String?

    /** 상품명 약어 */
    @SerialName("prdt_abrv_name")
    val nameShort: String?

    /** 상품명 영문 */
    @SerialName("prdt_eng_name")
    val nameEng: String?

    /** 상품명 영문 120 */
    @SerialName("prdt_eng_name120")
    val nameEng120: String?

    /** 상품명 영문 약어 */
    @SerialName("prdt_eng_abrv_name")
    val nameEngShort: String?

    /** 표준 상품 번호 */
    @SerialName("std_pdno")
    val codeStandard: String?
}