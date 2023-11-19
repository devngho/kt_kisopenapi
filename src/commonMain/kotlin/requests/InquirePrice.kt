package io.github.devngho.kisopenapi.requests

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.KisOpenApi
import io.github.devngho.kisopenapi.requests.response.*
import io.github.devngho.kisopenapi.requests.util.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class InquirePrice(override val client: KisOpenApi):
    DataRequest<InquirePrice.InquirePriceData, InquirePrice.InquirePriceResponse> {
    private val url = if (client.isDemo) "https://openapivts.koreainvestment.com:29443/uapi/domestic-stock/v1/quotations/inquire-price"
                        else             "https://openapi.koreainvestment.com:9443/uapi/domestic-stock/v1/quotations/inquire-price"

    @Serializable
    data class InquirePriceResponse(
        @SerialName("tr_id") override var tradeId: String?,
        @SerialName("tr_cont") var tradeCount: String?,
        @SerialName("gt_uid") override var globalTradeID: String?,
        @SerialName("msg_cd") override val code: String?,
        @SerialName("msg1") override val msg: String?,
        @SerialName("rt_cd") @Serializable(with = ResultCodeSerializer::class) override val isOk: Boolean?,

        var output: InquirePriceResponseOutput?, override var next: (suspend () -> Response)?,
        override var tradeContinuous: String?
    ): Response, TradeContinuousResponse, TradeIdMsg {
        @SerialName("error_description")
        override val errorDescription: String? = null

        @SerialName("error_code")
        override val errorCode: String? = null
    }

    @Serializable
    @Suppress("SpellCheckingInspection")
    data class InquirePriceResponseOutput(
        @SerialName("iscd_stat_cls_code") override val stockState: StockState?,
        @SerialName("marg_rate") @Contextual override val marginRate: BigDecimal?,
        @SerialName("rprs_mrkt_kor_name") override val marketName: String?,
        @SerialName("new_hgpr_lwpr_cls_code") @Contextual override val newHighLowCode: String?,
        @SerialName("bstp_kor_isnm") override val sectorName: String?,
        @SerialName("temp_stop_yn") @Serializable(with = YNSerializer::class) override val tempStop: Boolean?,
        @SerialName("oprc_rang_cont_yn") @Serializable(with = YNSerializer::class) override val marketPriceRangeExtended: Boolean?,
        @SerialName("clpr_rang_cont_yn") @Serializable(with = YNSerializer::class) override val endPriceRangeExtended: Boolean?,
        @SerialName("crdt_able_yn") @Serializable(with = YNSerializer::class) override val creditAble: Boolean?,
        @SerialName("elw_pblc_yn") @Serializable(with = YNSerializer::class) override val hasElw: Boolean?,
        @SerialName("stck_prpr") @Contextual override val price: BigInteger?,
        @SerialName("prdy_vrss") @Contextual override val changeFromYesterday: BigInteger?,
        @SerialName("prdy_vrss_sign") override val signFromYesterday: SignPrice?,
        @SerialName("prdy_ctrt") @Contextual override val rateFromYesterday: BigDecimal?,
        @SerialName("acml_vol") @Contextual override val accumulateTradeVolume: BigInteger?,
        @SerialName("prdy_vrss_vol_rate") @Contextual override val rateTradeVolumeFromYesterday: BigDecimal?,
        @SerialName("stck_oprc") @Contextual override val openingPrice: BigInteger?,
        @SerialName("stck_hgpr") @Contextual override val highPrice: BigInteger?,
        @SerialName("stck_lwpr") @Contextual override val lowPrice: BigInteger?,
        @SerialName("stck_mxpr") @Contextual override val maxPrice: BigInteger?,
        @SerialName("stck_llam") @Contextual override val minPrice: BigInteger?,
        @SerialName("wghn_avrg_stck_prc") @Contextual override val weightedAverageStockPrice: BigDecimal?,
        @SerialName("hts_frgn_ehrt") @Contextual override val htsForeignerExhaustionRate: BigDecimal?,
        @SerialName("frgn_ntby_qty") @Contextual override val foreignerNetBuyCount: BigInteger?,
        @SerialName("pgtr_ntby_qty") @Contextual override val programNetBuyCount: BigInteger?,
        @SerialName("pvt_scnd_dmrs_prc") @Contextual override val pivotSecondResistancePrice: BigInteger?,
        @SerialName("pvt_frst_dmrs_prc") @Contextual override val pivotFirstResistancePrice: BigInteger?,
        @SerialName("pvt_pont_val") @Contextual override val pivotPointValue: BigInteger?,
        @SerialName("pvt_frst_dmsp_prc") @Contextual override val pivotFirstBackingPrice: BigInteger?,
        @SerialName("pvt_scnd_dmsp_prc") @Contextual override val pivotSecondBackingPrice: BigInteger?,
        @SerialName("dmrs_val") @Contextual override val resistanceValue: BigInteger?,
        @SerialName("dmsp_val") @Contextual override val backingValue: BigInteger?,
        @SerialName("cpfn") @Contextual override val capitalFinance: BigInteger?,
        @SerialName("rstc_wdth_prc") @Contextual override val restrictedWidthPrice: BigInteger?,
        @SerialName("stck_fcam") @Contextual override val facePrice: BigInteger?,
        @SerialName("stck_sspr") @Contextual override val substitutePrice: BigInteger?,
        @SerialName("aspr_unit") @Contextual override val askingPriceUnit: BigInteger?,
        @SerialName("hts_deal_qty_unit_val") @Contextual override val htsSellCountUnit: BigInteger?,
        @SerialName("lstn_stcn") @Contextual override val listedStockCount: BigInteger?,
        @SerialName("hts_avls") @Contextual override val htsMarketCap: BigInteger?,
        @SerialName("per") @Contextual override val per: BigDecimal?,
        @SerialName("pbr") @Contextual override val pbr: BigDecimal?,
        @SerialName("stac_month") @Contextual override val settlementMonth: Int?,
        @SerialName("vol_tnrt") @Contextual override val tradeVolumeTurningRate: BigDecimal?,
        @SerialName("eps") @Contextual override val eps: BigDecimal?,
        @SerialName("bps") @Contextual override val bps: BigDecimal?,
        @SerialName("d250_hgpr") @Contextual override val highPriceD250: BigInteger?,
        @Serializable(with = YYYYMMDDSerializer::class) @SerialName("d250_hgpr_date") override val highPriceDateD250: Date?,
        @SerialName("d250_hgpr_vrss_prpr_rate") @Contextual override val highPriceRateD250: BigDecimal?,
        @SerialName("d250_lwpr") @Contextual override val lowPriceD250: BigInteger?,
        @Serializable(with = YYYYMMDDSerializer::class) @SerialName("d250_lwpr_date") override val lowPriceDateD250: Date?,
        @SerialName("d250_lwpr_vrss_prpr_rate") @Contextual override val lowPriceRateD250: BigDecimal?,
        @SerialName("stck_dryy_hgpr") @Contextual override val highPriceInYear: BigInteger?,
        @Serializable(with = YYYYMMDDSerializer::class) @SerialName("dryy_hgpr_date") override val highPriceDateInYear: Date?,
        @SerialName("dryy_hgpr_vrss_prpr_rate") @Contextual override val highPriceRateInYear: BigDecimal?,
        @SerialName("stck_dryy_lwpr") @Contextual override val lowPriceInYear: BigInteger?,
        @Serializable(with = YYYYMMDDSerializer::class) @SerialName("dryy_lwpr_date") override val lowPriceDateInYear: Date?,
        @SerialName("dryy_lwpr_vrss_prpr_rate") @Contextual override val lowPriceRateInYear: BigDecimal?,
        @SerialName("w52_hgpr") @Contextual override val highPriceW52: BigInteger?,
        @Serializable(with = YYYYMMDDSerializer::class) @SerialName("w52_hgpr_date") @Contextual override val highPriceDateW52: Date?,
        @SerialName("w52_hgpr_vrss_prpr_ctrt") @Contextual override val highPriceRateW52: BigDecimal?,
        @SerialName("w52_lwpr") @Contextual override val lowPriceW52: BigInteger?,
        @Serializable(with = YYYYMMDDSerializer::class) @SerialName("w52_lwpr_date") override val lowPriceDateW52: Date?,
        @SerialName("w52_lwpr_vrss_prpr_ctrt") @Contextual override val lowPriceRateW52: BigDecimal?,
        @SerialName("whol_loan_rmnd_rate") @Contextual override val totalLoanBalanceRate: BigDecimal?,
        @SerialName("ssts_yn") @Serializable(with = YNSerializer::class) override val shortSelling: Boolean?,
        @SerialName("stck_shrn_iscd") override val ticker: String?,
        @SerialName("fcam_cnnm") override val facePriceCurrencyName: String?,
        @SerialName("cpfn_cnnm") override val capitalFinanceCurrencyName: String?,
        @SerialName("apprch_rate") @Contextual override val approachRate: BigDecimal?,
        @SerialName("frgn_hldn_qty") @Contextual override val foreignerHoldingCount: BigInteger?,
        @SerialName("vi_cls_code") override val viCode: String?,
        @SerialName("otvm_vi_cls_code") override val viCodeOvertime: String?,
        @SerialName("last_ssts_cntg_qty") @Contextual override val shortSellingLastConfirmedTradeCount: BigInteger?,
        @SerialName("invt_caful_yn") @Serializable(with = YNSerializer::class) override val investmentCareful: Boolean?,
        @SerialName("mrkt_warn_cls_code") override val marketWarnCode: MarketWarnCode?,
        @SerialName("short_over_yn") @Serializable(with = YNSerializer::class) override val shortOver: Boolean?,
        @SerialName("stck_sdpr") @Contextual override val criteriaPrice: BigInteger?,
        @SerialName("acml_tr_pbmn") @Contextual override val accumulateTradePrice: BigInteger?,
        @SerialName("sltr_yn") @Serializable(with = YNSerializer::class) override val settlement: Boolean?
    ): StockPriceFull, StockTradeFull {
        @SerialName("error_description")
        override val errorDescription: String? = null

        @SerialName("error_code")
        override val errorCode: String? = null
    }

    data class InquirePriceData(
        override val ticker: String,
        override var corp: CorporationRequest? = null,
        override var tradeContinuous: String? = ""
    ) : Data, TradeContinuousData, Ticker

    @Suppress("SpellCheckingInspection")
    override suspend fun call(data: InquirePriceData): InquirePriceResponse = client.rateLimiter.rated {
        if (data.corp == null) data.corp = client.corp

        fun HttpRequestBuilder.inquirePrice() {
            auth(client)
            stock(data.ticker)
            tradeId("FHKST01010100")
            data.corp?.let { corporation(it) }
        }

        val res = client.httpClient.get(url) {
            inquirePrice()
        }

        res.body<InquirePriceResponse>().apply {
            if (this.errorCode != null) throw RequestError(this.errorDescription)

            processHeader(res)
            setNext(data, this)
        }
    }
}