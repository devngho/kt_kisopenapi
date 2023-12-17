package io.github.devngho.kisopenapi.requests.domestic.inquire.live

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.toBigInteger
import io.github.devngho.kisopenapi.KISApiClient
import io.github.devngho.kisopenapi.requests.LiveData
import io.github.devngho.kisopenapi.requests.LiveRequest
import io.github.devngho.kisopenapi.requests.Response
import io.github.devngho.kisopenapi.requests.data.CorporationRequest
import io.github.devngho.kisopenapi.requests.response.LiveResponse
import io.github.devngho.kisopenapi.requests.response.stock.Ticker
import io.github.devngho.kisopenapi.requests.response.stock.price.domestic.StockPriceBase
import io.github.devngho.kisopenapi.requests.response.stock.price.domestic.StockPriceChange
import io.github.devngho.kisopenapi.requests.response.stock.trade.StockTradeFull
import io.github.devngho.kisopenapi.requests.util.*
import io.github.devngho.kisopenapi.requests.util.HHMMSSSerializer.HHMMSS
import io.github.devngho.kisopenapi.requests.util.YNSerializer.YN
import kotlinx.coroutines.Job
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class InquireLiveConfirm(override val client: KISApiClient) :
    LiveRequest<InquireLiveConfirm.InquireLiveConfirmData, InquireLiveConfirm.InquireLiveConfirmResponse> {
    private val tradeId = if (client.isDemo) "H0STCNI9" else "H0STCNI0"
    @Serializable
    data class InquireLiveConfirmResponse(
        @SerialName("mksc_shrn_iscd") override val ticker: String? = null,
        @SerialName("stck_cntg_hour") val stockConfirmTime: Time? = null,
        @SerialName("stck_prpr") @Contextual override val price: BigInteger? = null,
        @SerialName("prdy_vrss_sign") override val signFromYesterday: SignPrice? = null,
        @SerialName("prdy_vrss") @Contextual override val changeFromYesterday: BigInteger? = null,
        @SerialName("prdy_ctrt") @Contextual override val rateFromYesterday: BigDecimal? = null,
        @SerialName("wghn_avrg_stck_prc") @Contextual val weightedAverageStockPrice: BigDecimal?  = null,
        @SerialName("stck_oprc") @Contextual val marketPrice: BigInteger?  = null,
        @SerialName("stck_hgpr") @Contextual val highPrice: BigInteger?  = null,
        @SerialName("stck_lwpr") @Contextual val lowPrice: BigInteger?  = null,
        @SerialName("askp_rsqn1") @Contextual val sellAskPriceCount1: BigInteger?  = null,
        @SerialName("bidp_rsqn1") @Contextual val buyAskPriceCount1: BigInteger?  = null,
        @SerialName("cntg_vol") @Contextual val confirmTradeVolume: BigInteger?  = null,
        @SerialName("acml_vol") @Contextual override val accumulateTradeVolume: BigInteger? = null,
        @SerialName("acml_tr_pbmn") @Contextual override val accumulateTradePrice: BigInteger? = null,
        @SerialName("seln_cntg_csnu") @Contextual val sellConfirmCount: BigInteger?  = null,
        @SerialName("shnu_cntg_csnu") @Contextual val buyConfirmCount: BigInteger?  = null,
        @SerialName("ntby_cntg_csnu") @Contextual val naturalConfirmCount: BigInteger?  = null,
        @SerialName("cttr") @Contextual val confirmStrength: BigDecimal?  = null,
        @SerialName("total_askp_rsqn") @Contextual val totalSellAskPriceCount: BigInteger?  = null,
        @SerialName("total_bidp_rsqn") @Contextual val totalBuyAskPriceCount: BigInteger?  = null,
        @SerialName("ccld_dvsn") val confirmDivision: String?  = null,
        @SerialName("shnu_rate") @Contextual val buyRate: BigDecimal?  = null,
        @SerialName("prdy_vol_vrss_acml_vol_rate") @Contextual override val rateTradeVolumeFromYesterday: BigDecimal? = null,
        @SerialName("oprc_hour") val marketPriceTime: String?  = null,
        @SerialName("oprc_vrss_prpr_sign") val differentMarketPriceSign: SignPrice?  = null,
        @SerialName("oprc_vrss_prpr") @Contextual val differentMarketPrice: BigInteger?  = null,
        @SerialName("hgpr_hour") val highPriceTime: String?  = null,
        @SerialName("hgpr_vrss_prpr_sign") val differentHighPriceSign: SignPrice?  = null,
        @SerialName("hgpr_vrss_prpr") @Contextual val differentHighPrice: BigInteger?  = null,
        @SerialName("lwpr_hour") val lowPriceTime: String?  = null,
        @SerialName("lwpr_vrss_prpr_sign") val differentLowPriceSign: SignPrice?  = null,
        @SerialName("lwpr_vrss_prpr") @Contextual val differentLowPrice: BigInteger?  = null,
        @SerialName("bsop_date") val bizDate: String?  = null,
        @SerialName("new_mkop_cls_code") val newMarketDivisionCode: String?  = null,
        @SerialName("trht_yn") @Serializable(with = YNSerializer::class) val isTradeStopped: Boolean? = null,
        @SerialName("askp1") @Contextual val sellAskPrice1: BigInteger?  = null,
        @SerialName("bidp1") @Contextual val buyAskPrice1: BigInteger?  = null,
        @SerialName("seln_cntg_smtn") @Contextual val sellTotalCount: BigInteger?  = null,
        @SerialName("shnu_cntg_smtn") @Contextual val buyTotalCount: BigInteger?  = null,
        @SerialName("vol_tnrt") @Contextual override val tradeVolumeTurningRate: BigDecimal? = null,
        @SerialName("prdy_smns_hour_acml_vol") @Contextual val accumulateTradeVolumeFromYesterdaySameHour: BigInteger?  = null,
        @SerialName("prdy_smns_hour_acml_vol_rate") @Contextual val rateAccumulateTradeVolumeFromYesterdaySameHour: BigInteger?  = null,
        @SerialName("hour_cls_code") val hourCode: HourCode?  = null,
        @SerialName("mrkt_trtm_cls_code") val marketTerminatedCode: String?  = null,
        @SerialName("vi_stnd_prc") @Contextual val viActivatePrice: BigInteger?  = null,
    ) : Response, Ticker, StockPriceBase, StockTradeFull, StockPriceChange {
        @SerialName("error_description")
        override val errorDescription: String? = null

        @SerialName("error_code")
        override val errorCode: String? = null
    }

    data class InquireLiveConfirmData(override var corp: CorporationRequest? = null) : LiveData {
        override fun tradeKey(client: KISApiClient): String {
            return client.htsId!!
        }
    }

    private var job: Job? = null
    private var subscribed: KISApiClient.WebSocketSubscribed? = null

    @Suppress("UNCHECKED_CAST")
    override suspend fun register(
        data: InquireLiveConfirmData,
        wait: Boolean,
        force: Boolean,
        init: (suspend (Result<LiveResponse>) -> Unit)?,
        block: suspend (InquireLiveConfirmResponse) -> Unit
    ) {
        subscribed = KISApiClient.WebSocketSubscribed(
            this@InquireLiveConfirm, data, init,
            block as suspend (Response) -> Unit
        )

        requestStart(
            data, subscribed!!, tradeId, data.tradeKey(client), wait,
            updateJob = { job = it },
            init = init ?: {},
            block = block,
        ) {
            InquireLiveConfirmResponse(
                it[0],
                it[1].HHMMSS,
                it[2].toBigInteger(),
                SignPrice.entries.find { f -> f.value.toString() == it[3] },
                it[4].toBigInteger(),
                it[5].toBigDecimal(),
                it[6].toBigDecimal(),
                it[7].toBigInteger(),
                it[8].toBigInteger(),
                it[9].toBigInteger(),
                it[10].toBigInteger(),
                it[11].toBigInteger(),
                it[12].toBigInteger(),
                it[13].toBigInteger(),
                it[14].toBigInteger(),
                it[15].toBigInteger(),
                it[16].toBigInteger(),
                it[17].toBigInteger(),
                it[18].toBigDecimal(),
                it[19].toBigInteger(),
                it[20].toBigInteger(),
                it[21],
                it[22].toBigDecimal(),
                it[23].toBigDecimal(),
                it[24],
                SignPrice.entries.find { f -> f.value.toString() == it[25] },
                it[26].toBigInteger(),
                it[27],
                SignPrice.entries.find { f -> f.value.toString() == it[28] },
                it[29].toBigInteger(),
                it[30],
                SignPrice.entries.find { f -> f.value.toString() == it[31] },
                it[32].toBigInteger(),
                it[33],
                it[34],
                it[35].YN,
                it[36].toBigInteger(),
                it[37].toBigInteger(),
                it[38].toBigInteger(),
                it[39].toBigInteger(),
                it[40].toBigDecimal(),
                it[41].toBigInteger(),
                it[42].toBigInteger(),
                HourCode.entries.find { f -> f.num == it[43] },
                it[44]
            )
        }
    }

    override suspend fun unregister(data: InquireLiveConfirmData, wait: Boolean) =
        requestEnd(data, subscribed, tradeId, data.tradeKey(client), wait, job)
}