package io.github.devngho.kisopenapi.requests.response.stock.price.domestic

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.requests.Response
import io.github.devngho.kisopenapi.requests.response.stock.Ticker
import io.github.devngho.kisopenapi.requests.util.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * [KIS Developers 문서](https://apiportal.koreainvestment.com/apiservice/apiservice-domestic-stock-quotations)를 참조하세요.
 * @see io.github.devngho.kisopenapi.requests.domestic.inquire.InquirePrice
 */
@Suppress("SpellCheckingInspection")
interface StockPriceFull : StockPrice, StockPriceForeigner, StockPriceChange, Response, Ticker {
    /** 종목 상태 코드 */
    @SerialName("iscd_stat_cls_code")
    val stockState: StockState?

    /** 증거금 비율 */
    @SerialName("marg_rate")
    @Contextual
    val marginRate: BigDecimal?

    /** 대표 시장 한국명 */
    @SerialName("rprs_mrkt_kor_name")
    val marketName: String?

    /** 신 고가/저가 구분 코드
     *
     * 종목이 신고가/저가를 기록하면 조회됩니다.
     */
    @SerialName("new_hgpr_lwpr_cls_code")
    @Contextual
    val newHighLowCode: String?

    /** 업종 한글명 */
    @SerialName("bstp_kor_isnm")
    val sectorName: String?

    /** 임시 정지 여부 */
    @SerialName("temp_stop_yn")
    @Serializable(with = YNSerializer::class)
    val isTradeTemporarilyStopped: Boolean?

    /** 시가 범위 연장 여부 */
    @SerialName("oprc_rang_cont_yn")
    @Serializable(with = YNSerializer::class)
    val marketPriceRangeExtended: Boolean?

    /** 종가 범위 연장 여부 */
    @SerialName("clpr_rang_cont_yn")
    @Serializable(with = YNSerializer::class)
    val endPriceRangeExtended: Boolean?

    /** 신용 거래 가능 여부 */
    @SerialName("crdt_able_yn")
    @Serializable(with = YNSerializer::class)
    val canTradeCredit: Boolean?

    /** ELW 발행 가능 여부 */
    @SerialName("elw_pblc_yn")
    @Serializable(with = YNSerializer::class)
    val canPublishElw: Boolean?

    /** 기준가 */
    @SerialName("stck_sdpr")
    @Contextual
    val criteriaPrice: BigInteger?

    /** 가중 평균 주식 가격 */
    @SerialName("wghn_avrg_stck_prc")
    @Contextual
    val weightedAverageStockPrice: BigDecimal?

    @SerialName("hts_frgn_ehrt")
    @Contextual
    override val htsForeignerExhaustionRate: BigDecimal?

    @SerialName("frgn_ntby_qty")
    @Contextual
    override val foreignerNetBuyCount: BigInteger?

    /** 프로그램 매매 순매수 수량 */
    @SerialName("pgtr_ntby_qty")
    @Contextual
    val programNetBuyCount: BigInteger?

    /** 피벗 2차 디저항 가격(직원용 데이터) */
    @SerialName("pvt_scnd_dmrs_prc")
    @Contextual
    val pivotSecondResistancePrice: BigInteger?

    /** 피벗 1차 디저항 가격(직원용 데이터) */
    @SerialName("pvt_frst_dmrs_prc")
    @Contextual
    val pivotFirstResistancePrice: BigInteger?

    /** 피벗 포인트 값(직원용 데이터) */
    @SerialName("pvt_pont_val")
    @Contextual
    val pivotPointValue: BigInteger?

    /** 피벗 1차 디지지 가격(직원용 데이터) */
    @SerialName("pvt_frst_dmsp_prc")
    @Contextual
    val pivotFirstBackingPrice: BigInteger?

    /** 피벗 2차 디지지 가격(직원용 데이터) */
    @SerialName("pvt_scnd_dmsp_prc")
    @Contextual
    val pivotSecondBackingPrice: BigInteger?

    /** 디저항 값(직원용 데이터) */
    @SerialName("dmrs_val")
    @Contextual
    val resistanceValue: BigInteger?

    /** 디지지 값(직원용 데이터) */
    @SerialName("dmsp_val")
    @Contextual
    val backingValue: BigInteger?

    /** 자본금 */
    @SerialName("cpfn")
    @Contextual
    val capitalFinance: BigInteger?

    /** 제한폭 가격 */
    @SerialName("rstc_wdth_prc")
    @Contextual
    val restrictedWidthPrice: BigInteger?

    /** 액면가 */
    @SerialName("stck_fcam")
    @Contextual
    val facePrice: BigDecimal?

    /** 대용가 */
    @SerialName("stck_sspr")
    @Contextual
    val substitutePrice: BigInteger?

    /** 호가 단위 */
    @SerialName("aspr_unit")
    @Contextual
    val askingPriceUnit: BigInteger?

    /** HTS 매매 수량 단위 */
    @SerialName("hts_deal_qty_unit_val")
    @Contextual
    val htsTradeCountUnit: BigInteger?

    /** 총 상장 주식 수 */
    @SerialName("lstn_stcn")
    @Contextual
    val listedStockCount: BigInteger?

    /** HTS 시가 총액 */
    @SerialName("hts_avls")
    @Contextual
    val htsMarketCap: BigInteger?

    /** PER */
    @SerialName("per")
    @Contextual
    val per: BigDecimal?

    /** PBR */
    @SerialName("pbr")
    @Contextual
    val pbr: BigDecimal?

    /** 결산 월 */
    @SerialName("stac_month")
    @Contextual
    val settlementMonth: Int?

    /** EPS */
    @SerialName("eps")
    @Contextual
    val eps: BigDecimal?

    /** BPS */
    @SerialName("bps")
    @Contextual
    val bps: BigDecimal?

    /** 250일 최고가 */
    @SerialName("d250_hgpr")
    @Contextual
    val highestPriceD250: BigInteger?

    /** 250일 최고가 날짜 */
    @Serializable(with = YYYYMMDDSerializer::class)
    @SerialName("d250_hgpr_date")
    val highestPriceDateD250: Date?

    /** 250일 최고가 대비 현재가 비율 */
    @SerialName("d250_hgpr_vrss_prpr_rate")
    @Contextual
    val highestPriceRateD250: BigDecimal?

    /** 250일 최저가 */
    @SerialName("d250_lwpr")
    @Contextual
    val lowestPriceD250: BigInteger?

    /** 250일 최저가 날짜 */
    @Serializable(with = YYYYMMDDSerializer::class)
    @SerialName("d250_lwpr_date")
    val lowestPriceDateD250: Date?

    /** 250일 최저가 대비 현재가 비율 */
    @SerialName("d250_lwpr_vrss_prpr_rate")
    @Contextual
    val lowestPriceRateD250: BigDecimal?

    /** 연중 최고가 */
    @SerialName("stck_dryy_hgpr")
    @Contextual
    val highestPriceInYear: BigInteger?

    /** 연중 최고가 날짜 */
    @Serializable(with = YYYYMMDDSerializer::class)
    @SerialName("dryy_hgpr_date")
    val highestPriceDateInYear: Date?

    /** 연중 최고가 대비 현재가 비율 */
    @SerialName("dryy_hgpr_vrss_prpr_rate")
    @Contextual
    val highestPriceRateInYear: BigDecimal?

    /** 연중 최저가 */
    @SerialName("stck_dryy_lwpr")
    @Contextual
    val lowestPriceInYear: BigInteger?

    /** 연중 최저가 날짜 */
    @Serializable(with = YYYYMMDDSerializer::class)
    @SerialName("dryy_lwpr_date")
    val lowestPriceDateInYear: Date?

    /** 연중 최저가 대비 현재가 비율 */
    @SerialName("dryy_lwpr_vrss_prpr_rate")
    @Contextual
    val lowestPriceRateInYear: BigDecimal?

    /** 52주 최고가 */
    @SerialName("w52_hgpr")
    @Contextual
    val highestPriceW52: BigInteger?

    /** 52주 최고가 날짜 */
    @Serializable(with = YYYYMMDDSerializer::class)
    @SerialName("w52_hgpr_date")
    val highestPriceDateW52: Date?

    /** 52주 최고가 대비 현재가 비율 */
    @SerialName("w52_hgpr_vrss_prpr_ctrt")
    @Contextual
    val highestPriceRateW52: BigDecimal?

    /** 52주 최저가 */
    @SerialName("w52_lwpr")
    @Contextual
    val lowestPriceW52: BigInteger?

    /** 52주 최저가 날짜 */
    @Serializable(with = YYYYMMDDSerializer::class)
    @SerialName("w52_lwpr_date")
    val lowestPriceDateW52: Date?

    /** 52주 최저가 대비 현재가 비율 */
    @SerialName("w52_lwpr_vrss_prpr_ctrt")
    @Contextual
    val lowestPriceRateW52: BigDecimal?

    /** 전체 융자 잔고 비율 */
    @SerialName("whol_loan_rmnd_rate")
    @Contextual
    val totalLoanBalanceRate: BigDecimal?

    /** 공매도 가능 여부 */
    @SerialName("ssts_yn")
    @Serializable(with = YNSerializer::class)
    val canShortSell: Boolean?

    /** 상품 번호 */
    @SerialName("stck_shrn_iscd")
    override val ticker: String?

    /** 액면가 통화명 */
    @SerialName("fcam_cnnm")
    val facePriceCurrencyName: String?

    /** 자본금 통화명 */
    @SerialName("cpfn_cnnm")
    val capitalFinanceCurrencyName: String?

    /** 접근도 */
    @SerialName("apprch_rate")
    @Contextual
    val approachRate: BigDecimal?

    /** 외국인 보유 수량 */
    @SerialName("frgn_hldn_qty")
    @Contextual
    val foreignerHoldingCount: BigInteger?

    /** VI 적용 구분 코드 */
    @SerialName("vi_cls_code")
    val viCode: String?

    /** 시간외 단일가 VI 적용 구분 코드 */
    @SerialName("otvm_vi_cls_code")
    val viCodeOvertime: String?

    /** 최종 공매도 체결 수량 */
    @SerialName("last_ssts_cntg_qty")
    @Contextual
    val shortSellingLastConfirmedTradeCount: BigInteger?

    /** 투자 유의 여부 */
    @SerialName("invt_caful_yn")
    @Serializable(with = YNSerializer::class)
    val investmentCareful: Boolean?

    /** 시장 경고 코드 */
    @SerialName("mrkt_warn_cls_code")
    val marketWarningCode: MarketWarnCode?

    /** 단기 과열 여부 */
    @SerialName("short_over_yn")
    @Serializable(with = YNSerializer::class)
    val isShortOver: Boolean?

    /** 정리매매 여부 */
    @SerialName("sltr_yn")
    @Serializable(with = YNSerializer::class)
    val settlement: Boolean?
}