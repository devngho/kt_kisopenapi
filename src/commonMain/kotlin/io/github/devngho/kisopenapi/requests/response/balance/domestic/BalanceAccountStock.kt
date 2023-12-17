package io.github.devngho.kisopenapi.requests.response.balance.domestic

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.requests.Response
import io.github.devngho.kisopenapi.requests.response.stock.Ticker
import io.github.devngho.kisopenapi.requests.response.stock.price.domestic.StockPriceBase
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName

/**
 * [KIS Developers 문서](https://apiportal.koreainvestment.com/apiservice/apiservice-domestic-stock)를 참조하세요.
 * @see io.github.devngho.kisopenapi.requests.domestic.inquire.InquireBalance
 */
interface BalanceAccountStock : Response, StockPriceBase, Ticker {
    /** 상품 번호 */
    @SerialName("pdno")
    override val ticker: String?

    /** 상품명 */
    @SerialName("prdt_name")
    val productName: String?

    /** 매매 구분명 */
    @SerialName("trad_dvsn_name")
    val buySellDivision: String?

    /** 전일 매수 수량 */
    @SerialName("bfdy_buy_qty")
    @Contextual
    val buyCountYesterday: BigInteger?

    /** 전일 매도 수량 */
    @SerialName("bfdy_sll_qty")
    @Contextual
    val sellCountYesterday: BigInteger?

    /** 당일 매수 수량 */
    @SerialName("thdt_buy_qty")
    @Contextual
    val buyCountToday: BigInteger?

    /** 당일 매도 수량 */
    @SerialName("thdt_sll_qty")
    @Contextual
    val sellCountToday: BigInteger?

    /** 보유 수량 */
    @SerialName("hldg_qty")
    @Contextual
    val count: BigInteger?

    /** 주문 가능 수량 */
    @SerialName("ord_psbl_qty")
    @Contextual
    val countCanOrder: BigInteger?

    /** 매입 평균가 */
    @SerialName("pchs_avg_pric")
    @Contextual
    val buyAveragePrice: BigDecimal?

    /** 매입 금액 */
    @SerialName("pchs_amt")
    @Contextual
    val buyAmount: BigInteger?

    /** 현재가 */
    @SerialName("prpr")
    @Contextual
    override val price: BigInteger?

    /** 평가 금액 */
    @SerialName("evlu_amt")
    @Contextual
    val evalAmount: BigInteger?

    /** 평가 손익 금액 */
    @SerialName("evlu_pfls_amt")
    @Contextual
    val evalProfitLossAmount: BigInteger?

    /** 평가 손익률 */
    @SerialName("evlu_pfls_rt")
    @Contextual
    val evalProfitLossRate: BigDecimal?

    /** 평가 수익률 */
    @SerialName("evlu_erng_rt")
    @Contextual
    val evalProfitRate: BigDecimal?

    /** 대출 실행 일자
     *
     * [io.github.devngho.kisopenapi.requests.domestic.inquire.InquireBalance]로 이를 조회할 때, [io.github.devngho.kisopenapi.requests.domestic.inquire.InquireBalance.InquireBalanceData.inquireDivision]를
     * [io.github.devngho.kisopenapi.requests.util.InquireDivisionCode.ByLoanDays]로 설정해야 조회할 수 있습니다.
     * */
    @SerialName("loan_dt")
    val loanDate: String?

    /** 대출 금액 */
    @SerialName("loan_amt")
    @Contextual
    val loanAmount: BigInteger?

    /** 대주 매각 금액 */
    @SerialName("stln_slng_chgs")
    @Contextual
    val amountShortSelling: BigInteger?

    /** 대출 만기 일자 */
    @SerialName("expd_dt")
    val expireDate: String?

    /** 등락률 */
    @SerialName("fltt_rt")
    @Contextual
    val changeRate: BigDecimal?

    /** 전일 대비 등락 금액 */
    @SerialName("bfdy_cprs_icdc")
    @Contextual
    val changeFromYesterday: BigInteger?

    /** 종목 증거금률명 */
    @SerialName("item_mgna_rt_name")
    val stockMarginRateName: String?

    /** 보증금률명 */
    @SerialName("grta_rt_name")
    val depositRateName: String?

    /** 대용 가격
     *
     * 증권매매 위탁 보증금으로서 현금 대신에 사용되는 가격입니다.
     */
    @SerialName("sbst_pric")
    @Contextual
    val substitutePrice: BigInteger?

    /** 주식 대출 단가 */
    @SerialName("stck_loan_unpr")
    @Contextual
    val stockLoanPrice: BigInteger?
}