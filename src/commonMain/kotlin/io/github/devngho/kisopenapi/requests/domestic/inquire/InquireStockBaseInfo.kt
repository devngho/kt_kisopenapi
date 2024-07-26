package io.github.devngho.kisopenapi.requests.domestic.inquire

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.KISApiClient
import io.github.devngho.kisopenapi.requests.Data
import io.github.devngho.kisopenapi.requests.DataRequest
import io.github.devngho.kisopenapi.requests.Response
import io.github.devngho.kisopenapi.requests.data.CorporationRequest
import io.github.devngho.kisopenapi.requests.data.TradeContinuousData
import io.github.devngho.kisopenapi.requests.data.TradeContinuousResponse
import io.github.devngho.kisopenapi.requests.data.TradeIdMsg
import io.github.devngho.kisopenapi.requests.response.stock.StockInfo
import io.github.devngho.kisopenapi.requests.response.stock.Ticker
import io.github.devngho.kisopenapi.requests.response.stock.price.domestic.StockPriceBase
import io.github.devngho.kisopenapi.requests.util.*
import io.github.devngho.kisopenapi.requests.util.RequestException.Companion.throwIfClientIsDemo
import io.ktor.client.request.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 국내주식의 기본 정보를 조회하고 반환합니다.
 */
@DemoNotSupported
class InquireStockBaseInfo(override val client: KISApiClient) :
    DataRequest<InquireStockBaseInfo.InquireProductBaseInfoData, InquireStockBaseInfo.InquireProductBaseInfoResponse> {
    private val url = "${client.options.baseUrl}/uapi/domestic-stock/v1/quotations/search-stock-info"

    @Serializable
    data class InquireProductBaseInfoResponse(
        @SerialName("tr_id") override var tradeId: String?,
        @SerialName("tr_cont") override var tradeContinuous: String?,
        @SerialName("gt_uid") override var globalTradeID: String?,
        @SerialName("msg_cd") override val code: String?,
        @SerialName("msg1") override val msg: String?,
        @SerialName("rt_cd") @Serializable(with = ResultCodeSerializer::class) override val isOk: Boolean?,

        var output: InquireProductBaseInfoResponseOutput?,
        override var next: (suspend () -> Result<InquireProductBaseInfoResponse>)?
    ) : Response, TradeContinuousResponse<InquireProductBaseInfoResponse>, TradeIdMsg {
        @SerialName("error_description")
        override val errorDescription: String? = null

        @SerialName("error_code")
        override val errorCode: String? = null
    }

    @Serializable
    @Suppress("SpellCheckingInspection")
    data class InquireProductBaseInfoResponseOutput(
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
        @SerialName("mket_id_cd") override val marketId: String?,
        @SerialName("scty_grp_id_cd") override val stockGroupId: String?,
        @SerialName("excg_dvsn_cd") override val exchangeDivision: String?,
        @SerialName("setl_mmdd") @Contextual override val settlementMonth: Int?,
        @SerialName("lstg_stqt") @Contextual override val listedStockCount: BigInteger?,
        @SerialName("lstg_cptl_amt") @Contextual override val listedCapital: BigInteger?,
        @SerialName("cpta") @Contextual override val capitalFinance: BigInteger?,
        @SerialName("papr") @Contextual override val facePrice: BigInteger?,
        @SerialName("issu_pric") @Contextual override val issuedPrice: BigInteger?,
        @SerialName("kospi200_item_yn") @Serializable(with = YNSerializer::class) override val isInKospi200: Boolean?,
        @SerialName("scts_mket_lstg_dt") @Serializable(with = YYYYMMDDSerializer::class) override val securitiesListedDate: Date?,
        @SerialName("scts_mket_lstg_abol_dt") @Serializable(with = YYYYMMDDSerializer::class) override val securitiesDelistedDate: Date?,
        @SerialName("kosdaq_mket_lstg_dt") @Serializable(with = YYYYMMDDSerializer::class) override val kosdaqListedDate: Date?,
        @SerialName("kosdaq_mket_lstg_abol_dt") @Serializable(with = YYYYMMDDSerializer::class) override val kosdaqDelistedDate: Date?,
        @SerialName("frbd_mket_lstg_dt") @Serializable(with = YYYYMMDDSerializer::class) override val freeboardListedDate: Date?,
        @SerialName("frbd_mket_lstg_abol_dt") @Serializable(with = YYYYMMDDSerializer::class) override val freeboardDelistedDate: Date?,
        @SerialName("reits_kind_cd") override val reitsKindCode: String?,
        @SerialName("etf_dvsn_cd") override val etfDivisionCode: String?,
        @SerialName("oilf_fund_yn") @Serializable(with = YNSerializer::class) override val isOilFund: Boolean?,
        @SerialName("idx_bztp_lcls_cd") override val indexSectorLargeClassCode: String?,
        @SerialName("idx_bztp_mcls_cd") override val indexSectorMiddleClassCode: String?,
        @SerialName("idx_bztp_scls_cd") override val indexSectorSmallClassCode: String?,
        @SerialName("idx_bztp_lcls_cd_name") override val indexSectorLargeClassName: String?,
        @SerialName("idx_bztp_mcls_cd_name") override val indexSectorMiddleClassName: String?,
        @SerialName("idx_bztp_scls_cd_name") override val indexSectorSmallClassName: String?,
        @SerialName("stck_kind_cd") override val stockKindCode: String?,
        @SerialName("mfnd_opng_dt") @Serializable(with = YYYYMMDDSerializer::class) override val mutualFundOpeningDate: Date?,
        @SerialName("mfnd_end_dt") @Serializable(with = YYYYMMDDSerializer::class) override val mutualFundEndDate: Date?,
        @SerialName("dpsi_erlm_cncl_dt") @Serializable(with = YYYYMMDDSerializer::class) override val depositaryShareExpirationDate: Date?,
        @SerialName("etf_cu_qty") @Contextual override val etfcuCount: BigInteger?,
        @SerialName("etf_txtn_type_cd") override val etfTaxTypeCode: String?,
        @SerialName("etf_type_cd") override val etfTypeCode: String?,
        @SerialName("lstg_abol_dt") @Serializable(with = YYYYMMDDSerializer::class) override val delistedDate: Date?,
        @SerialName("nwst_odst_dvsn_cd") override val newOldDivisionCode: String?,
        @SerialName("sbst_pric") @Contextual override val substitutePrice: BigInteger?,
        @SerialName("thco_sbst_pric") @Contextual override val kisSubstitutePrice: BigInteger?,
        @SerialName("thco_sbst_pric_chng_dt") @Serializable(with = YYYYMMDDSerializer::class) override val kisSubstitutePriceChangeDate: Date?,
        @SerialName("tr_stop_yn") @Serializable(with = YNSerializer::class) override val isTradeStopped: Boolean?,
        @SerialName("admn_item_yn") @Serializable(with = YNSerializer::class) override val isManaged: Boolean?,
        @SerialName("thdt_clpr") @Contextual override val price: BigInteger?,
        @SerialName("bfdy_clpr") @Contextual override val priceYesterday: BigInteger?,
        @SerialName("clpr_chng_dt") @Serializable(with = YYYYMMDDSerializer::class) override val closePriceChangeDate: Date?,
        @SerialName("std_idst_clsf_cd") override val standardIndustryClassCode: String?,
        @SerialName("std_idst_clsf_cd_name") override val standardIndustryClassName: String?,
        @SerialName("ocr_no") override val ocrNo: String?,
        @SerialName("crfd_item_yn") @Serializable(with = YNSerializer::class) override val isCrowdFunding: Boolean?,
        @SerialName("elec_scty_yn") @Serializable(with = YNSerializer::class) override val isElectricSecurities: Boolean?,
        @SerialName("issu_istt_cd") override val issueInstitutionCode: String?,
        @SerialName("etf_chas_erng_rt_dbnb") @Contextual override val etfCashEarningRate: BigDecimal?,
        @SerialName("etf_etn_ivst_heed_item_yn") @Serializable(with = YNSerializer::class) override val isEtfEtnInvestmentAlerted: Boolean?,
        @SerialName("stln_int_rt_dvsn_cd") override val settlementInterestRateClassCode: String?,
        @SerialName("frnr_psnl_lmt_rt") @Contextual override val foreignerPersonalLimitRate: BigDecimal?,
        @SerialName("lstg_rqsr_issu_istt_cd") override val listingRequestIssueInstitutionCode: String?,
        @SerialName("lstg_rqsr_item_cd") override val listingRequestItemCode: String?,
        @SerialName("trst_istt_issu_istt_cd") override val trustInstitutionIssueInstitutionCode: String?,
    ) : StockInfo, StockPriceBase {
        override val errorDescription: String? = null
        override val errorCode: String? = null
    }

    data class InquireProductBaseInfoData(
        /** 조회할 상품 번호. ETN의 경우 Q로 시작합니다. */
        override val ticker: String,
        /** 조회할 상품 종류. Stock, FutureOption, Bond, ELS만 사용할 수 있습니다. */
        val type: ProductTypeCode,
        override var corp: CorporationRequest? = null, override var tradeContinuous: String? = ""
    ) : Data, TradeContinuousData, Ticker

    @Suppress("SpellCheckingInspection")
    override suspend fun call(data: InquireProductBaseInfoData) = request(data) {
        throwIfClientIsDemo()

        when (data.type) {
            ProductTypeCode.Stock, ProductTypeCode.FutureOption, ProductTypeCode.Bond, ProductTypeCode.ELS -> {}
            else -> throw RequestException(
                "잘못된 ProductTypeCode ${data.type}입니다. InquireStockBaseInfo에는 Stock, FutureOption, Bond, ELS만 사용할 수 있습니다.",
                RequestCode.RequestError
            )
        }

        client.httpClient.get(url) {
            setAuth(client)
            setTradeId("CTPF1002R")
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