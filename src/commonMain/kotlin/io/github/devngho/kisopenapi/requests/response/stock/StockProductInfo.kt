@file:Suppress("SpellCheckingInspection")

package io.github.devngho.kisopenapi.requests.response.stock

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.Updatable
import io.github.devngho.kisopenapi.requests.util.Date
import io.github.devngho.kisopenapi.requests.util.ProductTypeCode
import io.github.devngho.kisopenapi.requests.util.YNSerializer
import io.github.devngho.kisopenapi.requests.util.YYYYMMDDSerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * [KIS Developers 문서](https://apiportal.koreainvestment.com/apiservice/apiservice-domestic-stock-quotations)를 참조하세요.
 * @see io.github.devngho.kisopenapi.requests.common.InquireProductBaseInfo
 */
@Updatable
interface StockProductInfo : ProductInfo, StockInfo {
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

    /** 단축 상품 번호 */
    @SerialName("shtn_pdno")
    override val codeShort: String?

    /** 시장 ID 코드 */
    @SerialName("mket_id_cd")
    override val marketId: String?

    /** 증권 그룹 ID 코드 */
    @SerialName("scty_grp_id_cd")
    override val stockGroupId: String?

    /** 거래소 구분 코드 */
    @SerialName("excg_dvsn_cd")
    override val exchangeDivision: String?

    /** 결산 월일 */
    @SerialName("setl_mmdd")
    @Contextual
    override val settlementMonth: Int?

    /** 상장주수 */
    @SerialName("lstg_stqt")
    @Contextual
    override val listedStockCount: BigInteger?

    /** 상장 자본 금액 */
    @SerialName("lstg_cptl_amt")
    @Contextual
    override val listedCapital: BigInteger?

    /** 자본금 */
    @SerialName("cpta")
    @Contextual
    override val capitalFinance: BigInteger?

    /** 액면가 */
    @SerialName("papr")
    @Contextual
    override val facePrice: BigInteger?

    /** 발행가 */
    @SerialName("issu_pric")
    @Contextual
    override val issuedPrice: BigInteger?

    /** 코스피 200 종목 여부 */
    @SerialName("kospi200_item_yn")
    @Serializable(with = YNSerializer::class)
    override val isInKospi200: Boolean?

    /** 유가증권 시장 상장 일자 */
    @SerialName("scts_mket_lstg_dt")
    @Serializable(with = YYYYMMDDSerializer::class)
    override val securitiesListedDate: Date?

    /** 유가증권 시장 상장폐지 일자 */
    @SerialName("scts_mket_lstg_abol_dt")
    @Serializable(with = YYYYMMDDSerializer::class)
    override val securitiesDelistedDate: Date?

    /** 코스닥 시장 상장 일자 */
    @SerialName("kosdaq_mket_lstg_dt")
    @Serializable(with = YYYYMMDDSerializer::class)
    override val kosdaqListedDate: Date?

    /** 코스닥 시장 상장폐지 일자 */
    @SerialName("kosdaq_mket_lstg_abol_dt")
    @Serializable(with = YYYYMMDDSerializer::class)
    override val kosdaqDelistedDate: Date?

    /** 프리보드 시장 상장 일자 */
    @SerialName("frbd_mket_lstg_dt")
    @Serializable(with = YYYYMMDDSerializer::class)
    override val freeboardListedDate: Date?

    /** 프리보드 시장 상장폐지 일자 */
    @SerialName("frbd_mket_lstg_abol_dt")
    @Serializable(with = YYYYMMDDSerializer::class)
    override val freeboardDelistedDate: Date?

    /** 리츠 종류 코드 */
    @SerialName("reits_kind_cd")
    override val reitsKindCode: String?

    /** ETF 구분 코드 */
    @SerialName("etf_dvsn_cd")
    override val etfDivisionCode: String?

    /** 유전 펀드 여부 */
    @SerialName("oilf_fund_yn")
    @Serializable(with = YNSerializer::class)
    override val isOilFund: Boolean?

    /** 지수 업종 대분류 코드 */
    @SerialName("idx_bztp_lcls_cd")
    override val indexSectorLargeClassCode: String?

    /** 지수 업종 중분류 코드 */
    @SerialName("idx_bztp_mcls_cd")
    override val indexSectorMiddleClassCode: String?

    /** 지수 업종 소분류 코드 */
    @SerialName("idx_bztp_scls_cd")
    override val indexSectorSmallClassCode: String?

    /** 지수 업종 대분류 코드명 */
    @SerialName("idx_bztp_lcls_cd_name")
    override val indexSectorLargeClassName: String?

    /** 지수 업종 중분류 코드명 */
    @SerialName("idx_bztp_mcls_cd_name")
    override val indexSectorMiddleClassName: String?

    /** 지수 업종 소분류 코드명 */
    @SerialName("idx_bztp_scls_cd_name")
    override val indexSectorSmallClassName: String?

    /** 주식 종류 코드 */
    @SerialName("stck_kind_cd")
    override val stockKindCode: String?

    /** 뮤추얼 펀드 개시 일자 */
    @SerialName("mfnd_opng_dt")
    @Serializable(with = YYYYMMDDSerializer::class)
    override val mutualFundOpeningDate: Date?

    /** 뮤추얼 펀드 종료 일자 */
    @SerialName("mfnd_end_dt")
    @Serializable(with = YYYYMMDDSerializer::class)
    override val mutualFundEndDate: Date?

    /** 예탁 등록 취소 일자 */
    @SerialName("dpsi_erlm_cncl_dt")
    @Serializable(with = YYYYMMDDSerializer::class)
    override val depositaryShareExpirationDate: Date?

    /** ETF CU 수량 */
    @SerialName("etf_cu_qty")
    @Contextual
    override val etfcuCount: BigInteger?

    /** ETF 과세 유형 코드 */
    @SerialName("etf_txtn_type_cd")
    override val etfTaxTypeCode: String?

    /** ETF 유형 코드 */
    @SerialName("etf_type_cd")
    override val etfTypeCode: String?

    /** 상장 폐지 일자 */
    @SerialName("lstg_abol_dt")
    @Serializable(with = YYYYMMDDSerializer::class)
    override val delistedDate: Date?

    /** 신주 구주 구분 코드 */
    @SerialName("nwst_odst_dvsn_cd")
    override val newOldDivisionCode: String?

    /** 대용 가격 */
    @SerialName("sbst_pric")
    @Contextual
    override val substitutePrice: BigInteger?

    /** 당사 대용 가격 */
    @SerialName("thco_sbst_pric")
    @Contextual
    override val kisSubstitutePrice: BigInteger?

    /** 당사 대용 가격 변경 일자 */
    @SerialName("thco_sbst_pric_chng_dt")
    @Serializable(with = YYYYMMDDSerializer::class)
    override val kisSubstitutePriceChangeDate: Date?

    /** 거래 정지 여부 */
    @SerialName("tr_stop_yn")
    @Serializable(with = YNSerializer::class)
    override val isTradeStopped: Boolean?

    /** 관리 종목 여부 */
    @SerialName("admn_item_yn")
    @Serializable(with = YNSerializer::class)
    override val isManaged: Boolean?

    /** 전일 종가 */
    @SerialName("bfdy_clpr")
    @Contextual
    override val priceYesterday: BigInteger?

    /** 종가 변경 일자 */
    @SerialName("clpr_chng_dt")
    @Serializable(with = YYYYMMDDSerializer::class)
    override val closePriceChangeDate: Date?

    /** 표준 산업 분류 코드 */
    @SerialName("std_idst_clsf_cd")
    override val standardIndustryClassCode: String?

    /** 표준 산업 분류 코드명 */
    @SerialName("std_idst_clsf_cd_name")
    override val standardIndustryClassName: String?

    /** OCR 번호 */
    @SerialName("ocr_no")
    override val ocrNo: String?

    /** 크라우드 펀딩 종목 여부 */
    @SerialName("crfd_item_yn")
    @Serializable(with = YNSerializer::class)
    override val isCrowdFunding: Boolean?

    /** 전자 증권 여부 */
    @SerialName("elec_scty_yn")
    @Serializable(with = YNSerializer::class)
    override val isElectricSecurities: Boolean?

    /** 발행 기관 코드 */
    @SerialName("issu_istt_cd")
    override val issueInstitutionCode: String?

    /** ETF 추적 수익률 배수 */
    @SerialName("etf_chas_erng_rt_dbnb")
    @Contextual
    override val etfCashEarningRate: BigDecimal?

    /** ETF ETN 투자 유의 종목 여부 */
    @SerialName("etf_etn_ivst_heed_item_yn")
    @Serializable(with = YNSerializer::class)
    override val isEtfEtnInvestmentAlerted: Boolean?

    /** 대주이자율 구분 코드 */
    @SerialName("stln_int_rt_dvsn_cd")
    override val settlementInterestRateClassCode: String?

    /** 외국인 개인 한도 비율 */
    @SerialName("frnr_psnl_lmt_rt")
    @Contextual
    override val foreignerPersonalLimitRate: BigDecimal?

    /** 상장 신청인 발행 기관 코드 */
    @SerialName("lstg_rqsr_issu_istt_cd")
    override val listingRequestIssueInstitutionCode: String?

    /** 상장 신청인 종목 코드 */
    @SerialName("lstg_rqsr_item_cd")
    override val listingRequestItemCode: String?

    /** 신탁 기관 발행 기관 코드 */
    @SerialName("trst_istt_issu_istt_cd")
    override val trustInstitutionIssueInstitutionCode: String?

    /** 상품 판매 상태 코드 */
    @SerialName("prdt_sale_stat_cd")
    override val productSaleState: String?

    /** 상품 위험 등급 코드 */
    @SerialName("prdt_risk_grad_cd")
    override val productRiskGrade: String?

    /** 상품 분류 코드 */
    @SerialName("prdt_clsf_cd")
    override val productClassifier: String?

    /** 상품 분류명 */
    @SerialName("prdt_clsf_name")
    override val productClassifierName: String?

    /** 판매 시작 일자 */
    @SerialName("sale_strt_dt")
    override val saleStartDate: String?

    /** 판매 종료 일자 */
    @SerialName("sale_end_dt")
    override val saleEndDate: String?

    /** 랩 어카운트 자산 유형 코드 */
    @SerialName("wrap_asst_type_cd")
    override val wrapAssetType: String?

    /** 투자상품 유형 코드 */
    @SerialName("ivst_prdt_type_cd")
    override val productInvestmentType: String?

    /** 투자상품 유형 코드명 */
    @SerialName("ivst_prdt_type_cd_name")
    override val productInvestmentTypeName: String?

    /** 최초 등록 일자 */
    @SerialName("frst_erlm_date")
    override val firstRegisterDate: String?
}