package io.github.devngho.kisopenapi.requests.response

import io.github.devngho.kisopenapi.requests.Response
import io.github.devngho.kisopenapi.requests.util.MarketWarnCode
import io.github.devngho.kisopenapi.requests.util.StockState
import io.github.devngho.kisopenapi.requests.util.YNSerializer
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface StockPriceFull: StockPrice, StockPriceForeigner, StockPriceChange, Response {
    @SerialName("iscd_stat_cls_code") val stockState: StockState?
    @SerialName("marg_rate") @Contextual val marginRate: BigDecimal?
    @SerialName("rprs_mrkt_kor_name") val marketName: String?
    @SerialName("new_hgpr_lwpr_cls_code") @Contextual val newHighLowCode: BigInteger?
    @SerialName("bstp_kor_isnm") val sectorName: String?
    @SerialName("temp_stop_yn") @Serializable(with = YNSerializer::class) val tempStop: Boolean?
    @SerialName("oprc_rang_cont_yn") @Serializable(with = YNSerializer::class) val marketPriceRangeExtended: Boolean?
    @SerialName("clpr_rang_cont_yn") @Serializable(with = YNSerializer::class) val endPriceRangeExtended: Boolean?
    @SerialName("crdt_able_yn") @Serializable(with = YNSerializer::class) val creditAble: Boolean?
    @SerialName("clw_pblc_yn") @Serializable(with = YNSerializer::class) val hasElw: Boolean?

    @SerialName("stck_sdpr") @Contextual val criteriaPrice: BigInteger?
    @SerialName("wghn_avrg_stck_prc") @Contextual val weightedAverageStockPrice: BigDecimal?
    @SerialName("hts_frgn_ehrt") @Contextual override val htsForeignerExhaustionRate: BigDecimal?
    @SerialName("frgn_ntby_qty") @Contextual override val foreignerNetBuyCount: BigInteger?
    @SerialName("pgtr_ntby_qty") @Contextual val programNetBuyCount: BigInteger?
    @SerialName("pvt_scnd_dmrs_prc") @Contextual val pivotSecondResistancePrice: BigInteger?
    @SerialName("pvt_frst_dmrs_prc") @Contextual val pivotFirstResistancePrice: BigInteger?
    @SerialName("pvt_pont_val") @Contextual val pivotPointValue: BigInteger?
    @SerialName("pvt_frst_dmsp_prc") @Contextual val pivotFirstBackingPrice: BigInteger?
    @SerialName("pvt_scnd_dmsp_prc") @Contextual val pivotSecondBackingPrice: BigInteger?
    @SerialName("dmrs_val") @Contextual val resistanceValue: BigInteger?
    @SerialName("dmsp_val") @Contextual val backingValue: BigInteger?
    @SerialName("cpfn") @Contextual val capitalFinance: BigInteger?
    @SerialName("rstc_wdth_prc") @Contextual val restrictedWidthPrice: BigInteger?
    @SerialName("stck_fcam") @Contextual val facePrice: BigInteger?
    @SerialName("stck_sspr") @Contextual val substitutePrice: BigInteger?
    @SerialName("rstc_wdth_pc") @Contextual val askingPrice: BigInteger?
    @SerialName("hts_deal_qty_unit_val") @Contextual val htsSellCountUnit: BigInteger?
    @SerialName("lstn_stcn") @Contextual val listedStockCount: BigInteger?
    @SerialName("hts_avls") @Contextual val htsMarketCap: BigInteger?
    @SerialName("per") @Contextual val per: BigDecimal?
    @SerialName("pbr") @Contextual val pbr: BigDecimal?
    @SerialName("stac_month") @Contextual val settlementMonth: Int?
    @SerialName("eps") @Contextual val eps: BigDecimal?
    @SerialName("bps") @Contextual val bps: BigDecimal?
    @SerialName("d250_hgpr") @Contextual val highPriceD250: BigInteger?
    @SerialName("d250_hgpr_date") val highPriceDateD250: String?
    @SerialName("d250_lwpr") @Contextual val lowPriceD250: BigInteger?
    @SerialName("d250_lwpr_date") val lowPriceDateD250: String?
    @SerialName("stck_dryy_hgpr") @Contextual val highPriceInYear: BigInteger?
    @SerialName("dryy_hgpr_date") val highPriceDateInYear: String?
    @SerialName("stck_dryy_lwpr") @Contextual val lowPriceInYear: BigInteger?
    @SerialName("dryy_lwpr_date") val lowPriceDateInYear: String?
    @SerialName("w52_hgpr") @Contextual val highPriceW52: BigInteger?
    @SerialName("w52_hgpr_date") @Contextual val highPriceDateW52: BigInteger?
    @SerialName("w52_lwpr") @Contextual val lowPriceW52: BigInteger?
    @SerialName("w52_lwpr_date") val lowPriceDateW52: String?
    @SerialName("whol_loan_rmnd_rate") @Contextual val totalLoanBalanceRate: BigDecimal?
    @SerialName("ssts_yn") @Serializable(with = YNSerializer::class) val shortSelling: Boolean?
    @SerialName("stck_shrn_iscd") val stockShortCode: String?
    @SerialName("fcam_cnnm") val facePriceCurrencyName: String?
    @SerialName("cpfn_cnnm") val capitalFinanceCurrencyName: String?
    @SerialName("apprch_rate") @Contextual val approachRate: BigDecimal?
    @SerialName("frgn_hldn_qty") @Contextual val foreignerHoldingCount: BigInteger?
    @SerialName("vi_cls_code") val viCode: String?
    @SerialName("otvm_vi_cls_code") val viCodeOvertime: String?
    @SerialName("last_ssts_cntg_qty") @Contextual val shortSellingLastConfirmedTradeCount: BigInteger?
    @SerialName("invt_caful_yn") @Serializable(with = YNSerializer::class) val investmentCareful: Boolean?
    @SerialName("mrkt_warn_cls_code") val marketWarnCode: MarketWarnCode?
    @SerialName("short_over_yn") @Serializable(with = YNSerializer::class) val shortOver: Boolean
}