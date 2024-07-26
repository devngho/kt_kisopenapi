package io.github.devngho.kisopenapi.requests.response.stock.price.overseas

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.requests.util.Currency
import io.github.devngho.kisopenapi.requests.util.Date
import io.github.devngho.kisopenapi.requests.util.SignPrice
import io.github.devngho.kisopenapi.requests.util.YYYYMMDDSerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * [KIS Developers 문서](https://apiportal.koreainvestment.com/apiservice/apiservice-domestic-stock-current)를 참조하세요.
 * @see io.github.devngho.kisopenapi.requests.overseas.inquire.InquireOverseasPrice
 */
@Suppress("SpellCheckingInspection")
interface StockOverseasPriceFull : StockOverseasPrice {
    /** 전일 가격 */
    @SerialName("base")
    @Contextual
    val priceYesterday: BigDecimal?

    /** 전일 거래량 */
    @SerialName("pvol")
    @Contextual
    val tradeVolumeYesterday: BigDecimal?

    /** 시가 */
    @SerialName("open")
    @Contextual
    val openingPrice: BigDecimal?

    /** 고가 */
    @SerialName("high")
    @Contextual
    val highestPrice: BigDecimal?

    /** 저가 */
    @SerialName("low")
    @Contextual
    val lowestPrice: BigDecimal?

    /** 시가총액 */
    @SerialName("tomv")
    @Contextual
    val marketCap: BigDecimal?

    /** 전일 거래 대금 */
    @SerialName("pamt")
    @Contextual
    val tradePriceVolumeFromYesterday: BigDecimal?

    /** 상한가 */
    @SerialName("uplp")
    @Contextual
    val upperLimitPrice: BigDecimal?

    /** 하한가 */
    @SerialName("dnlp")
    @Contextual
    val lowerLimitPrice: BigDecimal?

    /** 52주 최고가 */
    @SerialName("h52p")
    @Contextual
    val highPriceW52: BigDecimal?

    /** 52주 최고일자 */
    @SerialName("h52d")
    @Serializable(with = YYYYMMDDSerializer::class)
    val highPriceDateW52: Date?

    /** 52주 최저가 */
    @SerialName("l52p")
    @Contextual
    val lowPriceW52: BigDecimal?

    /** 52주 최저일자 */
    @SerialName("l52d")
    @Serializable(with = YYYYMMDDSerializer::class)
    val lowPriceDateW52: Date?

    /** PER */
    @SerialName("perx")
    @Contextual
    val per: BigDecimal?

    /** PBR */
    @SerialName("pbrx")
    @Contextual
    val pbr: BigDecimal?

    /** EPS */
    @SerialName("epsx")
    @Contextual
    val eps: BigDecimal?

    /** BPS */
    @SerialName("bpsx")
    @Contextual
    val bps: BigDecimal?

    /** 상장주수 */
    @SerialName("shar")
    @Contextual
    val listedStockCount: BigInteger?

    /** 자본금 */
    @SerialName("mcap")
    @Contextual
    val capitalFinance: BigDecimal?

    /** 통화 */
    @SerialName("curr")
    val currency: Currency?

    /** 매매 단위 */
    @SerialName("vnit")
    @Contextual
    val tradeUnit: BigInteger?

    /** 원환산 당일 가격 */
    @SerialName("t_xprc")
    @Contextual
    val priceKRW: BigDecimal?

    /** 원환산 전일 대비 변동 */
    @SerialName("t_xdif")
    @Contextual
    val changeKRW: BigDecimal?

    /** 원환산 전일 대비 등락률 */
    @SerialName("t_xrat")
    @Contextual
    val rateKRW: BigDecimal?

    /** 원환산 전일 대비 부호 */
    @SerialName("t_xsgn")
    val signKRW: SignPrice?

    /** 원환산 전일 가격 */
    @SerialName("p_xprc")
    @Contextual
    val priceKRWYesterday: BigDecimal?

    /** 원환산 전일의 전을 대비 변동 */
    @SerialName("p_xdif")
    @Contextual
    val changeYesterdayKRW: BigDecimal?

    /** 원환산 전일의 전일 대비 등락률 */
    @SerialName("p_xrat")
    @Contextual
    val rateYesterdayKRW: BigDecimal?

    /** 원환산 전일의 전일 대비 부호 */
    @SerialName("p_xsng")
    val signYesterdayKRW: SignPrice?

    /** 당일 환율 */
    @SerialName("t_rate")
    @Contextual
    val exchangeRate: BigDecimal?

    /** 전일 환율 */
    @SerialName("p_rate")
    @Contextual
    val exchangeRateYesterday: BigDecimal?

    /** 거래 가능 여부, True/False나 Y/N이 아니므로 주의하시기 바랍니다. */
    @SerialName("e_ordyn")
    val canOrder: String?

    /** 호가 단위 */
    @SerialName("e_hogau")
    @Contextual
    val askingPriceUnit: BigDecimal?

    /** 업종(섹터) */
    @SerialName("e_icod")
    val sectorName: String?

    /** 액면가 */
    @SerialName("e_parp")
    @Contextual
    val facePrice: BigDecimal?

    /** ETP 분류명 */
    @SerialName("etyp_nm")
    val etpClassName: String?
}