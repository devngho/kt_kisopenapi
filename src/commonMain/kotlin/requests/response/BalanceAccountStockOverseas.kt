package io.github.devngho.kisopenapi.requests.response

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.requests.Response
import io.github.devngho.kisopenapi.requests.util.LoanType
import kotlinx.serialization.Contextual
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * [KIS Developers 문서](https://apiportal.koreainvestment.com/apiservice/apiservice-overseas-stock)를 참조하세요.
 * @see io.github.devngho.kisopenapi.requests.InquireOverseasBalance
 */
interface BalanceAccountStockOverseas : Response, StockOverseasPriceBase, Ticker {
    @SerialName("ovrs_pdno")
    override val ticker: String?

    /** 상품명 */
    @SerialName("ovrs_item_name") val productName: String?

    /** 보유 수량 */
    @SerialName("ovrs_cblc_qty") @Contextual val count: BigInteger?

    /** 주문 가능 수량 */
    @SerialName("ord_psbl_qty") @Contextual val countCanOrder: BigInteger?

    /** 매입 평균가 */
    @SerialName("pchs_avg_pric") @Contextual val buyAveragePrice: BigDecimal?

    /** 외화 매입 금액1
     *
     * 이 상품의 외화 기준 매입 금액입니다.
     */
    @SerialName("frcr_pchs_amt1") @Contextual val buyAmountByForeignCurrency: BigDecimal?

    /** 현재가2 */
    @SerialName("now_pric2") @Contextual override val price: BigDecimal?

    /** 해외 주식 평가 금액
     *
     * 이 상품의 외화 기준 평가 금액입니다.
     */
    @SerialName("ovrs_stck_evlu_amt") @Contextual val evalAmount: BigDecimal?

    /** 외화 평가 손익 금액
     *
     * 매입 금액과 평가 금액을 외화 기준으로 비교한 손익입니다.
     */
    @SerialName("frcr_evlu_pfls_amt") @Contextual val evalProfitLossAmount: BigDecimal?

    /** 평가 손익률 */
    @SerialName("evlu_pfls_rt") @Contextual val evalProfitLossRate: BigDecimal?

    /** 대출 실행 일자 */
    @SerialName("loan_dt") val loanDate: String?

    /** 대출 유형 코드 */
    @OptIn(ExperimentalSerializationApi::class)
    @SerialName("loan_type_cd") @Serializable(with = LoanType.LoanTypeSerializer::class) val loanType: LoanType?

    /** 대출 만기 일자 */
    @SerialName("expd_dt") val expireDate: String?
}